# Store inventory: Spring Boot + Angular + Azure

- `api/` Spring Boot 4 (Java 21) REST API: `/api/items` CRUD, pagination, validation, H2, Actuator
- `web/` Angular front end
- `.github/workflows/deploy-api.yml` builds, tests and deploys the API to App Service

## Run locally
```
cd api && ./mvnw spring-boot:run      # http://localhost:8080/api/items, /actuator/health
cd web && npm start                   # http://localhost:4200
cd api && ./mvnw test                 # 12 tests: @WebMvcTest + @DataJpaTest
```

## Endpoints
| Method | Path | Notes |
|---|---|---|
| GET | /api/items?page=0&size=10&sort=name,asc | paged |
| GET | /api/items/{id} | 404 if missing |
| POST | /api/items | 201 + Location; 400 on validation; 409 on duplicate SKU |
| PUT | /api/items/{id} | |
| DELETE | /api/items/{id} | 204 |

All errors use one JSON shape: `{timestamp, status, error, message, fieldErrors}`.

## Azure steps (yours to do)
1. Create the free account. **First** set a budget alert (Cost Management > Budgets).
2. Create one resource group, e.g. `rg-inventory-demo`. Put everything in it.
3. Create an App Service: Publish = Code, Runtime = Java 21, Java SE, Linux, Free/B1 plan.
4. App Service > Settings > Environment variables (app settings):
   `APP_CORS_ALLOWED_ORIGINS` = your Static Web App URL (set after step 6).
5. Download the publish profile (App Service > Overview > Get publish profile), add it as GitHub secret
   `AZURE_WEBAPP_PUBLISH_PROFILE`, set `AZURE_WEBAPP_NAME` in the workflow, push to `main`.
6. Put your App Service URL in `web/src/environments/environment.ts`, then create a Static Web App
   (Deployment source GitHub, app location `web`, output location `dist/web/browser`). Azure adds its own workflow.
7. Check `https://<app>.azurewebsites.net/actuator/health`, then open the Static Web App URL.
8. When done: delete the resource group.

Note: H2 is in-memory, so data resets on every restart. That is fine for the exercise. Say so in an interview.

## Be able to explain
- **Beans / DI**: Spring creates objects (beans) for classes annotated `@Service`, `@RestController`, etc. and passes them
  into constructors. `ItemController` never calls `new ItemService(...)`; the container wires it. Constructor injection
  makes dependencies explicit and easy to mock in tests.
- **@Transactional**: wraps the method in a database transaction (via a proxy). Commit on return, rollback on a runtime
  exception. In `ItemService.update` there is no `save()` call: the entity is managed, so changes are flushed at commit
  (dirty checking). Class-level `readOnly = true` with write methods overriding it.
- **What JPA does**: maps `Item` to the `items` table, generates SQL for `findAll`, `save`, derived queries like
  `existsBySku`, tracks changes to managed entities, and Spring Data generates the repository implementation from the interface.
- **Request path**: HTTP request > DispatcherServlet > (CORS) > `ItemController` (JSON bound to `ItemRequest`,
  `@Valid` runs Bean Validation) > `ItemService` (logic + transaction) > `ItemRepository` > Hibernate > H2.
  Response goes back as JSON. Exceptions are caught by `GlobalExceptionHandler` and become the error JSON.
- **Why DTOs**: the API contract is separate from the DB entity; clients can't set `id` and you can change the schema freely.
- **Why `open-in-view=false`**: no lazy DB access during view rendering; the service layer owns the transaction.
- **CORS**: the browser blocks the Angular origin calling a different origin unless the API allows it. The allowed origin is an
  app setting, not code.

## Azure vocabulary
Resource group (container you delete as a unit) / App Service plan (the compute: size and price tier that apps run on) /
app settings (environment variables injected into the app, where config and secrets belong) / managed identity (Azure-issued
identity for your app so it can reach other services without stored passwords) / Application Insights (monitoring: requests,
failures, traces) / Cosmos DB (managed NoSQL database) / Service Bus (message queues and topics for reliable async messaging) /
Event Grid (event routing: "something happened" notifications to subscribers).
