package BackEnd.exceptions;

public class EntidadeDuplicadaException extends RuntimeException {

    public EntidadeDuplicadaException() {
        super("Entidade Duplicada");
    }
    
    
    
    public EntidadeDuplicadaException(String mensagem) {
        super(mensagem);
    }
}
