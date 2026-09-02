# run-api-smoke.ps1 - L1 API Runtime Smoke (AUTH + RDBS-001～006)
param(
    [string]$BaseUrl = 'http://localhost:8095',
    [string]$Username = 'demo',
    [string]$Password = 'demo123'
)

. "$PSScriptRoot\smoke-utf8.ps1"
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$api = '{0}/api/v1' -f $BaseUrl
$healthUrl = '{0}/actuator/health' -f $BaseUrl

Write-Host "API Smoke L1 -> $BaseUrl" -ForegroundColor Cyan

$h = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
if ($h.status -ne 'UP') { throw "health not UP: $($h.status)" }
Write-Host 'health=UP' -ForegroundColor Green

$ui = Invoke-WebRequest -Uri $BaseUrl -UseBasicParsing -TimeoutSec 5
if ($ui.StatusCode -ne 200) { throw "UI home not 200: $($ui.StatusCode)" }
Write-Host 'UI=200' -ForegroundColor Green

# AUTH-001 login
$loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json
try {
    $auth = Invoke-RestMethod -Method Post -Uri ('{0}/auth/login' -f $api) -ContentType 'application/json' -Body $loginBody
} catch {
    throw "AUTH-001 login failed: $_"
}
if (-not $auth.token) { throw 'AUTH-001 missing token' }
$headers = @{ Authorization = "Bearer $($auth.token)" }
Write-Host "AUTH-001 PASS user=$($auth.username)" -ForegroundColor Green

# AUTH-002 no token
try {
    Invoke-RestMethod -Uri ('{0}/accounts' -f $api) -ErrorAction Stop
    throw 'AUTH-002 expected 401'
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 401) { throw "AUTH-002 expected 401: $_" }
}
Write-Host 'AUTH-002 PASS 401 without token' -ForegroundColor Green

$suffix = [Guid]::NewGuid().ToString('N').Substring(0, 8)

# RDBS-001
$accBody = @{ accountNo = "SMK-$suffix"; ownerName = 'Smoke User' } | ConvertTo-Json
try {
    $acc = Invoke-RestMethod -Method Post -Uri ('{0}/accounts' -f $api) -ContentType 'application/json' -Body $accBody -Headers $headers
} catch {
    throw "RDBS-001 failed: $_"
}
if (-not $acc.id) { throw 'RDBS-001 missing id' }
Write-Host "RDBS-001 PASS accountId=$($acc.id)" -ForegroundColor Green

# RDBS-002
$symBody = @{ ticker = "T$suffix"; companyName = 'Smoke Corp'; exchangeCode = 'TWSE' } | ConvertTo-Json
$sym = Invoke-RestMethod -Method Post -Uri ('{0}/symbols' -f $api) -ContentType 'application/json' -Body $symBody -Headers $headers
if (-not $sym.id) { throw 'RDBS-002 missing id' }
Write-Host "RDBS-002 PASS symbolId=$($sym.id)" -ForegroundColor Green

# RDBS-003
$ordBody = @{
    accountId = $acc.id
    symbolId  = $sym.id
    side      = 'BUY'
    quantity  = 10
    unitPrice = 100.0
} | ConvertTo-Json
$ord = Invoke-RestMethod -Method Post -Uri ('{0}/orders' -f $api) -ContentType 'application/json' -Body $ordBody -Headers $headers
if ($ord.accountId -ne $acc.id -or $ord.symbolId -ne $sym.id) { throw 'RDBS-003 FK mismatch' }
Write-Host "RDBS-003 PASS orderId=$($ord.id)" -ForegroundColor Green

# RDBS-004
$detail = Invoke-RestMethod -Uri ('{0}/accounts/{1}' -f $api, $acc.id) -Headers $headers
if (-not $detail.orders -or $detail.orders.Count -lt 1) { throw 'RDBS-004 orders empty' }
Write-Host "RDBS-004 PASS orders=$($detail.orders.Count)" -ForegroundColor Green

# RDBS-005
$list = Invoke-RestMethod -Uri ('{0}/orders?symbolId={1}' -f $api, $sym.id) -Headers $headers
if (-not $list -or $list.Count -lt 1) { throw 'RDBS-005 no orders' }
Write-Host "RDBS-005 PASS count=$($list.Count)" -ForegroundColor Green

# RDBS-006
try {
    Invoke-RestMethod -Uri ('{0}/accounts/999999' -f $api) -Headers $headers -ErrorAction Stop
    throw 'RDBS-006 expected 404'
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 404) { throw "RDBS-006 expected 404: $_" }
}
Write-Host 'RDBS-006 PASS 404' -ForegroundColor Green

# Seed check
$allAcc = Invoke-RestMethod -Uri ('{0}/accounts' -f $api) -Headers $headers
$allSym = Invoke-RestMethod -Uri ('{0}/symbols' -f $api) -Headers $headers
if ($allAcc.Count -lt 2) { throw 'seed accounts < 2' }
if ($allSym.Count -lt 3) { throw 'seed symbols < 3' }
Write-Host "RDBS-SEED PASS accounts=$($allAcc.Count) symbols=$($allSym.Count)" -ForegroundColor Green

Write-Host ''
Write-Host 'ALL_API_SMOKE_OK' -ForegroundColor Green
Write-Host '劇情: AUTH-001=PASS; AUTH-002=PASS; RDBS-001=PASS; RDBS-002=PASS; RDBS-003=PASS; RDBS-004=PASS; RDBS-005=PASS; RDBS-006=PASS'
