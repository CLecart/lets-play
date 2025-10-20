#!/usr/bin/env python3
import json
import sys
from pathlib import Path

DATA_FILE = Path(__file__).resolve().parent / "audit1_questions.json"


def load_questions(path: Path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def print_qna(questions):
    for i, item in enumerate(questions, start=1):
        q = item.get('question')
        a = item.get('answer')
        print(f"{i}. Question: {q}\n   Réponse: {a}\n")


if __name__ == '__main__':
    if not DATA_FILE.exists():
        print(f"Fichier de questions introuvable: {DATA_FILE}")
        sys.exit(2)
    questions = load_questions(DATA_FILE)
    if len(sys.argv) > 1 and sys.argv[1] in ("--json", "-j"):
        print(json.dumps(questions, ensure_ascii=False, indent=2))
    else:
        print_qna(questions)
