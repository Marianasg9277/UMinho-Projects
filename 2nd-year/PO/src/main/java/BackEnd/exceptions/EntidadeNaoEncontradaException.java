package BackEnd.exceptions;

public class EntidadeNaoEncontradaException extends RuntimeException {

    public EntidadeNaoEncontradaException() {
        super("Entidade não encontrada no sistema");
    }

    public EntidadeNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}