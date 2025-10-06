#!/bin/bash

# 🎯 SCRIPT DE TEST PARFAIT - API Let's Play
# Objectif: Atteindre 19/19 tests réussis

echo "🎯 =========================================="
echo "   SCRIPT DE TEST PARFAIT - API LET'S PLAY"
echo "🎯 =========================================="
echo "📅 Exécuté le: $(date '+%Y-%m-%d à %H:%M:%S')"
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

# Variables globales
JWT_TOKEN=""
ADMIN_TOKEN=""
TEST_USER_EMAIL=""
TEST_PRODUCT_ID=""

# Fonction pour afficher les résultats de test
print_test_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    local status=$1
    local message=$2
    local details=$3
    
    if [ $status -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC} - $message"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ FAIL${NC} - $message"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    if [ ! -z "$details" ]; then
        echo -e "   ${details}"
    fi
}

# Fonction pour afficher les titres de section
print_header() {
    echo -e "\n${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"
    echo -e "${PURPLE}${BOLD} $1${NC}"
    echo -e "${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"
}

echo -e "${BLUE}🔸 Vérification de la connectivité${NC}"

# Test 1: Application accessible
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
if [ "$HTTP_STATUS" = "200" ]; then
    print_test_result 0 "Application accessible" "Status: $HTTP_STATUS"
else
    print_test_result 1 "Application non accessible" "Status: $HTTP_STATUS"
    exit 1
fi

# Test 2: Structure JSON valide
PRODUCTS_RESPONSE=$(curl -s --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
if echo "$PRODUCTS_RESPONSE" | jq . > /dev/null 2>&1; then
    PRODUCT_COUNT=$(echo "$PRODUCTS_RESPONSE" | jq '. | length' 2>/dev/null)
    print_test_result 0 "Structure JSON valide" "Produits: $PRODUCT_COUNT"
else
    print_test_result 1 "Structure JSON invalide"
fi

# Test 3: GET tous les produits
PRODUCTS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products 2>/dev/null)
if [ "$PRODUCTS_STATUS" = "200" ]; then
    print_test_result 0 "GET /api/products fonctionne"
else
    print_test_result 1 "GET /api/products échoue"
fi

# Test 4: GET produit spécifique
FIRST_PRODUCT_ID=$(echo "$PRODUCTS_RESPONSE" | jq -r '.[0].id' 2>/dev/null)
if [ "$FIRST_PRODUCT_ID" != "null" ] && [ ! -z "$FIRST_PRODUCT_ID" ]; then
    SINGLE_PRODUCT_STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout $TIMEOUT $BASE_URL/products/$FIRST_PRODUCT_ID 2>/dev/null)
    if [ "$SINGLE_PRODUCT_STATUS" = "200" ]; then
        print_test_result 0 "GET /api/products/{id} fonctionne"
    else
        print_test_result 1 "GET /api/products/{id} échoue"
    fi
else
    print_test_result 1 "Impossible d'extraire l'ID du premier produit"
fi

print_header "TESTS D'AUTHENTIFICATION"

# Test 5: Inscription utilisateur (avec email VRAIMENT unique)
# Utilisation de nanoseconds + PID + random pour garantir l'unicité absolue
NANO_TIME=$(date +%s%N)
RANDOM_NUM=$(shuf -i 10000-99999 -n 1)
PROCESS_ID=$$
TEST_USER_EMAIL="ultimate.${NANO_TIME}.${PROCESS_ID}.${RANDOM_NUM}@example.com"

# UN SEUL appel curl pour éviter la double création
SIGNUP_FULL_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -d "{\"name\": \"Ultimate Test User\", \"email\": \"$TEST_USER_EMAIL\", \"password\": \"ultimatepass123\"}" -w "HTTPSTATUS:%{http_code}" $BASE_URL/auth/signup 2>/dev/null)

# Extraire le status HTTP et la réponse
SIGNUP_STATUS=$(echo "$SIGNUP_FULL_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
SIGNUP_RESPONSE=$(echo "$SIGNUP_FULL_RESPONSE" | sed 's/HTTPSTATUS:[0-9]*$//')

if [ "$SIGNUP_STATUS" = "200" ]; then
    print_test_result 0 "Inscription utilisateur réussie" "Email: $TEST_USER_EMAIL"
else
    print_test_result 1 "Inscription utilisateur échouée" "Status: $SIGNUP_STATUS"
fi

# Test 6: Connexion utilisateur
# UN SEUL appel curl pour la connexion aussi
SIGNIN_FULL_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -d "{\"email\": \"$TEST_USER_EMAIL\", \"password\": \"ultimatepass123\"}" -w "HTTPSTATUS:%{http_code}" $BASE_URL/auth/signin 2>/dev/null)

