# FinTrackr - Personal Finance Tracking Application

`FinTrackr` is a backend service designed to help users track their financial investments. It provides a robust system for managing multiple portfolios, logging trades, calculating holdings, and applying transaction fees. The service is built with a modern Java stack and exposes a comprehensive REST API for integration with frontend applications or other services.

## Features

- **Broker Account Management:** Create and manage multiple broker accounts.
- **Portfolio Tracking:** Organize investments into different portfolios linked to broker accounts.
- **Instrument Definition:** Maintain a master list of financial instruments (e.g., stocks, bonds).
- **Immutable Trade Log:** Record every buy and sell transaction as an immutable event.
- **Automated Holding Calculation:** Automatically calculates and updates current holdings (quantity and average price) based on trades.
- **Dynamic Fee Engine:** Configure and apply transaction fees (percentage-based with a minimum) for different brokers and instrument types.
- **RESTful API:** A clean, well-documented API for all core functionalities.

## Technologies Used

- **Backend:** Java 21+, Spring Boot 3.x
- **Data:** Spring Data JPA, Hibernate, PostgreSQL
- **Database Migrations:** Flyway
- **Testing:** JUnit 5, Mockito, AssertJ, Testcontainers
- **Build:** Maven
- **Tooling:** Lombok, MapStruct
- **Containerization:** Docker, Docker Compose

## Getting Started

### Prerequisites

- JDK 21 or higher
- Apache Maven 3.8+
- Docker and Docker Compose (for running a local PostgreSQL instance)

### Installation & Running

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/byanto/fintrackr.git
    cd fintrackr
    ```

2.  **Start the database:**
    A `docker-compose.yml` file is provided for convenience to start a PostgreSQL database.
    ```bash
    docker-compose up -d
    ```
    This will start a PostgreSQL server on `localhost:5432`. The application is pre-configured to connect to it.

3.  **Build and run the application:**
    Navigate to the `investment-service` directory and use the Spring Boot Maven plugin to run the application.
    ```bash
    cd investment-service
    mvn spring-boot:run
    # Or, to activate the 'dev' profile for local database credentials:
    # mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```
    The service will be available at `http://localhost:8080`. Ensure your Docker Compose database is running before starting the application.

## API Endpoints

The following is a summary of the available API endpoints.

| HTTP Method | Endpoint                               | Description                                     |
|-------------|----------------------------------------|-------------------------------------------------|
| `POST`      | `/api/broker-accounts`                 | Create a new broker account.                    |
| `GET`       | `/api/broker-accounts`                 | Retrieve all broker accounts.                   |
| `GET`       | `/api/broker-accounts/{id}`            | Retrieve a single broker account by its ID.     |
| `PUT`       | `/api/broker-accounts/{id}`            | Update an existing broker account.              |
| `DELETE`    | `/api/broker-accounts/{id}`            | Delete a broker account.                        |
|             |                                        |                                                 |
| `POST`      | `/api/portfolios`                      | Create a new portfolio.                         |
| `GET`       | `/api/portfolios`                      | Retrieve all portfolios.                        |
| `GET`       | `/api/portfolios/{id}`                 | Retrieve a single portfolio by its ID.          |
| `PUT`       | `/api/portfolios/{id}`                 | Update an existing portfolio.                   |
| `DELETE`    | `/api/portfolios/{id}`                 | Delete a portfolio and its associated data.     |
|             |                                        |                                                 |
| `POST`      | `/api/instruments`                     | Create a new financial instrument.              |
| `GET`       | `/api/instruments`                     | Retrieve all instruments.                       |
| `GET`       | `/api/instruments/{id}`                | Retrieve a single instrument by its ID.         |
| `PUT`       | `/api/instruments/{id}`                | Update an existing instrument.                  |
| `DELETE`    | `/api/instruments/{id}`                | Delete an instrument.                           |
|             |                                        |                                                 |
| `POST`      | `/api/trades`                          | Record a new trade (buy/sell).                  |
| `GET`       | `/api/trades`                          | Retrieve all trades.                            |
| `GET`       | `/api/trades/{id}`                     | Retrieve a single trade by its ID.              |
|             |                                        |                                                 |
| `GET`       | `/api/holdings/{id}`                   | Retrieve a single holding by its ID.            |
| `GET`       | `/api/portfolios/{portfolioId}/holdings` | Retrieve all holdings for a specific portfolio. |
|             |                                        |                                                 |
| `POST`      | `/api/fee-rules`                       | Create a new fee rule.                          |
| `GET`       | `/api/fee-rules`                       | Retrieve all fee rules.                         |
| `GET`       | `/api/fee-rules/{id}`                  | Retrieve a single fee rule by its ID.           |
| `PUT`       | `/api/fee-rules/{id}`                  | Update an existing fee rule.                    |
| `DELETE`    | `/api/fee-rules/{id}`                  | Delete a fee rule.                              |

## Database Schema

The database schema is managed by Flyway migrations and consists of the following core tables:

- `broker_account`: Stores user-defined broker accounts (e.g., "My Fidelity Account").
- `portfolio`: Represents a collection of investments, linked to a `broker_account`.
- `instrument`: A master list of financial instruments (e.g., stock `BBCA`).
- `trade`: An immutable log of all buy and sell transactions.
- `holding`: A materialized view of the current position (quantity, average price) of an instrument within a portfolio. It is automatically updated when a new trade is processed.
- `fee_rule`: Defines the fee structure for a specific broker, instrument type, and trade type.

![Database Schema](docs/images/db-schema.png)

## Running Tests

The project has a comprehensive suite of unit and integration tests. The integration tests use **Testcontainers** to spin up a real PostgreSQL database, ensuring that repository and service-level tests run against a production-like environment.

To run all tests, execute the following Maven command from the `investment-service` directory:

```bash
mvn test
```

## Project Motivation & Future Goals

This project was born from a passion for investing and a personal journey of returning to the IT industry after a break since 2016. It serves as a practical showcase of my commitment to learning modern software engineering practices and technologies, specifically within the Java and Spring ecosystem, with the goal of securing a software development role in Germany.

### The Vision

`FinTrackr` is designed with a microservices architecture in mind. The long-term vision includes:
- **Business Service**: A future microservice to track transactions from my e-commerce businesses on platforms like Shopee, Tokopedia, and Lazada.
- **Market Data Service**: A service to fetch and store real-time or historical market data for instruments.
- **Frontend Application**: A modern, user-friendly web interface for interacting with the services.

This project is not just a demonstration of skills but a real-world application that I intend to use and grow over time. It reflects my dedication to continuous learning and my ability to build robust, scalable software.

To learn more about my journey, skills, and other projects, please visit my personal portfolio and blog at [www.budiyanto.com](https://www.budiyanto.com).
