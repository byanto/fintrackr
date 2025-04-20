# Contributing to Fintrackr

Thank you for considering contributing to **Fintrackr**! 🎉  
This guide will help you get started with setting up the project and following the best practices.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Branching Strategy](#branching-strategy)
- [Code Style](#code-style)
- [Commits](#commits)
- [Testing](#testing)
- [Pull Requests](#pull-requests)

---

## Prerequisites

Make sure you have the following installed:

- Java 21
- Maven 3.9+
- Spring Boot 3.4.4
- Git
- A code editor (e.g., VSCode or IntelliJ)

---

## Getting Started

1. Fork this repository.
2. Clone your fork:
   ```bash
   git clone https://github.com/your-username/fintrackr.git
3. Navigate to the project directory:
    ```bash
   cd fintrackr
4. Create and checkout a new feature branch:
    ```bash
   git checkout -b feature/<feature-name>
5. Import the project into your preferred IDE (e.g., IntelliJ or VSCode) and let it download dependencies.
6. To run the application, use the following command:
    ```bash
   ./mvnw spring-boot:run

---

## Branching Strategy

We follow GitHub Flow:

- `main`: always contains production-ready code.
- `dev`: development branch for ongoing work.
- `feature/xyz`: short-lived feature branches from `dev`.

Workflow:
- Create feature branches from `dev`.
- Open a Pull Request (PR) from `feature/xyz` to `dev`.
- Once `dev` is stable, merge `dev` into `main`.
- Delete feature branches after merging to keep the repo clean.

---

## Code Style

Please follow the [code style guidelines](CODE_STYLE.md) when writing code.

Highlights:
- Use `camelCase` for variables and methods.
- Use `PascalCase` for class names.
- Use `_` for test method naming (e.g., `testDeleteProduct_WhenProductExists`).
- Service layer returns domain entities, controller handles DTO conversion.

---

## Commits

Use clear, meaningful commit messages. Prefer imperative tone:

- ✅ `Add unit test for transaction creation`
- ✅ `Fix product stock update logic`

---

## Testing

Before pushing, run all unit tests:

    ./mvnw test

- We use **JUnit5** and Mockito for unit testing.
- Prefer **Test-Driven Development (TDD)** when building new features

---

## Pull Requests

- Push your branch to GitHub
- Open a Pull Request to the `dev` branch
- Include a meaningful title and clear description
- Assign reviewers if available
- Once merged into `dev`, the maintainer will handle merging into `main` when it is stable.
