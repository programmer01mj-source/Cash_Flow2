const dotenv = require('dotenv');
const { Client } = require('pg');

dotenv.config();

const connectionString = process.env.DATABASE_URL;

console.log("Testing connection to PostgreSQL database with 15s timeout...");

const client = new Client({
    connectionString: connectionString,
    ssl: { rejectUnauthorized: false },
    connectionTimeoutMillis: 15000
});

async function testConnection() {
    try {
        await client.connect();
        const res = await client.query('SELECT NOW() as current_time');
        console.log("DATABASE REACHABLE & CONNECTION SUCCESSFUL!");
        console.log("Server Time:", res.rows[0].current_time);
        await client.end();
    } catch (err) {
        console.error("DATABASE CONNECTION FAILED:", err);
    }
}

testConnection();
