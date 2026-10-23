# Challenge JAVA – Producto ATM

Repositorio del Challenge JAVA (Producto ATM) de LINK. Contiene dos proyectos
separados:

- **`atm-service`**: microservicio de backend (Java 8 + Spring Boot 2.7) que
  expone las operaciones de login, extraccion, deposito y consulta de saldo
  por HTTP/JSON, con persistencia en memoria y log transaccional en archivo.
- **`atm-console`**: aplicacion de escritorio en modo consola (Node.js) que
  ejecuta las 4 operaciones por linea de comando consumiendo el microservicio
  por HTTP.

## Estructura del repositorio

```
link-atm-challenge/
├── pom.xml                  # POM padre (Maven multi-modulo, importa el BOM de Spring Boot)
├── atm-service/             # Microservicio de backend (Java 8 + Spring Boot 2.7)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/link/atm/
│       │   ├── AtmServiceApplication.java   # @SpringBootApplication (main)
│       │   ├── model/        # Entidades: Card, Account, CardAccount
│       │   ├── repository/   # Acceso a datos (impl en memoria, hardcodeada, @Repository)
│       │   ├── service/      # Logica de negocio (login, withdraw, deposit, getBalance), @Service
│       │   ├── controller/   # @RestController + @ControllerAdvice (manejo centralizado de errores)
│       │   ├── dto/          # Objetos de request/response JSON
│       │   ├── exception/    # Excepciones de negocio, mapeadas a codigo HTTP
│       │   ├── logging/      # Log transaccional en archivo de texto, @Component
│       │   └── util/         # Utilidades (enmascarado de tarjeta, etc.)
│       ├── main/resources/   # application.properties, logback-spring.xml
│       └── test/java/com/link/atm/service/   # Tests unitarios (JUnit 5 + Mockito)
├── atm-console/             # Aplicacion de consola (Node.js, sin dependencias externas)
│   ├── package.json
│   ├── bin/atm.js           # Punto de entrada (comando "atm")
│   └── src/
│       ├── cli.js           # Comandos, validacion de parametros y mensajes de consola
│       ├── atmClient.js     # Cliente HTTP del microservicio (fetch nativo)
│       ├── money.js         # Parseo del importe y formato "$ 99999,99"
│       ├── config.js        # URL del servicio y timeout (variables de entorno)
│       └── exitCode.js      # Codigos de salida del proceso
└── docs/
    └── EVIDENCIAS_PRUEBAS.md
```

## Documentacion

- Este README: instalacion, ejecucion y pruebas, decisiones de diseño,
  cumplimiento de los requisitos y concesiones adoptadas.
- [`docs/EVIDENCIAS_PRUEBAS.md`](docs/EVIDENCIAS_PRUEBAS.md): evidencia de las pruebas automatizadas y manuales (end-to-end) realizadas para validar el proyecto.

## Requisitos

- **atm-service**: JDK 8 o superior y Maven 3.6+. El proyecto compila con
  `--release 8`, por lo que el bytecode generado es compatible con Java 8
  aunque se use un JDK mas nuevo para compilar.
- **atm-console**: Node.js 18 o superior. No tiene dependencias externas
  (usa `fetch` nativo), por lo que no hace falta `npm install`.

No se requieren bases de datos ni servicios externos: los datos se
persisten en memoria, con un dataset de prueba hardcodeado al arrancar.

## 1. Microservicio (`atm-service`)

### Compilar y correr los tests

Desde la raiz del repositorio:

```bash
mvn test
```

Corre los tests unitarios de la capa de servicio (`AtmServiceImplTest`,
JUnit 5 + Mockito, 6 casos).

### Ejecutar

Modo desarrollo, con el plugin de Spring Boot:

```bash
mvn -pl atm-service spring-boot:run
```

O empaquetando el jar ejecutable (incluye el servidor Tomcat embebido):

```bash
mvn -DskipTests package
java -jar atm-service/target/atm-service.jar
```

El servicio levanta en `http://localhost:8080` por defecto. Al arrancar,
imprime en consola los endpoints disponibles y la ruta del log
transaccional.

