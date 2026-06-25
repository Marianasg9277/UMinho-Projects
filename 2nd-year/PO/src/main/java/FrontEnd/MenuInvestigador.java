package FrontEnd;

import BackEnd.SistemaGestao;
import BackEnd.Investigador;
import BackEnd.Projeto;
import BackEnd.Atividade;
import BackEnd.EstadoProjeto;
import BackEnd.EstadoAtividade;
import java.util.ArrayList;

import java.util.Collection;


public class MenuInvestigador {

    private SistemaGestao sistema;
    private Consola consola;
    private Investigador investigador;
    
    public MenuInvestigador(SistemaGestao sistema,  Consola consola, Investigador investigador){
        this.sistema = sistema;
        this.consola = consola;
        this.investigador = investigador;
        
    }
    
    public void mostrarMenu(Investigador inv){
        int opcao;
        
        do{
            consola.escrever("\nMENU INVESTIGADOR");
            
            String [] opcoes= {
                "Dados Pessoais",
                "Projetos",
                "Atividades",
                "Terminar Atividade Em Curso",
                "Estatísticas Pessoais",
                "Estatísticas Bonus",
                "Estatísticas Salariais",
                "Sair"
            };

            opcao = consola.lerInteiro(opcoes);
            
            switch (opcao){
                case 1: {
                    menuDadosPessoais();
                    break;
                }
                case 2: {
                    menuProjetos();
                    break;
                }
                case 3: {
                    menuAtividades();
                    break;
                }
                
                case 4:{
                    terminarAtividade();
                    break;
                }
                
                case 5: {
                    menuEstatisticas();
                    break;
                }
                case 6: {
                    menuBonus();
                    break;
                }
                
                case 7:{
                    menuSalario();
                    break;
                }
                
                case 8: {
                    sistema.registarLogout(inv);
                    consola.escrever("A sair...");
                    break;
                }
                default: {
                    consola.escreverErro("Opção Inválida.");
                    break;
                }
            }
        }while (opcao != 8);
    }
    
    private void terminarAtividade() {
        consola.escrever("\nTERMINAR ATIVIDADE");

        // Filtrar atividades do investigador que estão EM_CURSO
        // Usamos a lista de atividades do próprio objeto investigador para facilitar
        ArrayList<Atividade> atividadesEmCurso = new java.util.ArrayList<>();

        for (Atividade a : investigador.getListaAtividades()) {
            if (a.getEstado() == EstadoAtividade.EM_CURSO) {
                atividadesEmCurso.add(a);
            }
        }

        if (atividadesEmCurso.isEmpty()) {
            consola.escrever("Não tem nenhuma atividade em curso para terminar.");
            return;
        }

        // Mostrar lista para escolha
        consola.escrever("Selecione a atividade que terminou:");
        for (int i = 0; i < atividadesEmCurso.size(); i++) {
            Atividade a = atividadesEmCurso.get(i);
            consola.escrever((i + 1) + ". " + a.getTipoAtividade()
                    + " | Projeto: " + a.getProjeto().getTituloProjeto()
                    + " | Data Início: " + a.getData().toLocalDate());
        }
        consola.escrever("0. Voltar");

        //Ler opção
        int opcao = consola.lerInteiro("Opção: ");
        if (opcao == 0) {
            return;
        }

        if (opcao < 1 || opcao > atividadesEmCurso.size()) {
            consola.escreverErro("Opção inválida.");
            return;
        }

        //Obter a atividade selecionada
        Atividade selecionada = atividadesEmCurso.get(opcao - 1);

        consola.escrever("\nDetalhes da Atividade: " + selecionada.getTipoAtividade());
        consola.escrever("A duração prevista pelo Coordenador era: " + selecionada.getDuracaoPrevista() + " horas.");

        double horasReais = consola.lerDecimal("Indique quantas horas demorou REALMENTE: ");

        // Atualiza a atividade
        try {
            // Guardamos o que estava previsto
            double horasPrevistas = selecionada.getDuracaoPrevista();
            
            // Concluimos a atividade
            selecionada.concluirAtividade(horasReais);
            
            // Registamos apenas a DIFERENÇA no investigador
            double diferenca = horasReais - horasPrevistas;

            // Soma ao banco de horas do investigador (para o salário/bónus)
            //O investigador recebe pelo que TRABALHOU (Real), não pelo planeado.
            investigador.registarHoras(diferenca);

            consola.escrever("\nAtividade concluída com sucesso.");

            // Feedback imediato (Gamificação simples)
            if (horasReais <= horasPrevistas) {
                consola.escrever("Parabéns! Cumpriu o prazo estipulado (Folga: "
                        + (selecionada.getDuracaoPrevista() - horasReais) + "h).");
            } else {
                double desvio = horasReais - horasPrevistas;
                consola.escrever("Atenção: Ultrapassou o previsto em " + String.format("%.2f", desvio) + " horas.");
            }

        } catch (Exception e) {
            consola.escreverErro("Erro ao terminar atividade: " + e.getMessage());
        }
    }
        
