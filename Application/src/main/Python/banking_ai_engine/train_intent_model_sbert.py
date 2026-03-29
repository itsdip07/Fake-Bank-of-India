import json
import pickle
import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, accuracy_score
from sklearn.model_selection import train_test_split
from sentence_transformers import SentenceTransformer

DATASET_FILE = "intent_dataset.json"
MODEL_FILE = "intent_model_sbert.pkl"

print("Loading dataset...")
with open(DATASET_FILE, "r", encoding="utf-8") as f:
    data = json.load(f)

texts = [item["text"] for item in data]
labels = [item["intent"] for item in data]

# ---------------------------------------------------------
# 1) Load strong sentence embedding model
# ---------------------------------------------------------
print("Loading Sentence-BERT model: all-mpnet-base-v2 ...")
model = SentenceTransformer("sentence-transformers/all-mpnet-base-v2")

# ---------------------------------------------------------
# 2) Convert sentences → embeddings
# ---------------------------------------------------------
print("Generating embeddings... (This may take ~10–15 seconds)")
embeddings = model.encode(texts, show_progress_bar=True)

# ---------------------------------------------------------
# 3) Split dataset
# ---------------------------------------------------------
X_train, X_test, y_train, y_test = train_test_split(
    embeddings, labels, test_size=0.2, random_state=42, stratify=labels
)

# ---------------------------------------------------------
# 4) Train Logistic Regression classifier
# ---------------------------------------------------------
print("Training classifier...")
clf = LogisticRegression(max_iter=2000)
clf.fit(X_train, y_train)

# ---------------------------------------------------------
# 5) Evaluate model
# ---------------------------------------------------------
print("\n==== MODEL ACCURACY REPORT ====\n")
y_pred = clf.predict(X_test)

print(classification_report(y_test, y_pred))
print("Accuracy:", accuracy_score(y_test, y_pred))

# ---------------------------------------------------------
# 6) Save the classifier + embedding model
# ---------------------------------------------------------
bundle = {
    "embedding_model": "sentence-transformers/all-mpnet-base-v2",
    "classifier": clf
}

pickle.dump(bundle, open(MODEL_FILE, "wb"))
print(f"\nSaved model as: {MODEL_FILE} ✔")
