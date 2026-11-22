const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const fs = require('fs');
const path = require('path');
const { JWT_SECRET } = require('../middleware/authMiddleware');
const { validate, signupSchema, loginSchema } = require('../middleware/validateMiddleware');
const { sendEmail } = require('../services/emailService');
const db = require('../../db');

const localUsersPath = path.join(__dirname, '../../database_users.json');

async function getUsers() {
    try {
        const res = await db.query('SELECT * FROM users');
        return res.rows.map(r => ({ id: r.id, name: r.name, email: r.email, role: r.role }));
    } catch (err) {
        console.warn('[AUTH] Remote DB getUsers failed, falling back to local JSON:', err.message);
        if (fs.existsSync(localUsersPath)) {
            return JSON.parse(fs.readFileSync(localUsersPath, 'utf8'));
        }
    }
    return [];
}

async function saveUser(user) {
    try {
        await db.query(
            'INSERT INTO users (id, name, email, role) VALUES ($1, $2, $3, $4) ON CONFLICT (email) DO UPDATE SET name = $2, role = $4',
            [user.id, user.name, user.email, user.role]
        );
    } catch (err) {
        console.warn('[AUTH] Remote DB saveUser failed, falling back to local JSON:', err.message);
    }
    try {
        let users = [];
        if (fs.existsSync(localUsersPath)) {
            users = JSON.parse(fs.readFileSync(localUsersPath, 'utf8'));
        }
        const idx = users.findIndex(u => u.email.toLowerCase() === user.email.toLowerCase());
        if (idx >= 0) users[idx] = user;
        else users.push(user);
        fs.writeFileSync(localUsersPath, JSON.stringify(users, null, 2));
    } catch (e) {}
}

router.post('/signup', validate(signupSchema), async (req, res) => {
    const { name, email, password, role } = req.body;
    let assignedRole = role || 'Customer';
    if (email.toLowerCase() === 'admin@qmail.com' || email.toLowerCase().includes('admin')) assignedRole = 'Admin';
    else if (email.toLowerCase() === 'officer@gmail.com' || email.toLowerCase().includes('officer')) assignedRole = 'Loan Officer';

    const users = await getUsers();
    const existing = users.find(u => u.email.toLowerCase() === email.toLowerCase());
    if (existing) {
        return res.status(400).json({ error: 'An account with this email already exists. Please log in instead.' });
    }

    const user = { id: Date.now().toString(), name, email, role: assignedRole };
    await saveUser(user);

    const token = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_SECRET, { expiresIn: '7d' });

    sendEmail(
        email,
        'Welcome to Cashflow - Account Created Successfully',
        `Hello ${name},\n\nYour Cashflow account has been successfully created with role: ${assignedRole}.\nYou can now log in and manage your loans securely.\n\nThank you for choosing Cashflow.`
    );

    res.json({ message: 'User registered successfully and stored in database', token, user });
});

router.post('/login', validate(loginSchema), async (req, res) => {
    const { email, password } = req.body;

    // Hardcoded Admin login check
    if (email.toLowerCase() === 'admin@qmail.com' && password === '1234@admin') {
        const adminUser = { id: 'admin-qmail', name: 'System Administrator', email: 'admin@qmail.com', role: 'Admin' };
        await saveUser(adminUser);
        const token = jwt.sign({ id: adminUser.id, email: adminUser.email, role: adminUser.role }, JWT_SECRET, { expiresIn: '7d' });

        sendEmail(
            'admin@qmail.com',
            'Cashflow Security Alert - Admin Login Detected',
            `Hello System Administrator,\n\nA successful login to admin@qmail.com was detected at ${new Date().toLocaleString()}.`
        );

        return res.json({ message: 'Login successful', token, user: adminUser });
    }

    // Hardcoded Loan Officer login check
    if (email.toLowerCase() === 'officer@gmail.com' && password === '1234@officer') {
        const officerUser = { id: 'officer-gmail', name: 'Loan Officer', email: 'officer@gmail.com', role: 'Loan Officer' };
        await saveUser(officerUser);
        const token = jwt.sign({ id: officerUser.id, email: officerUser.email, role: officerUser.role }, JWT_SECRET, { expiresIn: '7d' });

        sendEmail(
            'officer@gmail.com',
            'Cashflow Security Alert - Loan Officer Login Detected',
            `Hello Loan Officer,\n\nA successful login to officer@gmail.com was detected at ${new Date().toLocaleString()}.`
        );

        return res.json({ message: 'Login successful', token, user: officerUser });
    }

    const users = await getUsers();
    let user = users.find(u => u.email.toLowerCase() === email.toLowerCase());
    if (!user) {
        return res.status(400).json({ error: 'User not found. Please sign up first.' });
    }

    const token = jwt.sign({ id: user.id, email: user.email, role: user.role }, JWT_SECRET, { expiresIn: '7d' });

    sendEmail(
        email,
        'Cashflow Security Alert - Successful Login Detected',
        `Hello ${user.name},\n\nA successful login to your Cashflow account (${email}) was detected at ${new Date().toLocaleString()}.\nIf this was you, no further action is required.`
    );

    res.json({ message: 'Login successful', token, user });
});

module.exports = router;
