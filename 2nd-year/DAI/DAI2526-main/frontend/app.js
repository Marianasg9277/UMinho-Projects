// ── Base API URL ─────────────────────────────────────────────────────────────
const API_BASE = '';

// ─────────────────────────────────────────────────────────────────────────────
// Modal helpers
// ─────────────────────────────────────────────────────────────────────────────

function abrirModal() {
    document.getElementById('modal-overlay').classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    document.documentElement.style.overflow = 'hidden';
    alternarVista('login');
}

function fecharModal() {
    document.getElementById('modal-overlay').classList.add('hidden');
    document.body.style.overflow = '';
    document.documentElement.style.overflow = '';

    document.getElementById('login-erro').innerText = '';
    document.getElementById('reg-sucesso').innerText = '';

    document.getElementById('form-login').reset();
    document.getElementById('form-register').reset();

    const loginBtn = document.querySelector('#form-login .btn-primary');
    const registerBtn = document.querySelector('#form-register .btn-primary');

    if (loginBtn) { loginBtn.innerText = t('login.submit'); loginBtn.disabled = false; }
    if (registerBtn) { registerBtn.innerText = t('register.submit'); registerBtn.disabled = false; }

    const regStep1 = document.getElementById('reg-step-1');
    const regStep2 = document.getElementById('reg-step-2');
    if(regStep1 && regStep2) {
        regStep1.classList.remove('hidden');
        regStep2.classList.add('hidden');
    }
}

