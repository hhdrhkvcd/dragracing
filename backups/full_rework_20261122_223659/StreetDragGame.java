$path = "D:\pio\install_streetdrag.ps1"

    try {
    [scriptblock]::Create(
        [System.IO.File]::ReadAllText($path)
    ) | Out-Null

Write-Host "SCRIPT SYNTAX OK" -ForegroundColor Green
}
    catch {
Write-Host $_.Exception.Message -ForegroundColor Red
}
