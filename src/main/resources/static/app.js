/**
 * TradingRDBS 靜態前台：OAuth Bearer JWT + /api/v1，演示 3NF + Account(1)→Order(N)→Symbol(1)。
 */
import { createApp, ref, computed, onMounted } from 'https://unpkg.com/vue@3/dist/vue.esm-browser.js';

const API = '/api/v1';
const TOKEN_KEY = 'rdbs.jwt';

createApp({
    setup() {
        const loading = ref(false);
        const message = ref('');
        const messageType = ref('alert-info');
        const tab = ref('accounts');
        const accounts = ref([]);
        const symbols = ref([]);
        const orders = ref([]);
        const selectedAccount = ref(null);
        const orderFilter = ref(null);

        const token = ref(localStorage.getItem(TOKEN_KEY) || '');
        const username = ref('');
        const loginForm = ref({ username: 'demo', password: 'demo123' });
        const isLoggedIn = computed(() => !!token.value);

        const accountForm = ref({ accountNo: '', ownerName: '' });
        const symbolForm = ref({ ticker: '', companyName: '', exchangeCode: 'TWSE' });
        const orderForm = ref({ accountId: null, symbolId: null, side: 'BUY', quantity: 100, unitPrice: 580 });

        const orderFilterLabel = computed(() => {
            if (!orderFilter.value) return '';
            if (orderFilter.value.type === 'symbol') return `(symbolId=${orderFilter.value.id})`;
            return '';
        });

        const toast = (text, type = 'alert-info') => {
            message.value = text;
            messageType.value = type;
        };

        const authHeaders = (extra = {}) => {
            const h = { ...extra };
            if (token.value) h.Authorization = `Bearer ${token.value}`;
            return h;
        };

        const apiFetch = async (url, options = {}) => {
            const res = await fetch(url, {
                ...options,
                headers: authHeaders(options.headers || {})
            });
            if (res.status === 401 && token.value) {
                logout();
                throw new Error('登入已過期，請重新登入');
            }
            return res;
        };

        const login = async () => {
            loading.value = true;
            message.value = '';
            try {
                const res = await fetch(`${API}/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(loginForm.value)
                });
                const data = await res.json();
                if (!res.ok) {
                    toast(data.message || '登入失敗', 'alert-danger');
                    return;
                }
                token.value = data.token;
                username.value = data.username || loginForm.value.username;
                localStorage.setItem(TOKEN_KEY, data.token);
                toast(`歡迎 ${username.value}`, 'alert-success');
                await refreshAll();
            } catch (e) {
                toast(String(e), 'alert-danger');
            } finally {
                loading.value = false;
            }
        };

        const logout = () => {
            token.value = '';
            username.value = '';
            localStorage.removeItem(TOKEN_KEY);
            accounts.value = [];
            symbols.value = [];
            orders.value = [];
            selectedAccount.value = null;
            toast('已登出', 'alert-info');
        };

        const loadAccounts = async () => {
            const res = await apiFetch(`${API}/accounts`);
            if (!res.ok) throw new Error('accounts HTTP ' + res.status);
            accounts.value = await res.json();
        };

        const loadSymbols = async () => {
            const res = await apiFetch(`${API}/symbols`);
            if (!res.ok) throw new Error('symbols HTTP ' + res.status);
            symbols.value = await res.json();
        };

        const loadOrders = async (symbolId) => {
            const url = symbolId ? `${API}/orders?symbolId=${symbolId}` : `${API}/orders`;
            const res = await apiFetch(url);
            if (!res.ok) throw new Error('orders HTTP ' + res.status);
            orders.value = await res.json();
        };

        const refreshAll = async () => {
            if (!isLoggedIn.value) return;
            loading.value = true;
            message.value = '';
            try {
                await loadAccounts();
                await loadSymbols();
                const sid = orderFilter.value?.type === 'symbol' ? orderFilter.value.id : null;
                await loadOrders(sid);
                if (selectedAccount.value) {
                    await loadAccountDetail(selectedAccount.value.id);
                }
            } catch (e) {
                toast(String(e), 'alert-danger');
            } finally {
                loading.value = false;
            }
        };

        const loadAccountDetail = async (id) => {
            loading.value = true;
            try {
                const res = await apiFetch(`${API}/accounts/${id}`);
                if (!res.ok) throw new Error('account detail HTTP ' + res.status);
                selectedAccount.value = await res.json();
            } catch (e) {
                toast(String(e), 'alert-danger');
            } finally {
                loading.value = false;
            }
        };

        const createAccount = async () => {
            loading.value = true;
            try {
                const res = await apiFetch(`${API}/accounts`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(accountForm.value)
                });
                const data = await res.json();
                if (!res.ok) {
                    toast(data.message || '建立失敗', 'alert-danger');
                    return;
                }
                toast(`帳戶已建立 id=${data.id}`, 'alert-success');
                accountForm.value = { accountNo: '', ownerName: '' };
                await loadAccounts();
            } catch (e) {
                toast(String(e), 'alert-danger');
            } finally {
                loading.value = false;
            }
        };

        const createSymbol = async () => {
            loading.value = true;
            try {
                const res = await apiFetch(`${API}/symbols`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(symbolForm.value)
                });
                const data = await res.json();
                if (!res.ok) {
                    toast(data.message || '建立失敗', 'alert-danger');
                    return;
                }
                toast(`標的已建立 ticker=${data.ticker}`, 'alert-success');
                symbolForm.value = { ticker: '', companyName: '', exchangeCode: 'TWSE' };
                await loadSymbols();
            } catch (e) {
                toast(String(e), 'alert-danger');
            } finally {
                loading.value = false;
            }
        };

        const createOrder = async () => {
            loading.value = true;
            try {
                const res = await apiFetch(`${API}/orders`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(orderForm.value)
                });
                const data = await res.json();
                if (!res.ok) {
                    toast(data.message || '建立失敗', 'alert-danger');
                    return;
                }
                toast(`委託已建立 id=${data.id} (${data.accountNo} → ${data.ticker})`, 'alert-success');
                orderFilter.value = null;
                await loadOrders();
                if (orderForm.value.accountId) {
                    await loadAccountDetail(orderForm.value.accountId);
                }
            } catch (e) {
                toast(String(e), 'alert-danger');
            } finally {
                loading.value = false;
            }
        };

        const filterOrdersBySymbol = async (symbolId) => {
            tab.value = 'orders';
            orderFilter.value = { type: 'symbol', id: symbolId };
            loading.value = true;
            try {
                await loadOrders(symbolId);
                toast(`已篩選 symbolId=${symbolId}（N→1 反向）`, 'alert-info');
            } catch (e) {
                toast(String(e), 'alert-danger');
            } finally {
                loading.value = false;
            }
        };

        const clearOrderFilter = async () => {
            orderFilter.value = null;
            await loadOrders();
        };

        onMounted(() => {
            if (isLoggedIn.value) refreshAll();
        });

        return {
            loading, message, messageType, tab,
            accounts, symbols, orders, selectedAccount, orderFilter, orderFilterLabel,
            accountForm, symbolForm, orderForm,
            loginForm, isLoggedIn, username,
            login, logout, refreshAll, loadAccountDetail, createAccount, createSymbol, createOrder,
            filterOrdersBySymbol, clearOrderFilter
        };
    }
}).mount('#app');
