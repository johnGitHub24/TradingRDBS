/**
 * TradingRDBS L1 UI Smoke — AUTH + RDBS-001～006（純 fetch，無 Vue）。
 */
const API = '/api/v1';

const appEl = document.getElementById('smoke-app');
const runBtn = document.getElementById('run-btn');
const resultsEl = document.getElementById('results');

function setStatus(value, failures) {
    appEl.dataset.value = value;
    appEl.dataset.failures = String(failures ? 1 : 0);
}

function renderResult(entry) {
    const col = document.createElement('div');
    col.className = 'col-12';
    col.innerHTML = `
      <div class="card"><div class="card-body">
        <div class="d-flex justify-content-between">
          <strong>${entry.caseId}</strong>
          <span class="${entry.pass ? 'pass' : 'fail'}">${entry.pass ? 'PASS' : 'FAIL'}</span>
        </div>
        <div class="muted small">${entry.name}</div>
        ${entry.logs.length ? `<pre class="mt-2 mb-0">${entry.logs.join('\n')}</pre>` : ''}
      </div></div>`;
    resultsEl.appendChild(col);
}

async function runCase(caseId, name, fn) {
    const entry = { caseId, name, pass: false, logs: [] };
    try {
        await fn(entry.logs);
        entry.pass = true;
    } catch (e) {
        entry.logs.push(String(e));
    }
    renderResult(entry);
    return entry;
}

async function loginDemo(logs) {
    const res = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'demo', password: 'demo123' })
    });
    const data = await res.json();
    if (res.status !== 200 || !data.token) {
        throw new Error('login failed HTTP ' + res.status);
    }
    logs.push('token=' + data.token.slice(0, 12) + '...');
    return data.token;
}

function authHeaders(token, extra = {}) {
    return { Authorization: `Bearer ${token}`, ...extra };
}

async function runTests() {
    if (runBtn.disabled) return;
    runBtn.disabled = true;
    setStatus('running', false);
    resultsEl.innerHTML = '';
    runBtn.textContent = 'TESTING...';
    runBtn.className = 'btn btn-lg w-100 mb-4 btn-run btn-secondary';

    let bearerToken;
    let accountId;
    let symbolId;
    const suffix = Date.now().toString(36);

    const cases = [
        ['AUTH-001', 'POST /auth/login 取得 JWT', async (logs) => {
            bearerToken = await loginDemo(logs);
        }],
        ['AUTH-002', 'GET /accounts 無 token → 401', async (logs) => {
            const res = await fetch(`${API}/accounts`);
            if (res.status !== 401) throw new Error('expected 401 got ' + res.status);
            logs.push('401 OK');
        }],
        ['RDBS-001', 'POST /accounts 建立帳戶', async (logs) => {
            const res = await fetch(`${API}/accounts`, {
                method: 'POST',
                headers: authHeaders(bearerToken, { 'Content-Type': 'application/json' }),
                body: JSON.stringify({ accountNo: `SMK-${suffix}`, ownerName: 'Smoke User' })
            });
            const data = await res.json();
            if (res.status !== 201) throw new Error('HTTP ' + res.status + ' ' + JSON.stringify(data));
            accountId = data.id;
            logs.push('accountId=' + accountId);
        }],
        ['RDBS-002', 'POST /symbols 建立標的', async (logs) => {
            const res = await fetch(`${API}/symbols`, {
                method: 'POST',
                headers: authHeaders(bearerToken, { 'Content-Type': 'application/json' }),
                body: JSON.stringify({
                    ticker: `T${suffix}`.slice(0, 16),
                    companyName: 'Smoke Corp',
                    exchangeCode: 'TWSE'
                })
            });
            const data = await res.json();
            if (res.status !== 201) throw new Error('HTTP ' + res.status);
            symbolId = data.id;
            logs.push('symbolId=' + symbolId + ' ticker=' + data.ticker);
        }],
        ['RDBS-003', 'POST /orders 1→N→1 連結', async (logs) => {
            const res = await fetch(`${API}/orders`, {
                method: 'POST',
                headers: authHeaders(bearerToken, { 'Content-Type': 'application/json' }),
                body: JSON.stringify({
                    accountId, symbolId, side: 'BUY', quantity: 10, unitPrice: 100.0
                })
            });
            const data = await res.json();
            if (res.status !== 201) throw new Error('HTTP ' + res.status);
            if (data.accountId !== accountId || data.symbolId !== symbolId) {
                throw new Error('FK mismatch');
            }
            logs.push('orderId=' + data.id);
        }],
        ['RDBS-004', 'GET /accounts/{id} 含 orders', async (logs) => {
            const res = await fetch(`${API}/accounts/${accountId}`, {
                headers: authHeaders(bearerToken)
            });
            const data = await res.json();
            if (res.status !== 200) throw new Error('HTTP ' + res.status);
            if (!Array.isArray(data.orders) || data.orders.length < 1) {
                throw new Error('orders empty');
            }
            logs.push('orders.length=' + data.orders.length);
        }],
        ['RDBS-005', 'GET /orders?symbolId= N→1', async (logs) => {
            const res = await fetch(`${API}/orders?symbolId=${symbolId}`, {
                headers: authHeaders(bearerToken)
            });
            const data = await res.json();
            if (res.status !== 200) throw new Error('HTTP ' + res.status);
            if (!data.length) throw new Error('no orders for symbol');
            logs.push('count=' + data.length);
        }],
        ['RDBS-006', 'GET /accounts/999999 → 404', async (logs) => {
            const res = await fetch(`${API}/accounts/999999`, {
                headers: authHeaders(bearerToken)
            });
            if (res.status !== 404) throw new Error('expected 404 got ' + res.status);
            logs.push('404 OK');
        }],
        ['RDBS-SEED', '種子資料 ≥2 帳戶 ≥3 標的', async (logs) => {
            const acc = await fetch(`${API}/accounts`, { headers: authHeaders(bearerToken) }).then(r => r.json());
            const sym = await fetch(`${API}/symbols`, { headers: authHeaders(bearerToken) }).then(r => r.json());
            if (acc.length < 2) throw new Error('accounts < 2');
            if (sym.length < 3) throw new Error('symbols < 3');
            logs.push('accounts=' + acc.length + ' symbols=' + sym.length);
        }]
    ];

    let failed = 0;
    for (const [id, name, fn] of cases) {
        const r = await runCase(id, name, fn);
        if (!r.pass) failed += 1;
    }

    runBtn.disabled = false;
    if (failed === 0) {
        runBtn.textContent = 'SERVICE COMPLETED';
        runBtn.className = 'btn btn-lg w-100 mb-4 btn-run btn-success';
        setStatus('completed', false);
    } else {
        runBtn.textContent = `FAILED (${failed})`;
        runBtn.className = 'btn btn-lg w-100 mb-4 btn-run btn-danger';
        setStatus('completed', true);
    }
}

runBtn.addEventListener('click', runTests);
