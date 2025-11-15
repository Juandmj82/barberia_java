# Guía de Pruebas API - Barbería Backend

## Configuración Inicial

**Base URL:** `http://localhost:8080`

### Variables de Entorno en Postman
Crea las siguientes variables en Postman:
- `baseUrl`: `http://localhost:8080`
- `token`: (se actualizará automáticamente después del login)
- `clientId`: (ID del cliente creado)
- `barberId`: (ID del barbero)
- `serviceId`: (ID del servicio)
- `appointmentId`: (ID de la cita)

---

## 1. AUTENTICACIÓN (`/auth`)

### 1.1 Registrar Cliente
**POST** `{{baseUrl}}/auth/signup`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "username": "juan_cliente",
  "email": "juan@email.com",
  "password": "Password123!",
  "firstName": "Juan",
  "lastName": "Pérez",
  "phoneNumber": "+34612345678"
}
```

**Respuesta Exitosa (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "juan_cliente",
  "email": "juan@email.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "role": "CLIENT",
  "expiresIn": 86400000,
  "message": "Cliente registrado exitosamente"
}
```

**Script Post-Response (Tests):**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    pm.environment.set("clientId", jsonData.id);
}
```

---

### 1.2 Iniciar Sesión
**POST** `{{baseUrl}}/auth/signin`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "username": "juan_cliente",
  "password": "Password123!"
}
```

**Respuesta Exitosa (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "juan_cliente",
  "email": "juan@email.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "role": "CLIENT",
  "expiresIn": 86400000,
  "message": "Login exitoso"
}
```

**Script Post-Response (Tests):**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
}
```

---

## 2. SERVICIOS (`/services`)

### 2.1 Obtener Todos los Servicios (Público)
**GET** `{{baseUrl}}/services`

**Headers:**
```
(No requiere autenticación)
```

**Respuesta Exitosa (200):**
```json
[
  {
    "id": 1,
    "name": "Corte Clásico",
    "description": "Corte de cabello tradicional",
    "duration": 30,
    "price": 25.00,
    "active": true
  }
]
```

---

### 2.2 Obtener Servicio por ID
**GET** `{{baseUrl}}/services/1`

**Respuesta Exitosa (200):**
```json
{
  "id": 1,
  "name": "Corte Clásico",
  "description": "Corte de cabello tradicional",
  "duration": 30,
  "price": 25.00,
  "active": true
}
```

---

### 2.3 Crear Servicio (Solo ADMIN)
**POST** `{{baseUrl}}/services`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "name": "Corte Moderno",
  "description": "Corte de cabello con estilo moderno",
  "duration": 45,
  "price": 35.00
}
```

**Respuesta Exitosa (201):**
```json
{
  "id": 2,
  "name": "Corte Moderno",
  "description": "Corte de cabello con estilo moderno",
  "duration": 45,
  "price": 35.00,
  "active": true
}
```

---

### 2.4 Actualizar Servicio (Solo ADMIN)
**PUT** `{{baseUrl}}/services/1`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "name": "Corte Clásico Premium",
  "description": "Corte de cabello tradicional con acabado premium",
  "duration": 40,
  "price": 30.00
}
```

---

### 2.5 Eliminar Servicio (Solo ADMIN)
**DELETE** `{{baseUrl}}/services/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Respuesta Exitosa (204):** Sin contenido

---

### 2.6 Buscar por Rango de Precio
**GET** `{{baseUrl}}/services/price-range?minPrice=20&maxPrice=40`

**Respuesta Exitosa (200):**
```json
[
  {
    "id": 1,
    "name": "Corte Clásico",
    "price": 25.00
  }
]
```

---

### 2.7 Buscar por Duración Máxima
**GET** `{{baseUrl}}/services/max-duration/45`

---

## 3. BARBEROS (`/barbers`)

### 3.1 Obtener Barberos Disponibles (Público)
**GET** `{{baseUrl}}/barbers`

**Respuesta Exitosa (200):**
```json
[
  {
    "id": 1,
    "userId": 3,
    "firstName": "Carlos",
    "lastName": "Barbero",
    "email": "carlos@barberia.com",
    "phoneNumber": "+34612345679",
    "specialty": "Cortes clásicos",
    "yearsOfExperience": 5,
    "available": true,
    "rating": 4.8
  }
]
```

---

### 3.2 Obtener Todos los Barberos Activos (Solo ADMIN)
**GET** `{{baseUrl}}/barbers/all`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 3.3 Obtener Barbero por ID
**GET** `{{baseUrl}}/barbers/1`

---

### 3.4 Obtener Barbero por User ID (ADMIN/BARBER)
**GET** `{{baseUrl}}/barbers/user/3`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 3.5 Crear Barbero (Solo ADMIN)
**POST** `{{baseUrl}}/barbers`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "userId": 3,
  "specialty": "Cortes modernos y barba",
  "yearsOfExperience": 5,
  "bio": "Especialista en cortes modernos con más de 5 años de experiencia"
}
```

