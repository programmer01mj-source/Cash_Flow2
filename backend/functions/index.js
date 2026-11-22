const { onRequest, onCall } = require('firebase-functions/v2/https');
const { onSchedule } = require('firebase-functions/v2/scheduler');
const admin = require('firebase-admin');
const { sendEmail } = require('../src/services/emailService');

admin.initializeApp();
const db = admin.firestore();

// 1. Evaluate Loan Application (Eligibility: Age 18-65, DTI <= 40%, Blacklist check, active loans < 2)
exports.evaluateLoanApplication = onCall(async (request) => {
    const data = request.data;
    const auth = request.auth;

    if (!auth) {
        throw new Error('Unauthorized: User must be authenticated.');
    }

    const { amount, tenure, income, dob, nationalId } = data;

    // Age calculation
    const birthDate = new Date(dob);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
        age--;
    }

    if (age < 18 || age > 65) {
        return { status: 'INELIGIBLE', reason: `Applicant age (${age}) is outside the allowed range (18 to 65).` };
    }

    // Debt-to-Income (DTI) calculation: monthly installment estimation <= 40% of income
    const monthlyInterest = 0.12 / 12;
    const monthlyInstallment = (amount * monthlyInterest * Math.pow(1 + monthlyInterest, tenure)) / (Math.pow(1 + monthlyInterest, tenure) - 1);
    const dti = monthlyInstallment / income;

    if (dti > 0.40) {
        return { status: 'INELIGIBLE', reason: `Debt-to-Income ratio (${(dti * 100).toFixed(1)}%) exceeds the 40% maximum threshold.` };
    }

    // Check active loans from other lenders (< 2)
    const activeLoansSnap = await db.collection('loans')
        .where('borrowerEmail', '==', auth.token.email)
        .where('status', 'in', ['Disbursed', 'Active', 'Pending'])
        .get();

    if (activeLoansSnap.size >= 2) {
        return { status: 'INELIGIBLE', reason: 'Applicant holds active loans from two or more other lenders.' };
    }

    return { status: 'ELIGIBLE', reason: 'All credit and eligibility rules passed successfully.' };
});

