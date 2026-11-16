## STRICT VERSION LOCK (DO NOT MODIFY)
- **NEVER** modify, downgrade, or upgrade the `<parent><version>` or any dependency versions in `pom.xml` under any circumstances.
- The Spring Boot version is strictly locked and Java is locked as per pom.xml. Do not change the Java version in `pom.xml` or `mvnw` scripts.
- Treat existing dependency versions as read-only
- Do not modify parent or existing dependencies; only add new library when its required

# Repository Engineering Standards
- **Stack:** Java 21, Spring Boot 3.4.2, Maven (`./mvnw`).
- **Database:** Local file-based SQLite (`jdbc:sqlite:./data/app.db`) using `org.xerial:sqlite-jdbc` and `org.hibernate.orm:hibernate-community-dialects` (`org.hibernate.community.dialect.SQLiteDialect`). Never use Docker or Testcontainers.
- **Architecture:** Package-by-feature. Controllers accept/return Java `record` DTOs with `@Valid`; entities never leave the service layer. Constructor injection only.
- **Error Handling:** Single `@RestControllerAdvice` returning RFC 7807 `ProblemDetail`.
- **Testing Gate:** Every feature requires JUnit 5 + Mockito unit tests (`*Test.java`) and `@SpringBootTest` + `MockMvc` integration tests (`*IT.java` or `*IntegrationTest.java`) using an isolated test SQLite DB (`jdbc:sqlite:./target/test.db`).
- **Verification:** Run `./mvnw test` (or `./mvnw verify`) via terminal and fix all failures before finishing.