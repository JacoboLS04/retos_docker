param(
  [string]$GoImage = "golang:1.20-alpine",
  [string]$Network = "retos-net",
  [switch]$NoTidy,
  [switch]$Local
)

$ErrorActionPreference = "Stop"

$TestsPath = Split-Path -Parent $MyInvocation.MyCommand.Path

if ($Local) {
  Write-Host "Running locally (no Docker) from $TestsPath ..."
  $go = Get-Command go -ErrorAction SilentlyContinue
  if (-not $go) {
    Write-Error "Go toolchain not found on host. Install Go or run without -Local."
    exit 1
  }

  Push-Location $TestsPath
  try {
    # Mapear servicios expuestos por docker-compose hacia localhost
    if (-not $env:AMQP_URL -or $env:AMQP_URL -eq '') { $env:AMQP_URL = "amqp://guest:guest@localhost:5672" }
    if (-not $env:PGHOST -or $env:PGHOST -eq '') { $env:PGHOST = "localhost" }
    if (-not $env:PGPORT -or $env:PGPORT -eq '') { $env:PGPORT = "5434" }
    if (-not $env:PGDATABASE -or $env:PGDATABASE -eq '') { $env:PGDATABASE = "notifications_db" }
    if (-not $env:PGUSER -or $env:PGUSER -eq '') { $env:PGUSER = "retos_user" }
    if (-not $env:PGPASSWORD -or $env:PGPASSWORD -eq '') { $env:PGPASSWORD = "retos_pass" }

    if (-not $NoTidy) { & go mod tidy }
    # Ejecutar solo la prueba Rabbit->DB, ya que los endpoints /health no están expuestos al host
  # Publicamos directo en la cola de ms-notifications
  if (-not $env:TEST_PUBLISH_QUEUE -or $env:TEST_PUBLISH_QUEUE -eq '') { $env:TEST_PUBLISH_QUEUE = "notification.events.queue" }
    & go test -v -tags e2e -run Test_Rabbit_To_DB ./...
  }
  finally {
    Pop-Location
  }
  exit $LASTEXITCODE
}

# Si la imagen indicada no existe localmente, intentar usar una de fallback que suele estar ya descargada
function Test-DockerImageExists($image) {
  try {
    $null = docker image inspect $image 2>$null
    return $true
  } catch {
    return $false
  }
}

if (-not (Test-DockerImageExists $GoImage)) {
  $fallback = "golang:1.20-alpine"
  if ($GoImage -ne $fallback -and (Test-DockerImageExists $fallback)) {
    Write-Host "Docker image '$GoImage' not found locally. Falling back to '$fallback'."
    $GoImage = $fallback
  } else {
    Write-Host "Docker image '$GoImage' not found locally and fallback '$fallback' also missing."
    Write-Host "Please ensure internet access for 'docker pull $GoImage' or pre-pull '$fallback'."
    Write-Host "Tip: If behind a proxy, configure Docker Desktop proxy settings or set daemon DNS (8.8.8.8/1.1.1.1)."
    exit 1
  }
}

Write-Host "Running E2E tests from $TestsPath using $GoImage on network '$Network'..."

# Nota: En PowerShell, usar ${Var} evita que se interprete "$Var:/algo" como un nombre con calificador.
# Usamos --mount para evitar problemas con los dos puntos (:) en rutas de Windows.
docker run --rm `
  --network ${Network} `
  --mount type=bind,source="${TestsPath}",target=/app `
  -w /app `
  $GoImage `
  sh -lc "$(if ($NoTidy) { 'go test -v -tags e2e ./...' } else { 'go mod tidy && go test -v -tags e2e ./...' })"
