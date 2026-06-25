package BackEnd;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Atividade implements Serializable {
    private String idAtividade;
    private Projeto projeto;
    private Investigador investigador;
    private String tipoAtividade; //ex: experiência, teste, simulação, publicação
    private LocalDateTime data; // ex: DD/MM/AAAA
    
    private double duracaoPrevista; // Definida pelo Coordenador na criação
    private double duracaoReal;     // Definida pelo Investigador na conclusão
    
    private EstadoAtividade estado; //enum em curso, concluído, cancelada
    
    //para gerir automaticamente o id
    private static int contador = 1;
    
//Construtor    
    public Atividade(String idAtividade, Projeto projeto, Investigador investigador, String tipoAtividade,  LocalDateTime data, double duracaoPrevista, EstadoAtividade estado){
        this.idAtividade     = "ATIV" + (contador++);
        this.projeto         = projeto;
        this.investigador    = investigador;
        this.tipoAtividade   = tipoAtividade;
        this.data            = data;
        this.duracaoPrevista = duracaoPrevista;
        this.duracaoReal     = 0.0;
        this.estado          = estado;
        
        //com esta condicao sempre que for criada uma atividade ela é automaticamente adicionada a lista do respetivo projeto
        if (projeto !=null) {
            projeto.adicionarAtividade(this);
        }
    }
    
    public static void setContador(int valor) {
        contador = valor;
    }

//Funcao get
    public String getIdAtividade(){
        return idAtividade;
    }
    
    public Projeto getProjeto(){
        return projeto;
    }
    
    public Investigador getInvestigador(){
        return investigador;
    }
    
    public String getTipoAtividade (){
        return tipoAtividade;
    }
    
    public LocalDateTime getData(){
        return data;
    }
    
    public double getDuracaoPrevista() { 
        return duracaoPrevista; 
    }
    
// O getDuracao() passa a devolver a real se já estiver feita, ou a prevista se ainda estiver em curso (para cálculos provisórios)
    public double getDuracao() { 
        if (estado == EstadoAtividade.CONCLUIDA) {
            return duracaoReal;
        }
        return duracaoPrevista; //return duracaoPrevista se o investigador não a tiver concluido
    }
    
    
    public EstadoAtividade getEstado() {
        return estado;
    }

// Funcao set (nao precisamos de set de Investigador nem Projeto)
    public void setIdAtividade(String idAtividade) {
        this.idAtividade = idAtividade;
    }
    
    public void setTipoAtividade(String tipoAtividade) {
        this.tipoAtividade = tipoAtividade;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    
    public void setEstado(EstadoAtividade estado) {
        this.estado = estado;
    }        
       
    // Usa-se este quando se acaba o trabalho.
    public void concluirAtividade(double duracaoReal) {
        if (duracaoReal < 0) {
            throw new IllegalArgumentException("A duração não pode ser negativa.");
        }
        this.duracaoReal = duracaoReal;
        this.estado = EstadoAtividade.CONCLUIDA;
    }
    
    // Só deixa mudar a duração SE a atividade JÁ estiver concluída.
    public void corrigirDuracaoReal(double novaDuracao) {
        if (this.estado != EstadoAtividade.CONCLUIDA) {
            // Proteção: Não deixa meter duração real numa atividade que ainda decorre
            throw new IllegalStateException("Não pode definir duração real numa atividade em curso. Use concluirAtividade().");
        }
        
        if (novaDuracao < 0) {
            throw new IllegalArgumentException("A duração não pode ser negativa.");
        }

        this.duracaoReal = novaDuracao;
    }
}
