@echo off
setlocal
set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
if exist "%MAVEN_WRAPPER_JAR%" (
  java -cp "%MAVEN_WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
  exit /b %ERRORLEVEL%
)
echo Maven wrapper jar not found at %MAVEN_WRAPPER_JAR%
exit /b 1
