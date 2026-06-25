package BackEnd;

import static BackEnd.EstadoProjeto.EM_CURSO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Laboratorio implements Serializable {
    private String idLaboratorio;
    private String nomeLab;
    private String localizacao;
    private ArrayList<Investigador> investigadores;
    private ArrayList <Projeto> projetos;
    
    //para gerir automaticamente o id
    private static int contador = 1;
    
    public Laboratorio(String idLaboratorio, String nomeLab, String localizacao){
        this.idLaboratorio  = "LAB" + (contador++);
        this.nomeLab        = nomeLab;
        this.localizacao    = localizacao;
        this.investigadores = new ArrayList<>();
        this.projetos       = new ArrayList<>();
    }
    
    public static void setContador(int valor) {
        contador = valor;
    }
    
    public String getIdLaboratorio(){
        return idLaboratorio;
    }
    
    public String getNomeLab(){
        return nomeLab;
    }
    
    public String getLocalizacao(){
        return localizacao;
    }
    
    public ArrayList<Investigador> getInvestigadores(){
        return new ArrayList<>(investigadores);
    }
    
    public ArrayList<Projeto> getProjetos(){
        return projetos;
    }
    
    public void setIdLaboratorio(String idLaboratorio){
        this.idLaboratorio = idLaboratorio;
    }
    
    public void setNomeLab(String nomeLab){
        this.nomeLab = nomeLab;
    }
    
    public void setLocalizacao(String localizacao){
        this.localizacao = localizacao;
    }
    
    //Adicionar e Remover Investigadores
    
    public void adicionarInvestigador(Investigador inv){
        investigadores.add(inv);
    }
    
    public void removerInvestigador(Investigador inv){
        investigadores.remove(inv);
    }
    
    
    //total de projetos do lab
    public int totalProjetos(){
        return projetos.size();
    }
    
    //listar todos os projetos ativos do laboratorio
    public ArrayList<Projeto> listarProjetosAtivos(){
        ArrayList<Projeto> resultado = new ArrayList<>();
        for (Projeto p : projetos){
            if (p.getEstado() == EM_CURSO){
                resultado.add(p);
            }
        }
        return resultado;
    }
    
    // qnts projetos ativos tem o laboratorio
    public int totalProjetosAtivos(){
        return listarProjetosAtivos().size();
    }
    
    
    //Adicionar e remover projetos da lista de projeto que o laboratorio possui
    public void adicionarProjeto(Projeto projeto){
        if (projeto != null && !projetos.contains(projeto)){
            projetos.add(projeto);
        }
    }
    
    public void removerProjeto(Projeto projeto){
        if (projeto != null && projetos.contains(projeto)){
            projetos.remove(projeto);
        }
    }
    
    //Total investigadores
    public int getNumeroInvestigadores() {
        return investigadores.size();
    }
    
    //Total Projetos ativos
    public int getProjetosAtivos() {
        int total = 0;

        for (Projeto p : projetos) {
            if (p.getEstado() == EstadoProjeto.EM_CURSO) {
                total++;
            }
        }

        return total;
    }
    
    //Total de horas do laboratório
    public double getHorasTotaisLaboratorio() {
        double total = 0;

        for (Investigador inv : investigadores) {
            total += inv.getTotalHoras();
        }

        return total;
    }
    
    //Investigador mais produtivo
    public Investigador getInvestigadorMaisProdutivo() {
        Investigador melhor = null;
        double max = 0;

        for (Investigador inv : investigadores) {
            if (inv.getTotalHoras() > max) {
                max = inv.getTotalHoras();
                melhor = inv;
            }
        }

        return melhor;
    }
    
    //Área científica dominante
    public String getAreaDominante() {
        HashMap<String, Integer> areas = new HashMap<>();

        for (Investigador inv : investigadores) {
            String area = inv.getAreaEspecializacao();
            areas.put(area, areas.getOrDefault(area, 0) + 1);
        }

        return areas.entrySet()
                    .stream()
                    .max((a, b) -> a.getValue() - b.getValue())
                    .map(e -> e.getKey())
                    .orElse("Sem dados");
    }
}
