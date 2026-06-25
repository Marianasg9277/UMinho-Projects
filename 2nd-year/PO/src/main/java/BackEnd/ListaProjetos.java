package BackEnd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class ListaProjetos implements Serializable {
    private HashMap<String, Projeto> projetos; 


//construtor
    //construtor com capacidade inicial
    public ListaProjetos(int capacidade){
        projetos = new HashMap<>  (capacidade);
    }

    //HashMap tem capacidade inicial default de 15
    public ListaProjetos() {
        projetos = new HashMap<> ();
    }

    //construtor recebe uma Collection e faz uma copia
    public ListaProjetos(Collection<Projeto> lista) {
        HashMap<String, Projeto> outro = new HashMap<> (lista.size());
        for(Projeto p: lista){
            outro.put(p.getIdProjeto(), p);
        }
       projetos = outro;
    }

//metodods:
    //metodo para adicionar 1 projeto
    public void adicionarProjeto(Projeto p) {
        if (p != null && !projetos.containsValue(p)) {
            projetos.put(p.getIdProjeto(), p);
        }
    }

    // metodo para remover o projeto
    public void removerProjeto(Projeto p){
        if (p != null && projetos.containsValue(p)) {
            projetos.remove(p.getIdProjeto(), p);
        }
    }
    
    //procura um projeto na lista atraves do seu id
    public Projeto procurarPorId(String idProjeto) {
        return projetos.get(idProjeto);
    }

    //verifica se certo projeto existe
    public boolean existeProjeto(String idProjeto) {
        return projetos.containsKey(idProjeto);
    }

    //listar em ArrayList todos os projetos
    public Collection<Projeto> listaTotalProjetos() {
        return projetos.values();
    }

    //devolve o tamanho da lista
    public int tamanho() {
        return projetos.size();
    }
    
    
//Projeto Por Estado
    //devolve a lista de projetos filtrados pelo seu estado
    public Collection<Projeto> projetosPorEstado(EstadoProjeto estado) {

        Collection<Projeto> lista = new ArrayList<>();

        for (Projeto p : projetos.values()) {
            if (p.getEstado() == estado) {
                lista.add(p);
            }
        }

        return lista;
    }
    
    //devolve o num total de projetos em certo estado
    public int numProjetosPorEstado(EstadoProjeto estado) {
        return projetosPorEstado(estado).size();
    }
    
     
//Projetos por investigador    
    //devolve a lista de projetos q um certo investigador participa
    public Collection<Projeto> projetosPorInvestigador(String idInvestigador) {

        Collection<Projeto> resultado = new ArrayList<>();

        for (Projeto p : projetos.values()) {
            // percorre a equipa do projeto
            for (Investigador inv : p.getInvestigadores()) {
                
                //verifica se e o inv q queremos
                if (inv.getIdUtilizador().equals(idInvestigador)) {
                    resultado.add(p);
                    break; // Mal encontre o investigador passa para o próximo projeto
                }
                //sai do if passa para o próximo investigador
            }
        }

        return resultado;
    }

        
    //Devolve o num de projetos por investigador usando o metodo projetosPorInvestigador como auxiliar
    public int numProjetosPorInvestigador (String idInvestigador){
        return projetosPorInvestigador(idInvestigador).size();
    }

//Projetos por Coordenador
    //devolve a lista de projetos q um certo coordenador participa
    public Collection<Projeto> projetosPorCoordenador(String id) {

        Collection<Projeto> resultado = new ArrayList<>();

        for (Projeto p : projetos.values()) {

            Coordenador c = p.getCoordenador();

            if (c != null && c.getIdUtilizador().equals(id)) {
                resultado.add(p);
            }
        }

        return resultado;
    }

    
    //devolve o num por coordenador de projetos geridos
    public int numProjetosPorCoordenador (String id){
        return projetosPorCoordenador(id).size();
    }
    
//Projetos por Laboratorio
    //devolve a lista de projetos por coordenador 
     public Collection<Projeto> projetosPorLaboratorio(String idLaboratorio) {

        ArrayList<Projeto> resultado = new ArrayList<>();

        for (Projeto p : projetos.values()) {
            //passa por cada um dos laboratorios envolvido
            for (Laboratorio lab : p.getLaboratorios()) {
                
                if (lab.getIdLaboratorio().equals(idLaboratorio)) {
                    resultado.add(p);
                    break;// Mal encontre o laboratorio passa para o próximo projeto
                }
                //sai do if passa para o próximo laboratorio
            }
        }
        
        return resultado;
    }
     

    //Horas totais realizadas num laboratório
    public double totalHorasPorLaboratorio(String idLaboratorio) {

        double total = 0;

        // Percorrer todos os projetos do sistema
        for (Projeto p : projetos.values()) {

            // Verificar se este projeto pertence ao laboratório
            for (Laboratorio lab : p.getLaboratorios()) {
                if (lab.getIdLaboratorio().equals(idLaboratorio)) {

                    // Somar todas as atividades do projeto
                    for (Atividade a : p.getAtividades()) {
                        total += a.getDuracao();
                    }

                    break;// Mal encontre o laboratorio passa para o próximo projeto
                    
                }//sai do if passa para o próximo laboratorio
            }
        }

        return total;
    }
    
    //Devolve a lista de atiividades dos projetos de um laboratorio
    public Collection<Atividade> atividadesPorLaboratorio(String idLab) {

        ArrayList<Atividade> resultado = new ArrayList<>();

        for (Projeto p : projetosPorLaboratorio(idLab)) {

            for (Atividade a : p.getAtividades()) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    //resulta do numero do atividades dos projetos de certo laboratorio
    public int numAtividadesPorLaboratorio(String idLab){
        return atividadesPorLaboratorio(idLab).size();
    }

    
    //Devolve o num de projetos por laboratorio usando o metodo projetosPorLaboratorio
    public int numProjetosPorLaboratorio (String idLaboratorio){
        return projetosPorLaboratorio(idLaboratorio).size();
    }
        
}