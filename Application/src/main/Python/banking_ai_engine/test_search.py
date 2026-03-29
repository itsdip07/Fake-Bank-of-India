import os
import json
import numpy as np
import faiss
from sentence_transformers import SentenceTransformer

VECTOR_STORE = "vector_store"
EMBEDDING_MODEL = "sentence-transformers/all-mpnet-base-v2"

# Load model
model = SentenceTransformer(EMBEDDING_MODEL)

# Load FAISS index
index = faiss.read_index(f"{VECTOR_STORE}/faiss.index")

# Load metadata
with open(f"{VECTOR_STORE}/metadata.json", "r", encoding="utf-8") as f:
    metadata = json.load(f)

def semantic_search(query, top_k=3):
    query_embedding = model.encode([query]).astype("float32")
    distances, indices = index.search(query_embedding, top_k)

    results = []
    for dist, idx in zip(distances[0], indices[0]):
        results.append({
            "score": float(dist),
            "source": metadata[idx]["source"],
            "text": metadata[idx]["text"]
        })

    return results

# ----------- TEST ------------
while True:
    q = input("\nAsk something about banking (or 'exit'): ")

    if q.lower() == "exit":
        break

    hits = semantic_search(q)
    print("\nTop Results:")
    for hit in hits:
        print("\n-----------------------------")
        print(f"Source : {hit['source']}")
        print(f"Score  : {hit['score']}")
        print(f"Text   : {hit['text'][:400]}")
