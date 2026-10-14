# fintech-qa-lab

A test automation portfolio built around a small payments API.

The `payment-api` module is the **system under test**: a payments and fees service with
authentication, input validation and some deliberately planted defects.
The test suites find those defects and document them.

## Modules

| Module | What it contains |
| --- | --- |
| `payment-api` | Payments API (plain Java 21, no frameworks) and its unit tests (JUnit 5) |
| `qa-tests` | API tests (REST Assured); BDD (Cucumber) and UI (Playwright) suites will be added |

## Run it

Requirements: Java 21 and Maven (IntelliJ IDEA includes Maven).

1. Start the API: run `PaymentApiApplication` (listens on `http://localhost:8080`).
2. Run all tests: `mvn test` from the project root, or right-click a test class in IntelliJ and choose **Run**.

Demo credentials: `demo` / `demo123`.

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/health` | Health check |
| POST | `/api/auth/token` | Returns a bearer token |
| POST | `/api/payments` | Creates a payment and calculates its fee |
| GET | `/api/payments` | Lists payments |
| GET | `/api/payments/{id}` | Returns one payment |

Example request body for `POST /api/payments`:

```json
{ "amount": 250.00, "currency": "EUR", "iban": "IE29AIBK93115212345678", "instant": false }
```

## Roadmap

- [ ] Unit tests with JUnit 5, parameterised tests, boundary value analysis
- [ ] API tests with REST Assured and JSON Schema validation
- [ ] BDD scenarios with Cucumber and Allure reports
- [ ] UI tests with Playwright for Java
- [ ] CI with GitHub Actions
- [ ] Performance tests (JMeter, k6) and observability
- [ ] Contract tests with Pact
- [ ] LLM evaluation tests (Promptfoo, DeepEval)
- [ ] Accessibility tests (axe-core)
