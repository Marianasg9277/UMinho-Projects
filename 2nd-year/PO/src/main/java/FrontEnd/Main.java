package FrontEnd;

import BackEnd.Administrador;
import BackEnd.SistemaGestao;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        
        SistemaGestao sistema = null;
        Consola consola = new Consola();
        String nomeFicheiro = "sistema.dat";

        //Tentar carregar o sistema
        File f = new File(nomeFicheiro);
        if (f.exists()) {
            sistema = SistemaGestao.carregarSistema(nomeFicheiro);
        }

        // Validação de segurança: Se o ficheiro não existir ou se der erro a ler (null)
        // iniciamos um sistema vazio para o programa não ir abaixo
        if (sistema == null) {
            System.out.println("A iniciar um novo sistema vazio...");
            sistema = new SistemaGestao();

        // Só criamos o Admin inicial se o sistema for NOVO!
            try {
                Administrador admin = new Administrador("e@mail.pt", "nome", "teste", "teste");
                sistema.adicionarAdministrador(admin);
                System.out.println("Administrador inicial criado com sucesso.");
            } catch (Exception e) {
                System.err.println("Erro ao criar dados iniciais: " + e.getMessage());
            }
        }
        //Executar o programa
        // O programa fica "preso" aqui dentro enquanto o utilizador navega nos menus
        MenuLogin menuLogin = new MenuLogin(sistema, consola);
        menuLogin.fazerLogin();

        //Guardar o sistema ao sair (só acontece quando o user escolhe "Sair" ou faz logout final)
        sistema.guardarSistema(nomeFicheiro);
        System.out.println("A encerrar a aplicação.");
    }
} 