function alternarVista(vista) {
    const modalLogin = document.getElementById('modal-login');
    const modalRegister = document.getElementById('modal-register');

    document.getElementById('login-erro').innerText = '';
    document.getElementById('reg-sucesso').innerText = '';

    if (vista === 'login') {
        modalRegister.classList.add('hidden');
        modalLogin.classList.remove('hidden');
    } else {
        modalLogin.classList.add('hidden');
        modalRegister.classList.remove('hidden');
    }

    const regStep1 = document.getElementById('reg-step-1');
    const regStep2 = document.getElementById('reg-step-2');
    if(regStep1 && regStep2) {
        regStep1.classList.remove('hidden');
        regStep2.classList.add('hidden');
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Help modal
// ─────────────────────────────────────────────────────────────────────────────

function abrirAjuda() {
    const overlay = document.getElementById('help-overlay');
    if (overlay) overlay.classList.remove('hidden');
}
function fecharAjuda(target) {
    const overlay = document.getElementById('help-overlay');
    if (!target || target === overlay) {
        if (overlay) overlay.classList.add('hidden');
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Auth – Register
// ─────────────────────────────────────────────────────────────────────────────

function avancarRegisto() {
    const email = document.getElementById('reg-email');
    const pwd = document.getElementById('reg-password');
    const pwdConf = document.getElementById('reg-confirm-password');

    pwdConf.setCustomValidity("");

    if (!email.reportValidity() || !pwd.reportValidity() || !pwdConf.reportValidity()) {
        return;
    }

    if (pwd.value !== pwdConf.value) {
        pwdConf.setCustomValidity("As passwords não coincidem.");
        pwdConf.reportValidity();
        return;
    } else {
        pwdConf.setCustomValidity("");
    }

    document.getElementById('reg-step-1').classList.add('hidden');
    document.getElementById('reg-step-2').classList.remove('hidden');
}

function voltarRegisto() {
    document.getElementById('reg-step-2').classList.add('hidden');
    document.getElementById('reg-step-1').classList.remove('hidden');
}

async function criarConta(event) {
    event.preventDefault();

    const nome             = document.getElementById('reg-nome').value.trim();
    const sobrenome        = document.getElementById('reg-sobrenome')?.value.trim() ?? '';
    const email            = document.getElementById('reg-email').value.trim();
    const password         = document.getElementById('reg-password').value;
    const confirmPassword  = document.getElementById('reg-confirm-password')?.value ?? password;
    const dataNascimento   = document.getElementById('reg-data').value;
    const morada           = document.getElementById('reg-morada')?.value.trim() ?? '';
    const nif              = document.getElementById('reg-nif')?.value.trim() ?? '';
    const telefone         = document.getElementById('reg-telefone')?.value.trim() ?? '';
    const numeroCartaoCidadao = document.getElementById('reg-cc')?.value.trim() ?? '';

    const msgSucesso = document.getElementById('reg-sucesso');
    const btn = event.target.querySelector('.btn-primary');

    msgSucesso.style.color = '';
    msgSucesso.innerText = '';
    btn.innerText = 'A registar…';
    btn.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/api/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({
                email, password, confirmPassword, nome, sobrenome,
                dataNascimento, morada, nif, telefone, numeroCartaoCidadao
            })
        });

        const texto = await response.text();
        let data;
        try { data = JSON.parse(texto); } catch (e) {
            msgSucesso.innerText = 'Erro no servidor.';
            btn.innerText = 'Registar'; btn.disabled = false;
            return;
        }

        if (data.success) {
            msgSucesso.innerText = 'Conta criada! A iniciar sessão...';
            try {
                const loginRes = await fetch(`${API_BASE}/api/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify({ email, password })
                });
                const loginData = await loginRes.json();
                if (loginData.success) {
                    // Security: store only minimum necessary data
                    localStorage.setItem('nomeCompleto', loginData.nomeCompleto || `${nome} ${sobrenome}`);
                    localStorage.setItem('perfil', loginData.perfil || '');
                    localStorage.setItem('role', loginData.role || 'CLIENTE');
                    localStorage.setItem('idCliente', loginData.idCliente ? String(loginData.idCliente) : '');
                    // Do NOT store NIF, CC, morada, telefone, dataNascimento in localStorage
                }
            } catch(e) { console.error('Auto login falhou', e); }

            setTimeout(() => { window.location.href = 'index.html'; }, 800);
        } else {
            msgSucesso.style.color = 'red';
            msgSucesso.innerText = data.message || 'Erro ao criar conta.';
            btn.innerText = 'Registar'; btn.disabled = false;
        }
    } catch (error) {
        console.error(error);
        msgSucesso.style.color = 'red';
        msgSucesso.innerText = 'Erro ao comunicar com o servidor.';
        btn.innerText = 'Registar'; btn.disabled = false;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Auth – Login
// ─────────────────────────────────────────────────────────────────────────────

async function fazerLogin(event) {
    event.preventDefault();

    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    const errorMsg = document.getElementById('login-erro');
    const btn = event.target.querySelector('.btn-primary');

    errorMsg.innerText = '';
    btn.innerText = 'A verificar…';
    btn.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (data.success) {
            // Security: store minimal data — only name, profile type, role, and client id
            localStorage.setItem('nomeCompleto', data.nomeCompleto || '');
            localStorage.setItem('perfil', data.perfil || '');
            localStorage.setItem('role', data.role || 'CLIENTE');
            localStorage.setItem('idCliente', data.idCliente ? String(data.idCliente) : '');
            // Remove any previously stored sensitive data
            ['dataNascimento', 'morada', 'nif', 'telefone', 'cc'].forEach(k => localStorage.removeItem(k));
            btn.innerText = 'Sucesso!';
            // Redirecionamento por role ao login:
            //   ADMIN            → backoffice.html
            //   GESTOR_SERVICOS  → backoffice.html
            //   GESTOR_FROTAS    → backoffice.html
            //   MOTORISTA        → verificar_qr.html  (única função desta role)
            //   FISCALIZADOR     → fiscalizacao.html  (única função desta role)
            //   CLIENTE          → index.html         (página principal do cliente)
            let destino = 'index.html';
            if (['ADMIN', 'GESTOR_SERVICOS', 'GESTOR_FROTAS'].includes(data.role)) destino = 'backoffice.html';
            if (data.role === 'MOTORISTA')    destino = 'verificar_qr.html';
            if (data.role === 'FISCALIZADOR') destino = 'fiscalizacao.html';
            setTimeout(() => { window.location.href = destino; }, 800);
        } else {
            errorMsg.innerText = data.message || 'Email ou palavra-passe incorretos.';
            btn.innerText = 'Iniciar sessão'; btn.disabled = false;
        }
    } catch (error) {
        console.error(error);
        errorMsg.innerText = 'Erro ao comunicar com o servidor.';
        btn.innerText = 'Iniciar sessão'; btn.disabled = false;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dynamic data – Home page
// ─────────────────────────────────────────────────────────────────────────────

async function carregarAutocarros() {
    const container = document.getElementById('kf-bus-list');
    if (!container) return;

    try {
        const res = await fetch(`${API_BASE}/api/autocarros`);
        const horarios = await res.json();

        if (!horarios.length) {
            container.innerHTML = '<div style="padding:16px;text-align:center;color:#94a3b8;">Sem dados disponíveis.</div>';
            return;
        }

        const shown = horarios.slice(0, 2);
        container.innerHTML = shown.map((h, i) => `
            <div class="kf-bus-row">
                <div class="kf-bus-info">
                    <span class="kf-bus-line${i % 2 === 1 ? ' kf-bus-line--alt' : ''}"
                          style="background:${h.linha.cor || '#0ea5e9'};box-shadow:0 4px 12px ${(h.linha.cor || '#0ea5e9')}66">
                        ${h.linha.numero}
                    </span>
                    <div>
                        <p class="kf-bus-dest">${h.linha.origem} &rarr; ${h.linha.destino}</p>
                        <p class="kf-bus-stop">Paragem: ${h.paragem}</p>
                    </div>
                </div>
                <div class="kf-bus-time">
                    <span class="kf-min">${h.minutosAte}</span>
                    <span class="kf-min-label">min</span>
                </div>
            </div>
            ${i < shown.length - 1 ? '<div class="kf-bus-divider"></div>' : ''}
        `).join('');
    } catch (e) {
        console.error('Erro ao carregar autocarros:', e);
        container.innerHTML = '<div style="padding:16px;text-align:center;color:#ef4444;">Erro ao carregar dados.</div>';
    }
}

async function carregarContadorAvisos() {
    try {
        const res = await fetch(`${API_BASE}/api/avisos`);
        const avisos = await res.json();
        const novos = avisos.filter(a => a.novo).length;

        const badge = document.getElementById('avisos-badge');
        if (badge) badge.textContent = novos > 0 ? `${novos} novo${novos > 1 ? 's' : ''}` : '0 novos';

        const dot = document.querySelector('.kf-nav-dot');
        if (dot) dot.style.display = novos > 0 ? '' : 'none';
    } catch (e) {
        console.error('Erro ao carregar avisos:', e);
    }
}

/**
 * Load unread notification count for the authenticated user
 * and show notification bell with badge.
 */
async function carregarContagemNotificacoes() {
    const bellBtn = document.getElementById('nav-notif-btn');
    const countBadge = document.getElementById('notif-count');
    if (!bellBtn || !countBadge) return;

    if (!localStorage.getItem('nomeCompleto')) return; // not logged in
    if (localStorage.getItem('role') === 'MOTORISTA') {
        bellBtn.style.display = 'none';
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/user/notificacoes/count`, { credentials: 'include' });
        if (!res.ok) return;
        const data = await res.json();
        const count = data.naoLidas || 0;

        if (count > 0) {
            countBadge.textContent = count > 99 ? '99+' : count;
            countBadge.classList.remove('hidden');
        } else {
            countBadge.classList.add('hidden');
        }
    } catch (e) {
        // Silently ignore — user may not be authenticated
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Navbar state – adapt based on authentication
// ─────────────────────────────────────────────────────────────────────────────

function esconderElemento(id) {
    const el = document.getElementById(id);
    if (el) el.style.display = 'none';
}

function atualizarLabelNavegacao(id, href, label) {
    const el = document.getElementById(id);
    if (!el) return;
    el.href = href;
    const texto = el.querySelector('span');
    if (texto) texto.innerText = label;
}

function atualizarNavbar() {
    const nomeCompleto = localStorage.getItem('nomeCompleto');
    const role = localStorage.getItem('role');

    const navBtn = document.getElementById('nav-main-btn');
    const notifBtn = document.getElementById('nav-notif-btn');
    const adminLink = document.getElementById('nav-admin-link');

    if (nomeCompleto) {
        // Logged in
        if (navBtn) {
            navBtn.innerText = 'Dados Pessoais';
            navBtn.onclick = () => { window.location.href = 'perfil.html'; };
        }
        // Show notification bell
        if (notifBtn) notifBtn.style.display = 'flex';
        // Show admin link if ADMIN
        if (adminLink && role === 'ADMIN') {
            adminLink.classList.remove('hidden');
        } else if (adminLink) {
            adminLink.classList.add('hidden');
        }
    } else {
        // Not logged in
        if (navBtn) {
            navBtn.innerText = t('nav.login');
            navBtn.onclick = abrirModal;
        }
        if (notifBtn) notifBtn.style.display = 'none';
        if (adminLink) adminLink.classList.add('hidden');
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────────────────────────────────────

function togglePasswordVisibility(inputId, element) {
    const input = document.getElementById(inputId);
    const svg = element.querySelector('svg');

    if (input.type === 'password') {
        input.type = 'text';
        svg.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>';
    } else {
        input.type = 'password';
        svg.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Validação de Sessão – mata sessões fantasma após reinício do backend
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Verifica silenciosamente se a sessão do backend ainda é válida.
 * Se o backend devolver 401/403 (sessão expirada ou BD reiniciada),
 * limpa o localStorage e redireciona para o início.
 */
async function validarSessaoAtiva() {
    if (!localStorage.getItem('nomeCompleto')) return; // sem sessão local, nada a fazer
    try {
        const res = await fetch(`${API_BASE}/api/me`, { credentials: 'include' });
        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            window.location.replace('index.html');
        }
    } catch (e) {
        // Erro de rede (servidor desligado) — não limpar sessão agressivamente
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Estatuto – carrega e guarda no arranque da sessão
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Se existir sessão válida, faz fetch ao estatuto efetivo e guarda-o no
 * localStorage para que outras páginas o possam usar sem novo pedido.
 */
async function sincronizarEstatuto() {
    if (!localStorage.getItem('nomeCompleto')) return;
    // Roles operacionais (MOTORISTA) não têm estatuto de cliente — evitar 403 desnecessário
    const role = localStorage.getItem('role') || '';
    if (role !== 'CLIENTE' && role !== 'ADMIN') return;
    try {
        const res = await fetch(`${API_BASE}/api/user/estatuto/atual`, { credentials: 'include' });
        if (!res.ok) return;
        const data = await res.json();
        localStorage.setItem('estatuto', data.tipoEstatuto || 'SEM_ESTATUTO');
        localStorage.setItem('estatutoAutomatico', data.automatico ? 'true' : 'false');
    } catch (e) {
        // Sessão pode ter expirado — não bloquear o arranque
        console.error('Erro ao sincronizar estatuto:', e);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Init
// ─────────────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
    validarSessaoAtiva();
    atualizarNavbar();
    carregarAutocarros();
    carregarContadorAvisos();
    carregarContagemNotificacoes();
    sincronizarEstatuto();

    // Bloqueio de navegação — "Comprar Passe" requer sessão iniciada
    const btnComprarPasse = document.getElementById('btn-comprar-passe');
    if (btnComprarPasse) {
        btnComprarPasse.addEventListener('click', function (e) {
            if (!localStorage.getItem('nomeCompleto')) {
                e.preventDefault();
                abrirModal();
            }
        });
    }
});
