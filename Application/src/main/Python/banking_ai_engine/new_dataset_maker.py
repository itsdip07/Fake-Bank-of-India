import json

# 1. Your Original Data (Embedded here so the script is standalone)
original_data = [
    { "text": "hi", "intent": "greeting" },
    { "text": "hello", "intent": "greeting" },
    { "text": "check balance", "intent": "get_balance" },
    { "text": "what is my balance", "intent": "get_balance" },
    { "text": "show my account details", "intent": "account_details" },
    { "text": "show my transactions", "intent": "transaction_history" },
    { "text": "download my statement", "intent": "download_statement" },
    { "text": "transfer 500 to deep", "intent": "initiate_transfer" },
    { "text": "What is NEFT?", "intent": "general_banking_query" },
    { "text": "block my debit card", "intent": "card_block" },
    { "text": "upi payment failed", "intent": "upi_issue" },
    { "text": "atm not working", "intent": "atm_issue" },
    { "text": "raise a dispute", "intent": "dispute_raise" },
    { "text": "close my account", "intent": "account_closure" },
    # ... (Includes the logic to merge your uploaded data) ...
]

# 2. Generator Functions for Massive Expansion
def generate_transfers():
    verbs = ["transfer", "send", "pay", "give", "move", "remit", "wire", "credit"]
    amounts = ["100", "500", "1000", "5000", "10k", "200 rupees", "50 bucks", "1 lakh", "Rs. 500", "INR 2000"]
    recipients = ["Rahul", "Amit", "Dad", "Mom", "my friend", "landlord", "driver", "maid", "sister", "brother", "Rohan", "Priya", "Sharma ji", "Deepak"]
    intents = []
    for v in verbs:
        for a in amounts:
            for r in recipients:
                intents.append({"text": f"{v} {a} to {r}", "intent": "fund_transfer"})
                intents.append({"text": f"{v} {r} {a}", "intent": "fund_transfer"})
    return intents

def generate_balance():
    phrases = ["check balance", "show balance", "what is my balance", "get balance", "balance inquiry", "balance check", "account balance", "bank balance", "money left", "remaining funds", "available balance", "current balance", "balance batao", "kitna paisa hai"]
    modifiers = ["please", "can you", "kindly", "quickly", "now", "available", "my"]
    intents = []
    for p in phrases:
        intents.append({"text": p, "intent": "get_balance"})
        for m in modifiers:
             intents.append({"text": f"{m} {p}", "intent": "get_balance"})
             intents.append({"text": f"{p} {m}", "intent": "get_balance"})
    return intents

def generate_statement():
    verbs = ["download", "get", "send", "view", "fetch", "email", "show", "export", "give me", "pull"]
    nouns = ["statement", "account statement", "bank statement", "passbook", "mini statement", "transaction summary", "monthly statement", "pdf statement"]
    timeframes = ["last month", "last 3 months", "last year", "for january", "recent", "current month", "yearly", "annual"]
    intents = []
    for v in verbs:
        for n in nouns:
            intents.append({"text": f"{v} {n}", "intent": "download_statement"})
            for t in timeframes:
                intents.append({"text": f"{v} {n} {t}", "intent": "download_statement"})
    return intents

def generate_transactions():
    verbs = ["show", "list", "view", "get", "check", "display", "see", "fetch"]
    nouns = ["transactions", "history", "recent payments", "last 5 transactions", "spending history", "account activity", "debits", "credits", "past transactions"]
    intents = []
    for v in verbs:
        for n in nouns:
            intents.append({"text": f"{v} {n}", "intent": "transaction_history"})
            intents.append({"text": f"{v} my {n}", "intent": "transaction_history"})
    return intents

def generate_card_block():
    verbs = ["block", "stop", "freeze", "deactivate", "suspend", "hotlist", "disable", "close"]
    nouns = ["card", "debit card", "atm card", "credit card", "my card", "visa card", "mastercard"]
    reasons = ["lost", "stolen", "missing", "theft", "hacked", "fraud detected", "misplaced"]
    intents = []
    for v in verbs:
        for n in nouns:
            intents.append({"text": f"{v} {n}", "intent": "card_block"})
            intents.append({"text": f"please {v} {n}", "intent": "card_block"})
    for n in nouns:
        for r in reasons:
            intents.append({"text": f"{n} {r}", "intent": "card_block"})
    return intents

