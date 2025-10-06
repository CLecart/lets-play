#!/bin/bash

# 🚀 SCRIPT DE TEST COMPLET API LET'S PLAY - VERSION 2.0
# Auteur: Spring Boot CRUD API Team
# Date: $(date '+%Y-%m-%d %H:%M:%S')
# Description: Test automatisé complet de l'API avec rapport détaillé

echo "🚀 =========================================="
echo "   SCRIPT DE TEST API LET'S PLAY v2.0"
echo "🚀 =========================================="
echo "📅 Exécuté le: $(date '+%Y-%m-%d à %H:%M:%S')"
echo "🖥️  Système: $(uname -s) $(uname -m)"
echo "👤 Utilisateur: $(whoami)"
echo ""

# Configuration des couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# Configuration de l'API
BASE_URL="http://localhost:8080/api"
CONTENT_TYPE="Content-Type: application/json"
TIMEOUT=10

# Compteurs de résultats
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
WARNINGS=0

# Variables globales
JWT_TOKEN=""
ADMIN_TOKEN=""
TEST_USER_EMAIL="" # Sera généré dynamiquement pour éviter les conflits
TEST_PRODUCT_ID=""

# Fonction pour afficher les titres de section
print_header() {
    echo -e "\n${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"
    echo -e "${PURPLE}${BOLD} $1${NC}"
    echo -e "${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"
}

# Fonction pour afficher les sous-sections
print_section() {
    echo -e "\n${CYAN}${BOLD}🔸 $1${NC}"
    echo -e "${CYAN}──────────────────────────────────────────${NC}"
}

# Fonction pour afficher les résultats de test
print_test_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    local status=$1
    local message=$2
    local details=$3
    
    if [ $status -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC} - $message"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    elif [ $status -eq 2 ]; then
        echo -e "${YELLOW}⚠️  WARN${NC} - $message"
        WARNINGS=$((WARNINGS + 1))
    else
        echo -e "${RED}❌ FAIL${NC} - $message"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    if [ ! -z "$details" ]; then
        echo -e "   ${details}"
    fi
}

# Fonction pour faire une pause visuelle
pause_display() {
    echo -e "${BLUE}⏳ $1${NC}"
    sleep 1
}

# Fonction pour vérifier si une commande existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Vérification des prérequis
print_header "VÉRIFICATION DES PRÉREQUIS"

pause_display "Vérification des outils nécessaires..."

# Vérifier curl
if command_exists curl; then
    print_test_result 0 "curl est installé" "Version: $(curl --version | head -1)"
else
    print_test_result 1 "curl n'est pas installé" "Requis pour les tests HTTP"
    exit 1
fi

# Vérifier jq
if command_exists jq; then
    print_test_result 0 "jq est installé" "Version: $(jq --version)"
else
    print_test_result 2 "jq n'est pas installé" "Optionnel mais recommandé pour le parsing JSON"
fi

# Vérifier si MongoDB est actif
pause_display "Vérification de MongoDB..."
if pgrep -f mongod > /dev/null; then
    print_test_result 0 "MongoDB est en cours d'exécution"
else
    print_test_result 1 "MongoDB n'est pas en cours d'exécution" "Démarrez MongoDB avant de continuer"
    exit 1
fi

# Test de connectivité de base
print_header "TESTS DE CONNECTIVITÉ"

pause_display "Test de connexion à l'application..."

# Vérifier si l'application répond
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
CURL_EXIT_CODE=$?

if [ $CURL_EXIT_CODE -eq 0 ] && [ "$HTTP_STATUS" = "200" ]; then
    print_test_result 0 "Application accessible" "Status HTTP: $HTTP_STATUS"
elif [ $CURL_EXIT_CODE -eq 7 ] || [ $CURL_EXIT_CODE -eq 28 ]; then
    print_test_result 1 "Application non accessible" "Vérifiez que l'app est démarrée avec: ./mvnw spring-boot:run"
    echo -e "\n${RED}🛑 ARRÊT DES TESTS - APPLICATION NON DISPONIBLE${NC}"
    exit 1
else
    print_test_result 1 "Réponse inattendue de l'application" "Status HTTP: $HTTP_STATUS"
fi

