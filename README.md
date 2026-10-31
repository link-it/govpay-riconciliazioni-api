<p align="center">
<img src="https://www.link.it/wp-content/uploads/2025/01/logo-govpay.svg" alt="GovPay Logo" width="200"/>
</p>

# GovPay - Porta di accesso al sistema pagoPA - Riconciliazioni API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=link-it_govpay-riconciliazioni-api&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=link-it_govpay-riconciliazioni-api)
[![Docker Hub](https://img.shields.io/docker/v/linkitaly/govpay-riconciliazioni-api?label=Docker%20Hub&logo=docker)](https://hub.docker.com/r/linkitaly/govpay-riconciliazioni-api)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/link-it/govpay-riconciliazioni-api/main/LICENSE)
[![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fraw.githubusercontent.com%2Flink-it%2Fgovpay-bom%2Fmain%2Fpom.xml&query=%2F%2F*%5Blocal-name()%3D'spring-boot.version'%5D&label=Spring%20Boot&color=brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

API REST di GovPay per la riconciliazione dei pagamenti: consultazione di flussi di rendicontazione, riscossioni e ricevute e riconciliazione dei movimenti di cassa, destinate ai sistemi di ragioneria dell'Ente Creditore.

## Sommario

Le API sono rivolte ai sistemi di ragioneria dell'Ente Creditore e alle altre applicazioni censite su GovPay. Le risorse sono partizionate per Ente Creditore (`idDominio`) e gli elenchi sono paginati con le stesse convenzioni delle API Pendenze e Console (`page`/`limit` oppure `cursor`/`limit`, con `sort`, `fields` e `total`).

Operazioni definite nel contratto [src/main/resources/govpay-api-riconciliazioni.yaml](src/main/resources/govpay-api-riconciliazioni.yaml):

| Gruppo | Metodo e path | Descrizione |
|--------|---------------|-------------|
| Flussi di rendicontazione | `GET /flussi-rendicontazione/{idDominio}` | Elenco dei flussi di rendicontazione |
| | `GET /flussi-rendicontazione/{idDominio}/{idFlusso}` | Dettaglio di un flusso |
| | `GET /flussi-rendicontazione/{idDominio}/{idFlusso}/rendicontazioni` | Rendicontazioni di un flusso |
| | `GET /flussi-rendicontazione/{idDominio}/{idFlusso}/xml` | XML originale del flusso |
| Riscossioni | `GET /riscossioni/{idDominio}` | Elenco delle riscossioni |
| | `GET /riscossioni/{idDominio}/{iuv}/{iur}/{indice}` | Dettaglio di una riscossione |
| Riconciliazioni | `GET /riconciliazioni/{idDominio}` | Elenco delle riconciliazioni |
| | `PUT /riconciliazioni/{idDominio}/{idRiconciliazione}` | Riconciliazione di un movimento di cassa |
| | `GET /riconciliazioni/{idDominio}/{idRiconciliazione}` | Dettaglio di una riconciliazione |
| | `GET /riconciliazioni/{idDominio}/{idRiconciliazione}/riscossioni` | Riscossioni di una riconciliazione |
| Ricevute | `GET /ricevute/{idDominio}` | Elenco delle ricevute di pagamento |
| | `GET /ricevute/{idDominio}/{iuv}/{idRicevuta}` | Ricevuta di pagamento |
| Utenti | `GET /profilo` | Profilo e autorizzazioni dell'utenza chiamante |
| Servizio | `GET /status` | Stato del servizio |

Le interfacce Spring e i modelli sono generati dal contratto in fase di build (package `it.govpay.riconciliazioni.api` e `it.govpay.riconciliazioni.model`). Il contratto prevede l'autenticazione HTTP Basic, fornita da [govpay-common-auth](https://github.com/link-it/govpay-common-auth).

**Stato del progetto.** Il progetto è uno scheletro: contratto, build, pipeline e immagine sono pronti, ma le operazioni non sono ancora implementate (nessun controller) e lo schema del database del progetto è vuoto. Le catene di autenticazione di `govpay-common-auth` si attivano solo in presenza di un bean `GovpayPrincipalLoader`, che il progetto non definisce ancora: fino ad allora vale la configurazione di default di Spring Security e ogni richiesta sulla porta applicativa riceve `401`. Sulla porta di management risponde `/actuator/health`.

## Integrazione con GovPay

| Componente | Quando serve | Obbligatorio | Ruolo |
|------------|--------------|--------------|-------|
| Database di GovPay | runtime | sì | Connessione aperta all'avvio, con validazione dello schema (`spring.jpa.hibernate.ddl-auto=validate`). Le tabelle del core che le operazioni leggeranno (flussi, rendicontazioni, pagamenti, incassi, ricevute) non sono ancora mappate. Gli script dello schema del progetto, oggi vuoti, sono in `src/main/resources/db/sql/<dialetto>/` |
| [govpay-bom](https://github.com/link-it/govpay-bom) | build | sì | Parent Maven: versioni delle dipendenze e dei plugin, profilo `dist` |
| [govpay-common](https://github.com/link-it/govpay-common) | build | sì | Libreria comune: transaction id e correlation id nei log, metriche GovPay opzionali |
| [govpay-common-auth](https://github.com/link-it/govpay-common-auth) | build | sì | Catene di autenticazione Spring Security e configurazione dei metodi `govpay.auth.*` |
| [GovPay](https://github.com/link-it/govpay) | runtime | sì | Riceve da pagoPA flussi di rendicontazione e ricevute e ne scrive i dati nel database: le API li espongono in consultazione |
| [govpay-pendenze-api](https://github.com/link-it/govpay-pendenze-api) | runtime | no | Le risposte riportano le pendenze per identificativo; il dettaglio si recupera dalle API Pendenze |
| Sistemi di ragioneria dell'Ente Creditore | runtime | no | Client delle API, censiti su GovPay come applicazioni (vedi la [documentazione di GovPay](https://govpay.readthedocs.io/it/3.10.x/integrazione/api/index.html)) |

## Compilazione

Requisiti: JDK 21 e Maven 3.8.1 o successivo.

```bash
mvn clean install -P jar,dist   # fat jar e script SQL, per esecuzione standalone e container
mvn clean install -P war        # war, per il deploy su application server
```

Il profilo sceglie il packaging; il risultato è in `target/govpay-riconciliazioni-api.jar` o `target/govpay-riconciliazioni-api.war`. Il profilo `dist` (da `govpay-bom`) produce anche `target/sql.zip`, con gli script dello schema per dialetto, usato dall'immagine Docker e dalla release; va sempre indicato insieme a `jar`, perché passando `-P` Maven disattiva il profilo `jar` attivo di default.

La build esegue i test e l'analisi OWASP Dependency-Check delle dipendenze (esecuzione `check-owasp` ereditata da `govpay-bom`, nella fase indicata da `owasp.phase`). Per escluderla, per esempio in sviluppo:

```bash
mvn clean install -P jar,dist -Dowasp.phase=none
```

## Configurazione

Le proprietà si impostano in `application.properties`, da riga di comando (`-D<proprietà>=<valore>`) o con variabili d'ambiente. Alcune proprietà leggono una variabile dedicata, indicata nella colonna "Variabile d'ambiente"; le altre accettano la forma equivalente di Spring Boot (es. `spring.jpa.open-in-view` → `SPRING_JPA_OPEN_IN_VIEW`). Con l'immagine Docker si usano le variabili descritte in [Esecuzione con Docker](#esecuzione-con-docker).

### Server e API

| Proprietà | Variabile d'ambiente | Default | Descrizione |
|-----------|----------------------|---------|-------------|
| `server.port` | `SERVER_PORT` | `8080` | Porta HTTP del servizio |
| `riconciliazioni.time-zone` | - | `Europe/Rome` | Fuso orario di default del mapper JSON |
| `spring.mvc.date-format` | `SPRING_MVC_DATE_FORMAT` | `yyyy-MM-dd` | Formato delle date nei parametri |
| `spring.mvc.datetime-format` | `SPRING_MVC_DATETIME_FORMAT` | `yyyy-MM-dd'T'HH:mm:ssZZZ` | Formato di data e ora nei parametri |
| `spring.mvc.throw-exception-if-no-handler-found` | `SPRING_MVC_THROW_EXCEPTION_IF_NO_HANDLER_FOUND` | `true` | Risponde con un errore applicativo sui path inesistenti |
| `spring.jackson.default-property-inclusion` | `SPRING_JACKSON_DEFAULT_PROPERTY_INCLUSION` | `non_absent` | Esclude dalle risposte JSON i campi nulli o assenti |
| `server.error.whitelabel.enabled` | `SERVER_ERROR_WHITELABEL_ENABLED` | `true` | Pagina di errore di default di Spring Boot |
| `spring.main.banner-mode` | `SPRING_MAIN_BANNER_MODE` | `off` | Banner di Spring Boot all'avvio |
| `govpay.riconciliazioni.cluster-id` | `CLUSTER_ID` | `riconciliazioni-api` | Identificativo del nodo. Non ancora letto dal codice |

### Autenticazione

Proprietà di [govpay-common-auth](https://github.com/link-it/govpay-common-auth), effettive solo quando il progetto definirà il `GovpayPrincipalLoader` (vedi [Sommario](#sommario)). Gli altri metodi della libreria (LDAP, FORM, SSL, SSL da header, header, API key, SPID, sessione, OAuth2) sono disattivi e si abilitano con `govpay.auth.<metodo>.enabled=true` e le relative proprietà, descritte nel README della libreria.

| Proprietà | Variabile d'ambiente | Default | Descrizione |
|-----------|----------------------|---------|-------------|
| `govpay.auth.basic.enabled` | `GOVPAY_AUTH_BASIC_ENABLED` | `true` | Autenticazione HTTP Basic |
| `govpay.auth.public-chain.enabled` | `GOVPAY_AUTH_PUBLICCHAIN_ENABLED` | `true` | Path accessibili senza autenticazione, elencati sotto |
| `govpay.auth.public-chain.permit-all-paths[0].path`, `govpay.auth.public-chain.permit-all-paths[0].methods[0]` | - | `/auth/methods`, `GET` | Metodi di autenticazione attivi |
| `govpay.auth.public-chain.permit-all-paths[1].path`, `govpay.auth.public-chain.permit-all-paths[1].methods[0]` | - | `/actuator/prometheus`, `GET` | Metriche, servite solo sulla porta di management |
| `govpay.auth.public-chain.permit-all-paths[2].path`, `govpay.auth.public-chain.permit-all-paths[2].methods[0]` | - | `/actuator/health`, `GET` | Stato, servito solo sulla porta di management |

### Database

| Proprietà | Variabile d'ambiente | Default | Descrizione |
|-----------|----------------------|---------|-------------|
| `spring.datasource.url` | `DATASOURCE_URL` | - | URL JDBC del database di GovPay (obbligatoria, in alternativa a `spring.datasource.jndi-name`) |
| `spring.datasource.jndi-name` | `SPRING_DATASOURCE_JNDI_NAME` | - | Nome JNDI del datasource, per il deploy del war su application server |
| `spring.datasource.username` | `DATASOURCE_USERNAME` | - | Utente del database |
| `spring.datasource.password` | `DATASOURCE_PASSWORD` | - | Password del database |
| `spring.datasource.driver-class-name` | `DATASOURCE_DRIVER` | `org.postgresql.Driver` | Classe del driver JDBC |
| `spring.jpa.database-platform` | `JPA_DIALECT` | `org.hibernate.dialect.PostgreSQLDialect` | Dialetto Hibernate |
| `spring.jpa.hibernate.ddl-auto` | `HIBERNATE_DDL_AUTO` | `validate` | Gestione dello schema: lasciare `validate` o `none`, lo schema è di GovPay |
| `spring.jpa.open-in-view` | `SPRING_JPA_OPEN_IN_VIEW` | `false` | Sessione JPA limitata al servizio, non estesa alla vista |
| `spring.jpa.mapping-resources` | `SPRING_JPA_MAPPING_RESOURCES` | - | Mapping ORM del database: `META-INF/orm-<h2\|hsql\|mysql\|oracle\|postgres\|sqlserver>.xml` (oggi vuoti; nel container impostata dall'entrypoint) |
| `spring.datasource.hikari.minimum-idle` | `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE` | `10` (`2` nel container) | Connessioni minime inattive del pool |
| `spring.datasource.hikari.maximum-pool-size` | `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE` | `10` (`5` nel container) | Connessioni massime del pool |
| `spring.datasource.hikari.connection-timeout` | `SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT` | `30000` (`20000` nel container) | Attesa massima di una connessione, in ms |
| `spring.datasource.hikari.idle-timeout` | `SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT` | `600000` (`10000` nel container) | Tempo dopo cui una connessione inattiva viene chiusa, in ms |
| `spring.datasource.hikari.max-lifetime` | `SPRING_DATASOURCE_HIKARI_MAX_LIFETIME` | `1800000` (`600000` nel container) | Durata massima di una connessione, in ms |

I driver JDBC non sono inclusi nel jar: vedi [Esecuzione](#esecuzione).

### Logging

| Proprietà | Variabile d'ambiente | Default | Descrizione |
|-----------|----------------------|---------|-------------|
| `logging.file.name` | `LOGGING_FILE_NAME` | - (solo console) | File di log; nel container `/var/log/govpay-riconciliazioni-api/govpay-riconciliazioni-api.log` |
| `logging.level.it.govpay` | `LOGGING_LEVEL_IT_GOVPAY` | `INFO` | Livello di log dei package GovPay |
| `logging.pattern.level` | `LOGGING_PATTERN_LEVEL` | `%5p [%X{transactionId:-},%X{correlationId:-}]` | Aggiunge a ogni riga transaction id e correlation id |
| `govpay.logging.tracciatura.enabled` | `GOVPAY_LOGGING_TRACCIATURA_ENABLED` | `true` | Filtro di `govpay-common` che genera il transaction id e legge il correlation id (header `X-Correlation-ID` o `X-Request-ID`), restituendoli in risposta. Le altre proprietà `govpay.logging.tracciatura.*` sono descritte nel README di [govpay-common](https://github.com/link-it/govpay-common) |
| `govpay.logging.livelli-dinamici.enabled` | `GOVPAY_LOGGING_LIVELLIDINAMICI_ENABLED` | `true` | Livelli di log letti dalla tabella `configurazione` di GovPay. Oggi senza effetto: richiede il `ConfigurazioneService` di `govpay-common`, che il progetto non registra |
| `govpay.logging.livelli-dinamici.intervallo-refresh` | `LOG_LEVEL_REFRESH_INTERVAL` | `60s` | Intervallo di rilettura dei livelli da database |
| `govpay.logging.livelli-dinamici.logger-gestiti[0]` | - | `it.govpay` | Prefissi dei logger modificabili a runtime |

### Monitoraggio

| Proprietà | Variabile d'ambiente | Default | Descrizione |
|-----------|----------------------|---------|-------------|
| `management.server.port` | `MANAGEMENT_SERVER_PORT` | `9090` | Porta dedicata agli endpoint actuator: sulla porta applicativa non sono serviti |
| `management.endpoints.web.exposure.include` | `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | `health,prometheus,loggers` | Endpoint actuator esposti |
| `management.metrics.distribution.percentiles-histogram.http.server.requests` | - | `true` | Istogrammi delle latenze delle richieste ricevute, per aggregare i percentili tra più istanze |
| `management.metrics.distribution.percentiles-histogram.http.client.requests` | - | `true` | Istogrammi delle latenze delle chiamate in uscita |
| `management.metrics.distribution.slo.http.server.requests` | - | `100ms,500ms,1s,2s,5s` | Soglie esplicite di latenza, compresa quella SLA pagoPA di 2s |
| `management.metrics.tags.application` | `MANAGEMENT_METRICS_TAGS_APPLICATION` | `govpay-riconciliazioni-api` | Tag che identifica il servizio nelle metriche |
| `govpay.metrics.enabled` | `GOVPAY_METRICS_ENABLED` | `false` | Metriche custom di `govpay-common` (tempi interni ed esterni delle richieste) |

## Esecuzione

### Driver JDBC

I driver JDBC **non sono inclusi** nel fat jar e vanno forniti a runtime. Creare una directory (es. `jdbc-drivers/`) e copiarvi il driver del database utilizzato:

| Database   | Driver |
|------------|--------|
| PostgreSQL | [postgresql-42.7.14.jar](https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar) |
| MySQL      | [mysql-connector-j-26.10.0.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/26.10.0/mysql-connector-j-26.10.0.jar) |
| Oracle     | [ojdbc11-23.26.3.0.0.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/23.26.3.0.0/ojdbc11-23.26.3.0.0.jar) |
| SQL Server | [mssql-jdbc-13.6.1.jre11.jar](https://repo1.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/13.6.1.jre11/mssql-jdbc-13.6.1.jre11.jar) |
| H2         | [h2-2.5.252.jar](https://repo1.maven.org/maven2/com/h2database/h2/2.5.252/h2-2.5.252.jar) |

### Jar standalone

Il jar usa `PropertiesLauncher`: la proprietà `loader.path` indica la directory dei driver JDBC. Configurazione minima per PostgreSQL:

```bash
java -Dloader.path=./jdbc-drivers \
     -Dserver.port=8080 \
     -Dmanagement.server.port=9090 \
     -Dspring.datasource.url=jdbc:postgresql://localhost:5432/govpay \
     -Dspring.datasource.username=govpay \
     -Dspring.datasource.password=<password> \
     -jar target/govpay-riconciliazioni-api.jar
```

Per un altro database impostare anche `spring.datasource.driver-class-name` e `spring.jpa.database-platform`, che di default valgono per PostgreSQL.

### War su application server

Il war usa il datasource dell'application server, indicato con `spring.datasource.jndi-name` (es. `java:comp/env/it.govpay.datasource`), e la porta del container servlet; `management.server.port` va impostata a una porta libera o rimossa. Dove trovarlo:

| Origine | Dove si trova |
|---------|---------------|
| Release (tag) | Allegato alla [release GitHub](https://github.com/link-it/govpay-riconciliazioni-api/releases) come `govpay-riconciliazioni-api-<tag>.war` |
| Qualsiasi build | Artifact `govpay-riconciliazioni-api` della run di GitHub Actions |
| Build locale | `mvn clean install -P war`, in `target/govpay-riconciliazioni-api.war` |

L'immagine Docker contiene solo il jar.

## Esecuzione con Docker

L'immagine è pubblicata su Docker Hub come [linkitaly/govpay-riconciliazioni-api](https://hub.docker.com/r/linkitaly/govpay-riconciliazioni-api) (release) e `linkitaly/govpay-riconciliazioni-api-dev` (sviluppo, a ogni push su `main`). Il servizio risponde sulla porta `8080`, gli endpoint actuator sulla `9090`; i driver JDBC si montano in `/opt/jdbc-drivers`. L'immagine di sviluppo contiene anche gli script dello schema in `/opt/sql/<dialetto>/`.

```bash
docker run -d --name govpay-riconciliazioni-api -p 8080:8080 -p 9090:9090 \
  -v ./jdbc-drivers:/opt/jdbc-drivers:ro \
  -e GOVPAY_DB_TYPE=postgresql \
  -e GOVPAY_DB_SERVER=postgres:5432 \
  -e GOVPAY_DB_NAME=govpay \
  -e GOVPAY_DB_USER=govpay \
  -e GOVPAY_DB_PASSWORD=<password> \
  linkitaly/govpay-riconciliazioni-api
```

Variabili d'ambiente dell'immagine, oltre alle proprietà Spring Boot in forma di variabile (es. `LOGGING_LEVEL_IT_GOVPAY=DEBUG`):

| Variabile | Obbligatoria | Default | Descrizione |
|-----------|--------------|---------|-------------|
| `GOVPAY_DB_TYPE` | sì | - | Tipo di database: `postgresql`, `mysql`, `mariadb`, `oracle`, `sqlserver` |
| `GOVPAY_DB_SERVER` | sì | - | Server del database, `host` o `host:porta` (porta di default del database se omessa) |
| `GOVPAY_DB_NAME` | sì | - | Nome del database (per Oracle, service name o SID) |
| `GOVPAY_DB_USER` | sì | - | Utente del database |
| `GOVPAY_DB_PASSWORD` | no | - | Password del database (se assente l'entrypoint segnala un avviso) |
| `GOVPAY_DS_CONN_PARAM` | no | - | Parametri aggiunti all'URL JDBC (es. `zeroDateTimeBehavior=convertToNull` per MySQL) |
| `GOVPAY_DS_JDBC_LIBS` | no | `/opt/jdbc-drivers` | Directory dei driver JDBC |
| `GOVPAY_ORACLE_JDBC_URL_TYPE` | no | `servicename` | Formato dell'URL Oracle: `servicename` o `sid` |
| `ORACLE_TNS_ADMIN` | no | - | Directory dei file TNS di Oracle |
| `GOVPAY_RICONCILIAZIONI_API_MIN_POOL` | no | `2` | Connessioni minime inattive del pool |
| `GOVPAY_RICONCILIAZIONI_API_MAX_POOL` | no | `5` | Connessioni massime del pool |
| `GOVPAY_RICONCILIAZIONI_API_CLUSTER_ID` | no | `riconciliazioni-api` | Identificativo del nodo (`govpay.riconciliazioni.cluster-id`) |
| `SERVER_PORT` | no | `8080` | Porta HTTP del servizio |
| `MANAGEMENT_SERVER_PORT` | no | `9090` | Porta degli endpoint actuator |
| `GOVPAY_RICONCILIAZIONI_API_JVM_MAX_RAM_PERCENTAGE` | no | `80` | `-XX:MaxRAMPercentage` della JVM |
| `GOVPAY_RICONCILIAZIONI_API_JVM_INITIAL_RAM_PERCENTAGE` | no | - | `-XX:InitialRAMPercentage` della JVM |
| `GOVPAY_RICONCILIAZIONI_API_JVM_MIN_RAM_PERCENTAGE` | no | - | `-XX:MinRAMPercentage` della JVM |
| `GOVPAY_RICONCILIAZIONI_API_JVM_MAX_METASPACE_SIZE` | no | - | `-XX:MaxMetaspaceSize` della JVM |
| `GOVPAY_RICONCILIAZIONI_API_JVM_MAX_DIRECT_MEMORY_SIZE` | no | - | `-XX:MaxDirectMemorySize` della JVM |
| `JAVA_OPTS` | no | - | Opzioni aggiuntive della JVM |

Costruzione dell'immagine in locale, scansione delle vulnerabilità e diagnostica sono descritti in [docker/DOCKER.md](docker/DOCKER.md); la pipeline CI/CD in [.github/PIPELINE.md](.github/PIPELINE.md).

## Monitoraggio

Gli endpoint actuator rispondono solo sulla porta di management (`9090` di default), non su quella applicativa:

```bash
curl http://localhost:9090/actuator/health       # stato, usato anche dalle probe dei container
curl http://localhost:9090/actuator/prometheus   # metriche in formato Prometheus
```

La porta si cambia con `management.server.port` (nel container `MANAGEMENT_SERVER_PORT`). È esposto anche `loggers`, che permette di cambiare i livelli di log a caldo sul solo nodo interrogato: la porta di management va tenuta su una rete interna, non raggiungibile dall'esterno. Finché le catene di `govpay-common-auth` non sono attive (vedi [Sommario](#sommario)) solo `/actuator/health` risponde senza credenziali; `prometheus` e `loggers` rispondono `401`.

## Licenza

GovPay Riconciliazioni API è distribuito con licenza [GNU General Public License v3.0](LICENSE).
