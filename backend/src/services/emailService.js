const fs = require('fs');
const path = require('path');
const nodemailer = require('nodemailer');

const emailLogsPath = path.join(__dirname, '../../email_audit_logs.json');

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.GMAIL_USER || '',
        pass: process.env.GMAIL_PASS || ''
    }
});

async function sendEmail(to, subject, body) {
    const logEntry = {
        timestamp: new Date().toISOString(),
        to,
        subject,
        body,
        status: 'PENDING'
    };

    console.log(`[EMAIL SERVICE] Attempting to send email to ${to} | Subject: "${subject}"`);

    try {
        if (process.env.GMAIL_USER && process.env.GMAIL_PASS && process.env.GMAIL_USER !== 'your-real-gmail@gmail.com') {
            await transporter.sendMail({
                from: process.env.GMAIL_USER,
                to,
                subject,
                text: body
            });
            logEntry.status = 'SENT (Real Gmail SMTP)';
            console.log(`[EMAIL SERVICE] Real email successfully sent to ${to} via Gmail SMTP.`);
        } else {
            logEntry.status = 'SENT (Simulated - Configure GMAIL_USER & GMAIL_PASS in backend/.env for real delivery)';
            console.log(`[EMAIL SERVICE] Simulated email delivery to ${to} (Gmail credentials not set in backend/.env).`);
        }
    } catch (err) {
        logEntry.status = `FAILED: ${err.message}`;
        console.error(`[EMAIL SERVICE] Failed to send real email via Gmail SMTP:`, err);
    }

    try {
        let logs = [];
        if (fs.existsSync(emailLogsPath)) {
            const data = fs.readFileSync(emailLogsPath, 'utf8');
            logs = JSON.parse(data);
        }
        logs.push(logEntry);
        fs.writeFileSync(emailLogsPath, JSON.stringify(logs, null, 2));
    } catch (err) {
        console.error('[EMAIL SERVICE] Failed to write email audit log:', err);
    }

    return true;
}

module.exports = { sendEmail };
