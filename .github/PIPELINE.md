# Pipeline di Validazione CI/CD

Questo progetto utilizza GitHub Actions per la validazione automatica del codice, la pubblicazione delle immagini Docker e la creazione delle release. La struttura e' la stessa degli altri progetti GovPay (es. `govpay-console-api`, `govpay-notify-batch`).

## Workflow

| File | Quando | Cosa fa |
|------|--------|---------|
| `.github/workflows/maven.yml` | push su `main`, pull request verso `main`, tag | build, test, qualita', sicurezza, immagini, release |
| `.github/workflows/refresh-owasp-db.yml` | ogni notte alle 03:00 (e a mano) | aggiorna la cache del database NVD usata da OWASP Dependency-Check |

## Job di `maven.yml`

| Job | Quando | Descrizione |
|-----|--------|-------------|
| `build` | sempre | build WAR (senza test) e JAR (`-P jar,dist`) con test, JaCoCo, OWASP Dependency-Check, SonarCloud, analisi licenze; pubblica jar, war, `sql.zip` e report come artifact |
| `osv-scan` | push su `main`, tag (o `FORCE_OSV_JOB`) | OSV Scanner sul `pom.xml`; blocca su `main` e sui tag |
| `sbom` | push su `main`, tag (o `FORCE_SBOM_JOB`) | SBOM CycloneDX (json + xml) |
| `dependency-track-upload` | tag, `main` (o `FORCE_DTRACK_UPLOAD_JOB`) | carica lo SBOM su Dependency-Track |
| `docker-dev` | push su `main`, pull request (solo versioni `-SNAPSHOT`) | costruisce e scansiona `linkitaly/govpay-riconciliazioni-api-dev:<versione>`; lo pubblica solo sui push su `main` |
| `release` | tag | verifica che il tag coincida con la versione del pom (non SNAPSHOT) e crea la GitHub Release |
| `docker` | tag, dopo `release` | costruisce `linkitaly/govpay-riconciliazioni-api:<tag>` e `:latest` scaricando il jar dalla release, lo scansiona e lo pubblica |

La scansione delle immagini (Trivy come gate, Grype come report) e' descritta in [docker/SCANSIONE.md](../docker/SCANSIONE.md).

### Asset della release

- `govpay-riconciliazioni-api-<tag>.jar` e `govpay-riconciliazioni-api-<tag>.war`
- `sql.zip`: script dello schema per dialetto (`sql/<dialetto>/`) e file `VERSION`
- `release-reports-<tag>.zip`: report OWASP, JaCoCo, OSV, SBOM e licenze
- `image-scan-report-<tag>.zip`: report della scansione dell'immagine (allegato dal job `docker`)

## Configurazione del repository

### Secrets

| Secret | Uso | Obbligatorio |
|--------|-----|--------------|
| `SONAR_TOKEN` | analisi SonarCloud | Si' |
| `GH_TOKEN` | creazione release e upload degli asset | Si' |
| `DOCKERHUB_TOKEN` | push delle immagini su Docker Hub | Si' |
| `NVD_API_KEY` | aggiornamento del database NVD | Consigliato |
| `OSS_INDEX_PASSWORD` | analizzatore OSS Index | No |
| `GOVPAY_DTRACK_API_KEY` | upload SBOM su Dependency-Track | Si', salvo `DISABLE_DTRACK_UPLOAD_JOB` |

### Variables

| Variable | Uso |
|----------|-----|
| `DOCKERHUB_USERNAME` | utente Docker Hub |
| `OSS_INDEX_USER` | utente OSS Index |
| `IMAGE_SCAN_GATE` | `block` (default) o `warn` per la deroga temporanea del gate |
| `FORCE_OSV_JOB`, `FORCE_SBOM_JOB`, `FORCE_DTRACK_UPLOAD_JOB` | forzano i job anche fuori da `main` e dai tag |
| `DISABLE_SBOM_JOB`, `DISABLE_DTRACK_UPLOAD_JOB` | disattivano i job SBOM / Dependency-Track |

### Servizi esterni

- **SonarCloud**: progetto `link-it_govpay-riconciliazioni-api` nell'organizzazione `link-it`.
- **Docker Hub**: repository `linkitaly/govpay-riconciliazioni-api` e `linkitaly/govpay-riconciliazioni-api-dev`.
- **Dependency-Track**: progetto padre `GovPay-Backend` (usato da `script/sbom/upload-sbom-parent-required.sh`).

## Rilascio

```bash
# 1. togliere -SNAPSHOT dalla versione del pom e committare
mvn versions:set -DnewVersion=1.0.0 -DgenerateBackupPoms=false
# 2. creare e pubblicare il tag, uguale alla versione del pom
git tag 1.0.0
git push origin 1.0.0
```

Il job `release` fallisce se il pom e' ancora SNAPSHOT o se il tag non coincide con la versione.

## Esecuzione locale

```bash
# Build e test come in pipeline, senza OWASP Dependency-Check
mvn clean install -P jar,dist -Dspring.profiles.active=test -Dowasp.phase=none

# Analisi licenze
mvn org.codehaus.mojo:license-maven-plugin:2.4.0:aggregate-download-licenses -DexcludeScopes=test,provided,system -DincludeTransitiveDependencies=true
python3 .github/workflows/scripts/analyze_licenses.py --exceptions .github/workflows/scripts/license-exceptions.json
```

Report: JaCoCo in `target/site/jacoco/index.html`, OWASP in `dependency-check-report/`, licenze in `third-party-licenses/`.
