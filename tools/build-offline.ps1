# Offline build fallback. The public Gradle URL in gradle-wrapper.properties is
# the default; this script is only for network failures.
$ErrorActionPreference = 'Stop'

$pluginRoot = Split-Path $PSScriptRoot -Parent
$workspaceRoot = Split-Path $pluginRoot -Parent
$gradleVersion = '9.8.0'
$distributionZip = Join-Path $workspaceRoot "reference_resources\gradle-$gradleVersion-bin.zip"
if (-not (Test-Path $distributionZip)) {
    throw "Local Gradle distribution not found: $distributionZip"
}

$offlineRoot = Join-Path $workspaceRoot 'caches\gradle\offline'
$gradleHome = Join-Path $offlineRoot "gradle-$gradleVersion"
$gradleBat = Join-Path $gradleHome 'bin\gradle.bat'
if (-not (Test-Path $gradleBat)) {
    New-Item -ItemType Directory -Force -Path $offlineRoot | Out-Null
    Expand-Archive -Path $distributionZip -DestinationPath $offlineRoot -Force
}

$env:GRADLE_USER_HOME = Join-Path $workspaceRoot 'caches\gradle'
& $gradleBat -p $pluginRoot @args
exit $LASTEXITCODE
