# Cards — Debit and Credit

## Overview
Covers debit and credit card lifecycle, limits, blocking, disputes, and common user actions.

## Key Concepts
- **Debit Card**: Linked to account; immediate debit on payments/ATM.
- **Credit Card**: Bank extends a credit line; monthly billing cycle, minimum due.
- **Card Status**: Active, blocked, lost/stolen, replaced, expired.
- **EMV / Chip & PIN / Contactless**: Card tech variants.

## Operations
- **Block card**: Temporary/permanent block API → disable authorization.
- **Replace card**: Issue new PAN, link to same account, invalidate old PAN.
- **Change PIN**: Authenticate with OTP → remote PIN change or in-ATM process.

## Fraud & Chargebacks
- Merchant dispute → provisional credit → investigation → final resolution.
- Unauthorized transaction workflow: user complaint → block card → investigate → possible refund.

## Sample API actions (for integration)
- `POST /api/card/block` {accountId, reason}
- `POST /api/card/replace` {accountId, address}
- `GET /api/card/transactions?cardId=`

## Chunk marker
[[CHUNK_END]]
