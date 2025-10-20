#!/usr/bin/env python3
import json
from pathlib import Path
import argparse
import shutil

DATA = Path(__file__).resolve().parent / 'audit1_questions.json'
BACKUP = Path(__file__).resolve().parent / 'audit1_questions.json.bak'


def load():
    with DATA.open(encoding='utf-8') as f:
        return json.load(f)


def save(data):
    with DATA.open('w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


def dedupe(items):
    seen = {}
    out = []
    for it in items:
        q = it.get('question','').strip()
        key = ' '.join(q.lower().split())
        if not key:
            continue
        if key in seen:
            # prefer existing answer if present, otherwise take from current
            prev = seen[key]
            if not prev.get('answer') and it.get('answer'):
                prev['answer'] = it.get('answer')
            # merge optional flag
            prev['optional'] = prev.get('optional', False) or it.get('optional', False)
        else:
            obj = {'question': q, 'answer': it.get('answer',''), 'optional': it.get('optional', False)}
            seen[key] = obj
            out.append(obj)
    return out


def interactive_fill(items):
    for it in items:
        print('\nQuestion: ' + it['question'])
        print('Réponse actuelle: ' + (it['answer'] or '<vide>'))
        ans = input('Nouvelle réponse (ENTER pour garder): ').strip()
        if ans:
            it['answer'] = ans
    return items


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--dedupe-only', action='store_true', help='Remove duplicate questions and save (creates backup).')
    parser.add_argument('--interactive', action='store_true', help='Ask each question and optionally fill answers.')
    args = parser.parse_args()

    if not DATA.exists():
        print('Fichier de questions introuvable:', DATA)
        return

    shutil.copy2(DATA, BACKUP)
    items = load()
    cleaned = dedupe(items)
    if args.interactive:
        cleaned = interactive_fill(cleaned)
    save(cleaned)
    print(f'Nettoyé et enregistré {len(cleaned)} questions dans {DATA} (backup: {BACKUP})')


if __name__ == '__main__':
    main()