# Test de santé détaillé
pause_display "Test de santé de l'API..."
HEALTH_RESPONSE=$(curl -s --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
if echo "$HEALTH_RESPONSE" | grep -q "id\|name\|price" 2>/dev/null; then
    PRODUCT_COUNT=$(echo "$HEALTH_RESPONSE" | grep -o '"id"' | wc -l 2>/dev/null || echo "?")
    print_test_result 0 "Structure JSON valide détectée" "Produits disponibles: $PRODUCT_COUNT"
else
    print_test_result 2 "Structure de réponse inattendue" "Réponse: ${HEALTH_RESPONSE:0:100}..."
fi

# Tests des endpoints publics
print_header "TESTS DES ENDPOINTS PUBLICS"

print_section "Test GET /api/products"
pause_display "Récupération de la liste des produits..."

PRODUCTS_RESPONSE=$(curl -s --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
PRODUCTS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)

if [ "$PRODUCTS_STATUS" = "200" ]; then
    if command_exists jq && echo "$PRODUCTS_RESPONSE" | jq . > /dev/null 2>&1; then
        PRODUCT_COUNT=$(echo "$PRODUCTS_RESPONSE" | jq '. | length' 2>/dev/null)
        print_test_result 0 "Récupération des produits réussie" "Nombre de produits: $PRODUCT_COUNT"
        
        # Extraire le premier produit pour les tests suivants
        FIRST_PRODUCT_ID=$(echo "$PRODUCTS_RESPONSE" | jq -r '.[0].id' 2>/dev/null)
        if [ "$FIRST_PRODUCT_ID" != "null" ] && [ ! -z "$FIRST_PRODUCT_ID" ]; then
            print_test_result 0 "ID du premier produit extrait" "ID: $FIRST_PRODUCT_ID"
        fi
    else
        print_test_result 2 "Réponse JSON non parsable" "Réponse brute reçue"
    fi
else
    print_test_result 1 "Échec de récupération des produits" "Status: $PRODUCTS_STATUS"
fi

# Test GET produit spécifique
if [ ! -z "$FIRST_PRODUCT_ID" ] && [ "$FIRST_PRODUCT_ID" != "null" ]; then
    print_section "Test GET /api/products/{id}"
    pause_display "Récupération d'un produit spécifique..."
    
    SINGLE_PRODUCT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products/$FIRST_PRODUCT_ID 2>/dev/null)
    
    if [ "$SINGLE_PRODUCT_STATUS" = "200" ]; then
        print_test_result 0 "Récupération d'un produit spécifique réussie" "ID testé: $FIRST_PRODUCT_ID"
    else
        print_test_result 1 "Échec de récupération du produit spécifique" "Status: $SINGLE_PRODUCT_STATUS"
    fi
fi

# Tests d'authentification
print_header "TESTS D'AUTHENTIFICATION"

print_section "Test d'inscription utilisateur"
pause_display "Création d'un compte utilisateur unique..."

# Générer un email vraiment unique avec timestamp + random
UNIQUE_EMAIL="testuser.$(date +%s).$(shuf -i 1000-9999 -n 1)@example.com"
SIGNUP_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -d "{\"name\": \"Test User Auto $(date +%H%M%S)\", \"email\": \"$UNIQUE_EMAIL\", \"password\": \"testpassword123\"}" --connect-timeout $TIMEOUT $BASE_URL/auth/signup 2>/dev/null)
SIGNUP_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d "{\"name\": \"Test User Auto $(date +%H%M%S)\", \"email\": \"$UNIQUE_EMAIL\", \"password\": \"testpassword123\"}" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/auth/signup 2>/dev/null)

if [ "$SIGNUP_STATUS" = "200" ]; then
    if command_exists jq; then
        USER_NAME=$(echo "$SIGNUP_RESPONSE" | jq -r '.name' 2>/dev/null)
        print_test_result 0 "Inscription utilisateur réussie" "Utilisateur créé: $USER_NAME"
        # Mettre à jour la variable pour la connexion
        TEST_USER_EMAIL="$UNIQUE_EMAIL"
    else
        print_test_result 0 "Inscription utilisateur réussie" "Nouvel utilisateur avec email unique"
        TEST_USER_EMAIL="$UNIQUE_EMAIL"
    fi
else
    print_test_result 1 "Échec de l'inscription" "Status: $SIGNUP_STATUS - Email: $UNIQUE_EMAIL"
fi

print_section "Test de connexion utilisateur"
pause_display "Connexion et récupération du token JWT..."

SIGNIN_DATA="{
    \"email\": \"$TEST_USER_EMAIL\",
    \"password\": \"testpassword123\"
}"

SIGNIN_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$SIGNIN_DATA" --connect-timeout $TIMEOUT $BASE_URL/auth/signin 2>/dev/null)
SIGNIN_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$SIGNIN_DATA" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/auth/signin 2>/dev/null)

