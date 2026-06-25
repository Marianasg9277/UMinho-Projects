package BackEnd;

import java.io.Serializable;


public class InvestigadorRobotica extends Investigador implements Serializable {
    private String tipoSistemaTrabalho; //ex: drones, veículos autónomos)

    public static final double SALARIO_BASE = 2000.0;
     
    public InvestigadorRobotica(String email, String nome, String username, String password, String areaEspecializacao, String tipoSistemaTrabalho){
        super(email, nome, username, password, areaEspecializacao);
        this.tipoSistemaTrabalho = tipoSistemaTrabalho;
    }

    public String getTipoSistemaTrabalho(){
        return tipoSistemaTrabalho;
    }

    public void setTipoSistemaTrabalho(String tipoSistemaTrabalho){
        this.tipoSistemaTrabalho = tipoSistemaTrabalho;
    }
    
    @Override
    public String getTipoInvestigador() {
        return "Robótica";
    }
    
    @Override
    public double getSalarioBase() {
        return SALARIO_BASE; // Retorna 2000.0
    }
}