# ai_service.py
"""
Upgraded Flask AI for your Banking App.

Behavior:
 - Intent detection ALWAYS by intent_model_sbert.pkl + intent_vectorizer.pkl
 - If intent == general_info -> use FAISS semantic search + Ollama for a friendly reply (feminine tone)
 - If intent is an ACTION (get_balance, fund_transfer, account_details, transaction_history, download_statement)
   -> return structured response WITHOUT calling Llama; for fund_transfer we start a pending flow.
 - Pending transfer flow:
     1) User: "Transfer 500 to irvin@kol" => returns intent "initiate_transfer" + params + pending_id
     2) User: "confirm transfer" or "confirm transfer <pending_id>" => returns intent "transfer_funds" + params
 - Stable JSON output: {"intent": "...", "message": "...", "params": {...}}
"""

import os, re, json, time, uuid, subprocess
from typing import Optional, Dict, Any, List, Tuple
from flask import Flask, request, jsonify
import pickle
import faiss
import numpy as np
from sentence_transformers import SentenceTransformer

# ---------- INTENT LABELS WE EXPECT ----------
INTENT_LABELS = [
    "get_balance",
    "account_details",
    "transaction_history",
    "download_statement",
    "fund_transfer",
    "greeting",
    "general_banking_query",
    "dispute_raise",
    "upi_issue",
    "atm_issue",
    "account_closure",
    "unknown"
]

# ---------- PATTERN-BASED INTENT HINTS ----------
INTENT_PATTERNS = {
    "get_balance": [
        "balance", "available balance", "how much money", "check balance", "current balance"
    ],
    "download_statement": [
        "statement", "bank statement", "download statement", "pdf statement"
    ],
    "transaction_history": [
        "transaction history", "recent transactions", "last transactions", "mini statement"
    ],
    "account_details": [
        "account details", "account info", "my account number", "account information", "profile details"
    ],
    "fund_transfer": [
        "transfer", "send money", "pay", "send rupees", "send rs", "fund transfer"
    ],
    "greeting": [
        "hi", "hello", "hey", "good morning", "good evening"
    ]
}

# ---------- CANONICAL EXAMPLES FOR SEMANTIC REFINEMENT ----------
INTENT_EXAMPLES = {
    "get_balance": [
        "What is my account balance?",
        "Show my available balance.",
        "How much money do I have?"
    ],
    "account_details": [
        "Show my account details.",
        "What is my account number?",
        "Show my profile information."
    ],
    "transaction_history": [
        "Show my last 5 transactions.",
        "Give me my recent transaction history.",
        "Show recent debits and credits."
    ],
    "download_statement": [
        "Download my bank statement.",
        "Generate my account statement PDF.",
        "I want my statement for last month."
    ],
    "fund_transfer": [
        "Transfer 500 rupees to Raj.",
        "Send 2000 to my brother.",
        "Pay 1000 to Ankit via account."
    ],
    "greeting": [
        "Hi", "Hello", "Hey there"
    ],
    "general_banking_query": [
        "What is NEFT?",
        "Explain UPI.",
        "How does RTGS work?"
    ]
}


# load SBERT + classifier (this bundle was created by train_intent_model_sbert.py)
bundle = pickle.load(open("intent_model_sbert.pkl", "rb"))
clf = bundle["classifier"]
sbert = SentenceTransformer(bundle["embedding_model"])

def predict_intent(query: str) -> Tuple[str, float]:
    """
    Returns (intent_label, confidence_score).
    Tries predict_proba first (LogisticRegression supports it). If not available,
    falls back to decision_function -> softmax for a probability-like score.
    """
    vec = sbert.encode([query])
    # Ensure numpy float32 2D
    vec_np = np.asarray(vec, dtype="float32")
    try:
        # If classifier supports predict_proba (LogisticRegression), use it
        probs = clf.predict_proba(vec_np)[0]
        idx = int(np.argmax(probs))
        intent = clf.classes_[idx]
        confidence = float(probs[idx])
        return intent, confidence
    except Exception:
        try:
            # Fallback: use decision_function -> softmax to get normalized scores
            scores = clf.decision_function(vec_np)[0]
            # If binary it returns a single score, handle accordingly
            if np.ndim(scores) == 0 or (hasattr(scores, "shape") and scores.shape == ()):
                # convert single score to probability-like value via sigmoid
                val = float(scores)
                confidence = 1.0 / (1.0 + np.exp(-val))
                intent = clf.classes_[0] if confidence < 0.5 else clf.classes_[1] if len(clf.classes_) > 1 else clf.classes_[0]
                return intent, float(confidence)
            else:
                # multiclass: softmax
                exp_scores = np.exp(scores - np.max(scores))
                soft = exp_scores / np.sum(exp_scores)
                idx = int(np.argmax(soft))
                intent = clf.classes_[idx]
                confidence = float(soft[idx])
                return intent, confidence
        except Exception:
            # Last resort: predict label, return low confidence
            try:
                intent = clf.predict(vec_np)[0]
                return intent, 0.5
            except Exception:
                return "unknown", 0.0

