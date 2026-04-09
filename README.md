# Transaction Service

Servicio web RESTful que almacena transacciones en memoria y devuelve información sobre ellas.
Las transacciones tienen un tipo, un monto y pueden vincularse entre sí mediante `parent_id`.

## Stack tecnológico

- Java 17
- Spring Boot 3.4.4
- Maven 3.9.x (con Maven Wrapper)
- Lombok
- Bean Validation (Hibernate Validator)
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

Crea una nueva transacción. El monto utiliza `BigDecimal` para precisión exacta y admite como máximo 2 decimales.

```
PUT /transactions/10
Content-Type: application/json

{ "amount": 5000.50, "type": "cars" }
```

Respuesta: `{ "status": "ok" }`

Ejemplo de rechazo por decimales inválidos:

```
PUT /transactions/11
Content-Type: application/json

{ "amount": 5000.123, "type": "cars" }
```

Respuesta (400): `{ "status": 400, "error": "Bad Request", "message": "Amount must have at most 2 decimal places" }`

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

Respuesta: `{ "sum": 20000.00 }`

La suma siempre se devuelve con exactamente 2 decimales.

## Documentación interactiva

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Tests

```bash
./mvnw test
```

Incluye tests unitarios (repositorio y servicio) y tests de integración (flujo completo con MockMvc).

## Decisiones técnicas

### BigDecimal para montos

Se utiliza `BigDecimal` en lugar de `Double` para representar montos monetarios, evitando errores de precisión de punto flotante (ej: `0.1 + 0.2 != 0.3`). Los montos se almacenan y devuelven siempre con escala 2.

### Validación de entrada

La validación de campos se realiza de forma declarativa con Bean Validation (`@NotNull`, `@NotBlank`, `@Digits`) en el DTO de request, manteniendo el servicio enfocado exclusivamente en reglas de negocio.
