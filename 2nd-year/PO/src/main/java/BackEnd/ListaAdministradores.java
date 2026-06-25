package BackEnd;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class ListaAdministradores implements Serializable {

    private HashMap<String, Administrador> administradores;

//construtores
    //Construtor que cria um HashMap com capacidade inicial que é por default 15
    public ListaAdministradores(){
        administradores = new HashMap<>();
    }
    
    //construtor com capacidade inicial
     public ListaAdministradores(int capacidade){
        administradores = new HashMap<>  (capacidade);
    }

    //Construtor que recebe uma coleção e cria uma cópia
    public ListaAdministradores(Collection<Administrador> lista) {
        HashMap<String, Administrador> outro = new HashMap<> (lista.size());
        
        for(Administrador admin: lista){
            outro.put(admin.getIdUtilizador(), admin);
        }
       administradores = outro;
    }

    //metodo para adicionar admin lista 
    public void adicionarAdministrador (Administrador admin) {
        if (admin != null) {
            administradores.put(admin.getIdUtilizador(), admin);
        }
    }

    //metodo para emover da lista
    public void removerAdministrador (Administrador admin) {
        if (admin != null) {
            administradores.remove(admin.getIdUtilizador(), admin);
        }
    }
    
    //Metodo para procurar daodo o seu id 
    public Administrador procurarPorId(String id) {
        return administradores.get(id);
    }

    //devolve o tamanho total da lista de admnistradores
    public int tamanhoAdministradores() {
        return administradores.size();
    }
    
    public Collection<Administrador> getTodos() {
        return administradores.values();
    }
}