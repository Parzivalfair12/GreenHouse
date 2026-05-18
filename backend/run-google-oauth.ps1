$ErrorActionPreference = "Stop"

$clientId = Read-Host "Google Client ID"
$clientSecret = Read-Host "Google Client Secret"

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
$env:Path = "$env:JAVA_HOME\bin;C:\Users\andre\Tools\apache-maven-3.9.15\bin;$env:Path"
$env:GOOGLE_CLIENT_ID = $clientId
$env:GOOGLE_CLIENT_SECRET = $clientSecret
$env:FRONTEND_URL = "http://localhost:5173"

mvn spring-boot:run
