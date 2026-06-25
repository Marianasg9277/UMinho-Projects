package BackEnd.exceptions;

public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(){ 
            super("Dados inválidos");
    }
    
    
    
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
