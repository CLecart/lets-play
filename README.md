# Let's Play - Spring Boot CRUD API# Let's Play - Spring Boot CRUD API with MongoDB

## Important: secrets

This project reads the JWT signing secret from the environment variable `APP_JWT_SECRET` (preferred). If not present it falls back to `app.jwt.secret` in `application.properties`. For production use a high-entropy secret (>=32 chars) stored in a secrets manager or environment — never commit secrets to the repository.

A RESTful CRUD API built with Spring Boot and MongoDB for user and product management with JWT authentication.A RESTful CRUD API built with Spring Boot and MongoDB that provides user management and product management functionalities with JWT-based authentication and authorization.

## 🚀 Features## Features

- **User Management**: Create, read, update, and delete users- **User Management**: Complete CRUD operations for users

- **Product Management**: Full CRUD operations for products- **Product Management**: Complete CRUD operations for products

- **JWT Authentication**: Secure authentication with JSON Web Tokens- **JWT Authentication**: Token-based authentication system

- **Password Security**: BCrypt password hashing- **Role-based Authorization**: Admin and User roles with different permissions

- **Data Validation**: Comprehensive input validation- **Security Measures**:

- **CORS Support**: Cross-origin resource sharing enabled - Password hashing and salting with BCrypt

- **Rate Limiting**: API rate limiting protection - Input validation to prevent MongoDB injection

- **MongoDB Integration**: NoSQL database with Spring Data MongoDB - CORS configuration

- **RESTful Design**: Follows REST architectural principles - Rate limiting (60 requests per minute per IP)
  - Sensitive data protection

## 🛠️ Tech Stack- **Error Handling**: Comprehensive error handling with appropriate HTTP status codes

- **RESTful Design**: Follows REST principles

- **Java**: 21

- **Spring Boot**: 3.5.6## Database Schema

- **Spring Security**: JWT-based authentication

- **Spring Data MongoDB**: Database integration```

- **MongoDB**: NoSQL databaseUser:

- **Maven**: Dependency management- id: String (MongoDB ObjectId)

- **Lombok**: Reduce boilerplate code- name: String (2-50 characters)

- **Jakarta Validation**: Input validation- email: String (unique, valid email format)

- password: String (hashed with BCrypt, min 6 characters)

## 📋 Prerequisites- role: String (USER or ADMIN)

- Java 21 or higherProduct:

- Maven 3.6+- id: String (MongoDB ObjectId)

- MongoDB 4.4+- name: String (2-100 characters)

- description: String (max 500 characters)

## 🚀 Getting Started- price: Double (positive value)

- userId: String (owner's user ID)

### 1. Clone the repository```

````bash## Prerequisites

git clone <repository-url>

cd lets-play- Java 17 or higher

```- Maven 3.6+

- MongoDB (running on localhost:27017)

### 2. Start MongoDB

## Installation and Setup

Make sure MongoDB is running on your system:

1. **Clone the repository**

```bash

# Using systemctl (Linux)   ```bash

sudo systemctl start mongod   git clone <your-repo-url>

   cd lets-play

# Or using Docker   ```

docker run -d -p 27017:27017 --name mongodb mongo:latest

```2. **Install MongoDB**

   - Install MongoDB Community Edition from [MongoDB Download Center](https://www.mongodb.com/try/download/community)

### 3. Configure the application   - Start MongoDB service:



The application uses the following default configuration in `src/main/resources/application.properties`:     ```bash

     # On Linux/macOS

```properties     sudo systemctl start mongod

# MongoDB Configuration

spring.data.mongodb.uri=mongodb://localhost:27017/letsplay     # Or using brew on macOS

     brew services start mongodb-community

# JWT Configuration

jwt.secret=your-super-secure-jwt-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256-algorithm     # On Windows

     net start MongoDB

# Server Configuration     ```

server.port=8080

3. **Configure the application**

# Logging   - Update `src/main/resources/application.properties` if needed:

logging.level.com.example.lets_play=DEBUG     ```properties

```     spring.data.mongodb.uri=mongodb://localhost:27017/letsplay

     app.jwt.secret=mySecretKey

### 4. Run the application     app.jwt.expiration=86400000

     ```

```bash

# Using Maven wrapper4. **Build and run the application**

