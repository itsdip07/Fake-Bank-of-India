# UPI — Concepts & Troubleshooting

## Overview
Unified Payments Interface: real-time instant payment system using VPAs (user@bank) or QR.

## Key Components
- **VPA (Virtual Payment Address)**
- **NPCI switch** (handles routing and settlement)
- **Collect vs Pay** flows
- **ON-MOBILE / Merchant QR flows**

## Common Success/Failure Reasons
- **Success**: Sufficient balance, correct VPA, beneficiary confirmation, valid 2FA.
- **Failure**: Wrong VPA, NPCI timeout, payer bank outage, NPCI limits, UPI PIN error.

## Stuck Transactions
- If status = PENDING for > X minutes:
  - Query acquirer switch for transaction status.
  - If debited but no credit: open ticket with NPCI/settlement team.
  - Provide customer with transaction ID & advice to wait 24 hrs before reversal unless bank confirms.

## Security & Best Practices
- Never share UPI PIN.
- Validate QR scanned data before paying.
- MFA for high value transactions.

## Chunk marker
[[CHUNK_END]]
