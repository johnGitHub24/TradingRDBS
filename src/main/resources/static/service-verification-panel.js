/**
 * EOS Service Verification Panel — header dropdown from service-links.manifest.json
 * SSOT: docs/service-links.manifest.json (knowledge/service-verification-panel.md)
 * Rule: only groups/items listed in manifest render; empty groups skipped; footer from manifest.
 */
(function () {
  const MOUNT_ID = 'eos-svp-mount';
  const MANIFEST_URL = '/service-links.manifest.json';
  const FOOTER_BLUEPRINT_LABEL = '系統運作藍圖';
  const FOOTER_TABLE_LABEL = '服務驗證總表';

  function esc(s) {
    const d = document.createElement('div');
    d.textContent = s == null ? '' : String(s);
    return d.innerHTML;
  }

  function findItem(data, label) {
    for (const g of data.groups || []) {
      for (const item of g.items || []) {
        if (item.label === label) return item;
      }
    }
    return null;
  }

  function openLink(item) {
    if (!item || !item.url) return;
    window.open(item.url, '_blank', 'noopener,noreferrer');
  }

  function renderPanel(root, data) {
    root.innerHTML = '';
    root.className = 'eos-svp-root';

    const groups = (data.groups || []).filter((g) => (g.items || []).length > 0);
    if (groups.length === 0) {
      root.innerHTML = '<span class="muted small">系統檢測（無連結）</span>';
      return;
    }

    const toggle = document.createElement('button');
    toggle.type = 'button';
    toggle.className = 'eos-svp-toggle';
    toggle.setAttribute('aria-expanded', 'false');
    toggle.innerHTML = '<i class="bi bi-grid-3x3-gap"></i> 系統檢測';
    root.appendChild(toggle);

    const drop = document.createElement('div');
    drop.className = 'eos-svp-dropdown';
    drop.hidden = true;
    drop.setAttribute('role', 'dialog');
    drop.setAttribute('aria-label', '服務驗證與學習連結');

    const head = document.createElement('div');
    head.className = 'eos-svp-dropdown-head';
    head.innerHTML = '<strong>' + esc(data.project || 'Demo') + '</strong>'
      + '<span class="muted small">觀測 · API · 文件 · 測試</span>';
    drop.appendChild(head);

    const body = document.createElement('div');
    body.className = 'eos-svp-dropdown-body';

    groups.forEach((g) => {
      const sec = document.createElement('section');
      sec.className = 'eos-svp-group';
      const h = document.createElement('h6');
      h.textContent = g.title || 'Links';
      sec.appendChild(h);
      const chips = document.createElement('div');
      chips.className = 'eos-svp-chips';
      (g.items || []).forEach((item) => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'eos-svp-chip';
        if (item.optional) btn.classList.add('optional');
        btn.textContent = item.label || item.url || '?';
        if (item.hint) btn.title = item.hint;
        btn.addEventListener('click', () => openLink(item));
        chips.appendChild(btn);
      });
      sec.appendChild(chips);
      body.appendChild(sec);
    });

    const foot = document.createElement('div');
    foot.className = 'eos-svp-dropdown-foot';
    const footParts = [];
    const blueprintItem = findItem(data, FOOTER_BLUEPRINT_LABEL);
    if (blueprintItem) {
      footParts.push(
        '<a href="' + esc(blueprintItem.url) + '" class="eos-svp-blueprint">'
        + esc(FOOTER_BLUEPRINT_LABEL) + '</a>'
      );
    }
    const tableItem = findItem(data, FOOTER_TABLE_LABEL);
    if (tableItem) {
      footParts.push(
        '<a href="' + esc(tableItem.url) + '" target="_blank" rel="noopener">完整總表</a>'
      );
    }
    if (footParts.length) {
      foot.innerHTML = footParts.join('');
      drop.appendChild(body);
      drop.appendChild(foot);
    } else {
      drop.appendChild(body);
    }

    root.appendChild(drop);

    toggle.addEventListener('click', (e) => {
      e.stopPropagation();
      const open = drop.hidden;
      drop.hidden = !open;
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    });

    document.addEventListener('click', (e) => {
      if (!root.contains(e.target)) {
        drop.hidden = true;
        toggle.setAttribute('aria-expanded', 'false');
      }
    });
  }

  async function init() {
    const mount = document.getElementById(MOUNT_ID);
    if (!mount) return;
    try {
      const res = await fetch(MANIFEST_URL, { cache: 'no-store' });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      const data = await res.json();
      renderPanel(mount, data);
    } catch (e) {
      console.warn('eos-svp: manifest load failed', e);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