**Respuesta Exitosa (201):**
```json
{
  "id": 1,
  "userId": 3,
  "firstName": "Carlos",
  "lastName": "Barbero",
  "specialty": "Cortes modernos y barba",
  "yearsOfExperience": 5,
  "available": true
}
```

---

### 3.6 Actualizar Barbero (Solo ADMIN)
**PUT** `{{baseUrl}}/barbers/1`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "specialty": "Cortes clásicos y modernos",
  "yearsOfExperience": 6,
  "bio": "Especialista con 6 años de experiencia"
}
```

---

### 3.7 Eliminar Barbero (Solo ADMIN)
**DELETE** `{{baseUrl}}/barbers/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Respuesta Exitosa (204):** Sin contenido

---

### 3.8 Actualizar Disponibilidad (ADMIN/BARBER)
**PATCH** `{{baseUrl}}/barbers/1/availability?available=true`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 3.9 Barberos Disponibles a Cierta Hora
**GET** `{{baseUrl}}/barbers/available-at?time=10:00:00`

---

### 3.10 Barberos por Especialidad
**GET** `{{baseUrl}}/barbers/specialty/Cortes clásicos`

---

### 3.11 Barberos por Experiencia Mínima
**GET** `{{baseUrl}}/barbers/experience/5`

---

### 3.12 Verificar si Existe Barbero por User ID (Solo ADMIN)
**GET** `{{baseUrl}}/barbers/exists/user/3`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

## 4. HORARIOS (`/schedules`)

### 4.1 Obtener Horarios de un Barbero (ADMIN/BARBER propietario)
**GET** `{{baseUrl}}/schedules/barber/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Respuesta Exitosa (200):**
```json
[
  {
    "id": 1,
    "barberId": 1,
    "barberName": "Carlos Barbero",
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "18:00:00",
    "isActive": true
  }
]
```

---

### 4.2 Obtener Horarios Activos de un Barbero (Público)
**GET** `{{baseUrl}}/schedules/barber/1/active`

---

### 4.3 Obtener Horario por ID
**GET** `{{baseUrl}}/schedules/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 4.4 Crear Horario (ADMIN/BARBER)
**POST** `{{baseUrl}}/schedules`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "barberId": 1,
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "18:00:00"
}
```

**Respuesta Exitosa (201):**
```json
{
  "id": 1,
  "barberId": 1,
  "barberName": "Carlos Barbero",
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "18:00:00",
  "isActive": true
}
```

---

### 4.5 Actualizar Horario (ADMIN/BARBER)
**PUT** `{{baseUrl}}/schedules/1`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "08:00:00",
  "endTime": "17:00:00",
  "isActive": true
}
```

---

### 4.6 Eliminar Horario (ADMIN/BARBER)
**DELETE** `{{baseUrl}}/schedules/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 4.7 Barberos Disponibles en Día y Hora (Público)
**GET** `{{baseUrl}}/schedules/available?dayOfWeek=MONDAY&time=10:00:00`

---

### 4.8 Verificar Disponibilidad de Barbero (Público)
**GET** `{{baseUrl}}/schedules/barber/1/available?dayOfWeek=MONDAY&time=10:00:00`

---

### 4.9 Mis Horarios (Solo BARBER)
**GET** `{{baseUrl}}/schedules/my-schedules`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 4.10 Crear Mi Horario (Solo BARBER)
**POST** `{{baseUrl}}/schedules/my-schedules`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "dayOfWeek": "TUESDAY",
  "startTime": "09:00:00",
  "endTime": "18:00:00"
}
```

---

### 4.11 Horarios por Día de la Semana (Público)
**GET** `{{baseUrl}}/schedules/day/MONDAY`

---

## 5. DISPONIBILIDAD (`/availability`)

### 5.1 Obtener Slots Disponibles de un Barbero (Público)
**GET** `{{baseUrl}}/availability/barber/1?date=2024-12-15&duration=30`

