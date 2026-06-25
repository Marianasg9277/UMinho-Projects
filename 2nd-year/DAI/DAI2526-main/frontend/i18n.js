// ── i18n.js – Internacionalização PT / EN ────────────────────────────────────
// SW.06 / UI Requirements – Internacionalização básica

const TRANSLATIONS = {
  pt: {
    // Navbar
    'nav.login': 'Iniciar sessão',
    'nav.profile': 'Dados Pessoais',
    'nav.logout': 'Terminar sessão',
    'nav.admin': 'Backoffice',
    'nav.notifications': 'Notificações',
    // Bottom nav
    'nav.home': 'Início',
    'nav.lines': 'Linhas',
    'nav.tickets': 'Bilhetes',
    'nav.alerts': 'Avisos',
    // Login modal
    'login.title': 'Aceder',
    'login.email': 'Endereço de E-mail',
    'login.password': 'Palavra-passe',
    'login.submit': 'Iniciar sessão',
    'login.create': 'Criar conta',
    'login.forgot': 'Esqueci-me da password >',
    // Register
    'register.title': 'Criar Conta',
    'register.next': 'Seguinte',
    'register.back': 'Voltar ao Login',
    'register.submit': 'Registar',
    'register.return': 'Voltar',
    // Home
    'home.arrivals': 'Próximos autocarros',
    'home.live': 'AO VIVO',
    'home.seeAll': 'Ver todos',
    'home.buyTicket': 'Comprar Bilhete',
    'home.seeLines': 'Ver Linhas',
    'home.alerts': 'Avisos',
    'home.pricing': 'Ver Preçário',
    // Notifications
    'notif.title': 'Notificações',
    'notif.empty': 'Sem notificações.',
    'notif.markRead': 'Marcar como lida',
    'notif.markAllRead': 'Marcar todas como lidas',
    'notif.read': 'Lida',
    // Admin
    'admin.title': 'Backoffice',
    'admin.stats': 'Resumo do Sistema',
    'admin.logs': 'Logs de Auditoria',
    'admin.export': 'Exportação de Dados',
    'admin.alertsMgmt': 'Gestão de Avisos',
    'admin.users': 'Utilizadores',
    'admin.transactions': 'Transações',
    'admin.accessDenied': 'Acesso negado. Apenas administradores.',
    // Help
    'help.title': 'Ajuda Online',
    'help.close': 'Fechar',
    // General
    'general.loading': 'A carregar…',
    'general.error': 'Erro ao carregar dados.',
    'general.empty': 'Sem dados disponíveis.',
    'general.confirm': 'Tem a certeza?',
    'general.success': 'Operação concluída com sucesso.',
    'lang.pt': 'PT',
    'lang.en': 'EN',
  },
  en: {
    // Navbar
    'nav.login': 'Sign in',
    'nav.profile': 'My Profile',
    'nav.logout': 'Sign out',
    'nav.admin': 'Backoffice',
    'nav.notifications': 'Notifications',
    // Bottom nav
    'nav.home': 'Home',
    'nav.lines': 'Lines',
    'nav.tickets': 'Tickets',
    'nav.alerts': 'Alerts',
    // Login modal
    'login.title': 'Sign In',
    'login.email': 'E-mail address',
    'login.password': 'Password',
    'login.submit': 'Sign in',
    'login.create': 'Create account',
    'login.forgot': 'Forgot password >',
    // Register
    'register.title': 'Create Account',
    'register.next': 'Next',
    'register.back': 'Back to Login',
    'register.submit': 'Register',
    'register.return': 'Back',
    // Home
    'home.arrivals': 'Next buses',
    'home.live': 'LIVE',
    'home.seeAll': 'See all',
    'home.buyTicket': 'Buy Ticket',
    'home.seeLines': 'View Lines',
    'home.alerts': 'Alerts',
    'home.pricing': 'View Pricing',
    // Notifications
    'notif.title': 'Notifications',
    'notif.empty': 'No notifications.',
    'notif.markRead': 'Mark as read',
    'notif.markAllRead': 'Mark all as read',
    'notif.read': 'Read',
    // Admin
    'admin.title': 'Backoffice',
    'admin.stats': 'System Summary',
    'admin.logs': 'Audit Logs',
    'admin.export': 'Data Export',
    'admin.alertsMgmt': 'Manage Alerts',
    'admin.users': 'Users',
    'admin.transactions': 'Transactions',
    'admin.accessDenied': 'Access denied. Admin only.',
    // Help
    'help.title': 'Online Help',
    'help.close': 'Close',
    // General
    'general.loading': 'Loading…',
    'general.error': 'Error loading data.',
    'general.empty': 'No data available.',
    'general.confirm': 'Are you sure?',
    'general.success': 'Operation completed successfully.',
    'lang.pt': 'PT',
    'lang.en': 'EN',
  }
};

// ── Language management ───────────────────────────────────────────────────────

let currentLang = localStorage.getItem('tub_lang') || 'pt';

function t(key) {
  return (TRANSLATIONS[currentLang] && TRANSLATIONS[currentLang][key])
      || (TRANSLATIONS['pt'] && TRANSLATIONS['pt'][key])
      || key;
}

function setLang(lang) {
  if (!TRANSLATIONS[lang]) return;
  currentLang = lang;
  localStorage.setItem('tub_lang', lang);
  applyTranslations();
  updateLangButtons();
}

function applyTranslations() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    const val = t(key);
    if (el.tagName === 'INPUT' && el.hasAttribute('placeholder')) {
      el.placeholder = val;
    } else {
      el.textContent = val;
    }
  });
}

function updateLangButtons() {
  document.querySelectorAll('.lang-btn').forEach(btn => {
    btn.classList.toggle('lang-btn--active', btn.dataset.lang === currentLang);
  });
}

// ── Init on DOM ready ─────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  applyTranslations();
  updateLangButtons();
});

// ── Toast notifications ───────────────────────────────────────────────────────

function showToast(message, type = 'info') {
  const existing = document.getElementById('tub-toast');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.id = 'tub-toast';
  toast.className = `tub-toast tub-toast--${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);

  // Trigger animation
  requestAnimationFrame(() => toast.classList.add('tub-toast--visible'));

  setTimeout(() => {
    toast.classList.remove('tub-toast--visible');
    setTimeout(() => toast.remove(), 400);
  }, 3500);
}