### Configuracion

`atm-service/src/main/resources/application.properties` toma sus valores de
variables de entorno, con un valor por defecto si no estan definidas:

```properties
server.port=${PORT:8080}
atm.transaction-log.path=${ATM_TX_LOG_PATH:logs/transactions.log}
```

| Variable de entorno | Propiedad                  | Default                 | Descripcion                                                              |
|---------------------|----------------------------|-------------------------|--------------------------------------------------------------------------|
| `PORT`              | `server.port`              | `8080`                  | Puerto HTTP del servicio                                                 |
| `ATM_TX_LOG_PATH`   | `atm.transaction-log.path` | `logs/transactions.log` | Ruta del log transaccional (relativa al directorio desde donde se ejecuta) |

Ejemplo, levantando en el puerto 9090 con el log en otra ruta:

```bash
# bash / Git Bash
PORT=9090 ATM_TX_LOG_PATH=/tmp/atm/transactions.log java -jar atm-service/target/atm-service.jar
```

```powershell
# PowerShell
$env:PORT = "9090"; $env:ATM_TX_LOG_PATH = "C:\atm\transactions.log"
java -jar atm-service/target/atm-service.jar
```

```bat
:: cmd
set PORT=9090
set ATM_TX_LOG_PATH=C:\atm\transactions.log
java -jar atm-service/target/atm-service.jar
```

Tambien se pueden pasar las propiedades directamente como argumentos de
Spring Boot, sin variables de entorno:

```bash
java -jar atm-service/target/atm-service.jar --server.port=9090 --atm.transaction-log.path=C:/atm/transactions.log
mvn -pl atm-service spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

Si se cambia el puerto, la consola debe apuntar al mismo con
`ATM_SERVICE_URL` (ver seccion 2), por ejemplo
`ATM_SERVICE_URL=http://localhost:9090`.

## 2. Aplicacion de consola (`atm-console`)

Con el microservicio levantado, desde la raiz del repositorio:

```bash
node atm-console/bin/atm.js login <tarjeta>
node atm-console/bin/atm.js extraer <tarjeta> <cuenta> <importe>
node atm-console/bin/atm.js depositar <tarjeta> <cbu> <importe>
node atm-console/bin/atm.js saldo <tarjeta> <cuenta>
```

Opcionalmente, para usarla como comando `atm` (el `nombre_aplicación` del
enunciado):

```bash
cd atm-console
npm link
atm login 4111111111111111
```

Ejemplos con el dataset de prueba:

```bash
atm login 4111111111111111                                  # Ingreso exitoso
atm login 4222222222222222                                  # Ingreso no exitoso
atm extraer 4111111111111111 ACC-0001 500                   # Retire su dinero
atm depositar 4111111111111111 0720001288000009999999 100,50 # Depósito exitoso
atm saldo 4111111111111111 ACC-0001                         # Su saldo es $ 49500,00
atm ayuda                                                   # Modo de uso
```

- **Importe**: numero positivo con hasta 2 decimales, con `.` o `,` como
  separador decimal (`1500`, `1500.50`, `1500,50`).
- **Mensajes**: los casos exitosos se muestran por salida estandar; los
  errores (rechazo del servicio, parametros invalidos, servicio caido) por
  salida de error, con el motivo informado por el servicio.
- **Codigos de salida**: `0` operacion exitosa, `1` operacion rechazada por
  el servicio, `2` uso incorrecto (comando, cantidad de parametros o importe
  invalido), `3` servicio no disponible, `4` configuracion invalida.

Configuracion por variables de entorno:

| Variable          | Default                 | Descripcion                            |
|-------------------|-------------------------|----------------------------------------|
| `ATM_SERVICE_URL` | `http://localhost:8080` | URL base del microservicio             |
| `ATM_TIMEOUT_MS`  | `10000`                 | Timeout de cada llamada HTTP (en ms)   |

## Dataset de prueba (hardcodeado)

