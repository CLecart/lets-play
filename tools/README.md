Audit Q&A tool

This small tool prints a list of audit questions (Audit 1) and clear non-technical answers in French.

Usage:

````bash
# print questions and answers
python3 tools/audit_qna.py

## Guide rapide pour l'auditeur (français)

Si vous souhaitez exécuter les tests localement et vérifier les preuves fournies, suivez ces étapes depuis la racine du dépôt :

```bash
# 1) Placez-vous à la racine du projet
cd /home/zone01student/dev/Java/lets-play

# 2) Exécutez la vérification canonique (vérifie build, tests, Checkstyle, rapports)
./mvnw -U -DskipTests=false verify -DtrimStackTrace=false

# 3) Résumé rapide (après exécution) :
#    - La sortie complète est enregistrée dans tools/test_results.txt
#    - Le résumé lisible est dans tools/TESTS.md

# 4) Si vous ne voulez lancer que les tests unitaires :
./mvnw -DskipTests=false test

# 5) Vérification rapide des rapports générés (optionnel) :
ls -la target || true
ls -la target/site || true
````

Conseils rapides :

- Si la commande './mvnw' n'est pas exécutable, rendez-la exécutable : `chmod +x mvnw`.
- Si un port ou une base de données locale manque (par ex. MongoDB), les tests d'intégration peuvent échouer : les logs dans `tools/test_results.txt` indiquent la cause exacte.
- Pour partager les preuves avec d'autres, fournissez `tools/TESTS.md` et `tools/test_results.txt`.

# print raw JSON

python3 tools/audit_qna.py --json

```

```
