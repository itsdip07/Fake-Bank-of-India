# Fake-Bank-of-India
A full-stack online banking app built with Spring Boot and a Python Flask AI layer. Includes BCrypt + Spring Security, OTP two-factor auth, fund transfers, and a chatbot running intent classification via FAISS and a self-hosted LLaMA model — no third-party AI API required.

## What is this?
 
Smart-AI-Powered-Online-Banking-Software is a full-stack online banking application I built to explore what it looks like when you wire a real banking backend to a locally-hosted AI model. No OpenAI API keys. No cloud AI costs. The chatbot runs on your own hardware using [Ollama](https://ollama.ai) + LLama 3.2, and understands banking intent through a custom-trained SBERT classifier backed by FAISS.
 
The Spring Boot side handles everything you'd expect — account creation, OTP-based two-factor auth, fund transfers, transaction history, PDF statements, and a role-based admin panel. The Python microservice handles the intelligence layer separately, keeping concerns clean.
 
---
 
## Features
 
### Banking Core
- **Account Registration** with Aadhaar, PAN, mobile, and email verification
- **OTP Two-Factor Auth** — delivered via SMS (Twilio) and Email simultaneously
- **Deposit, Withdraw, Transfer** with real balance checks and transaction logging
- **Beneficiary Management** — save recipients by nickname (`"Mom"`, `"Boss"`) for quick transfers
- **Paginated Transaction History** — browse through all past transactions cleanly
- **PDF Statement Download** — export a full or date-range filtered statement, masked account number included
 
### AI Chatbot
- **Intent Classification** via a custom-trained SBERT model (`.pkl`) — covers 12 intents including balance checks, transfers, UPI issues, ATM complaints, and dispute raising
- **FAISS Semantic Search** for general banking queries — finds relevant answers from a curated knowledge base
- **Ollama + LLaMA** for natural language responses to general banking questions — no third-party AI API needed
- **Two-step Transfer Flow** — the bot confirms before executing any fund transfer (`"Transfer ₹500 to Raj"` → confirm → done)
- **Pattern + Semantic Hybrid** — pattern matching for speed, semantic fallback for accuracy
 
### Security
- **BCrypt password hashing** via Spring Security
- **Role-based access control** — `ROLE_USER` and `ROLE_ADMIN` are handled automatically at login
- **Account freeze/unfreeze** — admins can lock accounts instantly
- **Session invalidation on logout** — cookies cleared, session destroyed
- **Custom access-denied page** — no ugly 403 errors
 
### Admin Panel
- View and manage all accounts
- Freeze or unfreeze any account with one click
- Browse all transactions across the platform
 
---
 
## Tech Stack
 
| Layer | Technology |
|-----------------|-------------------------------------------------------------------------------|
| Backend         | Java 17, Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend        | Thymeleaf, Bootstrap 4.5, Font Awesome |
| AI Microservice | Python, Flask, FastAPI, FAISS, Sentence Transformers (SBERT), Ollama (LLaMA) |
| Database        | PostgreSQL 18 |
| SMS             | Twilio |
| Email           | JavaMail (SMTP) |
| PDF Generation  | OpenPDF (iText fork) |
| Auth            | BCrypt, Spring Session, OTP (6-digit, 5-min expiry) |
 
---
 
## Architecture
 
```
┌─────────────────────────────────────────────────────┐
│                    Browser / Client                 │
└────────────────────────┬────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────┐
│          Spring Boot Application (Port 8080)        │
│                                                     │
│  ┌──────────────┐  ┌────────────┐  ┌─────────────┐  │
│  │  Controllers │  │  Services  │  │ Repositories│  │
│  │  BankCtrl    │  │  Account   │  │  Account    │  │
│  │  AdminCtrl   │  │  Admin     │  │  Transaction│  │
│  │  OtpCtrl     │  │  Email     │  │  Beneficiary│  │
│  │  HomeCtrl    │  │  SMS       │  │  Admin      │  │
│  └──────┬───────┘  └─────┬──────┘  └──────┬──────┘  │
│         │                │                │         │
│  ┌──────▼────────────────▼────────────────▼──────┐  │
│  │           PostgreSQL 18 Database              │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  Spring Security (BCrypt + Role-Based Access) │  │
│  └───────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────┘
                         │ REST (JSON)
┌────────────────────────▼─────────────────────────────┐
│         Python AI Microservice (Flask, Port 5000)    │
│                                                      │
│  ┌──────────────────┐    ┌───────────────────────┐   │
│  │ Intent Classifier│    │  FAISS Semantic Search│   │
│  │  (SBERT + .pkl)  │    │  (all-mpnet-base-v2)  │   │
│  └────────┬─────────┘    └───────────┬───────────┘   │
│           │                          │               │
│  ┌────────▼──────────────────────────▼───────────┐   │
│  │        Ollama (LLaMA) — Local LLM             │   │
│  └───────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
         │                          │
    Twilio (SMS)             JavaMail (Email)
```
 
---
 
## Project Structure
 
```
bankapp/
├── src/main/java/com/example/bankapp/
│   ├── config/
│   │   └── SecurityConfig.java           # Spring Security setup
│   ├── controller/
│   │   ├── AdminController.java          # Admin panel routes
│   │   ├── BankController.java           # Core banking routes
│   │   ├── HomeController.java           # Landing page
│   │   ├── BeneficiaryController.java    # Beneficiary page
│   │   ├── ChatbotController.java        # Chatbot page
│   │   └── OtpController.java            # OTP verify & resend
│   ├── model/
│   │   ├── Account.java                  # UserDetails implementation
│   │   ├── Admin.java                    # Admin user entity
│   │   ├── Beneficiary.java              # Saved transfer recipients
│   │   └── Transaction.java              # Transaction record
│   ├── repository/                       
│   │   ├── AccountRepository.java        # Acc Repo page
│   │   ├── AdminRepository.java          # Admin Repo page
│   │   ├── BeneficiaryRepository.java    # Benef Repo page
│   │   ├── TransactionRepository.java    # Trans Repo page
│   ├── service/
│   │   ├── AccountService.java           # Business logic + auth
│   │   ├── AdminService.java             # Admin operations
│   │   ├── BeneficiaryService.java       # Beneficiary CRUD
│   │   ├── EmailService.java             # JavaMail OTP + welcome email
│   │   ├── SmsService.java               # Twilio SMS
│   │   └── TransactionService.java       # Transaction creation
│   ├── dto/
│   │   ├── ChatbotResponse.java
│   │   └── TransactionDTO.java
│   └── OTP/
│       └── otpUtil.java                  # 6-digit OTP generator
│
├── src/main/resources/templates/
│   ├── index.html                        # Public landing page
│   ├── login.html                        # Login form
│   ├── register.html                     # Account registration with Aadhaar/PAN validation
│   ├── verify-otp.html                   # OTP verification screen
│   ├── dashboard.html                    # Authenticated user dashboard
│   ├── account-details.html              # View account profile & KYC info
│   ├── transactions.html                 # Paginated transaction history + PDF/email download
│   ├── beneficiaries.html                # Manage saved transfer recipients
│   ├── privacy-policy.html               # Privacy policy (glassmorphism UI)
│   ├── termsofservice.html               # Terms of service
│   ├── access-denied.html                # Custom 403 page
│   ├── admin-dashboard.html              # Admin home panel
│   ├── admin-manage-accounts.html        # View, freeze, unfreeze all accounts
│   └── admin-transactions.html           # View all platform transactions
│   ├── ex_products_page.html             # Product catalogue grid (12 product cards)
│   ├── ai_banking.html                   # AI Banking feature page
│   ├── savings_acc.html                  # Savings Account — hero, features, KYC docs
│   ├── current_acc.html                  # Current Account — business banking page
│   ├── fixed_deposit.html                # Fixed Deposits — interest rate card, tax saver FD
│   ├── debit.html                        # Debit Cards — contactless, limits, benefits
│   ├── credit_cards.html                 # Credit Cards product page
│   ├── personal_loans.html               # Personal Loans — up to ₹40L, instant approval
│   ├── home_loans.html                   # Home Loans — rates starting 8.50%
│   ├── vehicle_loans.html                # Vehicle Loans — up to 100% on-road funding
│   ├── demat.html                        # Demat Account — stock trading, low brokerage
│   ├── investments.html                  # Investments — Bonds, NPS, Gold portfolios
│   ├── groww.html                        # Stocks & Mutual Funds — SIP from ₹500
│   └── support_page.html                 # 24×7 Support — AI chatbot
│
ai-service/
├── ai_service.py                         # Main Flask AI server
├── app.py                                # FastAPI semantic search endpoint
├── build_embeddings.py                   # Build FAISS vector store
├── train_intent_model_sbert.py           # Train SBERT intent classifier
└── test_search.py                        # Test semantic search
```
 
---
 
## Getting Started
 
### Prerequisites
 
- Java 17+
- Maven 3.8+
- PotgreSQL 8
- Python 3.8+
- [Ollama](https://ollama.ai) installed locally with LLaMA pulled (`ollama pull llama3`)
- A Twilio account (for SMS)
- An SMTP-enabled email account (Gmail works fine)
 
---
 
### 1. Clone the repository
 
```bash
git clone https://github.com/aakash8149/Smart-AI-Powered-Online-Banking-Software.git
```
 
---
 
### 2. Set up the database
 
```sql
CREATE DATABASE bankapp;
```
 
Spring Boot will create the tables on first run via JPA.
 
---
 
### 3. Configure `application.properties`
 
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/bankapp
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
 
# Email (Gmail example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
 
# Twilio
twilio.account.sid=your_twilio_sid
twilio.auth.token=your_twilio_token
twilio.phone.number=+1XXXXXXXXXX
```
 
---
 
### 4. Run the Spring Boot backend
 
```bash
mvn spring-boot:run
```
 
App will start at `http://localhost:8080`
 
---
 
### 5. Set up the Python AI microservice
 
```bash
cd ai-service
pip install -r requirements.txt
```
 
Build the FAISS vector store:
 
```bash
python build_embeddings.py
```
 
Train the intent classifier:
 
```bash
python train_intent_model_sbert.py
```
 
Start Ollama (in a separate terminal):
 
```bash
ollama serve
```
 
Start the Flask AI server:
 
```bash
python ai_service.py
```
 
AI service will run at `http://localhost:5000`
 
---
 
### 6. Creating an Admin Account
 
After registering a normal user, manually insert their account ID into the `admin_users` table:
 
```sql
INSERT INTO admin_users (account_id, username, admin_name)
VALUES (YOUR_ACCOUNT_ID, 'your_username', 'Admin Name');
```
 
On next login, Spring Security will assign `ROLE_ADMIN` automatically.
 
---
 
## Chatbot Intent Coverage
 
| Intent | Example Query |
|---|---|
| `get_balance` | "What's my balance?" |
| `account_details` | "Show my account info" |
| `transaction_history` | "Show my last 5 transactions" |
| `download_statement` | "Download my bank statement" |
| `fund_transfer` | "Transfer ₹500 to Raj" |
| `dispute_raise` | "I want to raise a dispute" |
| `atm_issue` | "Money was debited but not dispensed" |
| `account_closure` | "How do I close my account?" |
| `general_banking_query` | "What is NEFT?" / "Explain RTGS" |
| `greeting` | "Hi", "Hello" |
| `unknown` | Anything outside scope |
 
---
 
## Notes
 
- OTPs expire after **5 minutes** and can be resent
- Transaction IDs are 10–12 digit random integers checked for uniqueness before saving
- Account IDs are 8–9 digit random integers, same uniqueness check
- PDF statements mask the account number as `XXXXNNNN`
- Frozen accounts cannot log in — `isEnabled()` returns `false` when `frozen = true`
 
---
