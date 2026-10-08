param([Parameter(ValueFromRemainingArguments=$true)][string[]]$MavenArgs)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$mavenRoot = Join-Path $projectRoot '.tools\apache-maven-3.9.9'
if (-not (Test-Path (Join-Path $mavenRoot 'bin\mvn.cmd'))) {
    New-Item -ItemType Directory -Path (Join-Path $projectRoot '.tools') -Force | Out-Null
    $archivePath = Join-Path $projectRoot '.tools\maven.zip'
    Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile $archivePath
    Expand-Archive -LiteralPath $archivePath -DestinationPath (Join-Path $projectRoot '.tools') -Force
}
& (Join-Path $mavenRoot 'bin\mvn.cmd') @MavenArgs
exit $LASTEXITCODE