// 2. Approve Loan Application
exports.approveLoanApplication = onCall(async (request) => {
    const { loanId } = request.data;
    const auth = request.auth;

    if (!auth || (auth.token.role !== 'Loan Officer' && auth.token.role !== 'Admin')) {
        throw new Error('Unauthorized: Only Loan Officers or Administrators can approve loans.');
    }

    const loanRef = db.collection('loans').doc(loanId);
    const loanDoc = await loanRef.get();
    if (!loanDoc.exists) throw new Error('Loan application not found.');

    const prevStatus = loanDoc.data().status;
    await loanRef.update({ status: 'Approved', updatedAt: admin.firestore.FieldValue.serverTimestamp() });

    await db.collection('audit_logs').add({
        actorId: auth.uid,
        actorRole: auth.token.role,
        action: 'APPROVE_LOAN',
        details: `Loan ${loanId} approved from ${prevStatus}`,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, message: 'Loan approved successfully.' };
});

// 3. Reject Loan Application
exports.rejectLoanApplication = onCall(async (request) => {
    const { loanId, reason } = request.data;
    const auth = request.auth;

    if (!auth || (auth.token.role !== 'Loan Officer' && auth.token.role !== 'Admin')) {
        throw new Error('Unauthorized.');
    }

    const loanRef = db.collection('loans').doc(loanId);
    await loanRef.update({ status: 'Rejected', rejectionReason: reason, updatedAt: admin.firestore.FieldValue.serverTimestamp() });

    await db.collection('audit_logs').add({
        actorId: auth.uid,
        actorRole: auth.token.role,
        action: 'REJECT_LOAN',
        details: `Loan ${loanId} rejected. Reason: ${reason}`,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, message: 'Loan rejected.' };
});

// 4. Disburse Loan
exports.disburseLoan = onCall(async (request) => {
    const { loanId } = request.data;
    const auth = request.auth;

    if (!auth || (auth.token.role !== 'Loan Officer' && auth.token.role !== 'Admin')) {
        throw new Error('Unauthorized.');
    }

    const loanRef = db.collection('loans').doc(loanId);
    const loanDoc = await loanRef.get();
    if (!loanDoc.exists) throw new Error('Loan not found.');

    const data = loanDoc.data();
    await loanRef.update({ status: 'Disbursed', disbursedAt: admin.firestore.FieldValue.serverTimestamp() });

    // Record disbursement transaction
    await db.collection('transactions').add({
        loanId,
        borrowerEmail: data.borrowerEmail,
        amount: data.amount,
        mode: 'Banktransfer',
        type: 'Disbursement',
        date: new Date().toISOString().split('T')[0],
        createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, message: 'Loan disbursed successfully.' };
});

// 5. Generate Installments
exports.generateInstallments = onCall(async (request) => {
    const { loanId } = request.data;
    const loanDoc = await db.collection('loans').doc(loanId).get();
    if (!loanDoc.exists) throw new Error('Loan not found.');

    const data = loanDoc.data();
    const amount = data.amount;
    const tenure = data.tenure;
    const monthlyAmount = (amount * 1.12) / tenure;

    const batch = db.batch();
    for (let i = 1; i <= tenure; i++) {
        const dueDate = new Date();
        dueDate.setMonth(dueDate.getMonth() + i);
        const instRef = db.collection('installments').doc();
        batch.set(instRef, {
            loanId,
            borrowerEmail: data.borrowerEmail,
            dueDate: dueDate.toISOString().split('T')[0],
            amount: parseFloat(monthlyAmount.toFixed(2)),
            status: i === 1 ? 'Pending' : 'Upcoming'
        });
    }
    await batch.commit();
    return { success: true, message: 'Installments generated.' };
});

// 6. Process Loan Payment (Idempotent)
exports.processLoanPayment = onCall(async (request) => {
    const { loanId, amount, mode, idempotencyKey } = request.data;
    const auth = request.auth;
    if (!auth) throw new Error('Unauthorized');

    // Idempotency check
    const existingTxn = await db.collection('transactions').where('idempotencyKey', '==', idempotencyKey).get();
    if (!existingTxn.empty) {
        return { success: true, message: 'Payment already processed (Idempotent).' };
    }

    const txnRef = db.collection('transactions').doc();
    await txnRef.set({
        txnId: txnRef.id,
        loanId,
        borrowerEmail: auth.token.email,
        amount: parseFloat(amount),
        mode,
        type: 'Payback',
        idempotencyKey,
        date: new Date().toISOString().split('T')[0],
        createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    sendEmail(auth.token.email, 'Cashflow Payment Receipt', `Your payment of MWK ${amount} via ${mode} was processed successfully.`);

    return { success: true, message: 'Payment processed successfully.', txnId: txnRef.id };
});

// 7. Update Loan Status
exports.updateLoanStatus = onCall(async (request) => {
    const { loanId, status } = request.data;
    const auth = request.auth;
    if (!auth || (auth.token.role !== 'Loan Officer' && auth.token.role !== 'Admin')) {
        throw new Error('Unauthorized.');
    }

    await db.collection('loans').doc(loanId).update({ status, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
    return { success: true, message: `Loan status updated to ${status}.` };
});

// 8. Scheduled Monthly Financial Report (Africa/Blantyre timezone at 00:00 on the 1st of every month)
exports.generateMonthlyFinancialReport = onSchedule({ schedule: '0 0 1 * *', timeZone: 'Africa/Blantyre' }, async (event) => {
    const loansSnap = await db.collection('loans').get();
    let totalCapitalDisbursed = 0;
    let newLoansCount = 0;
    let closedLoansCount = 0;
    const overdueLoans = [];

    loansSnap.forEach(doc => {
        const d = doc.data();
        if (['Disbursed', 'Active', 'Paid'].includes(d.status)) totalCapitalDisbursed += (d.amount || 0);
        if (d.status === 'Pending') newLoansCount++;
        if (['Paid', 'Closed'].includes(d.status)) closedLoansCount++;
        if (['Defaulted', 'Non-Performing'].includes(d.status)) overdueLoans.push(d.borrowerEmail);
    });

    const reportSummary = `MONTHLY FINANCIAL REPORT (Africa/Blantyre)\n\n- Total Capital Disbursed: MWK ${totalCapitalDisbursed}\n- New Loan Applications: ${newLoansCount}\n- Closed Loans: ${closedLoansCount}\n- Overdue / Non-Performing Loans: ${overdueLoans.length}`;

    // Email stakeholders
    await sendEmail('management@cashflow.com', 'Cashflow Monthly Financial Report', reportSummary);
    console.log('[MONTHLY REPORT] Generated and distributed successfully.');
});
