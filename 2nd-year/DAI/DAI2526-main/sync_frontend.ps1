# ============================================================
#  sync_frontend.ps1
#  Sincroniza a pasta frontend/ com backend/src/main/resources/static/
#  Uso: .\sync_frontend.ps1          (sincroniza uma vez)
#       .\sync_frontend.ps1 -Watch   (fica a monitorizar alteracoes)
# ============================================================
param([switch]$Watch)

$src  = "$PSScriptRoot\frontend"
$dst  = "$PSScriptRoot\backend\src\main\resources\static"

function Sync-Files {
    $files = Get-ChildItem $src -File
    $count = 0
    foreach ($f in $files) {
        $target = Join-Path $dst $f.Name
        $copy   = $true
        if (Test-Path $target) {
            $t = Get-Item $target
            if ($f.Length -eq $t.Length -and $f.LastWriteTime -le $t.LastWriteTime) {
                $copy = $false
            }
        }
        if ($copy) {
            Copy-Item $f.FullName $target -Force
            Write-Host "  [COPIADO] $($f.Name)"
            $count++
        }
    }
    if ($count -eq 0) {
        Write-Host "  Tudo ja esta sincronizado." -ForegroundColor DarkGray
    } else {
        Write-Host "  $count ficheiro(s) sincronizado(s)." -ForegroundColor Green
    }
}

if ($Watch) {
    Write-Host "A monitorizar alteracoes em frontend\ ... (Ctrl+C para parar)" -ForegroundColor Cyan
    $watcher = New-Object System.IO.FileSystemWatcher
    $watcher.Path   = $src
    $watcher.Filter = "*.*"
    $watcher.IncludeSubdirectories = $false
    $watcher.EnableRaisingEvents   = $true

    $action = {
        $name = $Event.SourceEventArgs.Name
        $full = $Event.SourceEventArgs.FullPath
        Start-Sleep -Milliseconds 200   # aguarda a escrita terminar
        $target = Join-Path $dst $name
        Copy-Item $full $target -Force
        Write-Host "  [$(Get-Date -Format 'HH:mm:ss')] Sincronizado: $name" -ForegroundColor Green
    }

    Register-ObjectEvent $watcher Changed -Action $action | Out-Null
    Register-ObjectEvent $watcher Created -Action $action | Out-Null

    try {
        while ($true) { Start-Sleep -Seconds 1 }
    } finally {
        $watcher.Dispose()
        Write-Host "Monitor parado." -ForegroundColor Yellow
    }
} else {
    Write-Host "A sincronizar frontend -> static ..." -ForegroundColor Cyan
    Sync-Files
}
