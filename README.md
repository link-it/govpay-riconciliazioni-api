<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay - Porta di accesso al sistema pagoPA - Riconciliazioni API

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-riconciliazioni-api/main/LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

API per la riconciliazione dei pagamenti di GovPay.

## Build

```bash
mvn clean install -P jar,dist
```

Produce `target/govpay-riconciliazioni-api.jar` e `target/sql.zip` (script dello schema per dialetto). Con `-P war` produce invece il war.

Il contratto delle API e' in `src/main/resources/openapi/openapi.yaml`: le interfacce e i modelli sono generati in fase di build (pacchetti `it.govpay.riconciliazioni.api` e `it.govpay.riconciliazioni.model`).

La pipeline CI/CD e' descritta in [.github/PIPELINE.md](.github/PIPELINE.md), l'immagine Docker in [docker/DOCKER.md](docker/DOCKER.md).

## Configurazione porta Actuator / Prometheus

Gli endpoint `/actuator/health` e `/actuator/prometheus` rispondono di default su una porta
**separata** da quella applicativa (`9090`). Per modificarla impostare:

``` bash
management.server.port=[Porta dedicata per gli endpoint actuator]
```

oppure, in ambiente Docker, la variabile d'ambiente equivalente:

``` bash
MANAGEMENT_SERVER_PORT=[Porta dedicata per gli endpoint actuator]
```
