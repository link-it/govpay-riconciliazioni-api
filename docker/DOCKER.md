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
  -e GOVPAY_DB_PASSWORD=<password> \
  linkitaly/govpay-riconciliazioni-api:latest
```

## Variabili d'ambiente

L'elenco completo delle variabili dell'immagine, con obbligatorietà e default, è nel [README](../README.md#esecuzione-con-docker).

## Diagnostica

```bash
curl http://localhost:9090/actuator/health
curl http://localhost:9090/actuator/prometheus
docker logs -f govpay-riconciliazioni-api
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
