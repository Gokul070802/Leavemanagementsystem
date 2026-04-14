param(
    [string]$ProjectId = "project-0168023a-2379-46dc-989",
    [string]$Region = "us-central1",
    [string]$ServiceName = "leavepal-api",
    [string]$RepositoryName = "leavepal-repo",
    [string]$InstanceConnectionName = "project-0168023a-2379-46dc-989:us-central1:leavepal",
    [string]$DbName = "leavepal",
    [string]$DbUser = "leavepal"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command gcloud -ErrorAction SilentlyContinue)) {
    throw "gcloud CLI is not installed. Install Google Cloud CLI first, then re-run this script."
}

Write-Host "Setting gcloud project..."
gcloud config set project $ProjectId | Out-Null

Write-Host "Enabling required APIs..."
gcloud services enable run.googleapis.com sqladmin.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com secretmanager.googleapis.com

Write-Host "Ensuring Artifact Registry repository exists..."
$repoExists = gcloud artifacts repositories list --location $Region --format="value(name)" | Select-String -SimpleMatch $RepositoryName
if (-not $repoExists) {
    gcloud artifacts repositories create $RepositoryName --repository-format=docker --location=$Region
}

Write-Host "Configuring Docker auth for Artifact Registry..."
gcloud auth configure-docker "$Region-docker.pkg.dev" --quiet

Write-Host "Ensuring runtime secrets exist..."
$dbPasswordSecretName = "DB_PASSWORD"
$jwtSecretName = "JWT_SECRET"

$dbPasswordExists = gcloud secrets list --filter="name:$dbPasswordSecretName" --format="value(name)"
if (-not $dbPasswordExists) {
    $dbPassword = Read-Host "Enter Cloud SQL DB password" -AsSecureString
    $dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword))
    $dbPasswordFile = Join-Path $env:TEMP "db-password.txt"
    Set-Content -Path $dbPasswordFile -Value $dbPasswordPlain -NoNewline
    gcloud secrets create $dbPasswordSecretName --data-file=$dbPasswordFile
    Remove-Item $dbPasswordFile -Force -ErrorAction SilentlyContinue
}

$jwtSecretExists = gcloud secrets list --filter="name:$jwtSecretName" --format="value(name)"
if (-not $jwtSecretExists) {
    $jwtSecret = Read-Host "Enter JWT secret" -AsSecureString
    $jwtSecretPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($jwtSecret))
    $jwtSecretFile = Join-Path $env:TEMP "jwt-secret.txt"
    Set-Content -Path $jwtSecretFile -Value $jwtSecretPlain -NoNewline
    gcloud secrets create $jwtSecretName --data-file=$jwtSecretFile
    Remove-Item $jwtSecretFile -Force -ErrorAction SilentlyContinue
}

Write-Host "Deploying backend to Cloud Run from source..."
$envVars = "SPRING_DATASOURCE_URL=jdbc:postgresql:///$DbName?socketFactory=com.google.cloud.sql.postgres.SocketFactory&cloudSqlInstance=$InstanceConnectionName,SPRING_DATASOURCE_USERNAME=$DbUser,SPRING_JPA_HIBERNATE_DDL_AUTO=update,APP_CORS_ALLOWED_ORIGINS=*"

$deployCmd = @(
    "run", "deploy", $ServiceName,
    "--source", ".",
    "--region", $Region,
    "--platform", "managed",
    "--allow-unauthenticated",
    "--add-cloudsql-instances", $InstanceConnectionName,
    "--set-env-vars", $envVars,
    "--set-secrets", "SPRING_DATASOURCE_PASSWORD=DB_PASSWORD:latest,JWT_SECRET=JWT_SECRET:latest"
)

gcloud @deployCmd

$serviceUrl = gcloud run services describe $ServiceName --region $Region --format="value(status.url)"
Write-Host "Cloud Run service URL: $serviceUrl"
Write-Host "Set this as API base URL in frontend/env.js for production deployment."
