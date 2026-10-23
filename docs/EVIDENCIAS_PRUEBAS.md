# Evidencias de Pruebas — Producto ATM

Este documento reúne la evidencia de las pruebas realizadas sobre los dos proyectos del repositorio: el microservicio (`atm-service`) y la aplicación de consola (`atm-console`).

Entorno de prueba: Windows 11, JDK 25 (compilación con `--release 8`), Maven 3.9.16, Spring Boot 2.7.18, Node.js 24.

## 1. Pruebas automatizadas (`atm-service`)

Comando ejecutado desde la raíz del repositorio:

```bash
mvn test
```

Resultado (6 tests, 0 fallos, 0 errores):

```
[INFO] Running com.link.atm.service.AtmServiceImplTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.228 s -- in com.link.atm.service.AtmServiceImplTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

`AtmServiceImplTest` prueba la capa de servicio de forma aislada (JUnit 5 + Mockito): los repositorios y el `TransactionLogger` son mocks, por lo que cada test controla exactamente el estado de la tarjeta y la cuenta.

| Test | Qué verifica |
|---|---|
| `login_withActiveCard_logsSuccess` | Login con tarjeta activa: no lanza excepción y registra la operación como `OK` en el log transaccional |
| `login_withInactiveCard_throwsAndLogsError` | Login con tarjeta inactiva: lanza `InactiveCardException` y registra la operación como `ERROR` |
| `withdraw_withEnoughBalance_returnsResultingBalance` | Extracción con saldo suficiente: devuelve el saldo resultante |
| `withdraw_withInsufficientBalance_throws` | Extracción por encima del saldo: lanza `InsufficientBalanceException` (el saldo nunca queda negativo) |
| `deposit_intoExistingCbu_returnsResultingBalance` | Depósito a un CBU existente: acredita y devuelve el saldo resultante |
| `getBalance_ofAccountNotLinkedToCard_throws` | Consulta de saldo de una cuenta no vinculada a la tarjeta: lanza `AccountNotLinkedException` sin acceder a la cuenta |

## 2. Pruebas manuales end-to-end del microservicio

Se empaquetó el jar ejecutable y se lo corrió como proceso real, para validar el comportamiento completo incluyendo el servidor HTTP embebido y la escritura real del log transaccional:

```bash
mvn -DskipTests package
java -jar atm-service/target/atm-service.jar
```

Se ejecutaron llamadas HTTP reales con `curl` contra el servicio levantado, cubriendo los casos de éxito y de error de las 4 operaciones más los casos transversales (JSON inválido, parámetro faltante, método no soportado):

| # | Caso | Request | HTTP | Response |
|---|---|---|---|---|
| 1 | Health check | `GET /api/health` | 200 | `{"success":true,"message":"atm-service UP","balance":null}` |
| 2 | Login exitoso | `POST /api/login {"cardNumber":"4111111111111111"}` | 200 | `{"success":true,"message":"Ingreso exitoso","balance":null}` |
| 3 | Login — tarjeta inactiva | `POST /api/login {"cardNumber":"4222222222222222"}` | 403 | `{"success":false,"message":"Ingreso no exitoso","balance":null}` |
| 4 | Login — tarjeta inexistente | `POST /api/login {"cardNumber":"9999999999999999"}` | 404 | `{"success":false,"message":"Ingreso no exitoso","balance":null}` |
| 5 | Saldo antes de operar | `GET /api/balance?cardNumber=4111111111111111&accountNumber=ACC-0001` | 200 | `{"success":true,"message":"Consulta de saldo exitosa","balance":50000.00}` |
| 6 | Extracción exitosa ($500) | `POST /api/withdraw {"cardNumber":"4111111111111111","accountNumber":"ACC-0001","amount":500.00}` | 200 | `{"success":true,"message":"Retire su dinero","balance":49500.00}` |
| 7 | Extracción — saldo insuficiente | `POST /api/withdraw {..., "amount":999999999}` | 409 | `{"success":false,"message":"Saldo insuficiente para realizar la extraccion","balance":null}` |
| 8 | Extracción — cuenta no vinculada a la tarjeta | `POST /api/withdraw {"cardNumber":"4111111111111111","accountNumber":"ACC-0002","amount":10}` | 403 | `{"success":false,"message":"La cuenta no pertenece a la tarjeta indicada","balance":null}` |
| 9 | Extracción — importe inválido (0) | `POST /api/withdraw {..., "amount":0}` | 400 | `{"success":false,"message":"Importe invalido: debe ser mayor a cero","balance":null}` |
| 10 | Depósito a cuenta ajena (por CBU) | `POST /api/deposit {"cardNumber":"4111111111111111","cbu":"0720001288000009999999","amount":0.01}` | 200 | `{"success":true,"message":"Deposito exitoso","balance":100000.00}` |
| 11 | Depósito — CBU inexistente | `POST /api/deposit {..., "cbu":"000000"}` | 404 | `{"success":false,"message":"La cuenta no existe","balance":null}` |
| 12 | Depósito — tarjeta inactiva | `POST /api/deposit {"cardNumber":"4222222222222222", ...}` | 403 | `{"success":false,"message":"La tarjeta se encuentra inactiva","balance":null}` |
| 13 | Saldo después de operar | `GET /api/balance?cardNumber=4111111111111111&accountNumber=ACC-0001` | 200 | `{"success":true,"message":"Consulta de saldo exitosa","balance":49500.00}` |
| 14 | Saldo — cuenta no vinculada | `GET /api/balance?cardNumber=4111111111111111&accountNumber=ACC-0002` | 403 | `{"success":false,"message":"La cuenta no pertenece a la tarjeta indicada","balance":null}` |
| 15 | Saldo — falta parámetro obligatorio | `GET /api/balance?cardNumber=4111111111111111` | 400 | `{"success":false,"message":"Falta el parametro obligatorio: accountNumber","balance":null}` |
| 16 | JSON malformado | `POST /api/login` con cuerpo `{not-json` | 400 | `{"success":false,"message":"El cuerpo de la solicitud no es un JSON valido","balance":null}` |
| 17 | Método HTTP no soportado | `GET /api/login` (el endpoint solo acepta POST) | 405 | *(sin cuerpo, manejado por Spring MVC)* |

Nota sobre el caso 13 vs 6/7: el saldo pasó de 50000.00 a 49500.00 por la extracción exitosa del caso 6; el intento fallido del caso 7 (saldo insuficiente) no modificó el saldo, lo cual confirma que la validación ocurre antes de aplicar el débito.

## 3. Pruebas manuales end-to-end de la aplicación de consola

Con el microservicio recién levantado (`java -jar atm-service/target/atm-service.jar`, dataset inicial), se ejecutaron los siguientes comandos desde la raíz del repositorio con `node atm-console/bin/atm.js` (equivalente a `atm` luego de `npm link`). Salida real capturada en esta corrida:

| # | Comando | Salida en consola | Código de salida |
|---|---|---|---|
| 1 | `atm login 4111111111111111` | `Ingreso exitoso` | 0 |
| 2 | `atm login 4222222222222222` (tarjeta inactiva) | `Ingreso no exitoso` | 1 |
| 3 | `atm login 9999999999999999` (tarjeta inexistente) | `Ingreso no exitoso` | 1 |
| 4 | `atm saldo 4111111111111111 ACC-0001` | `Su saldo es $ 50000,00` | 0 |
| 5 | `atm extraer 4111111111111111 ACC-0001 500` | `Retire su dinero` | 0 |
| 6 | `atm extraer 4111111111111111 ACC-0001 999999999` | `Error: Saldo insuficiente para realizar la extraccion` | 1 |
| 7 | `atm extraer 4111111111111111 ACC-0002 10` | `Error: La cuenta no pertenece a la tarjeta indicada` | 1 |
| 8 | `atm extraer 4111111111111111 ACC-0001 abc` | `Error: Importe invalido: 'abc'. Use un numero positivo con hasta 2 decimales (ej. 1500 o 1500,50)`<br>`Uso: atm extraer <tarjeta> <cuenta> <importe>` | 2 |
| 9 | `atm depositar 4111111111111111 0720001288000009999999 100,50` (cuenta ajena) | `Depósito exitoso` | 0 |
| 10 | `atm depositar 4111111111111111 000000 1` | `Error: La cuenta no existe` | 1 |
| 11 | `atm depositar 4222222222222222 0720001288000009999999 1` | `Error: La tarjeta se encuentra inactiva` | 1 |
| 12 | `atm saldo 4111111111111111 ACC-0001` | `Su saldo es $ 49500,00` | 0 |
| 13 | `atm saldo 4111111111111111 ACC-0002` | `Error: La cuenta no pertenece a la tarjeta indicada` | 1 |
| 14 | `atm extraer 4111111111111111` (faltan parámetros) | `Error: cantidad de parametros incorrecta`<br>`Uso: atm extraer <tarjeta> <cuenta> <importe>` | 2 |
| 15 | `atm login 4111111111111111` con el servicio detenido | `Error: No fue posible comunicarse con el servicio ATM en http://localhost:8080. Verifique que el servicio este en ejecucion.` | 3 |

Puntos verificados:

- Los 4 mensajes exigidos por el enunciado (`Ingreso exitoso`/`Ingreso no exitoso`, `Retire su dinero`, `Depósito exitoso`, `Su saldo es $ 99999,99`) se muestran tal como se piden.
- El saldo del caso 12 refleja la extracción del caso 5 (50000,00 → 49500,00); el intento fallido del caso 6 no lo modificó.
- El depósito con coma decimal (`100,50`, caso 9) se interpretó correctamente como 100.50.
- Los casos 8 y 14 se rechazan en la consola, antes de llamar al servicio: no aparecen en el log transaccional (sección 4).

## 4. Evidencia del log transaccional

Contenido real del log transaccional generado por la corrida de la sección 3 (una línea por cada operación que llegó al backend):

```
2026-10-23T14:15:33.7791349-03:00|LOGIN|card=************1111|account=-|amount=-|result=OK|detail=Ingreso exitoso|resultingBalance=-
2026-10-23T14:15:33.9700301-03:00|LOGIN|card=************2222|account=-|amount=-|result=ERROR|detail=La tarjeta se encuentra inactiva|resultingBalance=-
2026-10-23T14:15:34.1489364-03:00|LOGIN|card=************9999|account=-|amount=-|result=ERROR|detail=La tarjeta no existe|resultingBalance=-
2026-10-23T14:15:34.3277131-03:00|GET_BALANCE|card=************1111|account=ACC-0001|amount=-|result=OK|detail=Consulta de saldo exitosa|resultingBalance=50000.00
2026-10-23T14:15:34.5172592-03:00|WITHDRAW|card=************1111|account=ACC-0001|amount=500|result=OK|detail=Retire su dinero|resultingBalance=49500.00
2026-10-23T14:15:34.7022176-03:00|WITHDRAW|card=************1111|account=ACC-0001|amount=999999999|result=ERROR|detail=Saldo insuficiente para realizar la extraccion|resultingBalance=-
2026-10-23T14:15:34.8886172-03:00|WITHDRAW|card=************1111|account=ACC-0002|amount=10|result=ERROR|detail=La cuenta no pertenece a la tarjeta indicada|resultingBalance=-
2026-10-23T14:15:35.2208019-03:00|DEPOSIT|card=************1111|account=0720001288000009999999|amount=100.5|result=OK|detail=Deposito exitoso|resultingBalance=100100.49
2026-10-23T14:15:35.4033754-03:00|DEPOSIT|card=************1111|account=000000|amount=1|result=ERROR|detail=La cuenta no existe|resultingBalance=-
2026-10-23T14:15:35.5906331-03:00|DEPOSIT|card=************2222|account=0720001288000009999999|amount=1|result=ERROR|detail=La tarjeta se encuentra inactiva|resultingBalance=-
2026-10-23T14:15:35.771818-03:00|GET_BALANCE|card=************1111|account=ACC-0001|amount=-|result=OK|detail=Consulta de saldo exitosa|resultingBalance=49500.00
2026-10-23T14:15:35.953468-03:00|GET_BALANCE|card=************1111|account=ACC-0002|amount=-|result=ERROR|detail=La cuenta no pertenece a la tarjeta indicada|resultingBalance=-
```

Puntos verificados con esta evidencia:

- **Trazabilidad completa**: cada operación de negocio (éxito o error) deja exactamente una línea, incluso cuando el mensaje mostrado en consola es genérico (login, casos 2/3: el log distingue "tarjeta inactiva" de "tarjeta no existe", aunque la consola muestre "Ingreso no exitoso" en ambos).
- **Enmascarado de tarjeta**: todas las líneas muestran `card=************xxxx`, nunca el número completo.
- **Consistencia de saldo**: el depósito de 100,50 sobre `ACC-0003` (saldo inicial 99999.99) dejó un saldo de 100100.49, y la extracción de 500 sobre `ACC-0001` dejó 49500.00, coincidiendo con lo informado por la consola.
- **Validación en la consola**: los casos 8 y 14 de la sección 3 (importe inválido y parámetros faltantes) no generaron línea, porque se rechazaron antes de llamar al servicio.
