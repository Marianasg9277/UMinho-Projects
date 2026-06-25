package BackEnd;

import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;

public class ListaInvestigadores implements Serializable {
    private HashMap<String, Investigador> investigadores;

//construtores
    //Construtor que cria um HashMap com capacidade inicial que é por default 15
    public ListaInvestigadores(){
        investigadores = new HashMap<>();
    }
    
    //construtor com capacidade inicial
     public ListaInvestigadores(int capacidade){
        investigadores = new HashMap<>  (capacidade);
    }

    //Construtor que recebe uma coleção e cria uma cópia
    public ListaInvestigadores(Collection<Investigador> listaInvestigadores) {
        HashMap<String, Investigador> outro = new HashMap<> (listaInvestigadores.size());
        for(Investigador inv: listaInvestigadores){
            outro.put(inv.getIdUtilizador(), inv);
        }
       investigadores = outro;
    }

    

    //Método para adicionar investigadores
    public void adicionarInvestigador(Investigador investigador){
        if(!investigadores.containsValue(investigador) && investigador != null)
            investigadores.put(investigador.getIdUtilizador(), investigador);
    }
    
    public void removerInvestigador(Investigador inv){
        if(investigadores.containsValue(inv) && inv != null)
            investigadores.remove(inv.getIdUtilizador(), inv);
    }
    
    //Método para devolver todos os investigadores
    public Collection<Investigador> getTodos() {
        return investigadores.values();
    }

    //Método para obter investigador por id
    public Investigador procurarPorId(String id){
        for(Investigador inv : investigadores.values()){
            if(inv.getIdUtilizador().equals(id)){
                return inv;
            }
        }
        return null;
    }

    //Método para verificar se existe
    public boolean verificarExiste(String id){
        return procurarPorId(id) != null;
    }

    //Método para obter o nºtotal de investigadores
    public int obterTotalInvestigadores(){
        return investigadores.size();
    }

    //Método para obter o nºtotal de investigadores por tipo
    public int calcularTotalInvestigadoresPorTipo(String tipo) {
        int total = 0;
         for (Investigador inv : investigadores.values()) {
            if (inv.getClass().getName().equalsIgnoreCase(tipo)) {
                total++;
            }
        }
        return total;
    }
    
    
    // Ranking geral
    public Collection<Investigador> rankingGeral() {
        ArrayList<Investigador> lista = new ArrayList<>(investigadores.values());
        lista.sort((a, b) -> Double.compare(b.getScoreRanking(), a.getScoreRanking()));
        return lista;
    }

    // Ranking de Investigadores livres (sem projetos em curso)
    public Collection<Investigador> investigadoresLivres(Collection<Projeto> projetos) {

        ArrayList<Investigador> livres = new ArrayList<>();

        for (Investigador inv : investigadores.values()) {
            boolean livre = true;

            for (Projeto pro : projetos) {
                if (pro.getEstado() == EstadoProjeto.EM_CURSO &&
                    pro.getInvestigadores().contains(inv)) {
                    livre = false;
                    break;
                }
            }

            if (livre) {
                livres.add(inv);
            }
        }

        return livres;
    }
    
    //Pesquisa avançada de investigadores (laboratório, nº de projetos,...)
    
    //Pesquisa por área geografica
    public Collection<Investigador> pesquisarPorArea(String area) {
        ArrayList<Investigador> resultado = new ArrayList<>();

        for (Investigador inv : investigadores.values()) {
            if (inv.getAreaEspecializacao().equalsIgnoreCase(area)) {
                resultado.add(inv);
            }
        }

        return resultado;
    }
    
    //Pesquisa por nº de projetos
    public Collection<Investigador> pesquisarPorNumeroProjetos(int minimo) {
        ArrayList<Investigador> resultado = new ArrayList<>();

        for (Investigador inv : investigadores.values()) {
            if (inv.getNumeroProjetos() >= minimo) {
                resultado.add(inv);
            }
        }

        return resultado;
    }
    
    //Pesquisa por laboratório
    public Collection<Investigador> pesquisarPorLaboratorio(Laboratorio lab) {
        ArrayList<Investigador> resultado = new ArrayList<>();

        for (Investigador inv : investigadores.values()) {
            if (lab.getInvestigadores().contains(inv)) {
                resultado.add(inv);
            }
        }

        return resultado;
    }
}