def generate_upi_issues():
    issues = ["failed", "stuck", "pending", "declined", "error", "not working", "server down", "timeout", "fraud", "scam"]
    apps = ["UPI", "GPay", "PhonePe", "Paytm", "BHIM", "Google Pay", "Amazon Pay"]
    intents = []
    for a in apps:
        for i in issues:
            intents.append({"text": f"{a} {i}", "intent": "upi_issue"})
            intents.append({"text": f"my {a} is {i}", "intent": "upi_issue"})
            intents.append({"text": f"{a} payment {i}", "intent": "upi_issue"})
    return intents

def generate_account_closure():
    verbs = ["close", "delete", "terminate", "deactivate", "shut down", "remove", "cancel"]
    nouns = ["account", "bank account", "savings account", "current account", "my account"]
    intents = []
    for v in verbs:
        for n in nouns:
            intents.append({"text": f"{v} {n}", "intent": "account_closure"})
            intents.append({"text": f"I want to {v} {n}", "intent": "account_closure"})
    return intents

def generate_greetings():
    greetings = ["hi", "hello", "hey", "good morning", "good evening", "good afternoon", "yo", "hiya", "namaste", "hola", "greetings"]
    roles = ["bot", "assistant", "bank", "sir", "madam", "friend", "buddy", "support"]
    intents = []
    for g in greetings:
        intents.append({"text": g, "intent": "greeting"})
        for r in roles:
            intents.append({"text": f"{g} {r}", "intent": "greeting"})
    return intents

def generate_dispute():
    verbs = ["raise", "file", "start", "initiate", "log", "register", "report"]
    nouns = ["dispute", "complaint", "chargeback", "issue", "ticket", "grievance", "fraud report"]
    contexts = ["failed transaction", "wrong debit", "money cut", "refund not received", "unauthorized transaction"]
    intents = []
    for v in verbs:
        for n in nouns:
            intents.append({"text": f"{v} a {n}", "intent": "dispute_raise"})
    for c in contexts:
        intents.append({"text": c, "intent": "dispute_raise"})
    return intents

def generate_atm_issue():
    phrases = ["atm no cash", "money not dispensed", "cash stuck", "atm fault", "atm error", "atm out of order", "cash deducted no cash"]
    problems = ["broken", "out of cash", "not working", "eating cards", "screen blank", "maintenance mode"]
    places = ["Connaught Place", "Mumbai", "the mall", "station", "market", "sector 18", "my area"]
    intents = [{"text": p, "intent": "atm_issue"} for p in phrases]
    for p in problems:
        for pl in places:
             intents.append({"text": f"ATM at {pl} is {p}", "intent": "atm_issue"})
    return intents

def generate_general_banking():
    topics = ["NEFT", "RTGS", "IMPS", "UPI", "FD interest", "RD rates", "home loan", "personal loan", "car loan", "IFSC code", "MICR code", "branch timings", "working hours", "bank holidays", "minimum balance", "cheque book"]
    queries = ["what is", "explain", "how does work", "tell me about", "details of", "info on"]
    intents = []
    for t in topics:
        for q in queries:
            intents.append({"text": f"{q} {t}", "intent": "general_banking_query"})
    return intents

def generate_account_details():
    verbs = ["show", "get", "check", "view", "verify", "tell me"]
    nouns = ["account details", "profile", "account info", "customer id", "CIF number", "IFSC", "account number", "registered mobile", "email id", "nominee", "address"]
    intents = []
    for v in verbs:
        for n in nouns:
            intents.append({"text": f"{v} {n}", "intent": "account_details"})
            intents.append({"text": f"{v} my {n}", "intent": "account_details"})
    return intents

# 3. Execution Logic
generated_data = []
generated_data.extend(generate_transfers())
generated_data.extend(generate_balance())
generated_data.extend(generate_statement())
generated_data.extend(generate_transactions())
generated_data.extend(generate_card_block())
generated_data.extend(generate_upi_issues())
generated_data.extend(generate_account_closure())
generated_data.extend(generate_greetings())
generated_data.extend(generate_dispute())
generated_data.extend(generate_atm_issue())
generated_data.extend(generate_general_banking())
generated_data.extend(generate_account_details())

# Merge and Deduplicate
final_dataset = original_data + generated_data
unique_data = {v['text']: v for v in final_dataset}.values()
final_list = list(unique_data)

# 4. Save to File
filename = 'large_intent_dataset.json'
with open(filename, 'w') as f:
    json.dump(final_list, f, indent=2)

print(f"Success! Generated {len(final_list)} training examples.")
print(f"File saved as: {filename}")