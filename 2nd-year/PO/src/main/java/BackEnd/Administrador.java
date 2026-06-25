package BackEnd;

import java.io.Serializable;

public class Administrador extends Utilizador implements Serializable {

    private static int contador = 1;

    //metodo PRIVADO para incrementar o contador e atribuir automaticamente um id ao utilizador
    private static String gerarId() {
        return "ADMIN" + (contador++);
    }

    public Administrador(String email, String nome, String username, String password) {
        super(gerarId(), email, nome, username, password);
    }
    
    public static void setContador(int valor) {
        contador = valor;
    }
} 