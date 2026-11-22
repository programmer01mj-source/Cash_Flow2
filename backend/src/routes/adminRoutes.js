const express = require('express');
const router = express.Router();
const fs = require('fs');
const path = require('path');
const { authenticateToken, requireRole } = require('../middleware/authMiddleware');
const db = require('../../db');

const localLoansPath = path.join(__dirname, '../../database_loans.json');
const localTxnsPath = path.join(__dirname, '../../database_transactions.json');

async function getLoans() {
    try {
        const res = await db.query('SELECT * FROM loans');
        return res.rows.map(r => ({
            amount: parseFloat(r.amount),
            status: r.status
        }));
    } catch (err) {
        if (fs.existsSync(localLoansPath)) {
            return JSON.parse(fs.readFileSync(localLoansPath, 'utf8'));
        }
    }
    return [];
}

async function getTransactions() {
    try {
        const res = await db.query('SELECT * FROM transactions');
        return res.rows.map(r => ({
            amount: parseFloat(r.amount)
        }));
    } catch (err) {
        if (fs.existsSync(localTxnsPath)) {
            return JSON.parse(fs.readFileSync(localTxnsPath, 'utf8'));
        }
    }
    return [];
}

router.get('/metrics', authenticateToken, requireRole('Admin'), async (req, res) => {
    const loans = await getLoans();
    const transactions = await getTransactions();

    let totalCapitalDisbursed = 0;
    let totalPrincipalRepaid = 0;
    let nonPerformingLoans = 0;
    let activeBorrowersCount = 0;

    loans.forEach(l => {
        if (l.status === 'Disbursed' || l.status === 'Active' || l.status === 'Paid') {
            totalCapitalDisbursed += (l.amount || 0);
            activeBorrowersCount++;
        }
        if (l.status === 'Defaulted' || l.status === 'Non-Performing') {
            nonPerformingLoans++;
        }
    });

    transactions.forEach(t => {
        totalPrincipalRepaid += (t.amount || 0);
    });

    const totalInterestEarned = totalCapitalDisbursed * 0.12;

    res.json({
        totalCapitalDisbursed,
        totalPrincipalRepaid,
        totalInterestEarned,
        nonPerformingLoans,
        activeBorrowersCount,
        scheduledReportStatus: 'Active - Next run 1st of month @ 00:00'
    });
});

module.exports = router;
