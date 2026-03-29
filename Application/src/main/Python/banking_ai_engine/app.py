from fastapi import FastAPI
from pydantic import BaseModel
import faiss
import json
import numpy as np
from sentence_transformers import SentenceTransformer
import pickle

# Load intent model + vectorizer
with open("intent_model_sbert.pkl", "rb") as f:
    intent_model = pickle.load(f)

with open("intent_vectorizer.pkl", "rb") as f:
    intent_vectorizer = pickle.load(f)


# -------- CONFIG --------
VECTOR_STORE = "vector_store"
EMBEDDING_MODEL = "sentence-transformers/all-mpnet-base-v2"

# -------- LOAD MODEL + INDEX --------
print("Loading model and FAISS index...")

model = SentenceTransformer(EMBEDDING_MODEL)
index = faiss.read_index(f"{VECTOR_STORE}/faiss.index")

with open(f"{VECTOR_STORE}/metadata.json", "r", encoding="utf-8") as f:
    metadata = json.load(f)

# -------- FASTAPI APP --------
app = FastAPI()

class Query(BaseModel):
    question: str
    top_k: int = 3

class IntentRequest(BaseModel):
    text: str



def semantic_search(query, top_k=3):
    embedding = model.encode([query]).astype("float32")
    distances, indices = index.search(embedding, top_k)

    results = []
    for dist, idx in zip(distances[0], indices[0]):
        results.append({
            "score": float(dist),
            "source": metadata[idx]["source"],
            "text": metadata[idx]["text"]
        })
    return results


@app.post("/semantic-search")
def search(req: Query):
    results = semantic_search(req.question, req.top_k)
    return {
        "query": req.question,
        "results": results
    }
@app.post("/intent")
def detect_intent(req: IntentRequest):
    X = intent_vectorizer.transform([req.text])
    intent = intent_model.predict(X)[0]
    return {"intent": intent}

