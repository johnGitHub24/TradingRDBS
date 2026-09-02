/**
 * L1 UI Smoke（Node fetch）— 與 static/test/suite.js 相同劇情，無需 Puppeteer。
 * 供 puppeteer 未安裝或 Chromium 下載失敗時使用。
 */
const baseUrl = (process.argv.find((a) => a.startsWith('--baseUrl='))?.split('=')[1]
    || process.env.SMOKE_BASE_URL
    || 'http://localhost:8095').replace(/\/$/, '');
const api = `${baseUrl}/api/v1`;

async function runCase(caseId, name, fn) {
    const logs = [];
    try {
        await fn(logs);
        console.log(`${caseId} PASS — ${name}`);
        logs.forEach((l) => console.log('  ', l));
        return true;
    } catch (e) {
        console.error(`${caseId} FAIL — ${name}: ${e.message || e}`);
        logs.forEach((l) => console.error('  ', l));
        return false;
    }
}

async function loginDemo(logs) {
    const res = await fetch(`${api}/auth/login`, {
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

async function main() {
    console.log(`Fetch UI Smoke -> ${baseUrl}`);

    const health = await fetch(`${baseUrl}/actuator/health`);
    if (!health.ok) throw new Error('health HTTP ' + health.status);
    const h = await health.json();
    if (h.status !== 'UP') throw new Error('health not UP');

    const home = await fetch(baseUrl);
    if (!home.ok) throw new Error('UI home HTTP ' + home.status);
    const homeHtml = await home.text();
    if (!homeHtml.includes('eos-svp-mount')) throw new Error('home missing #eos-svp-mount');
    if (!homeHtml.includes('/blueprint/')) throw new Error('home missing blueprint link');

    const blueprint = await fetch(`${baseUrl}/blueprint/`);
    if (!blueprint.ok) throw new Error('blueprint HTTP ' + blueprint.status);

    const manifest = await fetch(`${baseUrl}/service-links.manifest.json`);
    if (!manifest.ok) throw new Error('manifest HTTP ' + manifest.status);
    const manifestJson = await manifest.json();
    if (!manifestJson.groups?.length) throw new Error('manifest missing groups');

    const runner = await fetch(`${baseUrl}/test/runner.html`);
    if (!runner.ok) throw new Error('runner HTTP ' + runner.status);

    let bearerToken;
    let accountId;
    let symbolId;
    const suffix = Date.now().toString(36);

    const results = [];
    results.push(await runCase('AUTH-001', 'POST /auth/login', async (logs) => {
        bearerToken = await loginDemo(logs);
    }));

    results.push(await runCase('AUTH-002', 'GET /accounts no token → 401', async (logs) => {
        const res = await fetch(`${api}/accounts`);
        if (res.status !== 401) throw new Error('expected 401 got ' + res.status);
        logs.push('401 OK');
    }));

    results.push(await runCase('RDBS-001', 'POST /accounts', async (logs) => {
        const res = await fetch(`${api}/accounts`, {
            method: 'POST',
            headers: authHeaders(bearerToken, { 'Content-Type': 'application/json' }),
            body: JSON.stringify({ accountNo: `SMK-${suffix}`, ownerName: 'Smoke User' })
        });
        const data = await res.json();
        if (res.status !== 201) throw new Error('HTTP ' + res.status);
        accountId = data.id;
        logs.push('accountId=' + accountId);
    }));

    results.push(await runCase('RDBS-002', 'POST /symbols', async (logs) => {
        const res = await fetch(`${api}/symbols`, {
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
        logs.push('symbolId=' + symbolId);
    }));

    results.push(await runCase('RDBS-003', 'POST /orders 1→N→1', async (logs) => {
        const res = await fetch(`${api}/orders`, {
            method: 'POST',
            headers: authHeaders(bearerToken, { 'Content-Type': 'application/json' }),
            body: JSON.stringify({
                accountId, symbolId, side: 'BUY', quantity: 10, unitPrice: 100.0
            })
        });
        const data = await res.json();
        if (res.status !== 201) throw new Error('HTTP ' + res.status);
        if (data.accountId !== accountId || data.symbolId !== symbolId) throw new Error('FK mismatch');
        logs.push('orderId=' + data.id);
    }));

    results.push(await runCase('RDBS-004', 'GET account + orders', async (logs) => {
        const res = await fetch(`${api}/accounts/${accountId}`, { headers: authHeaders(bearerToken) });
        const data = await res.json();
        if (res.status !== 200) throw new Error('HTTP ' + res.status);
        if (!Array.isArray(data.orders) || data.orders.length < 1) throw new Error('orders empty');
        logs.push('orders=' + data.orders.length);
    }));

    results.push(await runCase('RDBS-005', 'GET orders?symbolId=', async (logs) => {
        const res = await fetch(`${api}/orders?symbolId=${symbolId}`, { headers: authHeaders(bearerToken) });
        const data = await res.json();
        if (res.status !== 200) throw new Error('HTTP ' + res.status);
        if (!data.length) throw new Error('empty list');
        logs.push('count=' + data.length);
    }));

    results.push(await runCase('RDBS-006', 'GET 404', async (logs) => {
        const res = await fetch(`${api}/accounts/999999`, { headers: authHeaders(bearerToken) });
        if (res.status !== 404) throw new Error('expected 404 got ' + res.status);
        logs.push('404 OK');
    }));

    results.push(await runCase('RDBS-SEED', 'seed data', async (logs) => {
        const acc = await (await fetch(`${api}/accounts`, { headers: authHeaders(bearerToken) })).json();
        const sym = await (await fetch(`${api}/symbols`, { headers: authHeaders(bearerToken) })).json();
        if (acc.length < 2) throw new Error('accounts < 2');
        if (sym.length < 3) throw new Error('symbols < 3');
        logs.push(`accounts=${acc.length} symbols=${sym.length}`);
    }));

    const failed = results.filter((r) => !r).length;
    if (failed > 0) {
        console.error(`UI_SMOKE_FAILED: ${failed} case(s)`);
        process.exit(1);
    }

    console.log('');
    console.log('ALL_UI_SMOKE_OK');
    console.log('劇情: AUTH-001=PASS; AUTH-002=PASS; RDBS-001=PASS; RDBS-002=PASS; RDBS-003=PASS; RDBS-004=PASS; RDBS-005=PASS; RDBS-006=PASS');
}

main().catch((e) => {
    console.error('UI_SMOKE_FAILED:', e.message || e);
    process.exit(1);
});
