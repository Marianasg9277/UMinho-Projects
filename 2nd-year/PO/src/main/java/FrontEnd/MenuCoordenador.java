package FrontEnd;

import BackEnd.Atividade;
import BackEnd.Coordenador;
import BackEnd.EstadoAtividade;
import BackEnd.EstadoProjeto;
import BackEnd.Investigador;
import BackEnd.Projeto;
import BackEnd.SistemaGestao;
import BackEnd.exceptions.DadosInvalidosException;
import BackEnd.exceptions.EntidadeDuplicadaException;

import java.time.LocalDateTime;
import java.util.Collection;

public class MenuCoordenador {

    private SistemaGestao sistema;
    private Consola consola;
    private Coordenador coordenador;

    public MenuCoordenador(SistemaGestao sistema, Consola consola, Coordenador coordenador) {
        this.sistema = sistema;
        this.coordenador = coordenador;
        this.consola = consola;
    }

    public void mostrarMenu(){
        int opcao;

        do{
            consola.escrever("\nMENU COORDENADOR");
            String [] opcoes= {
                "Dados Pessoais",
                "Projetos",
                "Atividades",
                "Estatíticas Gerais",
                "Estatísticas Bonus",
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
                case 4: {
                    menuEstatisticas();
                    break;
                }
                case 5: 
                    menuBonus();
                    break;
                case 6: {
                    sistema.registarLogout(coordenador);
                    consola.escrever("A sair...");
                    break;
                }
                default: {
                    consola.escreverErro("Opção Inválida.");
                    break;
                }

            }
        }while(opcao != 6);
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
                case 1:{
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

        consola.escrever("\n--- Dados Pessoais ---");
        consola.escrever("ID: " + coordenador.getIdUtilizador());
        consola.escrever("Nome: " + coordenador.getNome());
        consola.escrever("Email: " + coordenador.getEmail());
        consola.escrever("Username: " + coordenador.getUsername());
    }

    private void alterarEmail(){
        String novo = consola.lerEmail("Novo email: ");

        try {
            sistema.alterarEmail(coordenador, novo);
            consola.escrever("Email alterado com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void alterarUsername(){
        String novo = consola.lerString("Novo username: ");

        try {
            sistema.alterarUsername(coordenador, novo);
            consola.escrever("Username alterado com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }
    
    private void alterarPassword(){
        String nova = consola.lerString("Nova Password: ");

        try {
            sistema.alterarPassword(coordenador, nova);
            consola.escrever("Password alterada com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }


    //PROJETOS

    public void menuProjetos(){
        int opcao;

        do{
            consola.escrever("\nPROJETOS");

            String [] opcoes= {
                "Projetos em curso",
                "Histórico de Projetos",
                "Criar novo projeto",
                "Gerir Projeto",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao){
                case 1: {
                    mostrarProjetosPorEstado(EstadoProjeto.EM_CURSO);
                    break;
                }
                case 2: {
                    consola.escrever("\nProjetos Concluídos:");
                    mostrarProjetosPorEstado(EstadoProjeto.CONCLUIDO);

                    consola.escrever("\nProjetos Suspensos: ");
                    mostrarProjetosPorEstado(EstadoProjeto.SUSPENSO);
                    break;
                }
                case 3: {
                    criarProjeto();
                    break;
                }
                case 4: {
                    gerirProjeto();
                    break;
                }
                case 5: {
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida!");
                    break;
                }
            }
        }while(opcao != 5);
    }

    private void mostrarProjetosPorEstado(EstadoProjeto estado) {
        Collection<Projeto> projetos = sistema.getProjetos().projetosPorEstado(estado);

        boolean encontrou = false;

        for (Projeto p : projetos) {
            if (p.getCoordenador() != null && p.getCoordenador().equals(coordenador)) {
                consola.escrever(p.getIdProjeto() + " | "
                        + p.getTituloProjeto() + " | "
                        + p.getEstado());
                encontrou = true;
            }
        }

        if (!encontrou) {
            consola.escrever("Nenhum projeto deste coordenador neste estado.");
        }
    }

    private void criarProjeto(){

        String titulo = consola.lerString("Título do Projeto: ");
        consola.escrever("Área de Cientifica: \n");
        String[] areas = {
            "Biotecnologia",
            "Energia",
            "Robótica"
        };
        int area = consola.lerInteiro(areas);
        Projeto projeto = null;
        switch (area){
            case 1 : {
                projeto = new Projeto(null, titulo, "Biotecnologia", coordenador, EstadoProjeto.EM_CURSO);
                break;   
            }
            case 2: {
                projeto = new Projeto(null, titulo, "Energia", coordenador, EstadoProjeto.EM_CURSO);
                break;
            }
            case 3:{
                projeto = new Projeto(null, titulo, "Robótica", coordenador, EstadoProjeto.EM_CURSO);
                break;
            }
        }
 
        try {
            sistema.adicionarProjeto(projeto);
            consola.escrever("Projeto adicionado com sucesso!");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
            return;
        }

        int opcao =0 ;
        do {
            consola.escrever("\nDeseja adicionar investigadores ao projeto agora?");
            String[] opcoes = {
                "Adicionar investigador ao projeto",
                "Terminar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                case 1: {
                    adicionarInvestigadorProjeto(projeto);
                    break;
                }
                case 2: {
                    consola.escrever("Criação do projeto concluída.");
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida.");
                    break;
                }
            }

        } while (opcao != 2);
}

    private void gerirProjeto(){

        String id = consola.lerString("\nID do Projeto: ");
        Projeto projeto = sistema.getProjetos().procurarPorId(id);

        if(projeto == null){
            consola.escrever("Projeto não encontrado.");
            return;
        }

        if (!projeto.getCoordenador().equals(coordenador)) {
            consola.escreverErro("Não tem permissão para gerir este projeto.");
            return;
        }

        int opcao;

        do{
            consola.escrever("\nGERIR PROJETO " + projeto.getTituloProjeto());

            String [] opcoes= {
                "Adicionar Investigador",
                "Remover Investigador",
                "Alterar Estado do Projeto",
                "Eliminar Projeto",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch(opcao){
                case 1: {
                    adicionarInvestigadorProjeto(projeto);
                    break;
                }
                case 2: {
                    removerInvestigadorProjeto(projeto);
                    break;
                }
                case 3: {
                    alterarEstadoProjeto(projeto);
                    break;
                }
                case 4: {
                    removerProjeto(projeto);
                    break;
                }
                case 5: {
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida!");
                    break;
                }
            }
        }while(opcao != 5 );
    }

    private void adicionarInvestigadorProjeto(Projeto projeto){
        String idInv = consola.lerString("ID do Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);
        


        if(inv == null){
            consola.escrever("Investigador não encontrado.");
            return;
        }

        if (!inv.getTipoInvestigador().equalsIgnoreCase(projeto.getAreaCientifica())){
             consola.escrever("Investigador não pertence à area do projeto");
             return;
        }

        sistema.adicionarInvestigadorProjeto(inv, projeto);
        consola.escrever("Investigador adicionado com sucesso.");
    }

    private void removerInvestigadorProjeto(Projeto projeto){
        String idInv = consola.lerString("ID do Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

        if(inv == null){
            consola.escrever("Investigador não encontrado.");
            return;
        }

        try {
            sistema.removerInvestigadorDeProjeto(inv, projeto, coordenador);
            consola.escrever("Investigador removido com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void alterarEstadoProjeto(Projeto projeto){
        int opcao;


        consola.escrever("Novo Estado do Projeto:");

        String [] opcoes= {
                "Em curso",
                "Concluído",
                "Suspenso",
            };

        opcao = consola.lerInteiro(opcoes);

        EstadoProjeto novo = null;

        switch (opcao){
            case 1: {
                novo = EstadoProjeto.EM_CURSO;
                break;
            }
            case 2: {
                novo = EstadoProjeto.CONCLUIDO;
                break;
            }
            case 3: {
                novo = EstadoProjeto.SUSPENSO;
                break;
            }
            default: {
                novo = null;
                break;
            }
        }

        if(novo != null){
            sistema.alterarEstadoProjeto(projeto, novo);
            consola.escrever("Estado atualizado com sucesso.");
        }

    }

    private void removerProjeto(Projeto projeto){
        try {
            sistema.removerProjeto(projeto, coordenador);
            consola.escrever("Projeto removido com sucesso!");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage()); // Vai dizer "Só se estiver concluído" ou "Não tem permissão"
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
                "Atribuir nova Atividade",
                "Gerir Atividade",
                "Remover Atividade",
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
                    criarAtividade();
                    break;
                }
                case 4: {
                    gerirAtividade();
                    break;
                }
                case 5: {
                    removerAtividade();
                    break;
                }
                case 6: {
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida");
                    break;
                }
            }
        } while (opcao != 6);
    }

    private void listarAtividades(EstadoAtividade estado) {
        Collection<Atividade> atividades = sistema.getAtividades().atividadesPorEstado(estado);

        if (atividades.isEmpty()) {
            consola.escrever("Nenhuma atividade encontrada.");
            return;
        }

        for (Atividade a : atividades) {
            if (a.getProjeto() != null && a.getProjeto().getCoordenador().equals(coordenador)) {
                consola.escrever(a.getIdAtividade() + " | " + a.getTipoAtividade() + " | " + a.getInvestigador().getNome() + " | " + a.getEstado());
            }
        }
    }

    private void criarAtividade() {
        try {
            String idProjeto = consola.lerString("ID do Projeto: ");
            Projeto projeto = sistema.getProjetos().procurarPorId(idProjeto);
            if (projeto == null) {
                consola.escreverErro("Projeto inválido.");
                return;
            }

            if (!projeto.getCoordenador().equals(coordenador)) {
                consola.escreverErro("Não tem permissão neste projeto.");
                return;
            }

            String idInv = consola.lerString("ID do Investigador: ");
            Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

            if (inv == null) {
                consola.escreverErro("Investigador inválido.");
                return;
            }

            String tipo = consola.lerString("Tipo de Atividade: ");
            
            // O Coordenador define a meta
            double duracao = consola.lerDecimal("Duração Prevista (horas): ");

            Atividade atividade = new Atividade(null, projeto, inv, tipo, LocalDateTime.now(), duracao, EstadoAtividade.EM_CURSO);

            sistema.adicionarAtividade(atividade);

            consola.escrever("Atividade criada e adicionada com sucesso.");

        } catch (DadosInvalidosException | EntidadeDuplicadaException e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void gerirAtividade() {

        String id = consola.lerString("ID Atividade: ");
        Atividade a = sistema.getAtividades().procurarPorIdAtividade(id);

        if (a == null) {
            consola.escreverErro("Atividade não encontrada.");
            return;
        }

        consola.escrever("\nEstado Atual: " + a.getEstado());
        consola.escrever("Alterar Estado: ");
        String[] opcoes ={
            "Em Curso",
            "Concluída",
            "Cancelada"
        };

        int opcao = consola.lerInteiro(opcoes);

        switch (opcao) {
            case 1: { // Voltar a pôr EM_CURSO
                sistema.alterarEstadoAtividade(a, EstadoAtividade.EM_CURSO, coordenador);
                consola.escrever("Estado alterado para EM_CURSO.");
                break;
            }

            case 2: { // CONCLUIR (Caso Especial)
                //Pedimos a duração real
                consola.escrever("Previsão inicial: " + a.getDuracaoPrevista() + "h");
                double horasReais = consola.lerDecimal("Indique a duração REAL executada (horas): ");

                try {
                    sistema.concluirAtividade(a, horasReais, coordenador);
                    consola.escrever("Atividade concluída e horas processadas para o investigador.");
                } catch (Exception e) {
                    consola.escreverErro("Erro ao concluir: " + e.getMessage());
                }
                break;
            }

            case 3: { // CANCELAR
                sistema.alterarEstadoAtividade(a, EstadoAtividade.CANCELADA, coordenador);
                consola.escrever("Atividade cancelada.");
                break;
            }

            case 0:
                break;

            default: {
                consola.escreverErro("Opção inválida.");
                break;
            }
        }
    }

    private void removerAtividade() {
        String id = consola.lerString("ID da Atividade: ");
        Atividade atv = sistema.getAtividades().procurarPorIdAtividade(id);

        if(atv == null){ consola.escreverErro("Atividade não encontrada."); return; }

        try {
            sistema.removerAtividade(atv, coordenador);
            consola.escrever("Atividade removida com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    //ESTATISTICAS
    private void menuEstatisticas() {

        int opcao;

        do {
            consola.escrever("\nESTATÍSTICAS DO COORDENADOR");

            String[] opcoes = {
                "Estatísticas globais dos meus projetos",
                "Estatísticas individuais de um investigador",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                case 1: {
                    mostrarEstatisticasGlobais();
                    break;
                }
                case 2: {
                    mostrarEstatisticasInvestigador();
                    break;
                }
                case 3: {
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida.");
                }
            }

        } while (opcao != 3);
    }
    
    private void mostrarEstatisticasGlobais() {

        String idCoord = coordenador.getIdUtilizador();

        consola.escrever("\nESTATÍSTICAS GERAIS");

        consola.escrever("Projetos que gere: "
                + sistema.numeroProjetosDoCoordenador(idCoord));

        consola.escrever("Horas totais dos projetos: "
                + sistema.horasTotaisProjetosCoordenador(idCoord));

        consola.escrever("Total de investigadores envolvidos: "
                + sistema.totalInvestigadoresNosProjetosCoordenador(idCoord));

        consola.escrever("Total de atividades nos projetos: "
                + sistema.totalAtividadesNosProjetosCoordenador(idCoord));

        consola.escrever("\nTop 3 Investigadores (dos seus projetos):");

        int count = 0;
        for (Investigador inv : sistema.topInvestigadoresDoCoordenador(idCoord)) {
            consola.escrever(inv.getNome() + " | "
                    + String.format("%.2f", inv.getScoreRanking()));
            count++;
            if (count == 3) {
                break;
            }
        }
    }
    
    private void mostrarEstatisticasInvestigador() {

        String idInv = consola.lerString("ID do Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

        if (inv == null) {
            consola.escreverErro("Investigador não encontrado.");
            return;
        }

        //Verificação de permissão:
        if (!sistema.investigadorPertenceAProjetosDoCoordenador(inv, coordenador)) {
            consola.escreverErro("Este investigador não pertence a nenhum dos seus projetos.");
            return;
        }

        consola.escrever("\nESTATÍSTICAS DO INVESTIGADOR");
        consola.escrever("Nome: " + inv.getNome());
        consola.escrever("ID: " + inv.getIdUtilizador());

        consola.escrever("Horas totais: "
                + sistema.horasTotaisInvestigador(inv.getIdUtilizador()));

        consola.escrever("Número de atividades: "
                + sistema.numeroAtividadesInvestigador(inv.getIdUtilizador()));

        consola.escrever("Número de projetos: "
                + sistema.numeroProjetosInvestigador(inv.getIdUtilizador()));

        consola.escrever("Score de ranking: "
                + String.format("%.2f", sistema.rankingInvestigador(inv)));
    }
    
//BONUS
    
    private void menuBonus() {
        consola.escrever("\nESTATÍSTICAS BONUS");
        
        //Calcular os projetos concluídos DESTE coordenador
        int meusConcluidos = 0;
        for (Projeto p : coordenador.getProjetosGeridos()) {
            if (p.getEstado() == EstadoProjeto.CONCLUIDO) {
                meusConcluidos++;
            }
        }
        
        consola.escrever("O seu desempenho atual:");
        consola.escrever("-> Projetos Concluídos: " + meusConcluidos);

        //Calcular qual é o recorde atual no sistema (Competição)
        int maxNoSistema = 0;
        for (Coordenador c : sistema.getCoordenadores().getTodos()) {
            int count = 0;
            for (Projeto p : c.getProjetosGeridos()) {
                if (p.getEstado() == EstadoProjeto.CONCLUIDO) {
                    count++;
                }
            }
            if (count > maxNoSistema) {
                maxNoSistema = count;
            }
        }

        //Dar feedback
        consola.escrever("\n--- Competição ---");
        if (meusConcluidos > 0 && meusConcluidos >= maxNoSistema) {
            consola.escrever("Parabéns! Atualmente você lidera (ou empata) o ranking para o Prémio.");
        } else {
            consola.escrever("O atual líder tem " + maxNoSistema + " projetos concluídos.");
            consola.escrever("Faltam-lhe " + (maxNoSistema - meusConcluidos + 1) + " para ultrapassar.");
        }
    }




}