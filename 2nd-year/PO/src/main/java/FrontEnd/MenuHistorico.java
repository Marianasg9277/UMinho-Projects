package FrontEnd;

import BackEnd.Historico;
import BackEnd.SistemaGestao;
import java.util.ArrayList;
import java.util.Collections;

public class MenuHistorico {
    
    private SistemaGestao sistema;
    private Consola consola;

    public MenuHistorico(SistemaGestao sistema, Consola consola) {
        this.sistema = sistema;
        this.consola = consola;
    }
    
    public void mostrarMenu() {
        int opcao;
        
        do {
            consola.escrever("\nHISTÓRICO DO SISTEMA");
            String[] opcoes = {
                "Ver Histórico Completo",
                "Filtrar por ID de Utilizador",
                "Voltar"
            };
            
            opcao = consola.lerInteiro(opcoes);
            
            switch (opcao) {
                case 1:
                    listarTudo(); 
                    break;
                case 2:
                    filtrarPorUtilizador();
                    break;
                case 3:
                    break;
                default:
                    consola.escreverErro("Opção inválida.");
            }
            
        } while (opcao != 3);
    }
    
    private void listarTudo() {
        ArrayList<Historico> lista = sistema.getHistorico();
        
        if (lista.isEmpty()) {
            consola.escrever("O histórico está vazio.");
            return;
        }
        
        // Criamos uma cópia para não mexer na lista original do sistema
        ArrayList<Historico> copiaInvertida = new ArrayList<>(lista);
        
        // Invertemos a ordem (O último passa a ser o primeiro)
        Collections.reverse(copiaInvertida);
        
        consola.escrever("\nREGISTOS (do mais recente para o mais antigo)");
        for (Historico h : copiaInvertida) {
            imprimirLinhaHistorico(h);
        }
        consola.escrever("Total de registos: " + lista.size());
    }
    
    private void filtrarPorUtilizador() {
        String id = consola.lerString("Insira o ID do utilizador a pesquisar: ");
        
        // Para filtrar, também podemos querer ver do mais recente para o mais antigo
        // Então usamos a mesma lógica de inverter
        ArrayList<Historico> lista = sistema.getHistorico();
        ArrayList<Historico> copiaInvertida = new ArrayList<>(lista);
        Collections.reverse(copiaInvertida);

        boolean encontrou = false;
        int contador = 0;
        
        consola.escrever("\nAções de " + id + " (Recentes primeiro)");
        
        for (Historico h : copiaInvertida) {
            if (h.getIdUtilizador().equalsIgnoreCase(id)) {
                imprimirLinhaHistorico(h);
                encontrou = true;
                contador++;
            }
        }
        
        if (!encontrou) {
            consola.escrever("Nenhum registo encontrado para esse ID.");
        } else {
            consola.escrever("Total de ações encontradas: " + contador);
        }
    }
    
    // Método auxiliar para formatar a saída
    private void imprimirLinhaHistorico(Historico h) {
        // Chama o toString() do Historico
        consola.escrever(h.toString()); 
    }
}