package BackEnd;

import java.io.Serializable;


public class InvestigadorBiotecnologia extends Investigador implements Serializable {
    private String areaInvestigacao; //ex:microbiologia, genÃ©tica
    
    public static final double SALARIO_BASE = 1600.0;

    public InvestigadorBiotecnologia(String email, String nome, String username, String password, String areaEspecializacao, String areaInvestigacao){
        super(email, nome, username, password, areaEspecializacao);
        this.areaInvestigacao = areaInvestigacao;
    }

    public String getAreaInvestigacao(){
        return areaInvestigacao;
    }

    public void setAreaInvestigacao(String areaInvestigacao){
        this.areaInvestigacao = areaInvestigacao;
    }

    @Override
    public String getTipoInvestigador() {
        return "Biotecnologia";
    }
    
    @Override
    public double getSalarioBase() {
        return SALARIO_BASE;
    }
}