def rule_based_intent(text: str) -> Optional[str]:
    """
    Simple high-precision rule layer.
    Used to override or confirm the ML prediction when clear keywords exist.
    """
    t = text.lower()
    for intent, phrases in INTENT_PATTERNS.items():
        for p in phrases:
            if p in t:
                return intent
    return None


def refine_with_semantic_intent(text: str, initial_intent: str, threshold: float = 0.70) -> str:
    """
    Compare the user query with canonical intent examples using SBERT.
    If another intent is semantically much closer, switch to that.
    """
    q_emb = embed_model.encode([text]).astype("float32")  # shape (1, 768)
    # cosine similarity manually
    q_norm = q_emb / (np.linalg.norm(q_emb, axis=1, keepdims=True) + 1e-9)
    ex_norm = intent_example_embeddings / (np.linalg.norm(intent_example_embeddings, axis=1, keepdims=True) + 1e-9)
    sims = (q_norm @ ex_norm.T)[0]  # shape (N,)

    best_idx = int(np.argmax(sims))
    best_score = float(sims[best_idx])
    best_label = intent_example_labels[best_idx]

    # If similarity high and different from initial, prefer semantic
    if best_score >= threshold and best_label != initial_intent:
        print(f"[INTENT-REFINE] semantic override {initial_intent} -> {best_label} (score={best_score:.2f})")
        return best_label

    return initial_intent


def detect_intent_final(user_text: str) -> Tuple[str, float]:
    """
    Triple-layer intent:
      1) SBERT classifier
      2) Rule-based override
      3) Semantic similarity refinement
    """
    # 1) SBERT classifier
    ml_intent, ml_conf = predict_intent(user_text)

    # 2) Rule-based override (for clean cases)
    rb_intent = rule_based_intent(user_text)
    if rb_intent:
        # if rule-based and ml disagree, choose rule if ml confidence < 0.90
        if rb_intent != ml_intent and ml_conf < 0.90:
            print(f"[INTENT-RULE] overriding {ml_intent} -> {rb_intent}")
            return rb_intent, 0.98
        # they agree -> strong confidence
        if rb_intent == ml_intent:
            return ml_intent, max(ml_conf, 0.99)

    # 3) semantic refinement
    final_intent = refine_with_semantic_intent(user_text, ml_intent)
    if final_intent != ml_intent:
        return final_intent, 0.97

    return final_intent, ml_conf


# ---------------- CONFIG ----------------
VECTOR_STORE_DIR = "vector_store"
FAISS_INDEX_PATH = os.path.join(VECTOR_STORE_DIR, "faiss.index")
METADATA_PATH = os.path.join(VECTOR_STORE_DIR, "metadata.json")
EMBEDDING_MODEL = "sentence-transformers/all-mpnet-base-v2"

# Ollama HTTP API (same pattern used by your Jarvis)
OLLAMA_MODEL = "llama3.2"
OLLAMA_URL = "http://localhost:11434/api/generate"   # your working endpoint
OLLAMA_TIMEOUT = 90

# Confirmation tokens
CONFIRM_TOKENS = {"yes", "confirm", "confirm transfer", "proceed", "do it", "please proceed", "okay", "ok"}

# Banking-only system instructions and feminine tone (choice C)
SYSTEM_INSTRUCTIONS = (
    "You are a professional banking assistant and must only answer banking and finance related questions. "
    "Keep responses friendly and in a feminine tone. Use short clear sentences. "
    "Do not provide medical, legal, political, or sexual content. If the user asks outside banking, reply: "
    "\"I'm sorry — I can only help with banking and finance related questions. Please ask a banking question.\""
)

# Action intents list (these will NOT call Llama)
ACTION_INTENTS = {
    "get_balance",
    "fund_transfer",
    "account_details",
    "transaction_history",
    "download_statement",
    "initiate_transfer"  # internal transitional intent
}

