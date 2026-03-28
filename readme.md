# 📈 Trader Journal Backend - Open Source Edition

A robust, self-hosted backend system for cryptocurrency traders to automate trade journaling, sync history, and execute orders directly via API. Built with high-performance Java Spring Boot and secured with AES encryption.

---

## 👤 Author
- **Name:** Junia Tran
- **Role:** Backend Developer & Trading Enthusiast

---

## 🚀 Key Features

- **Multi-Exchange Support:** Integrated with Binance (Spot & Futures) with a modular architecture to add more exchanges easily.
- **Security First:** - **AES Encryption:** All API Secrets are encrypted at rest before being saved to the database.
  - **Security Audit:** Automated API permission checking (Blocks Withdrawals/Transfers for user safety).
- **Automated Sync:** Fetch and process trade history, symbols, and balances directly from exchanges.
- **Order Execution:** Supports placing Buy/Sell orders (Limit/Market) for both Spot and Futures markets.
- **Developer Friendly:** Clean architecture, Snake_case API standards, and comprehensive JWT Authentication.

---

## 🛠 Tech Stack

- **Framework:** Spring Boot 4.x
- **Security:** Spring Security, JWT (JSON Web Token)
- **Database:** PostgreSQL / MySQL / SQL Server (JPA/Hibernate)
- **Encryption:** AES-256
- **Build Tool:** Maven

---

## ⚙️ Installation & Setup

### Prerequisites
- JDK 21 or higher
- A running Database (PostgreSQL recommended)

### Steps
1. **Clone the repository:**
   ```bash
   git clone git@github.com:trandinh0506/trading-journal-backend.git
   cd trading-journal-backend
   ```
2. **Configure Environment:**
- Open **application.properties**
```bash
spring.application.name=journal-backend
# DB connection
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

# Auto create/update table base on Java Entity
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Enable Virtual Threads
spring.threads.virtual.enabled=true


# MinIO
minio.url=
minio.accessKey=
minio.secretKey=
minio.bucketName=

# Logging
logging.config=classpath:logback-spring.xml

# Encrypt service
app.security.encrypt-key=

# App Environment
app.env=development

# Jackson config
spring.jackson.generator.write-bigdecimal-as-plain=true

# Hibernate config
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# jwt
jwt.secret=
jwt.accessExpiration=
jwt.refreshExpiration=

# CORS config
app.cors.allowed-origins=
```

3. **Start docker compose:**
```bash
docker compose up -d
```

4. **Run the application:**
```bash
./mvnw spring-boot:run
```
