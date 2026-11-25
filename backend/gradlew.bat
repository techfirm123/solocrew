@echo off
setlocal
set "GRADLE_VERSION=9.9.2"
set "GRADLE_SHA256=971fcd7150ca4ba04852c0b2ca0d2d0ab9468e14b03473eab19528809914c76b"
set "GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"

if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"

set "CACHE_ROOT=%GRADLE_USER_HOME%\wrapper\dists\solocrew-gradle-%GRADLE_VERSION%"
set "INSTALL_DIR=%CACHE_ROOT%\gradle-%GRADLE_VERSION%"

if exist "%INSTALL_DIR%\bin\gradle.bat" goto run_gradle

where powershell.exe >nul 2>nul

if errorlevel 1 (
  echo Error: PowerShell is required to bootstrap Gradle.
  exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop'; $root=$env:CACHE_ROOT; $version=$env:GRADLE_VERSION; $url=$env:GRADLE_URL; $expected=$env:GRADLE_SHA256; New-Item -ItemType Directory -Force -Path $root | Out-Null; $tmp=Join-Path $root ([guid]::NewGuid().ToString()); New-Item -ItemType Directory -Path $tmp | Out-Null; try { $zip=Join-Path $tmp ('gradle-' + $version + '-bin.zip'); Invoke-WebRequest -Uri $url -OutFile $zip; $actual=(Get-FileHash -Algorithm SHA256 $zip).Hash.ToLowerInvariant(); if ($actual -ne $expected) { throw 'Gradle distribution checksum mismatch.' }; Expand-Archive -Path $zip -DestinationPath $tmp; if (Test-Path $env:INSTALL_DIR) { Remove-Item -Recurse -Force $env:INSTALL_DIR }; Move-Item (Join-Path $tmp ('gradle-' + $version)) $env:INSTALL_DIR } finally { Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue }"

if errorlevel 1 exit /b 1

:run_gradle
call "%INSTALL_DIR%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
