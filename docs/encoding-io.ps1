# encoding-io.ps1 — EOS 編碼一致性（SSOT 實作：knowledge/encoding.md）
# Dot-source：. "$PSScriptRoot\encoding-io.ps1"

function Get-EosUtf8NoBom {
    New-Object System.Text.UTF8Encoding $false
}

function Get-EosUtf8Bom {
    New-Object System.Text.UTF8Encoding $true
}

function Test-EosPathUsesUtf8Bom {
    param([string]$Path)
    $leaf = [IO.Path]::GetFileName($Path)
    return ($leaf -match '\.ps1$' -or $leaf -match '\.ps1\.template$')
}

function Get-EosEncodingForPath {
    param([string]$Path)
    if (Test-EosPathUsesUtf8Bom $Path) { return Get-EosUtf8Bom }
    return Get-EosUtf8NoBom
}

function Read-EosTextFile {
    param([string]$Path)
    $bytes = [IO.File]::ReadAllBytes($Path)
    $utf8 = Get-EosUtf8NoBom
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        return [string]$utf8.GetString($bytes, 3, $bytes.Length - 3)
    }
    if ($bytes.Length -ge 2 -and $bytes[0] -eq 0xFF -and $bytes[1] -eq 0xFE) {
        return [string][Text.Encoding]::Unicode.GetString($bytes, 2, $bytes.Length - 2)
    }
    if ($bytes.Length -ge 2 -and $bytes[0] -eq 0xFE -and $bytes[1] -eq 0xFF) {
        return [string][Text.Encoding]::BigEndianUnicode.GetString($bytes, 2, $bytes.Length - 2)
    }
    $asUtf8 = $utf8.GetString($bytes)
    $roundTrip = $utf8.GetBytes($asUtf8)
    if ($roundTrip.Length -eq $bytes.Length) {
        $same = $true
        for ($i = 0; $i -lt $bytes.Length; $i++) {
            if ($roundTrip[$i] -ne $bytes[$i]) { $same = $false; break }
        }
        if ($same) { return $asUtf8 }
    }
    return [string][Text.Encoding]::Default.GetString($bytes)
}

function Write-EosTextFile {
    param(
        [string]$Path,
        [string]$Content,
        [switch]$NoTrailingNewline
    )
    $enc = Get-EosEncodingForPath $Path
    if ($NoTrailingNewline) {
        $text = $Content
    } elseif ($Content.EndsWith("`n")) {
        $text = $Content
    } else {
        $text = $Content.TrimEnd() + "`n"
    }
    [IO.File]::WriteAllText($Path, $text, $enc)
}

function Initialize-EosConsoleUtf8 {
    try {
        cmd /c "chcp 65001 >NUL" 2>$null | Out-Null
        $u = Get-EosUtf8NoBom
        [Console]::OutputEncoding = $u
        [Console]::InputEncoding = $u
        $script:OutputEncoding = $u
    } catch { }
    $env:PYTHONUTF8 = '1'
    $env:PYTHONIOENCODING = 'utf-8'
    if ($env:JAVA_TOOL_OPTIONS -notmatch 'file\.encoding=UTF-8') {
        $env:JAVA_TOOL_OPTIONS = ("$($env:JAVA_TOOL_OPTIONS) -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8").Trim()
    }
}

function Get-EosWebText {
    param(
        [Parameter(Mandatory)][string]$Url,
        [int]$TimeoutSec = 10
    )
    $res = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec $TimeoutSec
    $utf8 = Get-EosUtf8NoBom
    if ($null -ne $res.RawContentStream -and $res.RawContentStream.CanSeek) {
        $res.RawContentStream.Position = 0
        $ms = New-Object IO.MemoryStream
        $res.RawContentStream.CopyTo($ms)
        return $utf8.GetString($ms.ToArray())
    }
    return [string]$res.Content
}

function Test-EosFileEncodingOk {
    param([string]$Path)
    $bytes = [IO.File]::ReadAllBytes($Path)
    if (Test-EosPathUsesUtf8Bom $Path) {
        return ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF)
    }
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        return $false
    }
    $utf8 = Get-EosUtf8NoBom
    $text = $utf8.GetString($bytes)
    $roundTrip = $utf8.GetBytes($text)
    if ($roundTrip.Length -ne $bytes.Length) { return $false }
    for ($i = 0; $i -lt $bytes.Length; $i++) {
        if ($roundTrip[$i] -ne $bytes[$i]) { return $false }
    }
    return $true
}