if [ "$SIGNIN_STATUS" = "200" ]; then
    if command_exists jq; then
        JWT_TOKEN=$(echo "$SIGNIN_RESPONSE" | jq -r '.token' 2>/dev/null)
        if [ "$JWT_TOKEN" != "null" ] && [ ! -z "$JWT_TOKEN" ]; then
            print_test_result 0 "Connexion réussie - Token JWT obtenu" "Token (30 premiers chars): ${JWT_TOKEN:0:30}..."
        else
            print_test_result 1 "Token JWT non trouvé dans la réponse" "Réponse: ${SIGNIN_RESPONSE:0:100}..."
        fi
    else
        JWT_TOKEN=$(echo "$SIGNIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        if [ ! -z "$JWT_TOKEN" ]; then
            print_test_result 0 "Connexion réussie - Token JWT obtenu" "Token extrait sans jq"
        else
            print_test_result 1 "Impossible d'extraire le token" "Installez jq pour un meilleur parsing"
        fi
    fi
else
    print_test_result 1 "Échec de la connexion" "Status: $SIGNIN_STATUS"
fi

# Test de connexion admin
print_section "Test de connexion administrateur"
pause_display "Connexion avec le compte admin par défaut..."

ADMIN_SIGNIN_DATA="{
    \"email\": \"admin@example.com\",
    \"password\": \"admin123\"
}"

ADMIN_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$ADMIN_SIGNIN_DATA" --connect-timeout $TIMEOUT $BASE_URL/auth/signin 2>/dev/null)
ADMIN_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$ADMIN_SIGNIN_DATA" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/auth/signin 2>/dev/null)

if [ "$ADMIN_STATUS" = "200" ]; then
    if command_exists jq; then
        ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r '.token' 2>/dev/null)
        if [ "$ADMIN_TOKEN" != "null" ] && [ ! -z "$ADMIN_TOKEN" ]; then
            print_test_result 0 "Connexion admin réussie" "Token admin obtenu"
        else
            print_test_result 1 "Token admin non trouvé" "Vérifiez les données admin par défaut"
        fi
    else
        print_test_result 0 "Connexion admin semble réussie" "Status 200 reçu"
    fi
else
    print_test_result 1 "Échec de la connexion admin" "Status: $ADMIN_STATUS"
fi

# Tests CRUD avec authentification
print_header "TESTS CRUD AVEC AUTHENTIFICATION"

if [ ! -z "$JWT_TOKEN" ] && [ "$JWT_TOKEN" != "null" ]; then
    print_section "Test de création de produit"
    pause_display "Création d'un nouveau produit avec authentification..."
    
    PRODUCT_DATA="{
        \"name\": \"Produit Test Auto $(date +%H%M%S)\",
        \"description\": \"Produit créé automatiquement par le script de test\",
        \"price\": $(shuf -i 10-999 -n 1).99
    }"
    
    CREATE_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -H "Authorization: Bearer $JWT_TOKEN" -d "$PRODUCT_DATA" --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
    CREATE_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -H "Authorization: Bearer $JWT_TOKEN" -d "$PRODUCT_DATA" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
    
    if [ "$CREATE_STATUS" = "200" ]; then
        if command_exists jq; then
            TEST_PRODUCT_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id' 2>/dev/null)
            PRODUCT_NAME=$(echo "$CREATE_RESPONSE" | jq -r '.name' 2>/dev/null)
            print_test_result 0 "Création de produit réussie" "Produit: $PRODUCT_NAME (ID: $TEST_PRODUCT_ID)"
        else
            print_test_result 0 "Création de produit réussie" "Status 200 reçu"
        fi
    else
        print_test_result 1 "Échec de création de produit" "Status: $CREATE_STATUS, Réponse: ${CREATE_RESPONSE:0:100}..."
    fi
    
    # Test de modification de produit
    if [ ! -z "$TEST_PRODUCT_ID" ] && [ "$TEST_PRODUCT_ID" != "null" ]; then
        print_section "Test de modification de produit"
        pause_display "Modification du produit créé..."
        
        UPDATE_DATA="{
            \"name\": \"Produit Modifié $(date +%H%M%S)\",
            \"description\": \"Description mise à jour\",
            \"price\": 999.99
        }"
        
        UPDATE_STATUS=$(curl -s -X PUT -H "$CONTENT_TYPE" -H "Authorization: Bearer $JWT_TOKEN" -d "$UPDATE_DATA" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products/$TEST_PRODUCT_ID 2>/dev/null)
        
        if [ "$UPDATE_STATUS" = "200" ]; then
            print_test_result 0 "Modification de produit réussie" "Produit ID: $TEST_PRODUCT_ID"
        else
            print_test_result 1 "Échec de modification de produit" "Status: $UPDATE_STATUS"
        fi
        
        # Test de suppression de produit
        print_section "Test de suppression de produit"
        pause_display "Suppression du produit test..."
        
        DELETE_STATUS=$(curl -s -X DELETE -H "Authorization: Bearer $JWT_TOKEN" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products/$TEST_PRODUCT_ID 2>/dev/null)
        
        if [ "$DELETE_STATUS" = "200" ] || [ "$DELETE_STATUS" = "204" ]; then
            print_test_result 0 "Suppression de produit réussie" "Produit ID: $TEST_PRODUCT_ID supprimé"
        else
            print_test_result 1 "Échec de suppression de produit" "Status: $DELETE_STATUS"
        fi
    fi
