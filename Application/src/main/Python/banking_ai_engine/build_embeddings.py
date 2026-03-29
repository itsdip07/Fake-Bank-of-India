import os
import json
import numpy as np
import faiss
from sentence_transformers import SentenceTransformer
from tqdm import tqdm

# --- CONFIG ---
KNOWLEDGE_FOLDER = r"D:\Fake Bank of India\Application\src\main\Python\banking_knowledge"
OUTPUT_FOLDER = "vector_store"
EMBEDDING_MODEL = "sentence-transformers/all-mpnet-base-v2"

# --- Ensure output folder exists ---
os.makedirs(OUTPUT_FOLDER, exist_ok=True)

# --- Load embedding model ---
print("Loading Embedding Model...")
model = SentenceTransformer(EMBEDDING_MODEL)

# --- Helper: Load all markdown files ---
def load_documents():
    docs = []
    for file in os.listdir(KNOWLEDGE_FOLDER):
        if file.endswith(".md"):
            path = os.path.join(KNOWLEDGE_FOLDER, file)
            with open(path, "r", encoding="utf-8") as f:
                docs.append((file, f.read()))
    return docs

# --- Helper: split into chunks ---
def split_into_chunks(text, max_words=120):
    chunks = []
    words = text.split()

    for i in range(0, len(words), max_words):
        chunk = " ".join(words[i:i + max_words])
        chunks.append(chunk)

    return chunks

# --- Process documents ---
print("Loading documents...")
docs = load_documents()

all_chunks = []
metadata = []

print("Splitting into chunks...")
for filename, content in docs:
    chunks = split_into_chunks(content)
    for chunk in chunks:
        all_chunks.append(chunk)
        metadata.append({"source": filename, "text": chunk})

print(f"Total chunks: {len(all_chunks)}")

# --- Generate embeddings ---
print("Generating embeddings...")
embeddings = model.encode(all_chunks, show_progress_bar=True)
embeddings = np.array(embeddings).astype("float32")

# --- Save FAISS index ---
dim = embeddings.shape[1]
index = faiss.IndexFlatL2(dim)
index.add(embeddings)

faiss.write_index(index, f"{OUTPUT_FOLDER}/faiss.index")

# --- Save metadata ---
with open(f"{OUTPUT_FOLDER}/metadata.json", "w", encoding="utf-8") as f:
    json.dump(metadata, f, indent=4)

print("\n✔ Embeddings created successfully!")
print("✔ FAISS vector store saved in 'vector_store' folder")
