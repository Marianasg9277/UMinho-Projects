/**
 * session-guard.js
 * Executa imediatamente ao ser carregado (IIFE).
 * Se existir uma sessão local mas o backend a rejeitar (401/403),
 * limpa o localStorage e redireciona para o início — elimina sessões fantasma.
 */
(async function validarSessaoAtiva() {
    if (!localStorage.getItem('nomeCompleto')) return;
    try {
        const res = await fetch('/api/me', { credentials: 'include' });
        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            window.location.replace('index.html');
        }
    } catch (e) {
        // Servidor inacessível (rede/offline) — não apagar sessão agressivamente
    }
})();
