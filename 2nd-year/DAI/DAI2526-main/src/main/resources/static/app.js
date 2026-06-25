function abrirModal() {
    document.getElementById('modal-overlay').classList.remove('hidden');
    alternarVista('login');
}

function fecharModal() {
    document.getElementById('modal-overlay').classList.add('hidden');

    document.getElementById('login-erro').innerText = '';
    document.getElementById('reg-sucesso').innerText = '';

    document.getElementById('form-login').reset();
    document.getElementById('form-register').reset();

    const loginBtn = document.querySelector('#form-login .btn-primary');
    const registerBtn = document.querySelector('#form-register .btn-primary');

    if (loginBtn) {
        loginBtn.innerText = 'Iniciar sessão';
        loginBtn.disabled = false;
    }

    if (registerBtn) {
        registerBtn.innerText = 'Registar';
        registerBtn.disabled = false;
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
}

async function criarConta(event) {
    event.preventDefault();

    const nomeCompleto = document.getElementById('reg-nome').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const dataNascimento = document.getElementById('reg-data').value;
    const perfil = document.getElementById('reg-perfil').value;

    const msgSucesso = document.getElementById('reg-sucesso');
    const btn = event.target.querySelector('.btn-primary');

    msgSucesso.innerText = '';
    btn.innerText = 'A registar...';
    btn.disabled = true;

    try {
        const response = await fetch('/api/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email,
                password,
                nomeCompleto,
                dataNascimento,
                perfil
            })
        });

        const texto = await response.text();
        let data;

        try {
            data = JSON.parse(texto);
        } catch (e) {
            msgSucesso.innerText = 'Erro no servidor.';
            btn.innerText = 'Registar';
            btn.disabled = false;
            console.error('Resposta não JSON:', texto);
            return;
        }

        if (data.success) {
            msgSucesso.innerText = data.message || 'Conta criada com sucesso!';

            setTimeout(() => {
                alternarVista('login');
                document.getElementById('form-register').reset();
                document.getElementById('login-email').value = email;
                btn.innerText = 'Registar';
                btn.disabled = false;
            }, 1200);
        } else {
            msgSucesso.innerText = data.message || 'Erro ao criar conta.';
            btn.innerText = 'Registar';
            btn.disabled = false;
        }
    } catch (error) {
        console.error(error);
        msgSucesso.innerText = 'Erro ao comunicar com o servidor.';
        btn.innerText = 'Registar';
        btn.disabled = false;
    }
}

async function fazerLogin(event) {
    event.preventDefault();

    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;
    const errorMsg = document.getElementById('login-erro');
    const btn = event.target.querySelector('.btn-primary');

    errorMsg.innerText = '';
    btn.innerText = 'A verificar...';
    btn.disabled = true;

    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email,
                password
            })
        });

        const data = await response.json();

        if (data.success) {
            localStorage.setItem('nomeCompleto', data.nomeCompleto || '');
            localStorage.setItem('perfil', data.perfil || '');
            localStorage.setItem('dataNascimento', data.dataNascimento || '');

            btn.innerText = 'Sucesso!';

            setTimeout(() => {
                window.location.href = 'perfil.html';
            }, 800);
        } else {
            errorMsg.innerText = data.message || 'Email ou palavra-passe incorretos.';
            btn.innerText = 'Iniciar sessão';
            btn.disabled = false;
        }
    } catch (error) {
        console.error(error);
        errorMsg.innerText = 'Erro ao comunicar com o servidor.';
        btn.innerText = 'Iniciar sessão';
        btn.disabled = false;
    }
}

function togglePasswordVisibility(inputId, element) {
    const input = document.getElementById(inputId);
    const svg = element.querySelector('svg');
    
    if (input.type === 'password') {
        input.type = 'text';
        // Eye-off icon (crossed out)
        svg.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>';
    } else {
        input.type = 'password';
        // Normal eye icon
        svg.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
    }
}