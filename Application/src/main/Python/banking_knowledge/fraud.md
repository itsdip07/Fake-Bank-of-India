# Fraud — Detection, Reporting, & Prevention

## Overview
Defines fraud patterns, initial detection steps, and customer-facing instructions.

## Common Fraud Types
- **Phishing / Vishing**: Credential or OTP theft via social engineering.
- **Card Skimming**: Physical capture of card data.
- **Account Takeover**: Unauthorized login / change of beneficiary.
- **Synthetic Identity Fraud**: Fake identities assembled from real & fake data.

## Detection Signals
- Unusual login locations / IPs
- Large transactions to new beneficiaries
- Rapid small value transactions (testing)
- Device fingerprint mismatch

## Response Workflow
1. **Alert**: Flag transaction, mark as suspicious.
2. **Block**: Temporary block on account or card if high risk.
3. **Investigate**: Collect logs, device fingerprints, merchant details.
4. **Remediate**: Reverse unauthorized transactions if validated, notify customer, change credentials.

## Customer Guidance (what the assistant should say)
- “We’ve temporarily blocked your card. Please confirm recent transactions: [list]. To proceed, call our fraud helpline or approve identity via OTP.”
- Always advise change of credentials and checking for unauthorized beneficiary additions.

## Regulatory Notes
- Keep audit trail, timestamps, and escalation matrix for any reversal.
- KYC & transaction reporting requirements per local regulator must be followed.

## Chunk marker
[[CHUNK_END]]
