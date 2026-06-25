package BackEnd.exceptions;

public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException() {
        super("Operação não permitida pelas regras de negócio");
    }

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}