param()
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Split-Path -Parent $scriptDir
$out = Join-Path $root "out"

if (-not (Test-Path $out)) {
  Write-Error "Output folder not found. Build the project first: scripts\\build.ps1"
  exit 1
}

& java -cp $out com.ams.app.Main
exit $LASTEXITCODE