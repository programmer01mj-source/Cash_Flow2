const express = require('express');
const router = express.Router();
const fs = require('fs');
const path = require('path');
const { authenticateToken } = require('../middleware/authMiddleware');
const { validate, paymentSchema } = require('../middleware/validateMiddleware');
const { sendEmail } = require('../services/emailService');
const db = require('../../db');

const localTxnsPath = path.join(__dirname, '../../database_transactions.json');

async function getTransactions() {
    try {
        const res = await db.query('SELECT * FROM transactions');
        return res.rows.map(r => ({
            id: r.id,
            date: r.date,
            amount: parseFloat(r.amount),
            mode: r.mode,
            type: r.type
        }));
    } catch (err) {
        console.warn('[TXNS] Remote DB getTransactions failed, falling back to local JSON:', err.message);
        if (fs.existsSync(localTxnsPath)) {
            return JSON.parse(fs.readFileSync(localTxnsPath, 'utf8'));
        }
    }
    return [];
}

async function saveTransaction(txn) {
    try {
        await db.query(
            'INSERT INTO transactions (id, date, amount, mode, type) VALUES ($1, $2, $3, $4, $5) ON CONFLICT (id) DO NOTHING',
            [txn.id, txn.date, txn.amount, txn.mode, txn.type]
        );
    } catch (err) {
        console.warn('[TXNS] Remote DB saveTransaction failed, falling back to local JSON:', err.message);
    }
    try {
        let txns = [];
        if (fs.existsSync(localTxnsPath)) {
            txns = JSON.parse(fs.readFileSync(localTxnsPath, 'utf8'));
        }
        txns.push(txn);
        fs.writeFileSync(localTxnsPath, JSON.stringify(txns, null, 2));
    } catch (e) {}
}

router.get('/', authenticateToken, async (req, res) => {
    const txns = await getTransactions();
    res.json(txns);
});

router.post('/pay', authenticateToken, validate(paymentSchema), async (req, res) => {
    const { amount, mode } = req.body;
    const txn = {
        id: `TXN-${Date.now()}`,
        date: new Date().toISOString().split('T')[0],
        amount: parseFloat(amount),
        mode: mode || 'Banktransfer',
        type: 'Payback'
    };
    await saveTransaction(txn);

    sendEmail(
        req.user.email,
        'Cashflow Notification: Payment Received Successfully',
        `Hello,\n\nYour repayment of MWK ${amount} via ${mode} has been received and stored in the database successfully.\nTransaction Reference: ${txn.id}.\n\nThank you!`
    );

    res.status(201).json({ message: 'Payment recorded successfully in database', transaction: txn });
});

module.exports = router;
