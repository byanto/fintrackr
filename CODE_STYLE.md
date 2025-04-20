# ✨ Fintrackr - Code Style & Naming Conventions

To keep this project clean, consistent, and professional, we follow these conventions:

---

## ✅ General Code Style

- Follow standard **Java naming conventions**:
  - `camelCase` for variables and method names.
  - `PascalCase` for class names.
  - `UPPER_CASE` for constants.
- Use **Lombok** (`@Builder`, `@Getter`, etc.) to reduce boilerplate where appropriate.
- Always add `@RequiredArgsConstructor` for constructor injection.

---

## ✅ Project Structure

- **Entities** live in the `model` package.
- **DTOs** (Data Transfer Objects) live in the `dto` package.
- **Business logic** goes into the `service` layer.
- **Controller** layer handles API input/output and maps between DTOs and domain entities.

---

## ✅ Controller vs. Service

- The **Service layer** should return **domain entities** (e.g., `Product`, `Transaction`).
- The **Controller layer** is responsible for:
  - Mapping request DTOs to domain models.
  - Mapping domain models to response DTOs.
  - Handling HTTP response codes and exceptions.

---

## ✅ Enum Handling

- Use `@Enumerated(EnumType.STRING)` on enum fields stored in the database to:
  - Improve readability in the DB (`"IN"` instead of `0`).
  - Avoid breaking changes if enum order changes.

---

## ✅ Test Style

- Prefer **Mockito** to mock dependencies (e.g., repositories).
- Prefer **unit tests for service layer**, mocking the repository.
- Test only **one behavior per test**.
- Use `@Mock` and `@InjectMocks` for setup.

---

## ✅ Unit Test Naming (Test Method Convention)

- Use **underscore-style** (`snake_case`) to describe behavior clearly.
- Format:  
  `test<MethodName>_When<Condition>_<ExpectedOutcome>`
- Examples:
  - `testDeleteProduct_WhenProductExists_ShouldRemoveFromRepository`
  - `testCreateTransaction_WhenStockIsInsufficient_ShouldThrowException`

---

## ✅ Git Branching & Flow

- `main`: always in a deployable state.
- `dev`: active development branch
- Use **feature branches** for new features.
  - Naming format: `feature/<feature-name>`
- Always open a **Pull Request** to merge into `main`.
- Once merged, delete the feature branch on GitHub (safe and good practice).

