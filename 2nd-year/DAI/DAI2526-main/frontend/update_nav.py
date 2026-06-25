import os
import re

dir_path = r'c:\Users\gcmon\DAI2526\frontend'

nav_block = """<nav class="kf-bottom-nav" aria-label="Navegação principal">
        <a href="index.html" class="kf-nav-item" id="nav-inicio">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
            <span>Início</span>
        </a>
        <a href="carteira.html" class="kf-nav-item" id="nav-carteira">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="5" width="20" height="14" rx="2"/>
                <path d="M16 12h4v4h-4a2 2 0 0 1 0-4z"/>
            </svg>
            <span>Carteira</span>
        </a>
        <a href="pagamentos.html" class="kf-nav-item" id="nav-pagamentos">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                <line x1="1" y1="10" x2="23" y2="10"></line>
            </svg>
            <span>Pagamentos</span>
        </a>
        <a href="perfil.html" class="kf-nav-item" id="nav-perfil">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <span>Perfil</span>
        </a>
    </nav>"""

pattern = re.compile(r'<nav class="kf-bottom-nav" aria-label="Navegação principal">.*?</nav>', re.DOTALL)

for file in os.listdir(dir_path):
    if file.endswith('.html'):
        filepath = os.path.join(dir_path, file)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        if '<nav class="kf-bottom-nav"' in content:
            new_content = pattern.sub(nav_block, content)
            
            # Simple logic to add --active class based on file name
            filename = os.path.basename(filepath)
            nav_id_to_activate = ''
            if filename == 'index.html': nav_id_to_activate = 'nav-inicio'
            elif filename == 'carteira.html': nav_id_to_activate = 'nav-carteira'
            elif filename == 'pagamentos.html': nav_id_to_activate = 'nav-pagamentos'
            elif filename == 'perfil.html': nav_id_to_activate = 'nav-perfil'
            
            if nav_id_to_activate:
                new_content = new_content.replace(f'id="{nav_id_to_activate}"', f'id="{nav_id_to_activate}" class="kf-nav-item kf-nav-item--active" aria-current="page"')
            
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f'Updated {file}')
