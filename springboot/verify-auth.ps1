# Uses an isolated H2 database and a temporary loopback HTTP server.
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & mvn.cmd -q test-compile
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
    if (!(Test-Path 'target/BOOT-INF/lib')) {
        if (!(Test-Path 'target/springboot-0.0.1-SNAPSHOT.jar')) { throw 'Run mvn package once before verification' }
        Push-Location target
        try {
            & jar xf springboot-0.0.1-SNAPSHOT.jar BOOT-INF/lib
            if ($LASTEXITCODE -ne 0) { throw 'Dependency extraction failed' }
        } finally { Pop-Location }
    }
    & java -cp 'target/test-classes;target/classes;target/BOOT-INF/lib/*' com.example.AuthSecurityVerification
    if ($LASTEXITCODE -ne 0) { throw 'Authentication verification failed' }
} finally { Pop-Location }
