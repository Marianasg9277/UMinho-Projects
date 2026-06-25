package BackEnd;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Historico implements Serializable{

    private String idUtilizador;      // quem fez a ação (investigador, coordenador ou administrador)
    private String descricao;         // o que aconteceu
    private LocalDateTime dataHora;   // quando aconteceu
    
    private static int contador = 1;
    private final String idHistorico;
     
    private static String gerarId() {
        return "H" + (contador++);
    }

    public Historico(String idUtilizador, String descricao) {
        this.idUtilizador = idUtilizador;
        this.descricao    = descricao;
        this.dataHora     = LocalDateTime.now();  
        this.idHistorico  = gerarId();
    }
    
    public static void setContador(int valor) {
        contador = valor;
    }

    // Funcao get
    public String getIdUtilizador() {
        return idUtilizador;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getIdHistorico() {
        return idHistorico;
    }
    
    @Override
    public String toString() {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String dataFormatada = dataHora.format(formatter);

        return "[" + dataFormatada + "] User: " + idUtilizador + " | Ação: " + descricao;
    }

} 