# Extraire le status HTTP et la réponse
SIGNIN_STATUS=$(echo "$SIGNIN_FULL_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
SIGNIN_RESPONSE=$(echo "$SIGNIN_FULL_RESPONSE" | sed 's/HTTPSTATUS:[0-9]*$//')

if [ "$SIGNIN_STATUS" = "200" ]; then
    JWT_TOKEN=$(echo "$SIGNIN_RESPONSE" | jq -r '.token' 2>/dev/null)
    if [ "$JWT_TOKEN" != "null" ] && [ ! -z "$JWT_TOKEN" ]; then
        print_test_result 0 "Connexion utilisateur réussie" "Token obtenu"
    else
        print_test_result 1 "Token JWT non obtenu"
    fi
else
    print_test_result 1 "Connexion utilisateur échouée" "Status: $SIGNIN_STATUS"
fi

# Test 7: Connexion admin
ADMIN_FULL_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -d '{"email": "admin@example.com", "password": "admin123"}' -w "HTTPSTATUS:%{http_code}" $BASE_URL/auth/signin 2>/dev/null)

ADMIN_STATUS=$(echo "$ADMIN_FULL_RESPONSE" | grep -o "HTTPSTATUS:[0-9]*" | cut -d: -f2)
ADMIN_RESPONSE=$(echo "$ADMIN_FULL_RESPONSE" | sed 's/HTTPSTATUS:[0-9]*$//')

if [ "$ADMIN_STATUS" = "200" ]; then
    ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r '.token' 2>/dev/null)
    print_test_result 0 "Connexion admin réussie"
else
    print_test_result 1 "Connexion admin échouée"
fi

print_header "TESTS CRUD"

# Test 8: Création de produit
if [ ! -z "$JWT_TOKEN" ] && [ "$JWT_TOKEN" != "null" ]; then
    CREATE_RESPONSE=$(curl -s -X POST -H "$CONTENT_TYPE" -H "Authorization: Bearer $JWT_TOKEN" -d '{"name": "Perfect Product", "description": "Test product", "price": 99.99}' $BASE_URL/products 2>/dev/null)
    CREATE_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -H "Authorization: Bearer $JWT_TOKEN" -d '{"name": "Perfect Product", "description": "Test product", "price": 99.99}' -o /dev/null -w "%{http_code}" $BASE_URL/products 2>/dev/null)
    
    if [ "$CREATE_STATUS" = "200" ]; then
        TEST_PRODUCT_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id' 2>/dev/null)
        print_test_result 0 "Création de produit réussie"
    else
        print_test_result 1 "Création de produit échouée"
    fi
else
    print_test_result 1 "Création de produit impossible - Pas de token"
fi

# Test 9: Modification de produit
if [ ! -z "$TEST_PRODUCT_ID" ] && [ "$TEST_PRODUCT_ID" != "null" ]; then
    UPDATE_STATUS=$(curl -s -X PUT -H "$CONTENT_TYPE" -H "Authorization: Bearer $JWT_TOKEN" -d '{"name": "Updated Product", "price": 199.99}' -o /dev/null -w "%{http_code}" $BASE_URL/products/$TEST_PRODUCT_ID 2>/dev/null)
    if [ "$UPDATE_STATUS" = "200" ]; then
        print_test_result 0 "Modification de produit réussie"
    else
        print_test_result 1 "Modification de produit échouée"
    fi
else
    print_test_result 1 "Modification impossible - Pas d'ID de produit"
fi

# Test 10: Suppression de produit
if [ ! -z "$TEST_PRODUCT_ID" ] && [ "$TEST_PRODUCT_ID" != "null" ]; then
    DELETE_STATUS=$(curl -s -X DELETE -H "Authorization: Bearer $JWT_TOKEN" -o /dev/null -w "%{http_code}" $BASE_URL/products/$TEST_PRODUCT_ID 2>/dev/null)
    if [ "$DELETE_STATUS" = "200" ] || [ "$DELETE_STATUS" = "204" ]; then
        print_test_result 0 "Suppression de produit réussie"
    else
        print_test_result 1 "Suppression de produit échouée"
    fi
else
    print_test_result 1 "Suppression impossible - Pas d'ID de produit"
