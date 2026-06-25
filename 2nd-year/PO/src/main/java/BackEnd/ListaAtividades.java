package BackEnd;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class ListaAtividades implements Serializable {

    private HashMap<String, Atividade> mapaAtividades; 
    /*
    HashMap <key, value>
    key corresponde a Id_Atividade logo String
    Value corresponde a Atividade
    */

//construtor
    //construtor com capacidade inicial
    public ListaAtividades(int capacidade){
        mapaAtividades = new HashMap<>(capacidade);
    }

    //HashMap tem capacidade inicial default de 15
    public ListaAtividades() {
        mapaAtividades = new HashMap<>();
    }

    //construtor recebe uma Collection e faz uma copia
    public ListaAtividades(Collection<Atividade> atividades) {
        HashMap<String, Atividade> outro = new HashMap<>(atividades.size());
        for (Atividade a : atividades) {
            outro.put(a.getIdAtividade(), a);
        }
        mapaAtividades = outro;
    }

    //construtor para devolver a coleção toda
    public Collection<Atividade> getTodas() {
        return mapaAtividades.values();
    }

//metodos:
    //metodo para inserir atividade
    public void adicionarAtividade(Atividade atividade) {
        if (atividade != null) {
            mapaAtividades.put(atividade.getIdAtividade(), atividade);
        }
    }

    //metodo para remover atividade
    public void removerAtividade (Atividade atividade){
        if(atividade != null){
             mapaAtividades.remove(atividade.getIdAtividade(), atividade);
            
        }
        
    }
    
   
    //procura uma atividade especifica através do seu id
    public Atividade procurarPorIdAtividade(String idAtividade){
        return mapaAtividades.get(idAtividade);
    }
    
    //Devolve o numero total de atividades
    public int tamanhoListaAtividades(){
        return mapaAtividades.size();
    }
    
    //Veifica se certa atividade existe usando o metodo procurarPorId
    public boolean verificarExiste(String idAtividade){
        return null != procurarPorIdAtividade(idAtividade);
    }
    
    //devolve o num total de atividades de um tipo (experiência, teste, simulação, publicação)
    public int calcularTotalAtividadesPorTipo(String tipo){
        int total =0;
        for (Atividade a : mapaAtividades.values()){
            if (a.getTipoAtividade().equalsIgnoreCase(tipo)){
                total++;
            }
    
        }
        return total;
    }
    
//Atividades de Investigador
    //lista tds as atividades de um dado investigador
    public Collection<Atividade> atividadesPorInvestigador(String idInvestigador){
    Collection<Atividade> resultado = new ArrayList<>();

    for (Atividade a : mapaAtividades.values()){
        if (a.getInvestigador().getIdUtilizador().equals(idInvestigador)){
            resultado.add(a);
        }
    }
    return resultado;
    }
    
    //Devolve o num de atividades por investigador usando o metodo atividadesPorInvestigador
    public int numAtividadesPorInvestigador(String idInvestigador){
        return atividadesPorInvestigador(idInvestigador).size();
    }
    
    //Calcula o total de horas q certo investigador gastou em tds as suas atividades
    public double totalHorasPorInvestigador(String idInvestigador) {
    double total = 0;

    for (Atividade a : mapaAtividades.values()) {
        if (a.getInvestigador().getIdUtilizador().equals(idInvestigador)) {
            total += a.getDuracao();
        }
    }
    return total;
    }
    
//Atividades por Projeto
    //lista todas as atividades de um projeto especifico
    public Collection<Atividade> atividadesPorProjeto(String idProjeto){
    Collection<Atividade> resultado = new ArrayList<>();

    for (Atividade a : mapaAtividades.values()){
        if (a.getProjeto().getIdProjeto().equals(idProjeto)){
            resultado.add(a);
        }
    }
    return resultado;
    }
    
    //Devolve o num de atividades por Projeto usando o metodo atividadesPorProjeto
    public int numAtividadesPorProjeto(String idProjeto){
        return atividadesPorProjeto(idProjeto).size();
    }
    
    //Calcula a duracao total de um projeto
    public double totalHorasProjeto(String idProjeto){
        double total =0;
        
        for (Atividade a : mapaAtividades.values()){
            if(a.getProjeto().getIdProjeto().equals(idProjeto))
                total += a.getDuracao();
        }
        return total;
    }

//Atividades Data
    //Listar as ataividades num intervalo de datas
    public Collection<Atividade> atividadesEntreDatas( LocalDateTime dataInicial,  LocalDateTime dataFinal){
        Collection<Atividade> resultado = new ArrayList<>();
        
        for (Atividade a : mapaAtividades.values()){
            if (a.getData().isAfter(dataInicial) && a.getData().isBefore(dataFinal)){
                resultado.add(a);
            }
        }
        return resultado;
    }
    
    //Devolve o num de atividades no intervalo de datas usando o metodo atividadesEntreDatas
    public int numAtividadesEntreDatas( LocalDateTime dataInicial,  LocalDateTime dataFinal){
        return atividadesEntreDatas(dataInicial, dataFinal).size();
    }
    
    //calcula a duracao total entre datas
    public double totalHorasEntreDatas( LocalDateTime dataInicial,  LocalDateTime dataFinal){
        double total =0;
        
        for (Atividade a : mapaAtividades.values()){
            if (a.getData().isAfter(dataInicial) && a.getData().isBefore(dataFinal))
                total += a.getDuracao();
        }
        return total;
    }
    
    //metodo q devolve o total de horas de certo investigador num prazo de tempo
    public double totalHorasPorInvestigadorEntreDatas(String idInvestigador,  LocalDateTime inicio,  LocalDateTime fim) {
        double total = 0;

        for(Atividade a : mapaAtividades.values()){
            if(a.getInvestigador().getIdUtilizador().equals(idInvestigador)){
                if(!a.getData().isBefore(inicio) && !a.getData().isAfter(fim)){
                    total += a.getDuracao();
                }
            }
        }

        return total;
    }
    
//Atividades Por estado
    //devolve a lista de atividades filtrados pelo seu estado
    public Collection<Atividade> atividadesPorEstado(EstadoAtividade estado) {

        Collection<Atividade> lista = new ArrayList<>();

        for (Atividade a: mapaAtividades.values()) {
            if (a.getEstado()== estado) {
                lista.add(a);
            }
        }

        return lista;
    }
    
    //devolve o num total de atividades em certo estado
    public int numAtividadesPorEstado(EstadoAtividade estado) {
        return atividadesPorEstado(estado).size();
    }
    
     
}
