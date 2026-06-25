package BackEnd;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

public class ListaCoordenadores implements Serializable {

    private HashMap<String, Coordenador> coordenadores;

//construtores
    //Construtor que cria um HashMap com capacidade inicial que é por default 15
    public ListaCoordenadores(){
        coordenadores = new HashMap<>();
    }
    
    //construtor com capacidade inicial
     public ListaCoordenadores(int capacidade){
        coordenadores = new HashMap<>  (capacidade);
    }

    //Construtor que recebe uma coleção e cria uma cópia
    public ListaCoordenadores(Collection<Coordenador> lista) {
        HashMap<String, Coordenador> outro = new HashMap<> (lista.size());
        
        for(Coordenador co: lista){
            outro.put(co.getIdUtilizador(), co);
        }
       coordenadores = outro;
    }

    //Metodo para adicionar coordenador
    public void adicionarCoordenador(Coordenador c) {
        if (c != null) {
            coordenadores.put(c.getIdUtilizador(), c);
        }
    }
    
    //metodo para remover coordenador
    public void removerCoordenador(Coordenador c){
        if (c != null) {
            coordenadores.remove(c.getIdUtilizador(), c);
        }
    }
    
    //Metodo para procurar por ids 
    public Coordenador procurarPorId(String id) {
        return coordenadores.get(id);
    }

    //Metodo para devolver o tamanho da lista
    public int tamanhoListaCoordenadores() {
        return coordenadores.size();
    }

    public Collection<Coordenador> getTodos() {
        return coordenadores.values();
    }
    
}