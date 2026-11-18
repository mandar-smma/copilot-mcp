# copilot-mcp

## Task API

Tasks are stored in the local SQLite database at `./data/app.db`, which is created when the application starts.

| Method | Endpoint | Behavior |
| --- | --- | --- |
| `GET` | `/api/tasks` | List tasks |
| `GET` | `/api/tasks/{id}` | Get a task |
| `POST` | `/api/tasks` | Create a task (`201 Created` with `Location`) |
| `PUT` | `/api/tasks/{id}` | Replace a task |
| `DELETE` | `/api/tasks/{id}` | Delete a task (`204 No Content`) |

Create and update requests use this JSON shape; `title` must be non-blank, `description` is optional, and `completed` is required:

```json
{
  "title": "Write API tests",
  "description": "Cover the task endpoints",
  "completed": false
}
```

Missing task IDs return `404`; invalid request bodies return an RFC 9457 `ProblemDetail`.

Run the application with `mvn spring-boot:run` and run unit plus integration tests with `mvn verify`. Integration tests use `./target/test.db`.
