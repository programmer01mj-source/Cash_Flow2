const { Pool } = require('pg');
const dotenv = require('dotenv');
dotenv.config();

const pool = new Pool({
    connectionString: process.env.DATABASE_URL,
    ssl: { rejectUnauthorized: false },
    connectionTimeoutMillis: 10000
});

async function query(text, params) {
    const client = await pool.connect();
    try {
        const res = await client.query(text, params);
        return res;
    } finally {
        client.release();
    }
}

async function initDb() {
    try {
        await query(`
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(255) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                role VARCHAR(50) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);
        await query(`
            CREATE TABLE IF NOT EXISTS loans (
                id VARCHAR(255) PRIMARY KEY,
                borrower_name VARCHAR(255) NOT NULL,
                amount NUMERIC NOT NULL,
                tenure INTEGER NOT NULL,
                income NUMERIC,
                national_id VARCHAR(100),
                dob VARCHAR(50),
                status VARCHAR(50) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);
        await query(`
            CREATE TABLE IF NOT EXISTS transactions (
                id VARCHAR(255) PRIMARY KEY,
                date VARCHAR(50) NOT NULL,
                amount NUMERIC NOT NULL,
                mode VARCHAR(50) NOT NULL,
                type VARCHAR(50) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        `);
        console.log("[DB SERVICE] Remote PostgreSQL tables verified/created successfully.");
    } catch (err) {
        console.error("[DB SERVICE] Failed to initialize remote database tables:", err);
    }
}

initDb();

module.exports = { query };