./mvnw spring-boot:run

   ```bash

# Or using Maven directly   # Build the project

mvn spring-boot:run   mvn clean install

````

# Run the application

The application will start on `http://localhost:8080` mvn spring-boot:run

````

## 📚 API Documentation

5. **The API will be available at `http://localhost:8080`**

### Authentication Endpoints

## API Endpoints

#### Register a new user

```http### Authentication Endpoints (Public)

POST /api/auth/register

Content-Type: application/json- **POST** `/api/auth/signup` - Register a new user

- **POST** `/api/auth/signin` - Login user

{

"username": "johndoe",### User Endpoints

"email": "john@example.com",

"password": "password123"- **POST** `/api/users` - Create user (Admin only)

}- **GET** `/api/users` - Get all users (Admin only)

```- **GET** `/api/users/{id}` - Get user by ID (Admin or own profile)

- **PUT** `/api/users/{id}` - Update user (Admin or own profile)

#### Login- **DELETE** `/api/users/{id}` - Delete user (Admin or own profile)

```http

POST /api/auth/login### Product Endpoints

Content-Type: application/json

- **GET** `/api/products` - Get all products (Public)

{- **GET** `/api/products/{id}` - Get product by ID (Public)

"username": "johndoe",- **POST** `/api/products` - Create product (Authenticated users)

"password": "password123"- **PUT** `/api/products/{id}` - Update product (Owner or Admin)

}- **DELETE** `/api/products/{id}` - Delete product (Owner or Admin)

```- **GET** `/api/products/user/{userId}` - Get products by user ID (Authenticated users)



**Response:**## API Usage Examples

```json

{### 1. Register a new user

"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",

"type": "Bearer",```bash

"username": "johndoe",curl -X POST http://localhost:8080/api/auth/signup \

"email": "john@example.com"  -H "Content-Type: application/json" \

}  -d '{

```    "name": "John Doe",

 "email": "john@example.com",

### User Management Endpoints    "password": "password123",

 "role": "USER"

All user endpoints require authentication. Include the JWT token in the Authorization header:  }'

````

Authorization: Bearer <your-jwt-token>

````### 2. Login



#### Get all users```bash

```httpcurl -X POST http://localhost:8080/api/auth/signin \

GET /api/users  -H "Content-Type: application/json" \

```  -d '{

    "email": "john@example.com",

#### Get user by ID    "password": "password123"

```http  }'

GET /api/users/{id}```

````

### 3. Create an admin user (first, you need to create a user and manually change their role in the database to ADMIN)

#### Update user

`http`bash

PUT /api/users/{id}# After getting the JWT token from login

Content-Type: application/jsoncurl -X POST http://localhost:8080/api/users \

-H "Content-Type: application/json" \

{ -H "Authorization: Bearer YOUR_JWT_TOKEN" \

"username": "newusername", -d '{

"email": "newemail@example.com" "name": "Admin User",

} "email": "admin@example.com",

````"password": "admin123",

    "role": "ADMIN"

#### Delete user  }'

```http```

DELETE /api/users/{id}

```### 4. Create a product



### Product Management Endpoints```bash

curl -X POST http://localhost:8080/api/products \

#### Get all products  -H "Content-Type: application/json" \

```http  -H "Authorization: Bearer YOUR_JWT_TOKEN" \

GET /api/products  -d '{

```    "name": "iPhone 13",

    "description": "Latest Apple smartphone",

#### Get product by ID    "price": 999.99

```http  }'

GET /api/products/{id}```

````

### 5. Get all products (public endpoint)

#### Create product (requires authentication)

`http`bash

POST /api/productscurl -X GET http://localhost:8080/api/products

Authorization: Bearer <your-jwt-token>```

Content-Type: application/json

## Authentication

{

"name": "Product Name",The API uses JWT (JSON Web Token) for authentication. After successful login, include the token in the Authorization header:

"description": "Product description",

"price": 99.99,```

"category": "Electronics"Authorization: Bearer YOUR_JWT_TOKEN

}```

````

## Authorization Roles

#### Update product (requires authentication)

```http- **USER**: Can create, read, update, and delete their own products and profile

PUT /api/products/{id}- **ADMIN**: Can perform all operations on all users and products

Authorization: Bearer <your-jwt-token>

