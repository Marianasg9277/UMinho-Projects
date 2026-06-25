package BackEnd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Coordenador extends Utilizador implements Serializable{

    private Collection<Projeto> projetosGeridos;
    private static int contador = 1;

    //metodo PRIVADO para incrementar o contador e atribuir automaticamente um id ao utilizador
    private static String gerarId() {
        return "CO" + (contador++);
    }


// Construtor
    public Coordenador(String email, String nome, String username, String password) {
        super(gerarId(), email, nome, username, password);
        this.projetosGeridos = new ArrayList<>(); // começa sempre vazio
    }
    
    public static void setContador(int valor) {
        contador = valor;
    }
    
//Getters
    public int getNumeroProjetosGeridos() {
        return projetosGeridos.size();
    }
    
    public Collection<Projeto> getProjetosGeridos() {
        return new ArrayList<>(projetosGeridos); // cópia defensiva
    }
    
    
    //metdo para adicionar um projeto ao coordenador
    public void adicionarProjetoGerido(Projeto p) {
        if (p != null && !projetosGeridos.contains(p)) {
            projetosGeridos.add(p);
        }
    }

    //metodo para remover x projeto da gerencia do coordenador
    public void removerProjetoGerido(Projeto p) {
        projetosGeridos.remove(p);
    }
}