const express = require('express');
const router = express.Router();
const fs = require('fs');
const path = require('path');
const { authenticateToken } = require('../middleware/authMiddleware');
const { validate, loanApplySchema, loanStatusSchema } = require('../middleware/validateMiddleware');
const { sendEmail } = require('../services/emailService');
const db = require('../../db');

const localLoansPath = path.join(__dirname, '../../database_loans.json');

async function getLoans() {
    try {
        const res = await db.query('SELECT * FROM loans');
        return res.rows.map(r => ({
            id: r.id,
            borrowerName: r.borrower_name,
            amount: parseFloat(r.amount),
            tenure: parseInt(r.tenure, 10),
            income: parseFloat(r.income || 0),
            nationalId: r.national_id,
            dob: r.dob,
            status: r.status
        }));
    } catch (err) {
        console.warn('[LOANS] Remote DB getLoans failed, falling back to local JSON:', err.message);
        if (fs.existsSync(localLoansPath)) {
            return JSON.parse(fs.readFileSync(localLoansPath, 'utf8'));
        }
    }
    return [];
}

async function saveLoan(loan) {
    try {
        await db.query(
            'INSERT INTO loans (id, borrower_name, amount, tenure, income, national_id, dob, status) VALUES ($1, $2, $3, $4, $5, $6, $7, $8) ON CONFLICT (id) DO UPDATE SET status = $8',
            [loan.id, loan.borrowerName, loan.amount, loan.tenure, loan.income, loan.nationalId, loan.dob, loan.status]
        );
    } catch (err) {
        console.warn('[LOANS] Remote DB saveLoan failed, falling back to local JSON:', err.message);
    }
    try {
        let loans = [];
        if (fs.existsSync(localLoansPath)) {
            loans = JSON.parse(fs.readFileSync(localLoansPath, 'utf8'));
        }
        const idx = loans.findIndex(l => l.id === loan.id);
        if (idx >= 0) loans[idx] = loan;
        else loans.push(loan);
        fs.writeFileSync(localLoansPath, JSON.stringify(loans, null, 2));
    } catch (e) {}
}

router.get('/', authenticateToken, async (req, res) => {
    const loans = await getLoans();
    res.json(loans);
});

router.post('/apply', authenticateToken, validate(loanApplySchema), async (req, res) => {
    const { amount, tenure, income, nationalId, dob } = req.body;

    const loans = await getLoans();
    const userActiveLoans = loans.filter(l => l.borrowerName === req.user.email && (l.status === 'Disbursed' || l.status === 'Active' || l.status === 'Pending'));
    if (userActiveLoans.length >= 2) {
        return res.status(400).json({ error: 'Loan application denied: You currently have active loans with two or more other lenders.' });
    }

    const newLoan = {
        id: Date.now().toString(),
        borrowerName: req.user.email,
        amount: parseFloat(amount),
        tenure: parseInt(tenure, 10),
        income: income ? parseFloat(income) : 300000,
        nationalId: nationalId || '',
        dob: dob || '',
        status: 'Pending'
    };
    await saveLoan(newLoan);

    // Send confirmation email to Customer
    sendEmail(
        req.user.email,
        'Cashflow Notification: Loan Application Submitted',
        `Hello,\n\nYour loan application for MWK ${amount} (Tenure: ${tenure} months) has been received and stored in the database. Status: Pending review.\n\nThank you for choosing Cashflow.`
    );

    // Send real-time notification email to Admin and Loan Officer for authorization/approval
    sendEmail(
        'programmer01mj@gmail.com',
        'Cashflow Admin/Officer Alert: New Loan Application Requires Authorization',
        `Hello Admin & Loan Officer,\n\nA new loan application has been submitted by ${req.user.email} for MWK ${amount} (Tenure: ${tenure} months).\nPlease log in to the Loan Officer Portal or Admin Portal to review and authorize/approve this application.\n\nLoan ID: ${newLoan.id}`
    );

    res.status(201).json({ message: 'Loan application submitted successfully and stored in database', loan: newLoan });
});

router.patch('/:id/status', authenticateToken, validate(loanStatusSchema), async (req, res) => {
    const { id } = req.params;
    const { status } = req.body;
    const loans = await getLoans();
    const loan = loans.find(l => l.id === id);
    if (!loan) return res.status(404).json({ error: 'Loan not found' });

    loan.status = status;
    await saveLoan(loan);

    sendEmail(
        loan.borrowerName,
        `Cashflow Notification: Loan Status Updated to ${status}`,
        `Hello,\n\nYour loan application (ID: ${id}) status has been updated in the database to: ${status}.\n\nLog in to your Cashflow portal for more details.`
    );

    res.json({ message: 'Loan status updated successfully in database', loan });
});

module.exports = router;