**Respuesta Exitosa (200):**
```json
[
  {
    "barberId": 1,
    "barberName": "Carlos Barbero",
    "date": "2024-12-15",
    "startTime": "09:00:00",
    "endTime": "09:30:00",
    "available": true
  },
  {
    "barberId": 1,
    "barberName": "Carlos Barbero",
    "date": "2024-12-15",
    "startTime": "09:30:00",
    "endTime": "10:00:00",
    "available": true
  }
]
```

---

### 5.2 Obtener Barberos Disponibles en Fecha y Hora (Público)
**GET** `{{baseUrl}}/availability/barbers?date=2024-12-15&time=10:00:00&duration=30`

---

### 5.3 Verificar Disponibilidad de Slot Específico (Público)
**GET** `{{baseUrl}}/availability/barber/1/slot?date=2024-12-15&startTime=10:00:00&duration=30`

**Respuesta Exitosa (200):**
```json
true
```

---

### 5.4 Resumen de Disponibilidad por Días (Público)
**GET** `{{baseUrl}}/availability/barber/1/summary?startDate=2024-12-15&endDate=2024-12-20&duration=30`

**Respuesta Exitosa (200):**
```json
[
  {
    "date": "2024-12-15",
    "dayOfWeek": "MONDAY",
    "availableSlots": 16,
    "hasAvailability": true,
    "firstAvailableTime": "09:00:00",
    "lastAvailableTime": "17:30:00"
  }
]
```

---

### 5.5 Próximos Slots Disponibles (Público)
**GET** `{{baseUrl}}/availability/barber/1/next-available?duration=30&limit=5`

---

## 6. CITAS (`/appointments`)

### 6.1 Obtener Todas las Citas (ADMIN/BARBER)
**GET** `{{baseUrl}}/appointments`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Respuesta Exitosa (200):**
```json
[
  {
    "id": 1,
    "clientId": 2,
    "clientName": "Juan Pérez",
    "clientEmail": "juan@email.com",
    "barberId": 3,
    "barberName": "Carlos Barbero",
    "serviceId": 1,
    "serviceName": "Corte Clásico",
    "serviceDuration": 30,
    "startTime": "2024-12-15T10:00:00",
    "endTime": "2024-12-15T10:30:00",
    "status": "PENDING",
    "notes": "Corte corto por favor",
    "totalPrice": 25.00,
    "createdAt": "2024-12-14T15:30:00",
    "updatedAt": "2024-12-14T15:30:00"
  }
]
```

---

### 6.2 Obtener Cita por ID (ADMIN/CLIENT/BARBER)
**GET** `{{baseUrl}}/appointments/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.3 Crear Cita (ADMIN/CLIENT)
**POST** `{{baseUrl}}/appointments`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "clientId": 2,
  "barberId": 1,
  "serviceId": 1,
  "startTime": "2024-12-15T10:00:00",
  "notes": "Corte corto por favor"
}
```

**Respuesta Exitosa (201):**
```json
{
  "id": 1,
  "clientId": 2,
  "clientName": "Juan Pérez",
  "clientEmail": "juan@email.com",
  "barberId": 1,
  "barberName": "Carlos Barbero",
  "serviceId": 1,
  "serviceName": "Corte Clásico",
  "serviceDuration": 30,
  "startTime": "2024-12-15T10:00:00",
  "endTime": "2024-12-15T10:30:00",
  "status": "PENDING",
  "notes": "Corte corto por favor",
  "totalPrice": 25.00,
  "createdAt": "2024-12-14T15:30:00",
  "updatedAt": "2024-12-14T15:30:00"
}
```

**Script Post-Response (Tests):**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("appointmentId", jsonData.id);
}
```

---

### 6.4 Actualizar Cita (ADMIN/CLIENT propietario)
**PUT** `{{baseUrl}}/appointments/1`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (JSON):**
```json
{
  "barberId": 1,
  "serviceId": 1,
  "startTime": "2024-12-15T11:00:00",
  "notes": "Cambio de horario"
}
```

---

### 6.5 Cancelar Cita (ADMIN/CLIENT propietario)
**DELETE** `{{baseUrl}}/appointments/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.6 Cambiar Estado de Cita (ADMIN/BARBER asignado)
**PATCH** `{{baseUrl}}/appointments/1/status?status=CONFIRMED`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Valores válidos para status:**
- `PENDING`
- `CONFIRMED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`
- `NO_SHOW`

---

