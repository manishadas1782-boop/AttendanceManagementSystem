param()
$ErrorActionPreference = "Stop"

# Determine project root from this script's location
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Split-Path -Parent $scriptDir
$src = Join-Path $root "src\main\java"
$out = Join-Path $root "out"

if (Test-Path $out) {
  Remove-Item -Recurse -Force $out
}
New-Item -ItemType Directory -Path $out -Force | Out-Null

$files = Get-ChildItem -Path $src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if (-not $files -or $files.Count -eq 0) {
  Write-Error "No Java source files found under $src"
  exit 1
}

& javac -d $out $files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Build complete. Classes written to $out"