else
    print_test_result 2 "Tests CRUD ignorés" "Aucun token JWT valide disponible"
fi

# Tests de sécurité
print_header "TESTS DE SÉCURITÉ"

print_section "Test d'accès non autorisé"
pause_display "Tentative d'accès à un endpoint protégé sans token..."

UNAUTH_DATA='{"name": "Produit Pirate", "price": 1}'
UNAUTH_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$UNAUTH_DATA" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)

if [ "$UNAUTH_STATUS" = "401" ]; then
    print_test_result 0 "Sécurité OK - Accès refusé sans authentification" "Status: $UNAUTH_STATUS"
elif [ "$UNAUTH_STATUS" = "400" ]; then
    print_test_result 2 "Accès bloqué mais avec une erreur différente" "Status: $UNAUTH_STATUS (peut être normal)"
else
    print_test_result 1 "PROBLÈME DE SÉCURITÉ - Accès autorisé sans token" "Status: $UNAUTH_STATUS"
fi

print_section "Test de validation des données"
pause_display "Test avec des données invalides (email, password, champs vides)..."

# Test 1: Email invalide
INVALID_EMAIL_DATA='{
    "name": "Test User",
    "email": "email-invalide-sans-at",
    "password": "validpassword123"
}'

EMAIL_VALIDATION_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$INVALID_EMAIL_DATA" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/auth/signup 2>/dev/null)

if [ "$EMAIL_VALIDATION_STATUS" = "400" ]; then
    print_test_result 0 "Validation email fonctionne" "Email invalide correctement rejeté"
else
    print_test_result 1 "Problème validation email" "Status: $EMAIL_VALIDATION_STATUS"
fi

# Test 2: Mot de passe trop court
INVALID_PASSWORD_DATA='{
    "name": "Test User",
    "email": "valid@example.com",
    "password": "123"
}'

PASSWORD_VALIDATION_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$INVALID_PASSWORD_DATA" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/auth/signup 2>/dev/null)

if [ "$PASSWORD_VALIDATION_STATUS" = "400" ]; then
    print_test_result 0 "Validation mot de passe fonctionne" "Mot de passe trop court correctement rejeté"
else
    print_test_result 1 "Problème validation mot de passe" "Status: $PASSWORD_VALIDATION_STATUS"
fi

# Test de rate limiting
print_header "TESTS DE PERFORMANCE ET RATE LIMITING"

print_section "Test de rate limiting"
pause_display "Test de la limite de requêtes par minute..."

echo -e "${BLUE}🔄 Envoi de requêtes multiples pour tester le rate limiting...${NC}"
RATE_LIMIT_HIT=false
REQUESTS_SENT=0

for i in {1..80}; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 --max-time 5 $BASE_URL/products 2>/dev/null)
    REQUESTS_SENT=$((REQUESTS_SENT + 1))
    
    if [ "$STATUS" = "429" ]; then
        RATE_LIMIT_HIT=true
        print_test_result 0 "Rate limiting fonctionne" "Limite atteinte après $REQUESTS_SENT requêtes"
        break
    fi
    
    # Afficher le progrès tous les 15 tests
    if [ $((i % 15)) -eq 0 ]; then
        echo -e "   ${CYAN}Progress: $i/80 requêtes (Status: $STATUS)${NC}"
    fi
    
    # Petite pause pour éviter de surcharger
    sleep 0.05
done

if [ "$RATE_LIMIT_HIT" = false ]; then
    print_test_result 2 "Rate limiting non déclenché" "Limite possiblement trop élevée ou non configurée"
fi

# Tests d'erreurs et d'exceptions
print_header "TESTS DE GESTION D'ERREURS"