| Tarjeta             | Activa | Cuenta vinculada | CBU                      | Saldo inicial |
|---------------------|--------|-------------------|---------------------------|----------------|
| 4111111111111111    | Si     | ACC-0001          | 0720001288000001234567    | 50000.00       |
| 4333333333333333    | Si     | ACC-0002          | 0720001288000007654321    | 1000.50        |
| 4222222222222222    | No     | -                 | -                          | -              |
| -                    | -      | ACC-0003          | 0720001288000009999999    | 99999.99       |

`ACC-0003` no tiene tarjeta vinculada a proposito, para poder probar el
deposito a una cuenta que no pertenece al dueno de la tarjeta (caso
habilitado explicitamente por el enunciado).

## Endpoints del microservicio

Todas las respuestas tienen la forma:

```json
{ "success": true, "message": "...", "balance": 12345.67 }
```

(`balance` solo se completa en las operaciones que corresponda; en error,
`success` es `false` y `message` describe la causa. Los mensajes de negocio
se mantienen en espanol, tal como los pide el enunciado para la consola).

### Login

```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"4111111111111111"}'
```

### Extraer (withdraw)

```bash
curl -X POST http://localhost:8080/api/withdraw \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"4111111111111111","accountNumber":"ACC-0001","amount":500.00}'
```

### Depositar (deposit)

```bash
curl -X POST http://localhost:8080/api/deposit \
  -H "Content-Type: application/json" \
  -d '{"cardNumber":"4111111111111111","cbu":"0720001288000009999999","amount":100.00}'
```

### Consultar saldo (balance)

```bash
curl "http://localhost:8080/api/balance?cardNumber=4111111111111111&accountNumber=ACC-0001"
```

### Health check

```bash
curl http://localhost:8080/api/health
```

## Decisiones de diseño

- **Dos proyectos independientes**: `atm-service` y `atm-console` se
  instalan y ejecutan por separado; la unica dependencia entre ellos es el
  contrato HTTP/JSON.
- **Arquitectura en capas en el backend**: `controller` → `service` →
  `repository` → `model`. Cada capa depende solo de la interfaz de la capa
  inferior, de modo que la persistencia en memoria se pueda reemplazar por
  una base de datos real sin tocar el controller ni la logica de negocio.
- **Spring Boot 2.7.x (no 3.x)**: es la ultima serie de Spring Boot que
  todavia corre sobre Java 8 (3.x requiere Java 17+), que es el requisito
  tecnico del challenge. El POM padre importa `spring-boot-dependencies`
  como BOM en lugar de heredar de `spring-boot-starter-parent`, para
  conservar la libertad de un `pom` padre propio en un repo multi-modulo.
- **`@RestController` + `@ControllerAdvice`**: `AtmController` solo resuelve
  el caso feliz de cada operacion; `GlobalExceptionHandler` centraliza el
  mapeo de errores (`AtmException` -> su HTTP status sugerido, JSON
  malformado y parametros faltantes -> 400, error inesperado -> 500) en una
  unica forma de respuesta (`ApiResponse`), sin depender del cuerpo de error
  por defecto de Spring.
- **BigDecimal para montos**: evita errores de precision de punto flotante
  en operaciones monetarias.
- **Saldo nunca negativo**: `Account.debit` valida el saldo resultante antes
  de aplicar el debito; las operaciones de debito/credito son
  `synchronized` por cuenta para soportar llamadas concurrentes sin
  corromper el saldo (requisito no funcional de servicio critico).
- **Withdraw / getBalance** validan que la tarjeta este vinculada a la
  cuenta indicada (`CardAccountRepository`); **deposit** no aplica esa
  regla, ya que el enunciado habilita depositar en una cuenta que no
  pertenece al dueno de la tarjeta.
- **Log transaccional sobre Logback**: cada operacion (exitosa o fallida) se
  registra en `logs/transactions.log`, en formato delimitado por `|`, con
  timestamp ISO-8601, operacion, tarjeta enmascarada (solo se ven los
  ultimos 4 digitos), cuenta/CBU, importe, resultado y saldo resultante.
