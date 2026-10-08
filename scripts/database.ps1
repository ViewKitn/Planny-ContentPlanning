param([ValidateSet('start','stop')][string]$Action='start',[string]$PgBin='C:\Program Files\PostgreSQL\18\bin')
$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot -Parent
$dataPath=Join-Path $projectRoot '.local\postgres'
$logPath=Join-Path $projectRoot '.local\postgres.log'
if (-not (Test-Path (Join-Path $PgBin 'pg_ctl.exe'))) { throw 'Set -PgBin to the PostgreSQL bin folder.' }
if ($Action -eq 'stop') { & (Join-Path $PgBin 'pg_ctl.exe') -D $dataPath stop; exit $LASTEXITCODE }
New-Item -ItemType Directory -Path (Join-Path $projectRoot '.local') -Force | Out-Null
if (-not (Test-Path (Join-Path $dataPath 'PG_VERSION'))) {
    & (Join-Path $PgBin 'initdb.exe') -D $dataPath -U planny --auth=trust --encoding=UTF8 --locale=C
    if ($LASTEXITCODE -ne 0) { throw 'Database initialization failed.' }
}
& (Join-Path $PgBin 'pg_ctl.exe') -D $dataPath status 2>$null
if ($LASTEXITCODE -ne 0) {
    & (Join-Path $PgBin 'pg_ctl.exe') -D $dataPath -l $logPath -o '-p 55432 -h 127.0.0.1' start
    if ($LASTEXITCODE -ne 0) { throw 'Database startup failed.' }
}
$exists=& (Join-Path $PgBin 'psql.exe') -h 127.0.0.1 -p 55432 -U planny -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='planny'"
if ($exists -ne '1') { & (Join-Path $PgBin 'createdb.exe') -h 127.0.0.1 -p 55432 -U planny planny }
if ($LASTEXITCODE -ne 0) { throw 'Database creation failed.' }
Write-Output 'Planny PostgreSQL is ready on 127.0.0.1:55432.'
