# Set-StrictMode -Version Latest # Optional: for strict error checking
$containerName = "webapp-server"
$imageName = "webapp:latest"

docker stop $containerName -ErrorAction SilentlyContinue
docker rm $containerName -ErrorAction SilentlyContinue

docker build --no-cache -t $imageName .

docker run -d `
    -p 8080:8080 `
    --name $containerName `
    -e SMTP_HOST="smtp.gmail.com" `
    -e SMTP_PORT="587" `
    -e SMTP_USER="lfkomarovme@gmail.com" `
    -e SMTP_PASS="mevr lrbz tfkt gnng" `
    $imageName

Write-Host ""
Write-Host "===========================================" -ForegroundColor DarkGray
Write-Host "[SUCCESS] Server launched: http://localhost:8080/" -ForegroundColor Green
```
eof