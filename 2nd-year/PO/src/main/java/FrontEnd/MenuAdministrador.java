package FrontEnd;

import BackEnd.*;
import BackEnd.exceptions.DadosInvalidosException;
import BackEnd.exceptions.EntidadeDuplicadaException;
import BackEnd.exceptions.EntidadeNaoEncontradaException;
import BackEnd.exceptions.RegraNegocioException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class MenuAdministrador {

    private SistemaGestao sistema;
    private Consola consola;
    private Administrador admin;

    public MenuAdministrador(SistemaGestao sistema, Consola consola, Administrador admin) {
        this.sistema = sistema;
        this.consola = consola;
        this.admin = admin;
    }

// Método auxiliar para mostrar um alerta
    private void verificarAlertasBonus() {
        boolean temAlertas = false;

        consola.escrever("\n--- CENTRO DE NOTIFICAÇÕES E ALERTAS ---");

        // Alerta de Investigadores (Presentes Tecnológicos)
        Collection<Investigador> pendentes = sistema.getInvestigadoresComPresentesPendentes();
        if (!pendentes.isEmpty()) {
            consola.escrever("\nALERTA: Existem presentes tecnológicos por atribuir:");
            for (Investigador inv : pendentes) {
                int emFalta = inv.presentesPorHorasExtra() - inv.getPresentesTecnologicosRecebidos();
                consola.escrever(" - " + inv.getNome() + " (ID: " + inv.getIdUtilizador() + ")");
                consola.escrever(" Tem " + inv.getHorasExtrasAcumuladas() + "h extra. Merece mais " + emFalta + " presente(s).");
            }
        }
        // Alerta de Investigadores (Prémio Produtividade)
        if (sistema.existeCandidatoProdutividade()) {
            temAlertas = true;
            consola.escrever("\nALERTA: Há investigadores elegíveis para o Prémio de Produtividade.");
        }

        // Alerta de Investigadores: Prémio de Equipa
        if (sistema.existeCandidatoPremioEquipa()) {
            temAlertas = true;
            consola.escrever("\nALERTA: Há projetos elegíveis para o Prémio de Equipa.");
        }

        // Alerta de Coordenadores (Melhor Gestor)
        if (sistema.existeCandidatoMelhorGestor()) {
            temAlertas = true;
            consola.escrever("\nALERTA: Há coordenadores elegíveis para o Prémio de Melhor Gestor.");
        }

        // Se houver algum alerta, mostra a mensagem final
        if (temAlertas) {
            consola.escrever("\nDirija-se ao menu 'Atribuir Bónus' (Opção 7) para regularizar/atribuir.");
            consola.escrever("**************************************************************************\n");
            consola.lerString("Pressione ENTER para continuar para o menu...");
        }else {
            consola.escrever("Sem notificações nem alertas");
        }
    }


    public void mostrarMenu() {

        // AQUI: O Alerta aparece antes de mostrar as opções
        verificarAlertasBonus();

        int opcao;

        do {
            consola.escrever("\n");
            String[] opcoes = {
                "Dados Pessoais",
                "Gerir Utilizadores",
                "Gerir Laboratórios",
                "Gerir Projetos",
                "Gerir Atividades",
                "Monitorizar Estatísticas do Sistema",
                "Atribuir Bónus e Prémios",
                "Consultar Histórico",
                "Desbloquear Utilizador",
                "Sair"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                case 1: {
                    menuDadosPessoais();
                    break;
                }

                case 2: {
                    menuGestaoUtilizadores();
                    break;
                }
                case 3: {
                    menuLaboratorios();
                    break;
                }
                case 4: {
                    menuProjetos();
                    break;
                }
                case 5:{
                    menuAtividades();
                    break;
                }
                case 6: {
                    menuEstatisticasAdmin();
                    break;
                }
                case 7: {
                    menuBonus();
                    break;
                }
                case 8: {
                    MenuHistorico menuH = new MenuHistorico(sistema, consola);
                    menuH.mostrarMenu();
                    break;
                }
                case 9: {
                    desbloquearUtilizador();
                    break;
                }
                case 10: {
                    sistema.registarLogout(admin);
                    consola.escrever("A sair...");
                    break;
                }
                default: {
                    consola.escreverErro("Opção inválida.");
                    break;
                }
            }

        } while (opcao != 10);
    }

//DADOS PESSOAIS
    private void menuDadosPessoais() {
        int opcao;

        do {
            consola.escrever("\nDADOS PESSOAIS");
            String [] opcoes= {
                "Ver Dados",
                "Alterar email",
                "Alterar username",
                "Alterar password",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
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
                default: consola.escreverErro("Opção inválida.");
            }

        } while (opcao != 5);
    }

    private void mostrarDados() {
        consola.escrever("\n--- Dados Pessoais ---");
        consola.escrever("ID: " + admin.getIdUtilizador());
        consola.escrever("Nome: " + admin.getNome());
        consola.escrever("Email: " + admin.getEmail());
        consola.escrever("Username: " + admin.getUsername());
        consola.escrever("Cargo: Administrador");
    }

    private void alterarEmail(){
        String novo = consola.lerEmail("Novo email: ");

        try {
            sistema.alterarEmail(admin, novo);
            consola.escrever("Email alterado com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void alterarUsername(){
        String novo = consola.lerString("Novo username: ");

        try {
            sistema.alterarUsername(admin, novo);
            consola.escrever("Username alterado com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void alterarPassword(){
        String nova = consola.lerString("Nova Password: ");

        try {
            sistema.alterarPassword(admin, nova);
            consola.escrever("Password alterada com sucesso.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

//PROJETOS
    private void menuProjetos() {
        int opcao;

        do {
            consola.escrever("\nPROJETOS");
            String[] opcoes= {
                "Projetos em curso",
                "Projetos concluídos",
                "Criar Projeto",
                "Gerir Projeto",
                "Remover Projeto",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {

                case 1: {
                    listarProjetosPorEstado(EstadoProjeto.EM_CURSO);
                    break;
                }

                case 2: {
                    listarProjetosPorEstado(EstadoProjeto.CONCLUIDO);
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
                    removerProjeto();
                    break;
                }

                case 6: break;

                default: consola.escreverErro("Opção inválida.");
            }
        } while (opcao != 6);
    }

    private void listarProjetosPorEstado(EstadoProjeto estado) {
        Collection<Projeto> lista = sistema.getProjetos().projetosPorEstado(estado);

        if (lista.isEmpty()) {
            consola.escrever("Nenhum projeto encontrado.");
            return;
        }

        for (Projeto p : lista) {
            consola.escrever(p.getIdProjeto() + " | " + p.getTituloProjeto() + " | " + p.getEstado());
        }
    }

    private void criarProjeto() {
        String titulo = consola.lerString("Título: ");
        consola.escrever("Área de Cientifica: \n");
        String[] areas = {
            "Biotecnologia",
            "Energia",
            "Robótica"
        };
        int area = consola.lerInteiro(areas);

        // adm pode criar projetos mas não é coordenador
        String idCoord = consola.lerString("ID do Coordenador responsável: ");
        Coordenador coordenador = sistema.getCoordenadores().procurarPorId(idCoord);

        if (coordenador == null) {
            consola.escreverErro("Coordenador inexistente!");
            return;
        }

        Projeto p = null;

        switch (area){
            case 1 : {
                p = new Projeto(null, titulo, "Biotecnologia", coordenador, EstadoProjeto.EM_CURSO);
                break;
            }
            case 2: {
                p = new Projeto(null, titulo, "Energia", coordenador, EstadoProjeto.EM_CURSO);
                break;
            }
            case 3:{
                p = new Projeto(null, titulo, "Robótica", coordenador, EstadoProjeto.EM_CURSO);
                break;
            }
        }



        try {
            sistema.adicionarProjeto(p);
            consola.escrever("Projeto criado com sucesso!");
            perguntarAdicionarInvestigadores(p);
        } catch (DadosInvalidosException | EntidadeDuplicadaException e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void perguntarAdicionarInvestigadores(Projeto projeto) {
        int opcao;

        do {
            consola.escrever("\nDeseja adicionar investigadores ao projeto agora?");
            String[] opcoes = {
                "Adicionar investigador ao projeto",
                "Terminar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                case 1: {
                    String idInv = consola.lerString("ID do Investigador: ");
                    Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

                    if (inv == null) {
                        consola.escreverErro("Investigador inválido.");
                    } else {
                        try {
                            sistema.adicionarInvestigadorProjeto(inv, projeto);
                            consola.escrever("Investigador adicionado com sucesso!");
                        } catch (Exception e) {
                            consola.escreverErro(e.getMessage());
                        }
                    }
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


    private void removerProjeto() {
        String id = consola.lerString("ID Projeto: ");
        Projeto p = sistema.getProjetos().procurarPorId(id);


        if (p == null) {
            consola.escreverErro("Projeto não existe.");
            return;
        }

        try {
            sistema.removerProjeto(p, admin);
            consola.escrever("Projeto removido.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }


    }

    private void gerirProjeto() {

        consola.escrever("\nGERIR PROJETO");

        String id = consola.lerString("ID do Projeto: ");
        Projeto projeto = sistema.getProjetos().procurarPorId(id);

        if (projeto == null) {
            consola.escreverErro("Projeto não encontrado.");
            return;
        }

        int opcao = -1;

        do {
            consola.escrever("\nProjeto: " + projeto.getTituloProjeto() + "\n");

            String[] opcoes = {
                "Adicionar Investigador",
                "Remover Investigador",
                "Alterar Estado",
                "Remover Projeto",
                "Alterar Coordenador do Projeto",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {

                case 1: {//ADICIONAR INVESTIGADOR
                    String idInv = consola.lerString("ID do Investigador: ");
                    Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

                    if (inv == null) {
                        consola.escreverErro("Investigador inválido.");
                    } else {
                        try {
                            sistema.adicionarInvestigadorProjeto(inv, projeto);
                            consola.escrever("Investigador adicionado com sucesso!");
                        } catch (Exception e) {
                            consola.escreverErro(e.getMessage());
                        }
                    }
                    break;
                }


                case 2: { // REMOVER INVESTIGADOR
                    String idInv = consola.lerString("ID do Investigador: ");
                    Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

                    if (inv == null) {
                        consola.escreverErro("Investigador inválido.");
                    } else {
                        try {
                            sistema.removerInvestigadorDeProjeto(inv, projeto, admin);
                            consola.escrever("Investigador removido com sucesso!");
                        } catch (Exception e) { consola.escreverErro(e.getMessage()); }
                    }
                    break;
                }

                case 3: { // ALTERAR ESTADO
                    consola.escrever("\nNovo Estado");

                    String[] estados = {
                        "Em Curso",
                        "Concluído",
                        "Suspenso"
                    };

                    int es = consola.lerInteiro(estados);

                    EstadoProjeto novo = null;

                    switch (es) {
                        case 1:{
                            novo = EstadoProjeto.EM_CURSO;
                            break;
                        }
                        case 2:{
                            novo = EstadoProjeto.CONCLUIDO;
                            break;
                        }
                        case 3:{
                            novo = EstadoProjeto.SUSPENSO;
                            break;
                        }
                    }

                    sistema.alterarEstadoProjeto(projeto, novo);
                    consola.escrever("Estado atualizado com sucesso!");
                    break;
                }

                case 4: { // REMOVER PROJETO
                    try {
                        sistema.removerProjeto(projeto, admin);
                        consola.escrever("Projeto removido com sucesso!");
                        return;
                    } catch (Exception e) {
                        consola.escreverErro(e.getMessage());
                    }
                    break;
                }

                case 5: {// ALTERAR COORDENADOR
                    alterarCoordenadorProjeto(projeto);
                    break;
                }

                case 6: { // VOLTAR
                    break;
                }

                default:
                    consola.escreverErro("Opção inválida.");
            }

        } while (opcao != 6);
    }

    private void alterarCoordenadorProjeto(Projeto projeto) {

        consola.escrever("\nALTERAR COORDENADOR DO PROJETO ");

        // Mostrar coordenador atual
        if (projeto.getCoordenador() != null) {
            consola.escrever("Coordenador atual: "
                    + projeto.getCoordenador().getIdUtilizador()
                    + " | " + projeto.getCoordenador().getNome());
        } else {
            consola.escrever("Projeto sem coordenador atribuído.");
        }

        consola.escrever("\nCoordenadores disponíveis:");

        for (Coordenador c : sistema.getCoordenadores().getTodos()) {
            // não listar o atual
            if (projeto.getCoordenador() == null
                    || !c.getIdUtilizador().equals(projeto.getCoordenador().getIdUtilizador())) {

                consola.escrever(c.getIdUtilizador() + " | " + c.getNome());
            }
        }

        String idNovo = consola.lerString("ID do novo Coordenador: ");
        Coordenador novo = sistema.getCoordenadores().procurarPorId(idNovo);

        if (novo == null) {
            consola.escreverErro("Coordenador inválido.");
            return;
        }

        try {
            sistema.definirCoordenadorProjeto(projeto, novo, admin);
            consola.escrever("Coordenador do projeto alterado com sucesso!");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }


//ATIVIDADES
    private void menuAtividades() {

        int opcao;

        do {
            consola.escrever("\nATIVIDADES");
            String[] opcoes = {
                "Atividades em curso",
                "Atividades concluídas",
                "Criar atividade",
                "Alterar estado de atividade",
                "Remover atividade",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {

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
                case 6:
                    break;

                default: consola.escreverErro("Opção inválida.");
            }

        } while (opcao != 6);
    }

    private void listarAtividades(EstadoAtividade estado) {
        Collection<Atividade> lista = sistema.getAtividades().atividadesPorEstado(estado);

        if (lista.isEmpty()) {
            consola.escrever("Nenhuma atividade.");
            return;
        }

        for (Atividade a : lista) {
            consola.escrever(a.getIdAtividade() + " | " + a.getTipoAtividade() + " | "
                    + a.getInvestigador().getNome() + " | " + a.getEstado());
        }
    }

    private void criarAtividade() {

        String idProj = consola.lerString("ID Projeto: ");
        Projeto projeto = sistema.getProjetos().procurarPorId(idProj);

        if (projeto == null) {
            consola.escreverErro("Projeto inválido.");
            return;
        }

        String idInv = consola.lerString("ID Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

        if (inv == null) {
            consola.escreverErro("Investigador inválido.");
            return;
        }

        String tipo = consola.lerString("Tipo: ");
        double horas = consola.lerDecimal("Duração: ");

        Atividade a = new Atividade(null, projeto, inv, tipo, LocalDateTime.now(), horas, EstadoAtividade.EM_CURSO);

        try {
            sistema.adicionarAtividade(a);
            consola.escrever("Atividade criada com sucesso!");
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
                sistema.alterarEstadoAtividade(a, EstadoAtividade.EM_CURSO, admin);
                consola.escrever("Estado alterado para EM_CURSO.");
                break;
            }

            case 2: { // CONCLUIR (Caso Especial)
                //Pedimos a duração real
                consola.escrever("Previsão inicial: " + a.getDuracaoPrevista() + "h");
                double horasReais = consola.lerDecimal("Indique a duração REAL executada (horas): ");

                try {
                    sistema.concluirAtividade(a, horasReais, admin);
                    consola.escrever("Atividade concluída e horas processadas para o investigador.");
                } catch (Exception e) {
                    consola.escreverErro("Erro ao concluir: " + e.getMessage());
                }
                break;
            }

            case 3: { // CANCELAR
                sistema.alterarEstadoAtividade(a, EstadoAtividade.CANCELADA, admin);
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
        String id = consola.lerString("ID Atividade: ");
        Atividade a = sistema.getAtividades().procurarPorIdAtividade(id);

        if (a == null) {
            consola.escreverErro("Atividade não existe.");
            return;
        }

        try {
            sistema.removerAtividade(a, admin);
            consola.escrever("Atividade removida.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

//Estatisticas
    private void menuEstatisticasAdmin() {

        int opcao;

        do {
            consola.escrever("\nESTATÍSTICAS (ADMIN)");

            String[] opcoes = {
                "Estatísticas Globais do Sistema",
                "Estatísticas de um Coordenador",
                "Estatísticas de um Investigador",
                "Estatísticas de um Projeto",
                "Estatísticas de um Laboratório",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                case 1: {
                    estatisticasGlobaisSistema();
                    break;
                }
                case 2: {
                    estatisticasCoordenadorEspecifico();
                    break;
                }
                case 3: {
                    estatisticasInvestigadorEspecifico();
                    break;
                }
                case 4: {
                    estatisticasProjetoEspecifico();
                    break;
                }

                case 5: {
                    estatisticasLaboratorioEspecifico();
                    break;
                }

                case 6: break;

                default:
                    consola.escreverErro("Opção inválida.");
            }

        } while (opcao != 6);
    }

    private void estatisticasGlobaisSistema() {

        consola.escrever("\nESTATÍSTICAS GLOBAIS");

        consola.escrever("Total de Investigadores: "
                + sistema.totalInvestigadores());

        consola.escrever("Investigadores Livres: "
                + sistema.totalInvestigadoresLivres());

        consola.escrever("Total de Projetos: "
                + sistema.getProjetos().listaTotalProjetos().size());

        consola.escrever("Total de Atividades: "
                + sistema.getAtividades().tamanhoListaAtividades());

        consola.escrever("Horas Totais do Sistema: "
                + sistema.horasTotaisSistema());

        // 🔹 Ranking geral
        consola.escrever("\nRanking Geral de Investigadores:");
        int pos = 1;
        for (Investigador inv : sistema.rankingGeral()) {
            consola.escrever(pos++ + ". " + inv.getNome()
                    + " | Score: " + String.format("%.2f", inv.getScoreRanking()));
        }

        // 🔹 Estatísticas por laboratório (USANDO OS MÉTODOS NOVOS)
        consola.escrever("\nEstatísticas por Laboratório:");
        for (Laboratorio lab : sistema.getLaboratorios().getTodos()) {
            consola.escrever(
                    lab.getNomeLab()
                    + " | Investigadores: "
                    + sistema.numeroInvestigadoresLaboratorio(lab.getIdLaboratorio())
                    + " | Projetos: "
                    + sistema.numeroProjetosLaboratorio(lab.getIdLaboratorio()) // <--- CORRIGIDO AQUI
                    + " | Horas: "
                    + sistema.horasTotaisLaboratorio(lab.getIdLaboratorio())
            );
        }
    }

    private void estatisticasCoordenadorEspecifico() {

        String id = consola.lerString("ID do Coordenador: ");
        Coordenador c = sistema.getCoordenadores().procurarPorId(id);

        if (c == null) {
            consola.escreverErro("Coordenador não encontrado.");
            return;
        }

        consola.escrever("\nESTATÍSTICAS DO COORDENADOR");
        consola.escrever("Nome: " + c.getNome());

        consola.escrever("Projetos geridos: "
                + sistema.numeroProjetosDoCoordenador(id));

        consola.escrever("Horas totais dos projetos: "
                + sistema.horasTotaisProjetosCoordenador(id));

        consola.escrever("Investigadores envolvidos: "
                + sistema.totalInvestigadoresNosProjetosCoordenador(id));

        consola.escrever("Total de atividades: "
                + sistema.totalAtividadesNosProjetosCoordenador(id));
    }

    private void estatisticasInvestigadorEspecifico() {

        String id = consola.lerString("ID do Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(id);

        if (inv == null) {
            consola.escreverErro("Investigador não encontrado.");
            return;
        }

        consola.escrever("\nESTATÍSTICAS DO INVESTIGADOR");
        consola.escrever("Nome: " + inv.getNome());

        consola.escrever("Horas totais: "
                + sistema.horasTotaisInvestigador(id));

        consola.escrever("Número de atividades: "
                + sistema.numeroAtividadesInvestigador(id));

        consola.escrever("Número de projetos: "
                + sistema.numeroProjetosInvestigador(id));

        consola.escrever("Score ranking: "
                + String.format("%.2f", sistema.rankingInvestigador(inv)));
    }

    private void estatisticasProjetoEspecifico() {

        String idProjeto = consola.lerString("ID do Projeto: ");
        Projeto p = sistema.getProjetos().procurarPorId(idProjeto);

        if (p == null) {
            consola.escreverErro("Projeto não encontrado.");
            return;
        }

        consola.escrever("\nESTATÍSTICAS DO PROJETO");
        consola.escrever("Título: " + p.getTituloProjeto());
        consola.escrever("Área Científica: " + p.getAreaCientifica());
        consola.escrever("Estado: " + p.getEstado());

        consola.escrever("Coordenador: "
                + p.getCoordenador().getNome());

        consola.escrever("Investigadores envolvidos: "
                + p.getInvestigadores().size());

        consola.escrever("Número de atividades: "
                + sistema.getAtividades().numAtividadesPorProjeto(idProjeto));

        consola.escrever("Horas totais do projeto: "
                + sistema.horasTotaisProjeto(idProjeto));
    }

    private void estatisticasLaboratorioEspecifico() {

        String idLab = consola.lerString("ID do Laboratório: ");
        Laboratorio lab = sistema.getLaboratorios().procurarPorId(idLab);

        if (lab == null) {
            consola.escreverErro("Laboratório não encontrado.");
            return;
        }

        consola.escrever("\nESTATÍSTICAS DO LABORATÓRIO");
        consola.escrever("Nome: " + lab.getNomeLab());

        consola.escrever("Investigadores: "
                + sistema.numeroInvestigadoresLaboratorio(idLab));

        consola.escrever("Projetos associados: "
                + sistema.numeroProjetosLaboratorio(idLab));

        consola.escrever("Atividades realizadas: "
                + sistema.totalAtividadesLaboratorio(idLab));

        consola.escrever("Horas totais do laboratório: "
                + sistema.horasTotaisLaboratorio(idLab));
    }


//ATRIBUIR BONUS
    private void menuBonus() {
        int opcao = -1;

        do {
            consola.escrever("\nATRIBUIÇÃO DE BÓNUS");
            String [] opcoes= {
                "Investigador: Mais ativo no período (Vale Refeição)",
                "Investigador: Mais Produtivo (Prémio Produtividade)",
                "Investigador: Horas Extra (Presentes Tecnológicos)",
                "Equipa: Prémio de Projeto (Projeto mais avançado)s", //Um por cada 100h extra de trabalho
                "Coordenador: Melhor Gestor (Mais projetos concluídos)",
                "Executar TODOS os bónus",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                case 1: {
                    atribuirInvestigadorDoPeriodo();
                    break;
                }
                case 2: {
                    Investigador inv = sistema.atribuirInvestigadorMaisProdutivo();
                    if (inv != null)
                        consola.escrever("Investigador mais produtivo: " + inv.getNome());
                    else
                        consola.escrever("Nenhum investigador encontrado.");
                    consola.escrever("\nTodos os bónus foram processados com sucesso!");
                    break;
                }
                case 3: {
                    consola.escrever("\nA verificar horas extra acumuladas...");
                    // O sistema faz a atribuição automática
                    sistema.verificarEAtribuirPresentesTecnologicos();

                    // Confirmamos ao admin que já não há pendentes
                    if (sistema.getInvestigadoresComPresentesPendentes().isEmpty()) {
                        consola.escrever("Sucesso: Todos os presentes devidos foram atribuídos.");
                    }
                    consola.escrever("\nTodos os bónus foram processados com sucesso!");

                    break;
                }
                case 4: {
                    Projeto p = sistema.atribuirPremioEquipaProjeto();
                    if (p != null)
                        consola.escrever("Prémio de equipa atribuído ao projeto " + p.getTituloProjeto());
                    else
                        consola.escrever("Nenhum projeto elegível.");
                    consola.escrever("\nTodos os bónus foram processados com sucesso!");
                    break;
                }
                case 5: {
                    Coordenador c = sistema.atribuirPremioMelhorGestor();
                    if (c != null) {
                        consola.escrever("Prémio de Melhor Gestor atribuído a: " + c.getNome());
                    } else {
                        consola.escrever("Nenhum coordenador elegível (sem projetos concluídos).");
                    }
                    consola.escrever("\nTodos os bónus foram processados com sucesso!");

                    break;
                }
                case 6: {
                    atribuirTodosBonus();
                    break;
                }
                case 7: break;
                default:
                    consola.escreverErro("Opção inválida.");
            }
        } while (opcao != 7);
    }

    private void atribuirInvestigadorDoPeriodo() {
        consola.escrever("\nAtribuir Investigador mais ativo entre:\n");

        //Pedir as datas
        LocalDateTime inicio = consola.lerData("Data inicial: ");
        LocalDateTime fim = consola.lerData("Data final: ");

        if (fim.isBefore(inicio)) {
            consola.escreverErro("Erro de Lógica: A Data Final (" + fim.toLocalDate() + ") "
                    + "não pode ser anterior à Data Inicial (" + inicio.toLocalDate() + ").");
            consola.escrever("A operação foi cancelada. Tente novamente.");
            return; // Sai do método
        }

        Collection<Investigador> lista = sistema.atribuirInvestigadorDoPeriodo(inicio, fim);
        if (lista != null){
            for (Investigador inv : lista){
                if (inv != null)
                    consola.escrever("Investigador mais ativo entre " + inicio.toLocalDate() + " e " + fim.toLocalDate() + " é: " + inv.getNome());
            }
        }else {
            consola.escrever("Nenhum investigador elegível encontrado neste período.");
        }

        consola.escrever("\nTodos os bónus foram processados com sucesso!");
    }

    private void atribuirTodosBonus() {
        consola.escrever("Atribuir TODOS os bónus");

        int ano = consola.lerAno("Ano (YYYY): ");
        int mes = consola.lerMes("Mês (1-12): ");

        LocalDateTime inicio = LocalDateTime.of(ano, mes, 1, 0, 0);
        LocalDateTime fim    = inicio.plusMonths(1).minusSeconds(1);

        sistema.atribuirBonusDoPeriodo(inicio, fim);

        consola.escrever("Todos os bónus foram atribuídos com sucesso!");
    }


// Gestao utilizadores
    private void menuGestaoUtilizadores() {
        int opcao;

        do {
            consola.escrever("\nGESTÃO DE UTILIZADORES");
            String[] opcoes = {
                "Listar Investigadores", // 1
                "Listar Coordenadores", // 2
                "Listar Administradores", // 3
                "Criar Investigador", // 4
                "Criar Coordenador", // 5
                "Criar Administrador", // 6
                "Remover Investigador", // 7
                "Remover Coordenador", // 8
                "Remover Administrador", // 9
                "Voltar" // 10
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                // LISTAS
                case 1:
                    listarInvestigadores();
                    break;
                case 2:
                    listarCoordenadores();
                    break;
                case 3:
                    listarAdministradores();
                    break;

                //CRIAÇÃO
                case 4:
                    criarInvestigador();
                    break;
                case 5:
                    criarCoordenador();
                    break;
                case 6:
                    criarAdministrador();
                    break;

                //REMOÇÃO
                case 7:
                    removerInvestigador();
                    break;
                case 8:
                    removerCoordenador();
                    break;
                case 9:
                    removerAdministrador();
                    break;

                case 10:
                    break;
                default:
                    consola.escreverErro("Opção inválida.");
            }

        } while (opcao != 10);
    }

    private void criarInvestigador() {
        consola.escrever("\nCriar Investigador");

        String nome = consola.lerString("Nome: ");
        String email = consola.lerEmail("Email: ");
        String user = consola.lerString("Username: ");
        String pass = consola.lerString("Password: ");
        consola.escrever("Área de Especialização: \n");
        String[] opcoes
                = {
                    "Biotecnologia",
                    "Energia",
                    "Robótica"
                };

        int opcao = consola.lerInteiro(opcoes);
        Investigador inv = null;

        switch (opcao) {
            case 1: {
                String areaInvestigacao = consola.lerString("Area de investigação: ");
                inv = new InvestigadorBiotecnologia(email, nome, user, pass, "Biotecnologia", areaInvestigacao);
                break;
            }
            case 2: {
                String fonte = consola.lerString("Fonte de Energia Principal Estudada: ");
                inv = new InvestigadorEnergia(email, nome, user, pass, "Energia", fonte);
                break;
            }
            case 3: {
                String sistemaTrabalho = consola.lerString("Tipo de Sistema de Trabalho: ");
                inv = new InvestigadorRobotica(email, nome, user, pass, "Robótica", sistemaTrabalho);
                break;
            }
            default: {
                consola.escreverErro("Opção inválida.");
                break;
            }
        }

        try {
            sistema.adicionarInvestigador(inv);
            consola.escrever("Investigador criado com sucesso!");
        } catch (DadosInvalidosException | EntidadeDuplicadaException e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void criarCoordenador() {
        consola.escrever("\nCriar Coordenador");

        String nome = consola.lerString("Nome: ");
        String email = consola.lerEmail("Email: ");
        String user = consola.lerString("Username: ");
        String pass = consola.lerString("Password: ");

        Coordenador c = new Coordenador(email, nome, user, pass);

        try {
            sistema.adicionarCoordenador(c);
            consola.escrever("Coordenador criado!");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void removerInvestigador() {
        String id = consola.lerString("ID Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(id);

        if (inv == null) {
            consola.escreverErro("Não existe.");
            return;
        }

        try {
            sistema.removerInvestigador(id, admin);
            consola.escrever("Investigador removido.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void removerCoordenador() {
        consola.escrever("\nRemover Coordenador");

        String id = consola.lerString("ID Coordenador a remover: ");
        Coordenador c = sistema.getCoordenadores().procurarPorId(id);

        if (c == null) {
            consola.escreverErro("Coordenador não existe.");
            return;
        }

        //Verificar se o coordenador tem projetos ativos
        Collection<Projeto> listaProjetos = sistema.projetosDoCoordenador(id);
        Coordenador novoCoord = null;

        if (!listaProjetos.isEmpty()) {
            consola.escrever("Atenção: Este coordenador gere " + listaProjetos.size() + " projeto(s).");

            // Se o tamanho da lista for 1, significa que este é o único coordenadr
            if (sistema.getCoordenadores().tamanhoListaCoordenadores() <= 1) {
                consola.escreverErro("ERRO DE OPERAÇÃO: Impossível remover.");
                consola.escreverErro("Motivo: Este é o único coordenador do sistema e tem projetos a seu cargo.");
                consola.escreverErro("Solução: Crie outro coordenador primeiro.");
                return;
            }

            consola.escrever("É obrigatório selecionar um substituto para assumir os projetos.");


            while (novoCoord == null) {
                consola.escrever("\nCoordenadores Disponíveis:");

                for (Coordenador cx : sistema.getCoordenadores().getTodos()) {
                    if (!cx.getIdUtilizador().equals(id)) {
                        consola.escrever(cx.getIdUtilizador() + " | " + cx.getNome());
                    }
                }

                String idNovo = consola.lerString("ID do Coordenador Substituto: ");

                // Opção de cancelar caso o utilizador mude de ideias
                if (idNovo.equalsIgnoreCase("0") || idNovo.equalsIgnoreCase("sair")) {
                    consola.escrever("Operação cancelada.");
                    return;
                }

                novoCoord = sistema.getCoordenadores().procurarPorId(idNovo);

                if (novoCoord == null || novoCoord.getIdUtilizador().equals(id)) {
                    consola.escreverErro("Coordenador inválido! Tente novamente (ou digite '0' para sair).");
                    novoCoord = null;
                }
            }
        }

        //Chamar método do sistema (com ou sem substituto)
        try {
            // O segundo parametro (novoCoord) será null se ele não tiver projetos, o que é válido.
            sistema.removerCoordenador(id, novoCoord);

            if (novoCoord != null) {
                consola.escrever("Sucesso: Coordenador removido e projetos transferidos para " + novoCoord.getNome());
            } else {
                consola.escrever("Sucesso: Coordenador removido (não tinha projetos).");
            }

        } catch (EntidadeNaoEncontradaException | RegraNegocioException e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void criarAdministrador() {
        consola.escrever("\nCriar Administrador");

        String nome = consola.lerString("Nome: ");
        String email = consola.lerEmail("Email: ");
        String user = consola.lerString("Username: ");
        String pass = consola.lerString("Password: ");

        Administrador admin = new Administrador(email, nome, user, pass);

        try {
            sistema.adicionarAdministrador(admin);
            consola.escrever("\nAdministrador criado!");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }

    }

    private void removerAdministrador() {
        consola.escrever("\nRemover Administrador");

        // Listar administradores
        consola.escrever("Administradores existentes:");
        for (Administrador a : sistema.getAdministradores().getTodos()) {
             String marcador = a.getIdUtilizador().equals(admin.getIdUtilizador()) ? " (Você)" : "";
             consola.escrever(a.getIdUtilizador() + " | " + a.getNome() + marcador);
        }

        String id = consola.lerString("ID do Administrador a remover: ");

        try {
            sistema.removerAdministrador(id, admin);
            consola.escrever("Administrador removido com sucesso.");
        } catch (EntidadeNaoEncontradaException | RegraNegocioException e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void listarInvestigadores() {
        consola.escrever("\nLISTA DE INVESTIGADORES");

        // converter para List para poder ordenar
        ArrayList<Investigador> lista = new ArrayList<>(sistema.getInvestigadores().getTodos());

        if (lista.isEmpty()) {
            consola.escrever("Não existem investigadores registados.");
            return;
        }

        // Ordenar por ID numérico (ignorando o texto "INV")
        lista.sort((i1, i2) -> {
            try {
                int id1 = Integer.parseInt(i1.getIdUtilizador().replaceAll("[^0-9]", ""));
                int id2 = Integer.parseInt(i2.getIdUtilizador().replaceAll("[^0-9]", ""));
                return Integer.compare(id1, id2);
            } catch (Exception e) {
                return i1.getIdUtilizador().compareTo(i2.getIdUtilizador());
            }
        });


        for (Investigador i : lista) {
            consola.escrever(String.format("> %s: %s", i.getIdUtilizador(), i.getNome()));
            consola.escrever(String.format("   Username: %s | Área: %s", i.getUsername(), i.getAreaEspecializacao()));
            consola.escrever("   ------------------------------------------------"); // Separador visual
        }
    }

    private void listarCoordenadores() {
        consola.escrever("\nLISTA DE COORDENADORES");

        ArrayList<Coordenador> lista = new ArrayList<>(sistema.getCoordenadores().getTodos());

        if (lista.isEmpty()) {
            consola.escrever("Não existem coordenadores registados.");
            return;
        }

        // Ordenar
        lista.sort((c1, c2) -> {
            try {
                int id1 = Integer.parseInt(c1.getIdUtilizador().replaceAll("[^0-9]", ""));
                int id2 = Integer.parseInt(c2.getIdUtilizador().replaceAll("[^0-9]", ""));
                return Integer.compare(id1, id2);
            } catch (Exception e) {
                return c1.getIdUtilizador().compareTo(c2.getIdUtilizador());
            }
        });

        // Mostrar
        for (Coordenador c : lista) {
            consola.escrever(String.format("> %s: %s", c.getIdUtilizador(), c.getNome()));
            consola.escrever(String.format("   Username: %s", c.getUsername()));
            consola.escrever("   ------------------------------------------------");
        }
    }

    private void listarAdministradores() {
        consola.escrever("\nLISTA DE ADMINISTRADORES");

        ArrayList<Administrador> lista = new ArrayList<>(sistema.getAdministradores().getTodos());

        // Ordenar
        lista.sort((a1, a2) -> {
            try {
                int id1 = Integer.parseInt(a1.getIdUtilizador().replaceAll("[^0-9]", ""));
                int id2 = Integer.parseInt(a2.getIdUtilizador().replaceAll("[^0-9]", ""));
                return Integer.compare(id1, id2);
            } catch (Exception e) {
                return a1.getIdUtilizador().compareTo(a2.getIdUtilizador());
            }
        });

        for (Administrador a : lista) {
            consola.escrever(String.format("> %s: %s", a.getIdUtilizador(), a.getNome()));
            consola.escrever(String.format("   Username: %s", a.getUsername()));
            consola.escrever("   ------------------------------------------------");
        }
    }


//Laboratorios
    private void menuLaboratorios() {
        int opcao;

        do {
            consola.escrever("\nGESTÃO DE LABORATÓRIOS");

            String[] opcoes = {
                "Listar Laboratórios",
                "Criar Laboratório",
                "Remover Laboratório",
                "Adicionar Investigador a Laboratório",
                "Remover Investigador do Laboratório",
                "Mover Investigador entre Laboratórios",
                "Voltar"
            };

            opcao = consola.lerInteiro(opcoes);

            switch (opcao) {
                case 1:
                    listarLaboratorios();
                    break;

                case 2:
                    criarLaboratorio();
                    break;

                case 3:
                    removerLaboratorio();
                    break;

                case 4:
                    adicionarInvestigadorLaboratorio();
                    break;

                case 5:
                    removerInvestigadorLaboratorio();
                    break;

                case 6:
                    moverInvestigadorLaboratorio();
                    break;

                case 7:
                    break;

                default:
                    consola.escreverErro("Opção inválida.");
            }

        } while (opcao != 7);
    }

    private void criarLaboratorio() {
        String nome = consola.lerString("Nome do laboratório: ");
        String loc = consola.lerString("Localização: ");

        Laboratorio lab = new Laboratorio(null, nome, loc);

        try {
            sistema.criarLaboratorio(admin, lab);
            consola.escrever("Laboratório criado com sucesso!");
        } catch (DadosInvalidosException | EntidadeDuplicadaException e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void removerLaboratorio() {
        consola.escrever("\nREMOVER LABORATÓRIO");

        String idRemover = consola.lerString("ID do Laboratório a remover: ");
        Laboratorio labRemover = sistema.getLaboratorios().procurarPorId(idRemover);

        if (labRemover == null) {
            consola.escreverErro("Laboratório não encontrado.");
            return;
        }

        Laboratorio labDestino = null;

        int numInvestigadores = labRemover.getInvestigadores().size();

        if (numInvestigadores > 0) {
            consola.escrever("\nATENÇÃO: Este laboratório tem " + numInvestigadores + " investigador(es).");
            consola.escrever("É obrigatório transferir a equipa para outro laboratório.");

            // Listar opções (exceto o próprio)
            consola.escrever("Laboratórios disponíveis para transferência:");
            boolean haOpcoes = false;
            for (Laboratorio l : sistema.getLaboratorios().getTodos()) {
                if (!l.getIdLaboratorio().equals(labRemover.getIdLaboratorio())) {
                    consola.escrever(l.getIdLaboratorio() + " | " + l.getNomeLab());
                    haOpcoes = true;
                }
            }

            if (!haOpcoes) {
                consola.escreverErro("Erro Crítico: Não existem outros laboratórios para receber a equipa.");
                consola.escreverErro("Crie um novo laboratório antes de remover este.");
                return;
            }

            // Pedir o laboratório de destino
            while (labDestino == null) {
                String idDestino = consola.lerString("ID do Laboratório de Destino: ");
                labDestino = sistema.getLaboratorios().procurarPorId(idDestino);

                if (labDestino == null || labDestino.equals(labRemover)) {
                    consola.escreverErro("Laboratório de destino inválido. Tente novamente.");
                    labDestino = null; // Força o loop a continuar
                }
            }
        }

        try {
            // Passamos o labDestino (pode ser null se o laboratório estivesse vazio, o sistema aceita)
            sistema.removerLaboratorio(admin, idRemover, labDestino);

            if (labDestino != null) {
                consola.escrever("Sucesso: Laboratório removido e equipa transferida para " + labDestino.getNomeLab() + ".");
            } else {
                consola.escrever("Sucesso: Laboratório (vazio) removido.");
            }

        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void listarLaboratorios() {
        for (Laboratorio l : sistema.getLaboratorios().getTodos()) {
            consola.escrever(l.getIdLaboratorio() + " | " + l.getNomeLab() + " | " + l.getLocalizacao());
        }
    }

    private void adicionarInvestigadorLaboratorio() {
        String idInv = consola.lerString("ID do Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

        if (inv == null) {
            consola.escreverErro("Investigador não encontrado.");
            return;
        }

        String idLab = consola.lerString("ID do Laboratório: ");
        Laboratorio lab = sistema.getLaboratorios().procurarPorId(idLab);

        if (lab == null) {
            consola.escreverErro("Laboratório não encontrado.");
            return;
        }

        try {
            sistema.adicionarInvestigadorLaboratorio(inv, lab);
            consola.escrever("Investigador adicionado ao laboratório.");
        } catch (EntidadeNaoEncontradaException | RegraNegocioException e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void removerInvestigadorLaboratorio() {
        String idInv = consola.lerString("ID do Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

        if (inv == null) {
            consola.escreverErro("Investigador não encontrado.");
            return;
        }

        String idLab = consola.lerString("ID do Laboratório: ");
        Laboratorio lab = sistema.getLaboratorios().procurarPorId(idLab);

        try {
            sistema.removerInvestigadorDoLaboratorio(inv, lab, admin);
            consola.escrever("Investigador removido do laboratório.");
        } catch (Exception e) {
            consola.escreverErro(e.getMessage());
        }
    }

    private void moverInvestigadorLaboratorio() {
        String idInv = consola.lerString("ID do Investigador: ");
        Investigador inv = sistema.getInvestigadores().procurarPorId(idInv);

        if (inv == null) {
            consola.escreverErro("Investigador não encontrado.");
            return;
        }

        String idNovoLab = consola.lerString("ID do novo Laboratório: ");
        Laboratorio novoLab = sistema.getLaboratorios().procurarPorId(idNovoLab);

        if (novoLab == null) {
            consola.escreverErro("Laboratório não encontrado.");
            return;
        }

        try {
            sistema.moverInvestigadorLaboratorio(inv, novoLab);
            consola.escrever("Investigador adicionado ao laboratório.");
        } catch (EntidadeNaoEncontradaException | RegraNegocioException e) {
            consola.escreverErro(e.getMessage());
        }
    }

//Desbloquear utilizador
    private void desbloquearUtilizador() {
        String id = consola.lerString("ID do utilizador: ");

        Utilizador u = sistema.getUtilizadores().procurarPorId(id);

        if (u == null) {
            consola.escreverErro("Não existe.");
            return;
        }

        sistema.desbloquearUtilizador(u);

        consola.escrever("Conta desbloqueada.");
    }


}