package BackEnd;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
        
public class ListaLaboratorios implements Serializable {
    private HashMap<String, Laboratorio> laboratorios;
    
    public ListaLaboratorios(){
        laboratorios = new HashMap<>();
    }//Cria uma lista vazia sem objetos
    
    
     public ListaLaboratorios(int capacidade){
        laboratorios = new HashMap<>  (capacidade);
    }//construtor com capacidade inicial

    
    public ListaLaboratorios(Collection<Laboratorio> labs){
        HashMap<String, Laboratorio> outro = new HashMap<> (labs.size());
        for(Laboratorio l: labs){
            outro.put(l.getIdLaboratorio(), l);
        }
       laboratorios = outro;
    }//Cria uma cópia da lista, quanod esta já existe!
    
    
    //Adicionar ou Remover Laboratorio
    public void adicionarLaboratorio(Laboratorio lab){
        if(!laboratorios.containsValue(lab) && lab !=null)
            laboratorios.put(lab.getIdLaboratorio(), lab);
    }
    
    public void removerLaboratorio(String id){
        if(laboratorios.containsKey(id) && id != null)
            laboratorios.remove(id);
    }

    
    
    public Laboratorio procurarPorId(String id){
        for(Laboratorio lab: laboratorios.values()){
            if(lab.getIdLaboratorio().equals(id)){
                return lab;
            }
        }
        return null;
    }
    
    public boolean verificarExiste(String id){
        return procurarPorId(id) != null;
    }
    
    public int obterTotalLaboratorios(){
        return laboratorios.size();
    }
    
    public Collection<Laboratorio> getTodos(){
        return laboratorios.values();
    }//Assim o exterior pode ver a lista sem poder alterar a lista internamente
    
}