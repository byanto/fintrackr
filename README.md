# 💸 Fintrackr

![Java CI](https://github.com/byanto/fintrackr/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue)
![Maven](https://img.shields.io/badge/Maven-3.9-blue)
![Platform](https://img.shields.io/badge/platform-linux--mac--windows-lightgrey)

A full-stack personal finance and investment management app built with **Spring Boot** and **React**, designed to provide a centralized platform for helping small business owners and individuals to track personal finances, starting with stock investments and designed to expand into marketplace sales and other asset classes.

**Fintrackr** is my flagship learning project. It combines my passion for:
- 💼 E-commerce & digital business
- 📊 Stock market investing
- 💻 Software development
- 📈 Data analytics

---

## 🏛️ Architecture & Technical Highlights

This project was designed not just to be functional, but to demonstrate a deep understanding of software architecture and professional best practices.

* **Modular Monolith Architecture:** The code is organized into independent business domain modules (`investment`, `marketplace`). This enhances scalability, maintainability, and separation of concerns.
* **Test-Driven Development (TDD):** Every feature is built with a "test-first" approach. The project has a high level of test coverage, using both fast unit tests (`@WebMvcTest`) and full integration tests (`@SpringBootTest`).
* **Clean API Design:** The REST API is designed for clarity and ease of use, featuring:
    * **Global Exception Handling:** A centralized `@ControllerAdvice` provides consistent and meaningful error responses.
    * **Custom Exceptions:** Semantic exceptions (`ResourceNotFoundException`) are used instead of generic ones.
    * **DTO Pattern:** Data Transfer Objects (DTOs) are used to separate API contracts from internal data models.
* **Domain-Driven Principles:** The data model is carefully designed with clear entity boundaries, using JPA Inheritance (`@Inheritance(strategy = InheritanceType.JOINED)`) to elegantly model different asset types (`Stock`, `MutualFund`).
* **Modern Java Practices:** The codebase uses modern Java features like `var` and `records` where they improve readability, and follows a consistent, professional coding style.

--- 

## 🛠️ Tech Stack

| Category      | Technology / Tool                                                                                                   |
|---------------|---------------------------------------------------------------------------------------------------------------------|
| Backend 	| [Java 21](https://www.oracle.com/java/), [Spring Boot 3](https://spring.io/projects/spring-boot)					  |
| Database 	| [PostgreSQL](https://www.postgresql.org/)                                                                           |
| Testing 	| [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/), [AssertJ](https://assertj.github.io/doc/), [Testcontainers](https://www.testcontainers.org/) |
| Build 	| [Apache Maven](https://maven.apache.org/)                                                                           |
| Versioning | Git & [Github](https://github.com/)                                                                                |
| DevOps 	| [Docker](https://www.docker.com/) & Docker Compose                                                                  |
| Utilities | [Lombok](https://projectlombok.org/)                                                                                |
| Frontend | [React](https://react.dev/)   																						  |

---

## 🚀 Setup Instructions

### Prerequisites

* Java 21 (or newer)
* Apache Maven 3.8+
* Docker and Docker Compose

### Running the Application Locally

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/byanto/fintrackr.git
    cd fintrackr
    ```

2.  **Start the database:**
    This command will start a PostgreSQL database in a Docker container.
    ```bash
    docker-compose up -d
    ```

3.  **Run the Spring Boot application:**
    ```bash
    mvn spring-boot:run
    ```
    The application will be running on `http://localhost:8080`.
    
---

## 📖 API Documentation

Here are the currently available endpoints.

| Method | Endpoint                            | Description                   |
|--------|-------------------------------------|-------------------------------|
| `POST` | `/api/investment/portfolios`        | Creates a new, empty portfolio. |
| `GET`  | `/api/investment/portfolios/{id}`   | Retrieves a single portfolio by its ID. |
| `GET`  | `/api/investment/portfolios`        | Retrieves a list of all portfolios. |

### Example Usage with cURL

**Create a new portfolio:**
```bash
curl -X POST http://localhost:8080/api/investment/portfolios \
-H "Content-Type: application/json" \
-d '{
    "name": "My Tech Stocks",
    "description": "A portfolio for high-growth tech stocks."
}'
```

**Get portfolio with ID 1:**
```bash
curl http://localhost:8080/api/investment/portfolios/1

---

## 📝 Learning Journey

Fintrackr is more than a project—it's my personal journey back into the world of software development.

After completing my MSc in Computer Science from TU Berlin in 2016 and working as a Werkstudent at IRB and KNIME, I returned to Indonesia and successfully built and managed my own e-commerce business with a team of about 10 employees. In 2025, I decided to return to my roots in tech and re-enter the industry as a backend developer.

To rebuild my skills, I used a combination of structured courses (I enrolled in a Spring Boot course by in28minutes at Udemy) and interactive exploration with ChatGPT (as my pair programmer) to deepen my understanding and problem-solving skills. I also supplemented it with hands-on practice through this app.

Want to follow along? I'm writing a blog series on [LinkedIn](https://www.linkedin.com/in/byanto/) documenting my learning journey and career comeback. 

---

## 💬 Connect with Me

- 🔗 LinkedIn: [byanto](https://www.linkedin.com/in/byanto/)
- 💻 GitHub: [@byanto](https://github.com/byanto)

---

**Let’s connect — especially if you’re hiring, mentoring, or just love developer comeback stories.**
