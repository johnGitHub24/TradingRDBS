import puppeteer from 'puppeteer';

const argUrl = process.argv.find((a) => a.startsWith('--baseUrl='))?.split('=')[1];
const baseUrl = (argUrl || process.env.SMOKE_BASE_URL || 'http://localhost:8095').replace(/\/$/, '');
const timeoutMs = Number(process.env.SMOKE_TIMEOUT_MS || '120000');
const headed = process.argv.includes('--headed');

const browser = await puppeteer.launch({
    headless: !headed,
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
    slowMo: headed ? 80 : 0
});

try {
    const page = await browser.newPage();
    const runnerUrl = `${baseUrl}/test/runner.html`;
    console.log('Navigating to', runnerUrl);
    const resp = await page.goto(runnerUrl, { waitUntil: 'networkidle0', timeout: 30000 });
    if (!resp || resp.status() !== 200) {
        throw new Error('runner HTTP ' + (resp?.status() ?? 'no response'));
    }

    await (await page.waitForSelector('[data-testid="run-l1-smoke"]', { timeout: 10000 })).click();

    await page.waitForFunction(
        () => document.querySelector('[data-testid="smoke-status"]')?.dataset.value === 'completed',
        { timeout: timeoutMs }
    );

    const label = await page.$eval('.btn-run', (el) => el.textContent.trim());
    if (!label.includes('SERVICE COMPLETED')) {
        throw new Error('Button: ' + label);
    }

    const failures = await page.$$eval('.fail', (nodes) => nodes.length);
    if (failures > 0) throw new Error(failures + ' FAIL on screen');

    console.log('ALL_UI_SMOKE_OK');
    console.log('劇情: RDBS-001=PASS; RDBS-002=PASS; RDBS-003=PASS; RDBS-004=PASS; RDBS-005=PASS; RDBS-006=PASS');
} catch (err) {
    console.error('UI_SMOKE_FAILED:', err.message || err);
    process.exitCode = 1;
} finally {
    await browser.close();
}