# ---------------- APP ----------------
app = Flask(__name__)

# ---------------- LOAD MODELS ----------------
print("Loading embedding model & FAISS index...")
embed_model = SentenceTransformer(EMBEDDING_MODEL)
if not os.path.exists(FAISS_INDEX_PATH) or not os.path.exists(METADATA_PATH):
    raise RuntimeError("FAISS index or metadata missing. Run build_embeddings.py first.")
index = faiss.read_index(FAISS_INDEX_PATH)
with open(METADATA_PATH, "r", encoding="utf-8") as f:
    metadata = json.load(f)
    
# --------- PRECOMPUTE EMBEDDINGS FOR INTENT EXAMPLES ----------
print("Building semantic intent example embeddings...")
intent_example_texts = []
intent_example_labels = []

for label, examples in INTENT_EXAMPLES.items():
    for ex in examples:
        intent_example_texts.append(ex)
        intent_example_labels.append(label)

intent_example_embeddings = embed_model.encode(intent_example_texts).astype("float32")
print(f"Loaded {len(intent_example_texts)} semantic intent examples.")


print("Loading intent detection model...")

print("Models loaded.")

# ---------------- in-memory pending transfers ----------------
# pending_store: pending_id -> {"amount": float, "recipient": str, "created_at": ts}
pending_store: Dict[str, Dict[str, Any]] = {}
# Keep a single last_pending id (fallback for simple confirm transfer without id)
last_pending_id: Optional[str] = None

# ---------------- HELPERS ----------------
AMOUNT_RE = re.compile(r"(?:rs\.?|inr|₹)?\s*([0-9]+(?:[.,][0-9]{1,2})?)", re.I)
RECEIVER_RE = re.compile(r"(?:to|pay|send to|for)\s+([\w@.\-+]{2,80})", re.I)
UPI_RE = re.compile(r"[a-zA-Z0-9.\-_]{2,}@[a-zA-Z0-9]{2,}")

def semantic_search(query: str, top_k: int = 3) -> List[Dict[str, Any]]:
    emb = embed_model.encode([query]).astype("float32")
    dists, idxs = index.search(emb, top_k)
    results = []
    for dist, idx in zip(dists[0], idxs[0]):
        results.append({"score": float(dist), "source": metadata[idx]["source"], "text": metadata[idx]["text"]})
    return results

def extract_amount(text: str) -> Optional[float]:
    s = text.replace(',', '')
    m = AMOUNT_RE.search(s)
    if not m:
        return None
    try:
        return float(m.group(1))
    except:
        return None

def extract_recipient(text: str) -> Optional[str]:
    m = RECEIVER_RE.search(text)
    if m:
        return m.group(1).strip()
    m2 = UPI_RE.search(text)
    if m2:
        return m2.group(0)
    # fallback: last word
    parts = text.split()
    if "to" in parts:
        i = parts.index("to")
        if i+1 < len(parts):
            return parts[i+1]
    return None

def contains_confirmation(text: str) -> Optional[str]:
    t = text.lower()
    for tok in CONFIRM_TOKENS:
        if tok in t:
            return tok
    return None

def call_ollama(prompt: str) -> str:
    """
    Calls Ollama HTTP API similar to your Jarvis. Expects JSON response with 'response' key.
    """
    payload = {
        "model": OLLAMA_MODEL,
        "prompt": prompt,
        "stream": False
    }
    try:
        import requests
        resp = requests.post(OLLAMA_URL, json=payload, timeout=OLLAMA_TIMEOUT)
        resp.raise_for_status()
        j = resp.json()
        raw = j.get("response") or j.get("text") or j.get("output") or ""
        # Basic cleaning: strip markdown characters for TTS/UX
        cleaned = raw.replace("**", "").replace("#", "").strip()
        return cleaned
    except Exception as e:
        print("OLLAMA call error:", e)
        return ""

# Ensure stable JSON output
def build_response(intent: str, message: str, params: Dict[str, Any] = None) -> Dict[str, Any]:
    if params is None:
        params = {}
    return {"intent": intent, "message": message, "params": params}