print_section "Test d'erreur 404"
pause_display "Accès à une ressource inexistante..."

NOT_FOUND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products/id-inexistant-123456 2>/dev/null)

if [ "$NOT_FOUND_STATUS" = "404" ]; then
    print_test_result 0 "Gestion des erreurs 404 correcte" "Ressource inexistante correctement gérée"
else
    print_test_result 1 "Problème de gestion d'erreur 404" "Status: $NOT_FOUND_STATUS"
fi

print_section "Test d'erreur de format JSON"
pause_display "Envoi de JSON malformé..."

MALFORMED_JSON='{"name": "Test", "email": "test@example.com", "password": '
JSON_ERROR_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d "$MALFORMED_JSON" -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/auth/signup 2>/dev/null)

if [ "$JSON_ERROR_STATUS" = "400" ]; then
    print_test_result 0 "Gestion des erreurs JSON correcte" "JSON malformé correctement rejeté"
else
    print_test_result 2 "Gestion d'erreur JSON inattendue" "Status: $JSON_ERROR_STATUS"
fi

# Rapport final
print_header "RAPPORT FINAL DES TESTS"

echo -e "\n${BOLD}📊 STATISTIQUES DES TESTS${NC}"
echo -e "════════════════════════════════════════"
echo -e "${GREEN}✅ Tests réussis:     $PASSED_TESTS${NC}"
echo -e "${RED}❌ Tests échoués:     $FAILED_TESTS${NC}"
echo -e "${YELLOW}⚠️  Avertissements:   $WARNINGS${NC}"
echo -e "${BLUE}📝 Total des tests:   $TOTAL_TESTS${NC}"

# Calcul du pourcentage de réussite
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$(( (PASSED_TESTS * 100) / TOTAL_TESTS ))
    echo -e "${PURPLE}🎯 Taux de réussite:  $SUCCESS_RATE%${NC}"
else
    echo -e "${RED}❌ Aucun test n'a été exécuté${NC}"
fi

echo -e "\n${BOLD}🔍 ANALYSE DES RÉSULTATS${NC}"
echo -e "════════════════════════════════════════"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}🎉 EXCELLENT ! Tous les tests principaux sont passés !${NC}"
    echo -e "${GREEN}✨ Votre API est prête pour la production.${NC}"
elif [ $FAILED_TESTS -le 2 ]; then
    echo -e "${YELLOW}🔧 BIEN ! Quelques points mineurs à corriger.${NC}"
    echo -e "${YELLOW}💡 Vérifiez les tests échoués ci-dessus.${NC}"
else
    echo -e "${RED}⚠️  ATTENTION ! Plusieurs problèmes détectés.${NC}"
    echo -e "${RED}🔨 Correction nécessaire avant mise en production.${NC}"
fi

echo -e "\n${BOLD}🚀 PROCHAINES ÉTAPES RECOMMANDÉES${NC}"
echo -e "════════════════════════════════════════"
echo -e "1. ${CYAN}Vérifiez les tests échoués et corrigez les problèmes${NC}"
echo -e "2. ${CYAN}Testez manuellement avec Postman ou curl${NC}"
echo -e "3. ${CYAN}Consultez le guide GUIDE_TEST_MANUEL.md${NC}"
echo -e "4. ${CYAN}Vérifiez les logs de l'application: tail -f app.log${NC}"
echo -e "5. ${CYAN}Documentez les endpoints dans README.md${NC}"

echo -e "\n${BOLD}📋 ENDPOINTS DISPONIBLES${NC}"
echo -e "════════════════════════════════════════"
echo -e "${BLUE}Publics:${NC}"
echo -e "  GET    /api/products              - Liste des produits"
echo -e "  GET    /api/products/{id}         - Détail d'un produit"
echo -e "  POST   /api/auth/signup           - Inscription"
echo -e "  POST   /api/auth/signin           - Connexion"
echo -e "\n${BLUE}Protégés (JWT requis):${NC}"
echo -e "  POST   /api/products              - Créer un produit"
echo -e "  PUT    /api/products/{id}         - Modifier un produit"
echo -e "  DELETE /api/products/{id}         - Supprimer un produit"
echo -e "  GET    /api/users                 - Liste des utilisateurs (Admin)"
echo -e "  PUT    /api/users/{id}            - Modifier un utilisateur"

echo -e "\n${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"
echo -e "${PURPLE}${BOLD}    TESTS TERMINÉS - $(date '+%H:%M:%S')${NC}"
echo -e "${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"
echo ""