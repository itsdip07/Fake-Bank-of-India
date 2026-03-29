# Payments — Basic & Electronic Channels

## Description
Explains payment rails, how they behave, and sender/receiver experience.

## Payment Rails
- **NEFT**: Batch settlement through RBI — generally hours (non-real-time historically).
- **RTGS**: For high-value real-time gross settlement (threshold applies).
- **IMPS**: Immediate Payment Service — real-time, 24x7 small/medium transfers.
- **UPI**: Real-time RTP using VPA/QR — overlay on IMPS; supports collect, push, mandates.
- **NACH/ACH**: Bulk debit/credit instructions (salaries, mandates).

## Payment Attributes
- Transaction ID, timestamp, debit/credit indicator
- Status codes: INITIATED / PENDING / SUCCESS / FAILED / REVERSED
- Settlement ledger entries, clearing reference, bank reference
- Failure reasons: insufficient funds, beneficiary mismatch, network timeout, RBI/NPCI rule rejection

## Common Scenarios
- **Payment stuck**: Check status via transaction ID → if PENDING for >X hrs, raise manual reconciliation.
- **Failed debit but account debited**: Could be reversal pending; reconcile with bank logs.

## Security & Checks
- Validate recipient account + IFSC / VPA on entry
- OTP / 2FA confirmation for initiations
- Limits enforcement (per transaction / per day)

## Chunk marker
[[CHUNK_END]]