    //DADOS PESSOAIS
    private void menuDadosPessoais(){
        int opcao;
        do{
            consola.escrever("\nDADOS PESSOAIS");
            
            String [] opcoes= {
                "Ver Dados",
                "Alterar email",
                "Alterar username",
                "Alterar Password",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);
           
            switch (opcao){
                case 1: {
                    mostrarDados();
                    break;
                }
                case 2: {
                    alterarEmail();
                    break;
                }
                case 3: {
                    alterarUsername();
                    break;
                }
                case 4: {
                    alterarPassword();
                    break;
                }
                case 5: {
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida.");
                    break;
                }
            }
        }while (opcao != 5);
    }
    
    private void mostrarDados(){
        
        consola.escrever("\nDados Pessoais");
        consola.escrever("ID: " + investigador.getIdUtilizador());
        consola.escrever("Nome: " + investigador.getNome());
        consola.escrever("Email: " + investigador.getEmail());
        consola.escrever("Username: " + investigador.getUsername());
        consola.escrever("Área: " + investigador.getAreaEspecializacao());
        
        if(investigador.getLaboratorioAtual() != null){
            consola.escrever("Laboratório: " + investigador.getLaboratorioAtual().getNomeLab());
        }else{
            consola.escrever("Laboratório: Nenhum");
            
        }
    }
    
