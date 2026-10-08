$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot -Parent
Push-Location (Join-Path $projectRoot 'frontend')
try { npm.cmd ci; if ($LASTEXITCODE -ne 0) {throw 'Dependency install failed.'}; npm.cmd run build; if ($LASTEXITCODE -ne 0) {throw 'Frontend build failed.'} } finally {Pop-Location}
Push-Location (Join-Path $projectRoot 'backend')
try { & '.\mvnw.cmd' package; if ($LASTEXITCODE -ne 0) {throw 'Backend package failed.'} } finally {Pop-Location}
Write-Output 'Built backend/target/planny-0.1.0.jar with the Vue application included.'
