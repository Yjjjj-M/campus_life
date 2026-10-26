@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
@echo off
@REM set title of command window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%"=="" @echo off

@REM set %HOME% to equivalent of $HOME
if "%HOME%"=="" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%"=="" goto skipRcPre
@REM check for pre script, once with legacy .bat ending and once with .cmd ending
if exist "%USERPROFILE%\mavenrc_pre.bat" call "%USERPROFILE%\mavenrc_pre.bat" %*
if exist "%USERPROFILE%\mavenrc_pre.cmd" call "%USERPROFILE%\mavenrc_pre.cmd" %*
:skipRcPre

@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%"=="" goto OkJHome
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
goto chkJCmd

:OkJHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:chkJCmd
if exist "%JAVACMD%" goto chkMHome

echo The JAVA_HOME environment variable is not defined correctly, >&2
echo this environment variable is needed to run this program. >&2
goto error

:chkMHome
set "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
if not "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set "EXEC_DIR=%CD%"
set "WDIR=%EXEC_DIR%"
:checkBaseDir
if exist "%WDIR%"\.mvn goto baseDirFound
cd ..
if "%WDIR%"=="%CD%" goto baseDirNotFound
set "WDIR=%CD%"
goto checkBaseDir

:baseDirFound
set "MAVEN_PROJECTBASEDIR=%WDIR%"
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set "MAVEN_PROJECTBASEDIR=%EXEC_DIR%"
cd "%EXEC_DIR%"

:endDetectBaseDir

set "MAVEN_CMD_LINE_ARGS=%*"

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
  %MAVEN_PROJECTBASEDIR%\.mvn\wrapper\MavenWrapperDownloader.bat "%MAVEN_PROJECTBASEDIR%"
  if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    echo Cannot download Maven Wrapper. Attempting to use mvn command instead.
    set "WRAPPER_LAUNCHER="
    for %%i in (mvn.cmd) do set "MVNCMD=%%~$PATH:i"
    if defined MVNCMD (
      set "MAVEN_CMD=%MVNCMD%"
    ) else (
      echo Cannot find mvn command. >&2
      goto error
    )
  ) else (
    set "MAVEN_CMD=%JAVACMD%"
  )
) else (
  set "MAVEN_CMD=%JAVACMD%"
)

SET "MAVEN_CMD_LINE_ARGS=%MAVEN_CMD_LINE_ARGS:"=\"%"

@REM Provide a "standardized" way to retrieve the CLI args that will
@REM temporary work://
SET "MAVEN_CMD_LINE_ARGS=%MAVEN_CMD_LINE_ARGS:"=\"%"

SET "MAVEN_JAVA_EXE="%JAVACMD%""
set "WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar""
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

%MAVEN_CMD% ^
  %MAVEN_OPTS% ^
  %MAVEN_DEBUG_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

if not "%MAVEN_SKIP_RC%"=="" goto skipRcPost
@REM check for post script, once with legacy .bat ending and once with .cmd ending
if exist "%USERPROFILE%\mavenrc_post.bat" call "%USERPROFILE%\mavenrc_post.bat"
if exist "%USERPROFILE%\mavenrc_post.cmd" call "%USERPROFILE%\mavenrc_post.cmd"
:skipRcPost

@REM pause the script if MAVEN_BATCH_PAUSE is set to 'on'
if "%MAVEN_BATCH_PAUSE%"=="on" pause

if "%MAVEN_TERMINATE_CMD%"=="on" exit %ERROR_CODE%

cmd /C exit /B %ERROR_CODE%