Content-Type: application/json## Security Features



{1. **Password Security**: Passwords are hashed using BCrypt with salt

  "name": "Updated Product Name",2. **Input Validation**: All inputs are validated to prevent injection attacks

  "description": "Updated description",3. **CORS**: Cross-Origin Resource Sharing is configured for security

  "price": 129.99,4. **Rate Limiting**: 60 requests per minute per IP address

  "category": "Electronics"5. **JWT Security**: Tokens expire after 24 hours

# Let's Play

Lightweight Spring Boot CRUD API (Users + Products) with JWT authentication and MongoDB.

This repository is prepared for an audit: tests run, JWT secrets are configurable via environment variables, and the README and CI workflow document how to run and verify the application.

## Quick facts

- Java: 21
- Spring Boot: 3.5.6
- Database: MongoDB (Spring Data MongoDB)
- Build: Maven (use `./mvnw` wrapper)

## Security / Secrets

- The application reads the signing secret from the environment variable `APP_JWT_SECRET`. If absent it falls back to `app.jwt.secret` in `application.properties`.
- For HS512 the signing key must be 512 bits (64 bytes) or larger. During development the code derives a 512-bit key from shorter secrets by hashing (not recommended for production).
- Never commit production secrets. Use environment injection or a secrets manager in production.

## Environment variables (important)

- `APP_JWT_SECRET` — JWT signing secret (recommended: base64 or url-safe string >= 64 bytes)
- `SPRING_DATA_MONGODB_URI` — MongoDB connection string (default: `mongodb://localhost:27017/letsplay`)
- `SPRING_PROFILES_ACTIVE` — e.g. `dev` / `test` / `prod`

## Local development

1. Copy the example env file and edit values:

```bash
cp .env.example .env
# edit .env to set a secure APP_JWT_SECRET
```

2. Start a local MongoDB instance, or use Docker:

```bash
# systemd
sudo systemctl start mongod

# or Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

3. Build and run the application:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Tests and CI

- Run unit + integration tests locally with:

```bash
./mvnw test
```

- CI: the repository contains a GitHub Actions workflow that starts a MongoDB service and runs `./mvnw test`.

## Docker (dev)

- docker-compose is provided for development (`docker-compose.dev.yml`). It reads `.env` values. Use:

```bash
docker compose -f docker-compose.dev.yml up --build
```

## Audit checklist (what I prepared)

- All tests pass locally (`./mvnw test`).
- Centralized error handling returns consistent error responses.
- JWT secret is configurable via environment and validated in code.
- Docker/dev artifacts and `.env.example` included; local `.env` is gitignored.

---

If you want, I can:

- Add Testcontainers to make integration tests hermetic (CI-friendly).
- Improve CI to publish Javadocs or run additional static analysis.


- Important environment variables (examples):
     - `APP_JWT_SECRET` — high-entropy secret (>=32 bytes base64/url-safe recommended)
     - `SPRING_DATA_MONGODB_URI` — e.g. `mongodb://localhost:27017/letsplay` (use `mongodb://mongo:27017/letsplay` when using docker-compose)
     - `SPRING_PROFILES_ACTIVE` — e.g. `dev` or `test`

- The local `.env` file is intentionally added to `.gitignore` to avoid committing secrets.

Run the application and tests:

```bash
# Run the app with the Maven wrapper
./mvnw spring-boot:run

# Build and run the full test suite
./mvnw clean install
./mvnw test

# Run the specific integration test used during development
./mvnw -Dtest=com.example.lets_play.ForbiddenIntegrationTest test
```

Run with Docker Compose (uses `.env` values):

```bash
docker compose -f docker-compose.dev.yml up --build
```

If you'd like, I can perform a more thorough README cleanup to remove duplicated sections and improve formatting.

## 🆘 Troubleshooting

### Common Issues

**MongoDB Connection Error:**
- Ensure MongoDB is running on localhost:27017
- Check if the database name is correct (letsplay)

**JWT Token Issues:**
- Verify the JWT secret key is at least 256 bits long
- Check token expiration (default: 24 hours)

**Build Issues:**
- Ensure Java 21 is installed and configured
- Run `./mvnw clean install` to refresh dependencies

## 📞 Support

For support and questions, please create an issue in the repository.
````
