package BackEnd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Projeto implements Serializable{
    private String idProjeto;
    private String tituloProjeto;
    private String areaCientifica; //Biotecnologia, Robótica, Energia
    private Coordenador coordenador;
    private EstadoProjeto estado; //enum em curso, concluído, suspenso

    // Estas são listas internas do projeto
    private Collection<Laboratorio> laboratorios;
    private Collection<Investigador> investigadores;
    private Collection<Atividade> atividades;
 
    //para gerir automaticamente o id
     private static int contador = 1;

//Construtor
    public Projeto(String idProjeto, String tituloProjeto, String areaCientifica, Coordenador coordenador, EstadoProjeto estado) {
        this.idProjeto      = "PROJ" + (contador++);
        this.tituloProjeto  = tituloProjeto;
        this.areaCientifica = areaCientifica;
        this.coordenador    = coordenador;
        this.estado         = estado;
        
        this.laboratorios   = new ArrayList <>();
        this.investigadores = new ArrayList <>();
        this.atividades     = new ArrayList <>();
    }
 
    public static void setContador(int valor) {
        contador = valor;
    }
    
// Funcoes Get
    public String getIdProjeto() {
        return idProjeto;
    }

    public String getTituloProjeto() {
        return tituloProjeto;
    }

    public String getAreaCientifica() {
        return areaCientifica;
    }

    public Coordenador getCoordenador() {
        return coordenador;
    }

    public EstadoProjeto getEstado() {
        return estado;
    }
    
    public Collection<Laboratorio> getLaboratorios() {
        return laboratorios;
    }

    public Collection<Investigador> getInvestigadores() {
        return investigadores;
    }
    
    public Collection<Atividade> getAtividades() {
        return atividades;
    }

//Funcoes Set
    public void setTituloProjeto(String tituloProjeto) {
        this.tituloProjeto = tituloProjeto;
    }

    public void setAreaCientifica(String areaCientifica) {
        this.areaCientifica = areaCientifica;
    }

    public void setCoordenador(Coordenador c) {
        this.coordenador = c;
        c.adicionarProjetoGerido(this); // Ao criar projeto atribuimos um coordenador para o gerir e adicionamos este projeto a lista de projetos geridos do coordenador
    }


    public void setEstado(EstadoProjeto estado) {
        this.estado = estado;
    }
    
//Equipa de investigadores do projeto
    //Metodo para adicionar um investigador ao projeto
    public void adicionarInvestigador (Investigador investigador){
        if(investigador != null && !investigadores.contains(investigador)){
            investigadores.add(investigador);
            investigador.adicionarProjeto(this); //Esse investigdor acrescenta o projeto a sua lista 
        }
    }
    
    //metodo para remover um investigador ao projeto
    public void removerInvestigador(Investigador investigador) {
        if (investigador != null && investigadores.contains(investigador)) {
            investigadores.remove(investigador);
            investigador.removerProjeto(this);  //remover projeto do investigador
        }
    }

//Atividades do projeto
    //metodo para adicionar uma Atividade ao projeto
    public void adicionarAtividade(Atividade atividade){
        if (atividade !=null && !atividades.contains(atividade))
            atividades.add(atividade);
    }
    
    //metodo para remover atividades do projeto
    public void removerAtividade(Atividade atv) {
        if (atv != null && atividades.contains(atv)) {
            atividades.remove(atv);

            // Remover atividade do investigador também
            Investigador investigador = atv.getInvestigador();
            if (investigador != null) {
                investigador.removerAtividade(atv);
            }
        }
    }
    
//Laboratorios Envolvidos ao projeto    
    //metodo para adicionar laboratorio a projeto
    public void adicionarLaboratorio(Laboratorio laboratorio){
        if (laboratorio !=null && !laboratorios.contains(laboratorio)){
            laboratorios.add(laboratorio);
            laboratorio.adicionarProjeto(this);
        }
    }
    
    //metodo para remover laboratorio do projeto
    public void removerLaboratorio(Laboratorio lab){
        if (lab !=null && laboratorios.contains(lab)){
            laboratorios.remove(lab);
            lab.removerProjeto(this);
        }
    }
    
}