    private void alterarEmail(){
        String novo = consola.lerEmail("Novo email: ");

        try {
            sistema.alterarEmail(investigador, novo);
            consola.escrever("Email alterado com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void alterarUsername(){
        String novo = consola.lerString("Novo username: ");

        try {
            sistema.alterarUsername(investigador, novo);
            consola.escrever("Username alterado com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }
    
    private void alterarPassword(){
        String nova = consola.lerString("Nova Password: ");

        try {
            sistema.alterarPassword(investigador, nova);
            consola.escrever("Password alterada com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    
    //PROJETOS
    
    private void menuProjetos(){
        int opcao;
        do{
            consola.escrever("\nPROJETOS");
            
            String [] opcoes= {
                "Projetos em curso",
                "Histórico de Projetos",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);
                        
            switch (opcao){
                case 1: {
                    listarProjetosPorEstado(EstadoProjeto.EM_CURSO);
                    break;
                }
                case 2: {
                    consola.escrever("\nProjetos Concluídos:");
                    listarProjetosPorEstado(EstadoProjeto.CONCLUIDO);
                    
                    consola.escrever("\nProjetos Suspensos: ");
                    listarProjetosPorEstado(EstadoProjeto.SUSPENSO);
                    break;
                }
                case 3: {
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida.");
                    break;
                }
            }
        }while (opcao != 3);
    }
    
    private void listarProjetosPorEstado(EstadoProjeto estado){
        Collection<Projeto> projetos = investigador.getListaProjetos();
        
        boolean encontrou = false;
        
        for(Projeto p :projetos){
            if(p.getEstado() == estado){
                consola.escrever(p.getIdProjeto() + " | " + p.getTituloProjeto() + " | " + p.getEstado());
                encontrou = true;
            }
        }
        if(!encontrou){
            consola.escrever("Não existem Projetos neste estado.");
        }
    }
     
    
    //ATIVIDADES
    
    private void menuAtividades(){
        int opcao;
        do{
            consola.escrever("\nATIVIDADES");
            
            String [] opcoes= {
                "Atividades em curso",
                "Histórico de Atividades",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);
            
            switch (opcao){
                case 1: {
                    listarAtividades(EstadoAtividade.EM_CURSO);
                    break;
                }
                case 2: {
                    listarAtividades(EstadoAtividade.CONCLUIDA);
                    break;
                }
                case 3: {
                    break;
                }
                default: {
                    System.out.println("Opção inválida.");
                    break;
                }
                
            }
        }while (opcao != 3);
    }
    
    private void listarAtividades(EstadoAtividade estado){
        Collection<Atividade> atividades = investigador.getListaAtividades();
        boolean encontrou = false;
        
        consola.escrever("--- Atividades ---");
        
        for(Atividade a : atividades){
            if(a.getEstado() == estado){
                consola.escrever( a.getIdAtividade() + " | " + a.getTipoAtividade() + " | " + a.getDuracao() + " horas");
                encontrou = true;
            }
        }
        if(!encontrou){
            consola.escrever("Não existem atividades neste estado.");
        }
    }
    
    
    
    //ESTATISTICAS

    private void menuEstatisticas() {

        String id = investigador.getIdUtilizador();

        consola.escrever("\nESTATÍSTICAS PESSOAIS");

        consola.escrever("Total de horas trabalhadas: "
                + sistema.horasTotaisInvestigador(id));

        consola.escrever("Número de atividades realizadas: "
                + sistema.numeroAtividadesInvestigador(id));

        consola.escrever("Número de projetos em que participa: "
                + sistema.numeroProjetosInvestigador(id));

        consola.escrever("Score de ranking: "
                + String.format("%.2f", sistema.rankingInvestigador(investigador)));

        // posição no ranking geral (sem expor dados de outros)
        int posicao = 1;
        for (Investigador i : sistema.rankingGeral()) {
            if (i.getIdUtilizador().equals(id)) {
                consola.escrever("Posição no ranking geral: " + posicao);
                break;
            }
            posicao++;
        }
        
        
    }

 
    //BONUS
    
    private void menuBonus() {
        consola.escrever("\nESTATÍSTICAS BONUS");

        int falta = investigador.horasExtraEmFaltaParaProximoPresente();

        if (falta == 0) {
            consola.escrever("Já reúne horas suficientes para um presente tecnológico!");
        } else {
            consola.escrever("Horas extra até próximo presente: " + falta + "h");
        }

        if (investigador.isRecebeuPremioProdutividade()) {
            consola.escrever("Já recebeu Prémio de Produtividade.");
        } else {
            consola.escrever("Ainda não recebeu Prémio de Produtividade.");
        }

        if (investigador.isRecebeuValeRefeicao()) {
            consola.escrever("Já recebeu Vale de Refeição.");
        } else {
            consola.escrever("Elegível para Vale de Refeição (dependente do período).");
        }

    }
   
    //SALARIO
    
    private void menuSalario(){
        consola.escrever("\nESTATÍSTICAS SALARIAIS");
        
        consola.escrever("Categoria Salarial: " + investigador.getTipoInvestigador());
        consola.escrever("Salário Base Contratual: " + String.format("%.2f €", investigador.getSalarioBase()));
        
        consola.escrever("\n--- SIMULAÇÃO DE RECIBO DE VENCIMENTO ---");
        int ano = consola.lerAno("Indique o Ano (AAAA): ");
        int mes = consola.lerMes("Indique o Mês (1-12): ");
        
        // Cálculos
        double horasReaisPagas = investigador.getHorasTrabalhadasNoMes(ano, mes);
        double salarioProcessado = investigador.calcularSalarioDoMes(ano, mes);
        
        consola.escrever("\n>> Resumo de " + mes + "/" + ano + ":");
        
        if (horasReaisPagas == 0) {
            consola.escreverErro("Aviso: Não tem atividades CONCLUÍDAS registadas neste mês.");
            consola.escrever("Nota: Atividades 'Em Curso' não contam para o salário até serem terminadas.");
        } else {
            consola.escrever("Total Horas Validadas (Concluídas): " + String.format("%.2f h", horasReaisPagas));
            
            if (horasReaisPagas > 160) {
                double extras = horasReaisPagas - 160;
                consola.escrever("Horas Normais: 160h");
                consola.escrever("Horas Extra Apuradas: " + String.format("%.2f h", extras));
            } else {
                consola.escrever("Horas Normais: " + String.format("%.2f h", horasReaisPagas));
                consola.escrever("Horas Extra: 0h");
            }
        }
        
        consola.escrever("-------------------------------------");
        consola.escrever("TOTAL LÍQUIDO A RECEBER: " + String.format("%.2f €", salarioProcessado));
        consola.escrever("-------------------------------------");
    }

}
