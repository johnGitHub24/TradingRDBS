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

async function main() {
    console.log(`Fetch UI Smoke -> ${baseUrl}`);

    const health = await fetch(`${baseUrl}/actuator/health`);
    if (!health.ok) throw new Error('health HTTP ' + health.status);
    const h = await health.json();
    if (h.status !== 'UP') throw new Error('health not UP');

    const home = await fetch(baseUrl);
    if (!home.ok) throw new Error('UI home HTTP ' + home.status);

    const runner = await fetch(`${baseUrl}/test/runner.html`);
    if (!runner.ok) throw new Error('runner HTTP ' + runner.status);

    let accountId;
    let symbolId;
    const suffix = Date.now().toString(36);

    const results = [];
    results.push(await runCase('RDBS-001', 'POST /accounts', async (logs) => {
        const res = await fetch(`${api}/accounts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
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
            headers: { 'Content-Type': 'application/json' },
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
            headers: { 'Content-Type': 'application/json' },
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
        const res = await fetch(`${api}/accounts/${accountId}`);
        const data = await res.json();
        if (res.status !== 200) throw new Error('HTTP ' + res.status);
        if (!Array.isArray(data.orders) || data.orders.length < 1) throw new Error('orders empty');
        logs.push('orders=' + data.orders.length);
    }));

    results.push(await runCase('RDBS-005', 'GET orders?symbolId=', async (logs) => {
        const res = await fetch(`${api}/orders?symbolId=${symbolId}`);
        const data = await res.json();
        if (res.status !== 200) throw new Error('HTTP ' + res.status);
        if (!data.length) throw new Error('empty list');
        logs.push('count=' + data.length);
    }));

    results.push(await runCase('RDBS-006', 'GET 404', async (logs) => {
        const res = await fetch(`${api}/accounts/999999`);
        if (res.status !== 404) throw new Error('expected 404 got ' + res.status);
        logs.push('404 OK');
    }));

    results.push(await runCase('RDBS-SEED', 'seed data', async (logs) => {
        const acc = await (await fetch(`${api}/accounts`)).json();
        const sym = await (await fetch(`${api}/symbols`)).json();
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
    console.log('劇情: RDBS-001=PASS; RDBS-002=PASS; RDBS-003=PASS; RDBS-004=PASS; RDBS-005=PASS; RDBS-006=PASS');
}

main().catch((e) => {
    console.error('UI_SMOKE_FAILED:', e.message || e);
    process.exit(1);
});
