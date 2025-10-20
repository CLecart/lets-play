# 🧪 GUIDE DE TEST MANUEL - API Let's Play

## 📋 Prérequis

1. L'application doit être démarrée : `./mvnw spring-boot:run`
2. MongoDB doit être en cours d'exécution
3. L'application est accessible sur http://localhost:8080

## 🚀 TESTS ÉTAPE PAR ÉTAPE

### 1. ✅ Vérifier que l'application fonctionne

```bash
curl -s http://localhost:8080/api/products
```

**Résultat attendu :** Liste des produits en JSON (2 produits par défaut)

### 2. 👤 Créer un compte utilisateur

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mon Nom",
    "email": "monemail@example.com",
    "password": "monmotdepasse123"
  }'
```

**Résultat attendu :** Informations de l'utilisateur créé

### 3. 🔐 Se connecter et récupérer le token JWT

```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "monemail@example.com",
    "password": "monmotdepasse123"
  }' | jq '.token'
```

**Résultat attendu :** Token JWT (copie-le pour les prochaines étapes)

### 4. 📦 Créer un produit (avec authentification)

```bash
# Remplace YOUR_JWT_TOKEN par le token obtenu à l'étape 3
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Mon Super Produit",
    "description": "Description de mon produit",
    "price": 199.99
  }'
```

**Résultat attendu :** Détails du produit créé avec un ID

### 5. 📖 Lire un produit spécifique

```bash
# Remplace PRODUCT_ID par l'ID obtenu à l'étape 4
curl -s http://localhost:8080/api/products/PRODUCT_ID
```

### 6. ✏️ Modifier un produit

```bash
# Remplace YOUR_JWT_TOKEN et PRODUCT_ID
curl -X PUT http://localhost:8080/api/products/PRODUCT_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Produit Modifié",
    "description": "Nouvelle description",
    "price": 299.99
  }'
```

### 7. 🗑️ Supprimer un produit

```bash
# Remplace YOUR_JWT_TOKEN et PRODUCT_ID
curl -X DELETE http://localhost:8080/api/products/PRODUCT_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🧪 TESTS DE SÉCURITÉ

### Test 1: Tentative d'accès sans token

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "price": 100}'
```

**Résultat attendu :** Erreur 401 (Unauthorized)

### Test 2: Validation des données

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "email": "email-invalide",
    "password": "123"
  }'
```

**Résultat attendu :** Erreur 400 avec messages de validation

### Test 3: Rate Limiting

```bash
# Exécute cette boucle pour tester la limite de requêtes
for i in {1..70}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/products)
  echo "Requête $i: Status $STATUS"
  if [ "$STATUS" = "429" ]; then
    echo "Rate limit atteint !"
    break
  fi
done
```

## 🔑 TESTER AVEC L'UTILISATEUR ADMIN PAR DÉFAUT

### Connexion admin

```bash
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }' | jq '.token'
```

### Accéder à la liste des utilisateurs (admin uniquement)

```bash
# Remplace ADMIN_JWT_TOKEN
curl -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  http://localhost:8080/api/users
```

## 📊 CODES DE STATUT ATTENDUS

- **200** : Succès
- **201** : Créé avec succès
- **400** : Données invalides
- **401** : Non authentifié
- **403** : Non autorisé (pas les bonnes permissions)
- **404** : Ressource non trouvée
- **429** : Trop de requêtes (rate limiting)

## 💡 CONSEILS

1. Utilise `jq` pour formater les réponses JSON : `curl ... | jq .`
2. Sauvegarde ton token JWT dans une variable : `TOKEN="ton_token_ici"`
3. Utilise Postman pour une interface graphique plus conviviale
4. Regarde les logs de l'application pour déboguer : `tail -f logs/app.log`

## 🛠️ Script d'audit automatique

Pour faciliter les vérifications, un script automatise la séquence signup → signin → extraction du JWT → test produits (création/suppression).

Usage :

```bash
# depuis la racine du projet
bash tools/audit_manual.sh
```

Fichiers générés par le script :

- `tools/audit_manual_output.txt` : sortie synthétique et résultats
- `tools/audit_manual_run.log` : log brut de l'exécution

Le script essaie d'extraire automatiquement le token (utilise `jq` si présent) et effectue un cleanup (suppression du produit créé).

Si vous préférez lancer manuellement les étapes, suivez la section principale ci‑dessus.

## 🎯 RÉSULTAT ATTENDU

Si tous les tests passent, ton API est complètement fonctionnelle avec :

- ✅ Authentification JWT
- ✅ CRUD complet
- ✅ Sécurité (autorisation, validation)
- ✅ Rate limiting
- ✅ Gestion d'erreurs appropriée