- **Consola en Node.js, sin dependencias**: un proceso por operacion
  (stateless, igual que el backend), `fetch` nativo para HTTP y codigos de
  salida diferenciados para poder usarla desde scripts. Valida la cantidad
  de parametros y el formato del importe antes de llamar al servicio.
- **Formato de presentacion en la consola**: el servicio devuelve datos
  crudos (`balance` como numero); la consola arma los mensajes que pide el
  enunciado (ej. `"Su saldo es $ 99999,99"`, `"Depósito exitoso"`).
- **Login fallido**: el mensaje se fija siempre en `"Ingreso no exitoso"`,
  sin importar si la causa fue tarjeta inactiva o inexistente; el codigo
  HTTP si distingue el motivo (403 vs 404), y el detalle real queda en el
  log transaccional.

## Cumplimiento de los requisitos

### Funcionales

| Operacion | Consola | Endpoint | Mensaje en consola |
|---|---|---|---|
| Identificarse (tarjeta existente y activa) | `atm login <tarjeta>` | `POST /api/login` | `Ingreso exitoso` / `Ingreso no exitoso` |
| Extraccion (el saldo no puede quedar negativo) | `atm extraer <tarjeta> <cuenta> <importe>` | `POST /api/withdraw` | `Retire su dinero` / `Error: <motivo>` |
| Deposito en el CBU indicado (propio o ajeno) | `atm depositar <tarjeta> <cbu> <importe>` | `POST /api/deposit` | `Depósito exitoso` / `Error: <motivo>` |
| Consulta de saldo | `atm saldo <tarjeta> <cuenta>` | `GET /api/balance` | `Su saldo es $ 99999,99` / `Error: <motivo>` |

### No funcionales

- **Servicio critico**: manejo de errores centralizado (una excepcion no
  prevista responde 500 sin tumbar el proceso), debito/credito
  `synchronized` por cuenta para soportar operaciones concurrentes, y
  timeout en cada llamada de la consola, que informa la caida del servicio
  con un mensaje claro y codigo de salida `3`.
- **Trazabilidad para auditoria**: toda operacion de negocio, exitosa o
  fallida, deja una linea en `logs/transactions.log` con timestamp,
  operacion, tarjeta enmascarada, cuenta/CBU, importe, resultado, detalle y
  saldo resultante. El detalle real del error queda en el log aunque la
  consola muestre un mensaje generico (ej. login fallido).

## Concesiones y supuestos

- **Persistencia en memoria**: permitida por el enunciado; los datos se
  reinician al reiniciar el servicio.
- **El deposito exige tarjeta activa**: el enunciado no lo especifica, pero
  el comando recibe la tarjeta; se asumio que identifica a quien opera el
  cajero, aunque la cuenta destino no sea suya.
- **Sin sesion ni autenticacion**: cada operacion envia la tarjeta y el
  backend la valida de forma independiente (*stateless*); `login` es una
  verificacion de la tarjeta, no un paso previo obligatorio.
- **Sin HTTPS ni bloqueo por intentos fallidos**: no los pide el enunciado.
  La consola acepta una URL `https://` en `ATM_SERVICE_URL` si hiciera falta.
- **Consola en Node.js**: el enunciado pide Java 8 y menciona Node.js/Python
  como alternativa para el servicio web. Se mantuvo el backend en Java 8 y
  la consola se hizo en Node.js por ser un cliente HTTP liviano, sin
  dependencias ni compilacion. Toda la logica de negocio, las validaciones
  de saldo y el log transaccional quedan en el backend Java.
- **Importe como numero JSON**: la consola acepta hasta 2 decimales y el
  backend lo deserializa a `BigDecimal`, donde se hacen todos los calculos.
- **Pruebas automatizadas acotadas**: solo tests unitarios basicos de la
  capa de servicio (JUnit 5 + Mockito); el resto se valido con pruebas
  manuales end-to-end contra el servicio real (ver
  [`docs/EVIDENCIAS_PRUEBAS.md`](docs/EVIDENCIAS_PRUEBAS.md)).
