# GovPay Riconciliazioni API - Configurazione Docker

## Panoramica

L'immagine `linkitaly/govpay-riconciliazioni-api` contiene il jar Spring Boot dell'API, gli script SQL dello schema in `/opt/sql/<dialetto>/` e l'entrypoint `docker/commons/entrypoint.sh`, che traduce le variabili `GOVPAY_*` nelle property Spring Boot.

I driver JDBC non sono inclusi: vanno montati in `/opt/jdbc-drivers` (vedi [jdbc-drivers/README.md](jdbc-drivers/README.md)).

## Costruire l'immagine

```bash
cd docker

# Da una release pubblicata su GitHub (Dockerfile.github, con verifica sha256 del jar)
./build_image.sh -v <versione>

# Da un jar costruito in locale (Dockerfile.daFile); prima: mvn -P jar,dist package
./build_image.sh -l ../target/govpay-riconciliazioni-api.jar -t govpay-riconciliazioni-api:local

# Scansione dell'immagine (vedi SCANSIONE.md)
./scan_image.sh govpay-riconciliazioni-api:local
```

## Avvio

```bash
docker run -d --name govpay-riconciliazioni-api \
  -p 8080:8080 -p 9090:9090 \
  -v /percorso/driver:/opt/jdbc-drivers:ro \
  -e GOVPAY_DB_TYPE=postgresql \
  -e GOVPAY_DB_SERVER=db:5432 \
  -e GOVPAY_DB_NAME=govpay \
  -e GOVPAY_DB_USER=govpay \
  -e GOVPAY_DB_PASSWORD=govpay \
  linkitaly/govpay-riconciliazioni-api:latest
```

## Variabili d'ambiente

### Database (obbligatorie)

| Variabile | Descrizione | Default |
|-----------|-------------|---------|
| `GOVPAY_DB_TYPE` | `postgresql`, `mysql`, `mariadb`, `oracle`, `sqlserver` | - |
| `GOVPAY_DB_SERVER` | `host[:porta]` | porta standard del database |
| `GOVPAY_DB_NAME` | nome del database (service name o SID per Oracle) | - |
| `GOVPAY_DB_USER` | utente | - |
| `GOVPAY_DB_PASSWORD` | password | - |

### Database (opzionali)

| Variabile | Descrizione | Default |
|-----------|-------------|---------|
| `GOVPAY_DS_CONN_PARAM` | parametri aggiunti all'URL JDBC | - |
| `GOVPAY_DS_JDBC_LIBS` | directory dei driver JDBC | `/opt/jdbc-drivers` |
| `GOVPAY_ORACLE_JDBC_URL_TYPE` | `servicename` o `sid` | `servicename` |
| `ORACLE_TNS_ADMIN` | directory dei file TNS | - |
| `GOVPAY_RICONCILIAZIONI_API_MIN_POOL` | connessioni minime del pool | `2` |
| `GOVPAY_RICONCILIAZIONI_API_MAX_POOL` | connessioni massime del pool | `5` |

### Applicazione

| Variabile | Descrizione | Default |
|-----------|-------------|---------|
| `SERVER_PORT` | porta applicativa | `8080` |
| `MANAGEMENT_SERVER_PORT` | porta degli endpoint actuator (`/actuator/health`, `/actuator/prometheus`) | `9090` |
| `GOVPAY_RICONCILIAZIONI_API_CLUSTER_ID` | identificativo del nodo | `riconciliazioni-api` |

### JVM

| Variabile | Descrizione | Default |
|-----------|-------------|---------|
| `GOVPAY_RICONCILIAZIONI_API_JVM_MAX_RAM_PERCENTAGE` | `-XX:MaxRAMPercentage` | `80` |
| `GOVPAY_RICONCILIAZIONI_API_JVM_INITIAL_RAM_PERCENTAGE` | `-XX:InitialRAMPercentage` | - |
| `GOVPAY_RICONCILIAZIONI_API_JVM_MIN_RAM_PERCENTAGE` | `-XX:MinRAMPercentage` | - |
| `GOVPAY_RICONCILIAZIONI_API_JVM_MAX_METASPACE_SIZE` | `-XX:MaxMetaspaceSize` | - |
| `GOVPAY_RICONCILIAZIONI_API_JVM_MAX_DIRECT_MEMORY_SIZE` | `-XX:MaxDirectMemorySize` | - |
| `JAVA_OPTS` | opzioni JVM aggiuntive | - |

## Diagnostica

```bash
curl http://localhost:9090/actuator/health
curl http://localhost:9090/actuator/prometheus
docker logs -f govpay-riconciliazioni-api
docker exec govpay-riconciliazioni-api cat /tmp/entrypoint_debug.log
```

## Struttura

```
docker/
├── build_image.sh                       # build dell'immagine (release GitHub o jar locale)
├── scan_image.sh                        # scansione Trivy + Grype
├── commons/entrypoint.sh                # entrypoint del container
├── govpay-riconciliazioni-api/
│   ├── Dockerfile.github                # immagine di release (jar scaricato dalla release)
│   └── Dockerfile.daFile                # immagine da jar locale (sviluppo, pipeline su main)
└── jdbc-drivers/README.md
```
