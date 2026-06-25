package FrontEnd;

import BackEnd.Administrador;
import BackEnd.Coordenador;
import BackEnd.Investigador;
import BackEnd.SistemaGestao;
import BackEnd.Utilizador;



public class MenuLogin {
    private SistemaGestao sistema;
    private Consola consola;
    

    public MenuLogin(SistemaGestao sistema, Consola consola) {
        this.sistema = sistema;
        this.consola = consola;
    }
    
    void fazerLogin() {

        Utilizador user = null;

        for (int i = 0; i < 3 && user == null; i++) {
            String username = consola.lerString("Username: ");
            String password = consola.lerString("Password: ");

            user = sistema.validarLogin(username, password);

            if (user == null)
                consola.escreverErro("Credenciais inválidas.");
        }

        if (user == null) {
            consola.escreverErro("Conta bloqueada ou 3 tentativas falhadas. Contacte o Administrador.");
            return;
        }

        consola.escrever("Login efetuado com sucesso!\n");

        if (user instanceof Administrador) {
            new MenuAdministrador(sistema, consola, (Administrador) user).mostrarMenu();

        } else if (user instanceof Coordenador) {
            new MenuCoordenador(sistema, consola, (Coordenador) user).mostrarMenu();

        } else if (user instanceof Investigador) {
            new MenuInvestigador(sistema, consola, (Investigador) user).mostrarMenu((Investigador) user);
        }
    }

    
}