# Load .env file and run user-service with dev profile

if (Test-Path .env) {
    Get-Content .\.env | ForEach-Object {
        $kv = $_ -split '=', 2
        if ($kv.Length -eq 2) {
            $envName = $kv[0].Trim()
            $envVal = $kv[1].Trim()
            Set-Item -Path "env:$envName" -Value $envVal
        }
    }
}

Set-Location services\user-service
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

