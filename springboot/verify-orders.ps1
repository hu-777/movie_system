# Run from any directory. Uses a dedicated in-memory database only.
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & mvn.cmd -q test-compile
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
    if (!(Test-Path 'target/BOOT-INF/lib')) {
        if (!(Test-Path 'target/springboot-0.0.1-SNAPSHOT.jar')) { throw 'Run mvn package once before verification' }
        Push-Location target
        try { & jar xf springboot-0.0.1-SNAPSHOT.jar BOOT-INF/lib } finally { Pop-Location }
    }
    & java -cp 'target/test-classes;target/classes;target/BOOT-INF/lib/*' com.example.OrderTransactionVerification
    if ($LASTEXITCODE -ne 0) { throw 'Order verification failed' }
} finally { Pop-Location }
