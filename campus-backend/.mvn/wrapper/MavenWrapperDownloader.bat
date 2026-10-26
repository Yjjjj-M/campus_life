@REM Maven Wrapper Downloader
@echo off
setlocal

set "WRAPPER_VERSION=3.3.2"
set "WRAPPER_JAR=%~1\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/%WRAPPER_VERSION%/maven-wrapper-%WRAPPER_VERSION%.jar"

echo Downloading Maven Wrapper from %WRAPPER_URL%
powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"

if exist "%WRAPPER_JAR%" (
  echo Maven Wrapper downloaded successfully.
) else (
  echo Failed to download Maven Wrapper.
  exit /b 1
)