### 6.7 Obtener Citas por Cliente (ADMIN/CLIENT propietario)
**GET** `{{baseUrl}}/appointments/client/2`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.8 Obtener Citas por Barbero (ADMIN/BARBER propietario)
**GET** `{{baseUrl}}/appointments/barber/1`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.9 Obtener Citas por Estado (ADMIN/BARBER)
**GET** `{{baseUrl}}/appointments/status/PENDING`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.10 Obtener Citas por Rango de Fechas (ADMIN/BARBER)
**GET** `{{baseUrl}}/appointments/date-range?startDate=2024-12-15T00:00:00&endDate=2024-12-20T23:59:59`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.11 Mis Citas (CLIENT)
**GET** `{{baseUrl}}/appointments/my-appointments`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.12 Citas de Hoy (ADMIN/BARBER)
**GET** `{{baseUrl}}/appointments/today`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 6.13 Próximas Citas (ADMIN/BARBER)
**GET** `{{baseUrl}}/appointments/upcoming`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

## 7. HEALTH CHECK (`/health`)

### 7.1 Verificar Estado del Servicio (Público)
**GET** `{{baseUrl}}/health`

**Respuesta Exitosa (200):**
```json
{
  "status": "UP",
  "timestamp": "2024-12-14T15:30:00",
  "service": "Barbería Backend",
  "version": "1.0.0"
}
```

---

## 8. TEST ENDPOINTS (`/test`)

### 8.1 Endpoint Público
**GET** `{{baseUrl}}/test/public`

**Respuesta Exitosa (200):**
```json
{
  "message": "Este es un endpoint público",
  "status": "success"
}
```

---

### 8.2 Endpoint Protegido
**GET** `{{baseUrl}}/test/protected`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Respuesta Exitosa (200):**
```json
{
  "message": "Este es un endpoint protegido",
  "user": "juan_cliente",
  "authorities": ["ROLE_CLIENT"],
  "status": "success"
}
```

---

### 8.3 Endpoint Solo Admin
**GET** `{{baseUrl}}/test/admin`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

### 8.4 Endpoint Admin/Barber
**GET** `{{baseUrl}}/test/barber`

**Headers:**
```
Authorization: Bearer {{token}}
```

---

## CÓDIGOS DE RESPUESTA HTTP

- **200 OK**: Solicitud exitosa
- **201 Created**: Recurso creado exitosamente
- **204 No Content**: Operación exitosa sin contenido de respuesta
- **400 Bad Request**: Error de validación en los datos
- **401 Unauthorized**: Token JWT inválido o expirado
- **403 Forbidden**: Acceso denegado por permisos insuficientes
- **404 Not Found**: Recurso no encontrado
- **409 Conflict**: Conflicto (ej: usuario duplicado, horario ocupado)
- **500 Internal Server Error**: Error interno del servidor

---

## ROLES Y PERMISOS

### CLIENT
- Registrarse y hacer login
- Ver servicios y barberos
- Ver disponibilidad
- Crear y gestionar sus propias citas
- Ver sus propias citas

### BARBER
- Hacer login
- Ver todas las citas
- Ver citas asignadas a él
- Gestionar sus horarios
- Cambiar estado de sus citas
- Actualizar su disponibilidad

### ADMIN
- Todos los permisos de BARBER
- Crear/editar/eliminar servicios
- Crear/editar/eliminar barberos
- Ver todas las citas de todos los barberos
- Gestionar horarios de todos los barberos
- Crear citas para cualquier cliente

---

## FLUJO DE PRUEBA RECOMENDADO

1. **Registrar Cliente** → Guardar token
2. **Crear Servicio** (como ADMIN) → Guardar serviceId
3. **Crear Barbero** (como ADMIN) → Guardar barberId
4. **Crear Horario para Barbero** → Configurar disponibilidad
5. **Consultar Disponibilidad** → Ver slots disponibles
6. **Crear Cita** (como CLIENT) → Guardar appointmentId
7. **Confirmar Cita** (como BARBER/ADMIN)
8. **Ver Mis Citas** (como CLIENT)
9. **Cancelar Cita** (si es necesario)

---

## NOTAS IMPORTANTES

1. **Autenticación**: La mayoría de endpoints requieren el header `Authorization: Bearer {token}`
2. **Formato de Fechas**: 
   - Fecha: `YYYY-MM-DD` (ej: `2024-12-15`)
   - Hora: `HH:mm:ss` (ej: `10:00:00`)
   - DateTime: `YYYY-MM-DDTHH:mm:ss` (ej: `2024-12-15T10:00:00`)
3. **Días de la Semana**: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`
4. **Estados de Cita**: `PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `NO_SHOW`
5. **Validaciones**: Todos los campos marcados como obligatorios deben incluirse en las peticiones

---

## SWAGGER UI

También puedes acceder a la documentación interactiva en:
`http://localhost:8080/swagger-ui.html`
