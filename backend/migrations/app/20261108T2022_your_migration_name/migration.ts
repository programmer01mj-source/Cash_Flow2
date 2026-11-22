#!/usr/bin/env -S node
import type { Contract as End } from '../../snapshots/84172394b1d62add9fcf11861bc99f257036d289a8cfc4a47fcdc26e68448d31/contract';
import endContract from '../../snapshots/84172394b1d62add9fcf11861bc99f257036d289a8cfc4a47fcdc26e68448d31/contract.json' with { type: 'json' };
import type { Contract as Start } from '../../snapshots/a51b4833fdaffe730572670ccc27dd89af7e0726ab3de7f428d21702d687cc15/contract';
import startContract from '../../snapshots/a51b4833fdaffe730572670ccc27dd89af7e0726ab3de7f428d21702d687cc15/contract.json' with { type: 'json' };
import {
  Migration,
  MigrationCLI,
  checkExpression,
  col,
  fn,
  lit,
  primaryKey,
  sql,
} from '@prisma/orm-postgres/migration';

export default class M extends Migration<Start, End> {
  override readonly startContractJson = startContract;
  override readonly endContractJson = endContract;

  override get operations() {
    return [
      this.dropTable({ schema: 'public', table: 'Post' }),
      this.dropDefault({ schema: 'public', table: 'User', column: 'id' }),
      this.createTable({
        schema: 'public',
        table: 'Loan',
        columns: [
          col('amount', 'float8', { notNull: true, codecRef: { codecId: 'pg/float8@1' } }),
          col('authorId', 'text', { notNull: true, codecRef: { codecId: 'pg/text@1' } }),
          col('content', 'text', { codecRef: { codecId: 'pg/text@1' } }),
          col('createdAt', 'timestamptz', {
            notNull: true,
            default: fn(sql`now()`),
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
          col('id', 'text', { notNull: true, codecRef: { codecId: 'pg/text@1' } }),
          col('title', 'text', { notNull: true, codecRef: { codecId: 'pg/text@1' } }),
          col('updatedAt', 'timestamptz', {
            notNull: true,
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
        ],
        constraints: [primaryKey(['id'])],
      }),
      this.createTable({
        schema: 'public',
        table: 'Transaction',
        columns: [
          col('TransactionType', 'text', {
            notNull: true,
            default: lit('Offer'),
            codecRef: { codecId: 'pg/text@1' },
          }),
          col('amount', 'float8', { notNull: true, codecRef: { codecId: 'pg/float8@1' } }),
          col('createdAt', 'timestamptz', {
            notNull: true,
            default: fn(sql`now()`),
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
          col('id', 'text', { notNull: true, codecRef: { codecId: 'pg/text@1' } }),
          col('loanId', 'text', { notNull: true, codecRef: { codecId: 'pg/text@1' } }),
          col('transactionMode', 'text', {
            notNull: true,
            default: lit('Cash'),
            codecRef: { codecId: 'pg/text@1' },
          }),
          col('updatedAt', 'timestamptz', {
            notNull: true,
            codecRef: { codecId: 'pg/timestamptz-string@1' },
          }),
        ],
        constraints: [
          primaryKey(['id']),
          checkExpression(
            'Transaction_TransactionType_check_4386e8dc',
            sql`"TransactionType" IN ('Offer', 'Payback')`,
          ),
          checkExpression(
            'Transaction_transactionMode_check_05795bb1',
            sql`"transactionMode" IN ('Cash', 'Banktransfer', 'Tnm_Mpamba', 'Airtel_money')`,
          ),
        ],
      }),
      this.addColumn({
        schema: 'public',
        table: 'User',
        column: col('role', 'text', {
          notNull: true,
          default: lit('Customer'),
          codecRef: { codecId: 'pg/text@1' },
        }),
      }),
      this.addColumn({
        schema: 'public',
        table: 'User',
        column: col('state', 'text', {
          notNull: true,
          default: lit('Active'),
          codecRef: { codecId: 'pg/text@1' },
        }),
      }),
      this.alterColumnType({
        schema: 'public',
        table: 'User',
        column: 'id',
        type: 'text',
        operationClass: 'widening',
      }),
      this.setNotNull({ schema: 'public', table: 'User', column: 'name' }),
      this.addCheckConstraint({
        schema: 'public',
        table: 'User',
        constraint: 'User_role_check_95d382a9',
        expression: sql`"role" IN ('Customer', 'Officer', 'Admin')`,
      }),
      this.addCheckConstraint({
        schema: 'public',
        table: 'User',
        constraint: 'User_state_check_580c9e9f',
        expression: sql`"state" IN ('Active', 'Blacklisted')`,
      }),
      this.createIndex({
        schema: 'public',
        table: 'Loan',
        index: 'Loan_authorId_idx_e47547ed',
        columns: ['authorId'],
      }),
      this.createIndex({
        schema: 'public',
        table: 'Transaction',
        index: 'Transaction_loanId_idx_d4eddfc5',
        columns: ['loanId'],
      }),
      this.addForeignKey({
        schema: 'public',
        table: 'Loan',
        foreignKey: {
          name: 'Loan_authorId_fkey',
          columns: ['authorId'],
          references: { schema: 'public', table: 'User', columns: ['id'] },
        },
      }),
      this.addForeignKey({
        schema: 'public',
        table: 'Transaction',
        foreignKey: {
          name: 'Transaction_loanId_fkey',
          columns: ['loanId'],
          references: { schema: 'public', table: 'Loan', columns: ['id'] },
        },
      }),
    ];
  }
}

MigrationCLI.run(import.meta.url, M);