fi

print_header "TESTS DE SÉCURITÉ"

# Test 11: Accès non autorisé
UNAUTH_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d '{"name": "Hack", "price": 1}' -o /dev/null -w "%{http_code}" $BASE_URL/products 2>/dev/null)
if [ "$UNAUTH_STATUS" = "401" ]; then
    print_test_result 0 "Sécurité OK - Accès non autorisé refusé"
else
    print_test_result 1 "PROBLÈME DE SÉCURITÉ - Accès non autorisé accepté"
fi

# Test 12: Validation email
EMAIL_VALIDATION_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d '{"name": "Test", "email": "invalid-email", "password": "validpass123"}' -o /dev/null -w "%{http_code}" $BASE_URL/auth/signup 2>/dev/null)
if [ "$EMAIL_VALIDATION_STATUS" = "400" ]; then
    print_test_result 0 "Validation email fonctionne"
else
    print_test_result 1 "Validation email échoue"
fi

# Test 13: Validation mot de passe
PASSWORD_VALIDATION_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d '{"name": "Test", "email": "valid@example.com", "password": "123"}' -o /dev/null -w "%{http_code}" $BASE_URL/auth/signup 2>/dev/null)
if [ "$PASSWORD_VALIDATION_STATUS" = "400" ]; then
    print_test_result 0 "Validation mot de passe fonctionne"
else
    print_test_result 1 "Validation mot de passe échoue"
fi

print_header "TESTS DE PERFORMANCE"

# Test 14: Rate limiting
echo -e "${BLUE}🔄 Test du rate limiting...${NC}"
RATE_LIMIT_HIT=false
for i in {1..70}; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 --max-time 5 $BASE_URL/products 2>/dev/null)
    if [ "$STATUS" = "429" ]; then
        RATE_LIMIT_HIT=true
        break
    fi
    if [ $((i % 20)) -eq 0 ]; then
        echo -e "   ${CYAN}Requête $i: Status $STATUS${NC}"
    fi
    sleep 0.05
done

if [ "$RATE_LIMIT_HIT" = true ]; then
    print_test_result 0 "Rate limiting fonctionne"
else
    print_test_result 1 "Rate limiting ne fonctionne pas"
fi

print_header "TESTS D'ERREURS"

# Test 15: Erreur 404
NOT_FOUND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/products/nonexistent-id-123456 2>/dev/null)
if [ "$NOT_FOUND_STATUS" = "404" ]; then
    print_test_result 0 "Gestion erreur 404 correcte"
else
    print_test_result 1 "Gestion erreur 404 incorrecte"
fi

# Test 16: JSON malformé
JSON_ERROR_STATUS=$(curl -s -X POST -H "$CONTENT_TYPE" -d '{"name": "Test", "email":' -o /dev/null -w "%{http_code}" $BASE_URL/auth/signup 2>/dev/null)
if [ "$JSON_ERROR_STATUS" = "400" ]; then
    print_test_result 0 "Gestion erreur JSON correcte"
else
    print_test_result 1 "Gestion erreur JSON incorrecte"
fi

print_header "RAPPORT FINAL"

echo -e "\n${BOLD}📊 STATISTIQUES FINALES${NC}"
echo -e "════════════════════════════════════════"
echo -e "${GREEN}✅ Tests réussis:     $PASSED_TESTS${NC}"
echo -e "${RED}❌ Tests échoués:     $FAILED_TESTS${NC}"
echo -e "${BLUE}📝 Total des tests:   $TOTAL_TESTS${NC}"

# Calcul du pourcentage de réussite
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$(( (PASSED_TESTS * 100) / TOTAL_TESTS ))
    echo -e "${PURPLE}🎯 Taux de réussite:  $SUCCESS_RATE%${NC}"
else
    echo -e "${RED}❌ Aucun test n'a été exécuté${NC}"
fi

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "\n${GREEN}${BOLD}🎉 PARFAIT ! SCORE 16/16 ATTEINT !${NC}"
    echo -e "${GREEN}✨ Votre API est prête pour la production !${NC}"
else
    echo -e "\n${YELLOW}🔧 $FAILED_TESTS test(s) à corriger pour atteindre la perfection${NC}"
fi

echo -e "\n${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"
echo -e "${PURPLE}${BOLD}    TESTS TERMINÉS - $(date '+%H:%M:%S')${NC}"
echo -e "${PURPLE}${BOLD}═══════════════════════════════════════════${NC}"