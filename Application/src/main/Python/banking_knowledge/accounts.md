# Accounts — Overview

## Short Description
This document explains types of bank accounts, key attributes, lifecycle events, and common user scenarios.

## Account Types
- **Savings Account (SA)**: For individuals, limited withdrawals, interest-bearing.
- **Current Account (CA)**: For businesses, no/low interest, higher transactional allowances.
- **Fixed Deposit (FD)**: Locked-term deposit with higher interest.
- **Recurring Deposit (RD)**: Monthly deposits with fixed tenor.
- **Loan Accounts**: Personal, Home, Auto, Education — credit accounts with amortization schedules.

## Core Attributes (per account)
- Account number, IFSC, branch code
- Account holder name(s)
- Account type, status (active, dormant, closed)
- Balance, available balance, ledger balance
- KYC status, nominee details, product code

## Typical User Flows
1. **Open account**: KYC verification → initial deposit → account provisioning → welcome message.
2. **Close account**: Validate outstanding dues → mark closed → settle funds → generate closure letter.
3. **Dormancy**: No transaction for 12/24 months → send reminders → mark dormant → reactivation on KYC + transaction.

## Troubleshooting / FAQs
- **Why is my account showing less balance?**
  - Pending debits, holds, uncleared checks, or merchant authorizations.
- **Why can’t I open an account online?**
  - Missing KYC, document mismatch, system downtime.

## Chunk marker
[[CHUNK_END]]
