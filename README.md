# Transaction Service

Servicio web RESTful que almacena transacciones en memoria y devuelve información sobre ellas.
Las transacciones tienen un tipo, un monto y pueden vincularse entre sí mediante `parent_id`.

## Stack tecnológico

- Java 17
- Spring Boot 3.4.4
- Maven 3.9.x (con Maven Wrapper)
- Lombok
- SpringDoc OpenAPI (Swagger UI)
- Docker (multi-stage build)
- JUnit 5 + MockMvc

## Build y ejecución

### Con Maven (local)

```bash
./mvnw clean package
java -jar target/transaction-service-1.0.0.jar
```

### Con Docker

```bash
docker build -t transaction-service .
docker run -p 8080:8080 transaction-service
```

### Con Docker Compose

```bash
docker-compose up --build
```

La aplicación estará disponible en `http://localhost:8080`.

## Endpoints

### PUT /transactions/{transaction_id}

Crea una nueva transacción.

```
PUT /transactions/10
Content-Type: application/json

{ "amount": 5000, "type": "cars" }
```

Respuesta: `{ "status": "ok" }`

### GET /transactions/types/{type}

Devuelve los IDs de todas las transacciones del tipo indicado.

```
GET /transactions/types/cars
```

Respuesta: `[10]`

### GET /transactions/sum/{transaction_id}

Devuelve la suma transitiva de la transacción y todas sus hijas vinculadas por `parent_id`.

```
GET /transactions/sum/10
```

Respuesta: `{ "sum": 20000.0 }`

## Documentación interactiva

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Tests

```bash
./mvnw test
```

Incluye tests unitarios (repositorio y servicio) y tests de integración (flujo completo con MockMvc).
