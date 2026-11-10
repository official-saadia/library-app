# Library App

Small Spring Boot REST API used to practise CI/CD (GitHub Actions, Jenkins, GitLab CI).
Books live in an in-memory map, seeded with ten books on startup. Data resets on every restart.

Requirements: Java 25, Maven 3.9+.

## Run

```
mvn spring-boot:run
```

## Test

```
mvn test
```

`mvn test` runs three kinds of tests:

- `LibraryServiceTest`: unit tests for the service logic.
- `BookApiIntegrationTest`: starts the app on a random port and calls it with Spring's `RestClient`.
- `BookApiRestAssuredTest`: API tests written with REST Assured (status codes, JSON fields, full CRUD lifecycle).

## Smoke test (curl)

With the app running (locally or on a server), run:

```
chmod +x smoke-test.sh
./smoke-test.sh http://localhost:8080
```

It waits for the app to respond, makes a few curl calls, and exits non-zero if any fail.
Use it as the last step of a deploy stage in your pipeline.

## Build the JAR

```
mvn clean package
java -jar target/library-app-0.0.1-SNAPSHOT.jar
```

## Endpoints

| Method | Path                    | Description                                  |
|--------|-------------------------|----------------------------------------------|
| GET    | /api/books              | List all books                               |
| GET    | /api/books/{id}         | Get one book (404 if missing)               |
| GET    | /api/books/search?q=    | Search by name, author, or genre             |
| POST   | /api/books              | Add a book (201, or 400 if name is blank)    |
| PUT    | /api/books/{id}         | Update a book (404 if missing)              |
| DELETE | /api/books/{id}         | Delete a book (204, or 404 if missing)      |

Example:

```
curl http://localhost:8080/api/books/search?q=tolkien

curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"name":"Neuromancer","author":"William Gibson","genre":"Science Fiction","description":"A hacker takes one last job."}'
```
