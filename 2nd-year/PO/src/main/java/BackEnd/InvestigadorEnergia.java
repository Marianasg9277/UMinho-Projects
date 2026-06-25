package BackEnd;

import java.io.Serializable;

public class InvestigadorEnergia extends Investigador implements Serializable {
    private String fonteEnergeticaPrincipal;
    
    public static final double SALARIO_BASE = 1800.0;

    public InvestigadorEnergia(String email, String nome, String username, String password, String areaEspecializacao, String fonteEnergeticaPrincipal){
        super(email, nome, username, password, areaEspecializacao);
        this.fonteEnergeticaPrincipal = fonteEnergeticaPrincipal;
    }

    public String getFonteEnergeticaPrincipal(){
        return fonteEnergeticaPrincipal;
    }

    public void setFonteEnergeticaPrincipal(String fonteEnergeticaPrincipal){
        this.fonteEnergeticaPrincipal = fonteEnergeticaPrincipal;
    }

    @Override
    public String getTipoInvestigador() {
        return "Energia";
    }
    
    @Override
    public double getSalarioBase() {
        return SALARIO_BASE;
    }
}