# Disputes & Chargebacks

## What is a Dispute?
A customer challenge to a posted charge — may result in provisional credit, investigation, and final settlement.

## Dispute Types
- **Card charge dispute** (merchant vs customer)
- **ACH/NACH dispute**
- **UPI/IMPS reversal requests**

## Dispute Lifecycle
1. **Raise dispute**: Customer provides transaction ID, reason, and evidence.
2. **Acknowledge**: System issues dispute ID and timelines.
3. **Investigate**: Contact merchant acquirer, request evidence.
4. **Resolution**: Refund / partial refund / rejection. Notify customer.

## Required Data
- Transaction ID, timestamp, merchant ID/MCC, amount, card/account details, screenshots, communication logs.

## SLAs & Timeframes
- Acknowledge within 24 hours.
- Investigations typically 7–45 days depending on rails and evidence.

## UI/UX for Assistant
- Collect the minimum required fields via chat form.
- Provide expected resolution date and escalation options.

## Chunk marker
[[CHUNK_END]]