# ---------------- ENDPOINT ----------------
@app.route("/ai/query", methods=["POST"])
def ai_query():
    global last_pending_id
    try:
        payload = request.get_json(force=True)
        user_text = payload.get("query", "").strip()

        if not user_text:
            return jsonify(build_response("unknown", "Empty query received.", {})), 200

        # --------------------------------------------------------
        # STEP 1 — SBERT INTENT DETECTION (Authoritative)
        # --------------------------------------------------------
        intent, confidence = detect_intent_final(user_text)
        print(f"[INTENT-FINAL] {intent}  (confidence={confidence:.2f})")


        # --------------------------------------------------------
        # STEP 2 — DIRECT ACTION INTENTS (NO LLAMA!)
        # --------------------------------------------------------
        if intent == "get_balance":
            return jsonify(build_response(
                "get_balance",
                "Fetching your account balance…",
                {}
            )), 200

        if intent == "account_details":
            return jsonify(build_response(
                "account_details",
                "Retrieving your account details…",
                {}
            )), 200

        if intent == "transaction_history":
            return jsonify(build_response(
                "transaction_history",
                "Opening your transaction history…",
                {}
            )), 200

        if intent == "download_statement":
            return jsonify(build_response(
                "download_statement",
                "Preparing your downloadable bank statement…",
                {}
            )), 200

        # --------------------------------------------------------
        # STEP 3 — FUND TRANSFER (Two-step flow with pending)
        # --------------------------------------------------------
        if intent == "fund_transfer":
            amount = extract_amount(user_text)
            recipient = extract_recipient(user_text)

            # CHECK FOR CONFIRMATION PHRASE
            if contains_confirmation(user_text):
                if not pending_store:
                    return jsonify(build_response(
                        "unknown",
                        "No pending transfer to confirm.",
                        {}
                    )), 200

                pid = last_pending_id
                if not pid or pid not in pending_store:
                    return jsonify(build_response(
                        "unknown",
                        "Pending transfer not found or expired.",
                        {}
                    )), 200

                p = pending_store.pop(pid)
                last_pending_id = None

                final_params = {
                    "amount": p["amount"],
                    "recipient": p["recipient"],
                    "pending_id": pid
                }

                return jsonify(build_response(
                    "transfer_funds",
                    f"Confirmed. Proceeding to transfer ₹{p['amount']:.2f} to {p['recipient']}.",
                    final_params
                )), 200

            # MISSING INFO CASE
            missing = []
            if amount is None:
                missing.append("amount")
            if not recipient:
                missing.append("recipient")

            if missing:
                return jsonify(build_response(
                    "initiate_transfer",
                    f"I need the {', '.join(missing)} to proceed. Example: 'Transfer 500 to Deep'.",
                    {}
                )), 200

            # CREATE NEW PENDING TRANSFER
            pid = str(uuid.uuid4())[:8]
            pending_store[pid] = {
                "amount": float(amount),
                "recipient": recipient,
                "created_at": time.time()
            }
            last_pending_id = pid

            return jsonify(build_response(
                "initiate_transfer",
                f"I understand you want to transfer ₹{amount:.2f} to {recipient}. "
                f"Reply 'confirm transfer' or 'confirm {pid}' to proceed.",
                {
                    "amount": float(amount),
                    "recipient": recipient,
                    "pending_id": pid
                }
            )), 200

        # --------------------------------------------------------
        # STEP 4 — GENERAL INFO / GREETING / UNKNOWN → Llama
        # --------------------------------------------------------
        if intent == "greeting":
            prompt = (
                SYSTEM_INSTRUCTIONS +
                "\n\nUser: " + user_text +
                "\n\nRespond in a short, warm feminine tone."
            )
            reply = call_ollama(prompt)
            return jsonify(build_response("greeting", reply, {})), 200

        # Semantic search
        hits = semantic_search(user_text, top_k=3)
        context = "\n".join(
            f"Source: {h['source']}\n{h['text'][:600]}\n---"
            for h in hits
        )

        prompt = (
            SYSTEM_INSTRUCTIONS +
            "\n\nContext:\n" + context +
            "\n\nUser: " + user_text +
            "\n\nAnswer in a concise, friendly feminine tone. Do NOT exceed two paragraphs."
        )

        answer = call_ollama(prompt)
        if not answer:
            fallback = hits[0]["text"] if hits else "Sorry, I couldn't find an answer."
            answer = fallback[:900]

        return jsonify(build_response("general_info", answer, {})), 200

    except Exception as e:
        print("AI service error:", e)
        return jsonify(build_response("unknown", "An internal error occurred.", {})), 500


if __name__ == "__main__":
    print("Starting Python AI Service on port 5000...")
    app.run(host="0.0.0.0", port=5000, debug=True)
    
