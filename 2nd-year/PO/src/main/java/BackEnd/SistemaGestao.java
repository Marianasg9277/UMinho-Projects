package BackEnd;

import BackEnd.exceptions.DadosInvalidosException;
import BackEnd.exceptions.EntidadeDuplicadaException;
import BackEnd.exceptions.EntidadeNaoEncontradaException;
import BackEnd.exceptions.RegraNegocioException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

// Implementa Serializable para permitir gravar todo o estado do programa num ficheiro binário
public class SistemaGestao implements Serializable{
    
    // Repositórios (Listas) que armazenam as entidades do sistema
    private final ListaInvestigadores investigadores; //faz sentido declarar estas variáveis como finais pois as listas são instanciadas no construtor e não mudam de referência
    private final ListaCoordenadores coordenadores;
    private final ListaAdministradores administradores;
    private final ListaLaboratorios laboratorios;
    private final ListaProjetos projetos;
    private final ListaAtividades atividades;
    private final ListaUtilizadores utilizadores;
    
    // Lista para registar o log de eventos do sistema
    private final ArrayList<Historico> historico;
    
    // Construtor: Inicializa todas as listas vazias quando o sistema é criado de novo
    public SistemaGestao() {
        this.investigadores  = new ListaInvestigadores();
        this.coordenadores   = new ListaCoordenadores();
        this.administradores = new ListaAdministradores();
        this.laboratorios    = new ListaLaboratorios();
        this.projetos        = new ListaProjetos();
        this.atividades      = new ListaAtividades();
        this.utilizadores    = new ListaUtilizadores();
        this.historico       = new ArrayList<>();
    }
    
    // getters
    public ListaInvestigadores getInvestigadores() {
        return investigadores;
    }

    public ListaCoordenadores getCoordenadores() {
        return coordenadores;
    }

    public ListaAdministradores getAdministradores() {
        return administradores;
    }

    public ListaLaboratorios getLaboratorios() {
        return laboratorios;
    }

    public ListaProjetos getProjetos() {
        return projetos;
    }

    public ListaAtividades getAtividades() {
        return atividades;
    }

    public ListaUtilizadores getUtilizadores() {
        return utilizadores;
    }

    public ArrayList<Historico> getHistorico() {
        return historico;
    }

    //metodo para registar acontecimentos no historico
    public void registarHistorico(String idUtilizador, String descricao) {
        historico.add(new Historico(idUtilizador, descricao));
    }

//investigadores:
    //metodo para adicionar investigadores ao sistema
    public void adicionarInvestigador(Investigador inv) throws EntidadeDuplicadaException, DadosInvalidosException{

        if (inv == null){
            throw new DadosInvalidosException("Investigador inválido.");
        }

        if (investigadores.procurarPorId(inv.getIdUtilizador()) != null){
            throw new EntidadeDuplicadaException("Investigador com ID " + inv.getIdUtilizador() + " já existe.");
        }

        if (utilizadores.procuraPorUsername(inv.getUsername()) != null){
            throw new EntidadeDuplicadaException("Username '" + inv.getUsername() + "' já está em uso.");
        }

        investigadores.adicionarInvestigador(inv);
        utilizadores.adicionarUtilizador(inv);

        registarHistorico(inv.getIdUtilizador(), 
            "Investigador registado no sistema.");
    }

    
    //metodo para eliminar investigador do sistema
    public void removerInvestigador(String id, Utilizador user) throws EntidadeNaoEncontradaException{
        
        if (!(user instanceof Administrador)) {
            throw new RegraNegocioException("Apenas Administradores podem remover investigadores do sistema.");
        }
        
        Investigador inv = investigadores.procurarPorId(id);
        
        if (inv == null){
            throw new EntidadeNaoEncontradaException("Não existe investigador com ID " + id);
        }
        
        // remover de projetos onde possa estar
        Collection<Projeto> projs = projetos.projetosPorInvestigador(id);
        for (Projeto p : projs) {
            p.removerInvestigador(inv);
        }
        
        //remover de laboratorio
        if (inv.getLaboratorioAtual() != null) {
            inv.getLaboratorioAtual().removerInvestigador(inv);
        }

        // remover as suas atividades
        Collection<Atividade> atvs = atividades.atividadesPorInvestigador(id);
        for (Atividade a : new ArrayList<>(atvs)) {
            removerAtividade(a, user);
        }
        
        // remover do sistema central
        utilizadores.removerUtilizador(inv);
        investigadores.removerInvestigador(inv);


        registarHistorico(user.getIdUtilizador(),
            "Removeu o investigador " + id + " do sistema.");
    }

//Projetos:    
    //metodo para adicionar projeto ao sistema
    public void adicionarProjeto(Projeto p)throws DadosInvalidosException, EntidadeDuplicadaException {

        if (p == null) {
            throw new DadosInvalidosException("Projeto inválido.");
        }

        if (projetos.procurarPorId(p.getIdProjeto()) != null) {
            throw new EntidadeDuplicadaException(
                "Projeto " + p.getIdProjeto() + " já existe."
            );
        }

        projetos.adicionarProjeto(p);

        // 🔗 associação bidirecional
        Coordenador c = p.getCoordenador();
        if (c != null) {
            c.adicionarProjetoGerido(p);
        }

        registarHistorico(
            c.getIdUtilizador(),
            "Criou o projeto " + p.getIdProjeto()
        );
    }

    //metodo para remover projeto de todo o sistema
    public void removerProjeto(Projeto p, Utilizador user) throws EntidadeNaoEncontradaException, RegraNegocioException {

        // verificar validação básica
        if (p == null) {
            throw new EntidadeNaoEncontradaException("O projeto que tentou remover não existe.");
        }

        // verificar lógica de permissões
        // se for administrador pode remover sem restrições
        if (user instanceof Administrador) {
        } // se for coordenador tem regras:
        else if (user instanceof Coordenador) {

            // só pode remover projetos que ele coordena (verifica se tem coordenador e se é ele o coordenador)
            if (p.getCoordenador() == null || !p.getCoordenador().getIdUtilizador().equals(user.getIdUtilizador())) {
                throw new RegraNegocioException("Não tem permissão para remover projetos de outros coordenadores.");
            }

            // só pode remover se já estiver CONCLUIDO
            if (p.getEstado() != EstadoProjeto.CONCLUIDO) {
                throw new RegraNegocioException("Coordenadores só podem remover projetos que já estejam no estado 'CONCLUIDO'.");
            }

        } else {
            // Investigadores (ou outros) não podem remover projetos nunca
            throw new RegraNegocioException("Apenas Administradores e Coordenadores podem remover projetos.");
        }
        
    // se cumpriu as regras acima executa a remoção
        // remover atividades associadas 
        Collection<Atividade> atvs = new ArrayList<>(p.getAtividades());
        for (Atividade a : atvs) {
            removerAtividade(a, user);
        }

        // remover de investigadores
        for (Investigador inv : new ArrayList<>(p.getInvestigadores())) {
            p.removerInvestigador(inv); //remove investigadores do projeto
            inv.removerProjeto(p); //remove o projeto dos investigadores
        }

        // remover de laboratorios
        for (Laboratorio lab : new ArrayList<>(p.getLaboratorios())) {
            lab.removerProjeto(p);
        }

        // remover da lista do coordenador
        Coordenador coord = p.getCoordenador();
        if (coord != null) {
            coord.removerProjetoGerido(p);
            /// remover projeto dos projetos geridos
            p.setCoordenador(null); //tirar coordenador do projeto
        }

        // finalmente remover do repositório principal
        projetos.removerProjeto(p);

        registarHistorico(user.getIdUtilizador(),
                "Removeu projeto: " + p.getIdProjeto() + "(" + p.getTituloProjeto() + ")");
    }

    // metodo para trocar a liderança de um projeto
    public void definirCoordenadorProjeto(Projeto p, Coordenador novo, Administrador admin) {

       if (p == null || novo == null || admin == null) {
           throw new DadosInvalidosException("Dados inválidos.");
       }

       Coordenador antigo = p.getCoordenador();

       if (antigo != null) {
           antigo.removerProjetoGerido(p);
       }

       p.setCoordenador(novo);
       novo.adicionarProjetoGerido(p);

       registarHistorico(
           admin.getIdUtilizador(),
           "Alterou coordenador do projeto " + p.getIdProjeto() +
           " para " + novo.getIdUtilizador()
       );
   }

    //metodo para adicionar investigador a projeto e projeto a investigador
    public void adicionarInvestigadorProjeto(Investigador inv, Projeto p) {
        
        if (inv == null || p == null) {
            return;
        }

        p.adicionarInvestigador(inv);

        Laboratorio lab = inv.getLaboratorioAtual();
        
        if (lab != null) {
            // Se o investigador tem laboratório, o projeto passa a estar ligado a esse laboratório
            criarLigacaoProjetoLaboratorio(p, lab);
        }
        
        registarHistorico(
                inv.getIdUtilizador(),
                "Adicionado ao projeto " + p.getIdProjeto()
        );
    }

    //metodo para remover investigador de projeto
    public void removerInvestigadorDeProjeto(Investigador inv, Projeto p, Utilizador user) throws EntidadeNaoEncontradaException, RegraNegocioException{
        
        if (inv == null || p == null) {
            throw new EntidadeNaoEncontradaException("Investigador ou Projeto não encontrados.");
        }
        
        // verificar lógica de permissões
        // se for administrador pode remover sem restrições
        if (user instanceof Administrador) {
        } // se for coordenador tem regras: 
        else if (user instanceof Coordenador) {
        
            // só pode remover projetos que ele coordena
            if (p.getCoordenador() == null || !p.getCoordenador().getIdUtilizador().equals(user.getIdUtilizador())) {
                throw new RegraNegocioException("Não tem permissão para gerir equipas de outros coordenadores.");
            }
            
        } else {
            // Investigadores (ou outros) não podem remover projetos nunca
            throw new RegraNegocioException("Apenas Administradores e Coordenadores podem gerir equipas.");
        }
        
        Laboratorio lab = inv.getLaboratorioAtual();

        p.removerInvestigador(inv);

        if (lab != null) {
            // Se NÃO sobrou ninguém desse laboratório no projeto...
            if (!laborarioAindaTemInvestigadoresNoProjeto(lab, p)) {
                desfazerLigacaoProjetoLaboratorio(p, lab);
            }
        }

        registarHistorico(user.getIdUtilizador(), 
            "Removeu investigador " + inv.getIdUtilizador() + " do projeto " + p.getIdProjeto());
    }

    // metodo para alterar o estado do projeto
    public void alterarEstadoProjeto(Projeto p, EstadoProjeto novoEstado) {
        if (p == null || novoEstado == null) {
            return;
        }

        EstadoProjeto antigo = p.getEstado();
        p.setEstado(novoEstado);

         registarHistorico(
                p.getCoordenador().getIdUtilizador(),
                "Projeto " + p.getIdProjeto() +
                " passou de " + antigo + " para " + novoEstado
        );
    }

//Laboratorios:
    //metodo para criar laboratorio
    public void criarLaboratorio(Administrador admin, Laboratorio lab) throws DadosInvalidosException, EntidadeDuplicadaException {

        if (admin == null || lab == null)
            throw new DadosInvalidosException("Administrador ou laboratório inválido.");

        if (laboratorios.procurarPorId(lab.getIdLaboratorio()) != null)
            throw new EntidadeDuplicadaException("Laboratório já existe: " + lab.getIdLaboratorio());

        laboratorios.adicionarLaboratorio(lab);

        registarHistorico(
            admin.getIdUtilizador(),
            "Criou o laboratório " + lab.getIdLaboratorio()
        );
    }

    //metodo para remover laboratorio
    public void removerLaboratorio(Administrador admin, String idLab, Laboratorio labDestino) throws EntidadeNaoEncontradaException, RegraNegocioException {

        Laboratorio labAlvo = laboratorios.procurarPorId(idLab);

        if (labAlvo == null) {
            throw new EntidadeNaoEncontradaException("Laboratório com ID " + idLab + " não existe.");
        }

        // Verificar se há investigadores para mover
        if (!labAlvo.getInvestigadores().isEmpty()) {

            // Se houver pessoas, o destino é obrigatório
            if (labDestino == null) {
                throw new RegraNegocioException("O laboratório tem investigadores. Indique um laboratório de destino para a transferência.");
            }

            // O destino não pode ser o mesmo que vamos apagar
            if (labDestino.equals(labAlvo)) {
                throw new RegraNegocioException("O laboratório de destino não pode ser o mesmo que está a remover.");
            }

            // TRANSFERÊNCIA DE PESSOAL
            for (Investigador inv : new ArrayList<>(labAlvo.getInvestigadores())) {
                // Remove do antigo
                labAlvo.removerInvestigador(inv);

                // Adiciona ao novo
                labDestino.adicionarInvestigador(inv);

                // Atualiza a referência no investigador
                inv.setLaboratorioAtual(labDestino);
            }
        }

        //Projetos apenas saem do lab antigo, não precisam de ir para o novo obrigatoriamente
        for (Projeto p : new ArrayList<>(labAlvo.getProjetos())) {
            p.removerLaboratorio(labAlvo);
        }

        //Remover o laboratório do sistema
        laboratorios.removerLaboratorio(idLab);

        String msgHistorico = "Laboratório " + idLab + " removido.";
        if (labDestino != null) {
            msgHistorico += " Equipa transferida para " + labDestino.getIdLaboratorio() + ".";
        }

        registarHistorico(admin.getIdUtilizador(), msgHistorico);
    }

    //metodo para adicionar um investigador a um laboratorio e atualiza o estado do investigador
    public void adicionarInvestigadorLaboratorio(Investigador inv, Laboratorio lab) throws EntidadeNaoEncontradaException, RegraNegocioException {

        if (inv == null || lab == null) {
            throw new EntidadeNaoEncontradaException(
                    "Investigador ou laboratório não existe.");
        }

        if (inv.getLaboratorioAtual() != null) {
            throw new RegraNegocioException(
                    "O investigador já pertence a um laboratório.");
        }

        lab.adicionarInvestigador(inv);
        inv.setLaboratorioAtual(lab);

        //O Laboratório ganha acesso a todos os projetos deste investigador
        for (Projeto p : inv.getListaProjetos()) {
            criarLigacaoProjetoLaboratorio(p, lab);
        }
        registarHistorico(
                inv.getIdUtilizador(),
                "Entrou no laboratório " + lab.getNomeLab()
        );
    }

    //metodo para remover investigador de laboratorio
    public void removerInvestigadorDoLaboratorio(Investigador inv, Laboratorio lab, Utilizador user) throws EntidadeNaoEncontradaException, RegraNegocioException {

        if (inv == null || lab == null) {
            throw new EntidadeNaoEncontradaException(
                    "Investigador ou laboratório não existe.");
        }

        if (!(user instanceof Administrador)) {
            throw new RegraNegocioException(
                    "Apenas Administradores podem gerir laboratórios.");
        }

        if (inv.getLaboratorioAtual() == null ||
            !inv.getLaboratorioAtual().equals(lab)) {
            throw new RegraNegocioException(
                    "O investigador não pertence a este laboratório.");
        }

        lab.removerInvestigador(inv);
        inv.setLaboratorioAtual(null);

        for (Projeto p : inv.getListaProjetos()) {
            // Como ele já saiu do Lab (passo 1), verificamos se sobrou lá alguém
            if (!laborarioAindaTemInvestigadoresNoProjeto(lab, p)) {
                desfazerLigacaoProjetoLaboratorio(p, lab);
            }
        }

        registarHistorico(user.getIdUtilizador(),
                "Removeu investigador " + inv.getIdUtilizador() +
                " do laboratório " + lab.getNomeLab());
    }
   
    //metodo para mover um investigador dum laboratorio para outro
    public void moverInvestigadorLaboratorio(Investigador inv, Laboratorio novoLab) throws EntidadeNaoEncontradaException, RegraNegocioException {

        if (inv == null || novoLab == null) {
            throw new EntidadeNaoEncontradaException(
                    "Investigador ou laboratório não existe.");
        }

        Laboratorio atual = inv.getLaboratorioAtual();

        if (atual == null) {
            throw new RegraNegocioException(
                    "O investigador não pertence a nenhum laboratório.");
        }

        if (atual.equals(novoLab)) {
            throw new RegraNegocioException(
                    "O investigador já pertence a este laboratório.");
        }

        atual.removerInvestigador(inv);
        novoLab.adicionarInvestigador(inv);
        inv.setLaboratorioAtual(novoLab);

        registarHistorico(
                inv.getIdUtilizador(),
                "Mudou do laboratório " + atual.getNomeLab() +
                " para " + novoLab.getNomeLab()
        );
    }
  
//Atividades:
    //metodo para adicionar atividade
    public void adicionarAtividade(Atividade a) throws DadosInvalidosException, EntidadeDuplicadaException, RegraNegocioException{

        if (a == null) {
            throw new DadosInvalidosException("Atividade inválida.");
        }

        // verificar validações básicas
        if (a.getDuracao() <= 0) {
            throw new DadosInvalidosException("A duração da atividade deve ser maior que zero.");
        }

        if (a.getData() == null) {
            throw new DadosInvalidosException("A atividade deve ter uma data válida.");
        }

        if (a.getData().isAfter(LocalDateTime.now())) {
            throw new DadosInvalidosException("A data da atividade não pode ser no futuro.");
        }

        // verificar duplicados
        if (atividades.procurarPorIdAtividade(a.getIdAtividade()) != null) {
            throw new EntidadeDuplicadaException("A atividade já existe no sistema.");
        }
        
        // verificar validação de Lógica (o Projeto tem de estar EM CURSO)
        Projeto p = a.getProjeto();
        if (p != null && p.getEstado() != EstadoProjeto.EM_CURSO) {
            throw new RegraNegocioException("Não é possível adicionar atividades a um projeto " + p.getEstado() + ".");
        }
        
        Investigador inv = a.getInvestigador();

        // verifica se o investigador não está a ultrapassar o numero de horas máximas semanais (60 horas)
        if (inv != null) {
            if (!inv.podeAdicionarHoras(a.getDuracao())) {
                throw new RegraNegocioException("Operação cancelada: O investigador atingiria mais de "
                        + Investigador.HORAS_MAXIMAS_SEMANA + " horas semanais."
                );
            }

            inv.adicionarAtividade(a);
        }

        atividades.adicionarAtividade(a);

        registarHistorico(
                inv.getIdUtilizador(),
                "Criou a atividade " + a.getIdAtividade() + " (" + a.getDuracao() + "h)"
        );
    }

    //metodo para remover atividade
    public void removerAtividade(Atividade a, Utilizador user) throws EntidadeNaoEncontradaException, RegraNegocioException{
        
        if (a == null) {
            throw new EntidadeNaoEncontradaException("Atividade não encontrada.");
        }
        
        // não deixamos apagar atividades CONCLUIDAS (a menos que seja Admin)
        if (a.getEstado() == EstadoAtividade.CONCLUIDA && !(user instanceof Administrador)) {
             throw new RegraNegocioException("Não é possível remover atividades já concluídas (histórico protegido).");
        }
        
        // Lógica de Permissões (Coordenador só mexe nos seus projetos)
        if (user instanceof Coordenador) {
            Projeto p = a.getProjeto();
            if (p != null && p.getCoordenador() != null) {
                if (!p.getCoordenador().getIdUtilizador().equals(user.getIdUtilizador())) {
                    throw new RegraNegocioException("Não pode remover atividades de projetos que não coordena.");
                }
            }
        }

        atividades.removerAtividade(a);
        
        //remover atividade do investigador
        Investigador inv = a.getInvestigador();
        if (inv != null) {
            inv.removerAtividade(a);
        }
        
        //remover atividade do projeto
        Projeto proj =a.getProjeto();
        if(proj != null) {
            proj.removerAtividade(a);
        }
        
        registarHistorico(user.getIdUtilizador(), 
                "Removeu a atividade " + a.getIdAtividade());
    }
    
    //metodo para alterar estado de atividade
    public void alterarEstadoAtividade(Atividade atividade, EstadoAtividade novoEstado, Utilizador utilizador) {
        if (atividade == null || novoEstado == null || utilizador == null) {
            return;
        }

        EstadoAtividade estadoAntigo = atividade.getEstado();

        // se não houver mudança real, não regista nada no histórico
        if (estadoAntigo == novoEstado) {
            return;
        }

        atividade.setEstado(novoEstado);

        registarHistorico(
                utilizador.getIdUtilizador(),
                "Atividade " + atividade.getIdAtividade() +
                " passou de " + estadoAntigo + " para " + novoEstado
        );
    }

    // Método específico para quando o Admin ou Coordenador forçam a conclusão
    public void concluirAtividade(Atividade a, double horasReais, Utilizador user) {
        if (a == null || user == null) return;

        // 1. Atualizar a atividade (Define duração real e muda estado para CONCLUIDA)
        a.concluirAtividade(horasReais);

        // 2. Processar as horas no investigador (CRUCIAL para o salário/bónus)
        Investigador inv = a.getInvestigador();
        if (inv != null) {
            inv.registarHoras(horasReais);
        }

        // 3. Registar Histórico
        registarHistorico(
            user.getIdUtilizador(),
            "Concluiu manualmente a atividade " + a.getIdAtividade() + 
            " com duração real de " + horasReais + "h."
        );
    }
//Coordenadores
    //metodo para criar um coordenador
    public void adicionarCoordenador(Coordenador co) throws DadosInvalidosException, EntidadeDuplicadaException {

        if (co == null) {
            throw new DadosInvalidosException("Coordenador inválido.");
        }

        if (coordenadores.procurarPorId(co.getIdUtilizador()) != null) {
            throw new EntidadeDuplicadaException(
                    "Já existe um coordenador com esse ID.");
        }

        if (utilizadores.procuraPorUsername(co.getUsername()) != null) {
            throw new EntidadeDuplicadaException(
                    "Username já está em uso.");
        }

        coordenadores.adicionarCoordenador(co);
        utilizadores.adicionarUtilizador(co);

        registarHistorico(co.getIdUtilizador(),
                "Coordenador criado e registado no sistema");
    }

    //metodo para remover um coordenador de todo o sistema
    public void removerCoordenador(String id, Coordenador novoCoordenador) throws EntidadeNaoEncontradaException, RegraNegocioException {

        Coordenador co = coordenadores.procurarPorId(id);

        if (co == null) {
            throw new EntidadeNaoEncontradaException(
                    "Coordenador com ID " + id + " não existe.");
        }

        Collection<Projeto> projs = projetos.projetosPorCoordenador(id);

        // Se gere projetos, tem de haver substituto
        if (!projs.isEmpty()) {

            if (novoCoordenador == null) {
                throw new RegraNegocioException(
                        "É obrigatório indicar um coordenador substituto.");
            }

            if (novoCoordenador.equals(co)) {
                throw new RegraNegocioException(
                        "O coordenador substituto não pode ser o mesmo.");
            }

            for (Projeto p : projs) {
                p.setCoordenador(novoCoordenador);
                novoCoordenador.adicionarProjetoGerido(p);
            }
        }

        utilizadores.removerUtilizador(co);
        coordenadores.removerCoordenador(co);

        if (novoCoordenador != null) {
            registarHistorico(
                    co.getIdUtilizador(),
                    "Coordenador removido. Projetos transferidos para "
                    + novoCoordenador.getIdUtilizador()
            );
        } else {
            registarHistorico(
                    co.getIdUtilizador(),
                    "Coordenador removido sem projetos associados."
            );
        }

    }
    
//Admnistradores:    
    //metodo para criar administrador
    public void adicionarAdministrador(Administrador admin) throws DadosInvalidosException, EntidadeDuplicadaException {
        if (admin == null){
            throw new DadosInvalidosException("Investigador inválido.");
        }
        if (administradores.procurarPorId(admin.getIdUtilizador()) != null) {
            throw new EntidadeDuplicadaException(
                    "Já existe um administrador com esse ID.");
        }
        if (utilizadores.procuraPorUsername(admin.getUsername()) != null) {
            throw new EntidadeDuplicadaException(
                    "Username já está em uso.");
        }
        
        administradores.adicionarAdministrador(admin);
        utilizadores.adicionarUtilizador(admin);

        registarHistorico(admin.getIdUtilizador(),
            "Administrador adicionado ao sistema");
    }
    
    // Método para remover administrador com segurança
    public void removerAdministrador(String idAlvo, Administrador adminAutenticado) throws EntidadeNaoEncontradaException, RegraNegocioException {
        
        // Verificar se o admin existe
        Administrador adminAlvo = administradores.procurarPorId(idAlvo);
        if (adminAlvo == null) {
            throw new EntidadeNaoEncontradaException("Administrador com ID " + idAlvo + " não encontrado.");
        }

        // Regra de Segurança: Não permitir auto-remoção
        if (idAlvo.equals(adminAutenticado.getIdUtilizador())) {
            throw new RegraNegocioException("Operação inválida: Não pode remover a sua própria conta.");
        }

        //Impedir remoção do último administrador do sistema
        if (administradores.tamanhoAdministradores() <= 1) {
            throw new RegraNegocioException("Não é possível remover o único administrador do sistema.");
        }

        utilizadores.removerUtilizador(adminAlvo);
        administradores.removerAdministrador(adminAlvo);

        registarHistorico(adminAutenticado.getIdUtilizador(), 
            "Removeu o administrador " + idAlvo + " (" + adminAlvo.getNome() + ")");
    }
    
//Metodos para autenticar
    //metodo para validar Login
    public Utilizador validarLogin(String username, String password) {
        Utilizador user = utilizadores.procuraPorUsername(username);

        if (user == null) return null; // username não existe
        if (user.isBloqueado()){//user esta bloqueado
            registarHistorico(user.getIdUtilizador(), "Tem a conta bloqueada, devido a atividade suspeita");
            return null;
        } 
        if (!user.validarPassword(password)){
            user.falhaLogin();
            return null;
        } // password errada
        
        // limpar tentativas pois auteticou com sucesso
        user.resetTentativas();
        registarHistorico(user.getIdUtilizador(), "Autenticou no sistema");
        return user; // sucesso
    }
    
    //metodo para administrador desbloquear utilizador
    public void desbloquearUtilizador(Utilizador user) {
        if (user != null && user.isBloqueado()) {
            user.resetTentativas();
            registarHistorico(user.getIdUtilizador(), "Conta foi desbloqueada pelo administrador");
        }
    }
    
    //metodo para registar logout
    public void registarLogout(Utilizador user) {
        if (user == null) {
            return;
        }

        registarHistorico(user.getIdUtilizador(), "Fez logout do sistema");
    }
    
    public void alterarUsername(Utilizador u, String novoUsername) throws DadosInvalidosException, EntidadeDuplicadaException {

        if (u == null || novoUsername == null || novoUsername.isBlank()) {
            throw new DadosInvalidosException("Username inválido.");
        }

        if (utilizadores.existeUsername(novoUsername)) {
            throw new EntidadeDuplicadaException("Username já existe.");
        }

        utilizadores.atualizarUsername(u, novoUsername);

        registarHistorico(
                u.getIdUtilizador(),
                "Alterou o username para " + novoUsername
        );
    }

    public void alterarEmail(Utilizador u, String novoEmail) throws DadosInvalidosException, EntidadeDuplicadaException {
        if (u == null || novoEmail == null || novoEmail.isBlank()) {
            throw new DadosInvalidosException("Email inválido.");
        }

        if (utilizadores.existeEmail(novoEmail)) {
            throw new EntidadeDuplicadaException("Email já se encontra registado.");
        }

        utilizadores.atualizarEmail(u, novoEmail);

        registarHistorico(
                u.getIdUtilizador(),
                "Alterou o email para " + novoEmail
        );
    }

    public void alterarPassword(Utilizador u, String novaPassword) throws DadosInvalidosException {
        if (u == null || novaPassword == null || novaPassword.isBlank()) {
            throw new DadosInvalidosException("Password inválida.");
        }

        utilizadores.atualizarPassword(u, novaPassword);

        registarHistorico(
                u.getIdUtilizador(),
                "Alterou a password"
        );
    }
    
//Metodos do bonus e premios investigador:
    //metodo para selecionar o investigador mais ativo num intervalo de dados: maior total de horas no período e desempate por nº de atividades
    public Investigador selecionarCandidatoInvestigadorDoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Investigador melhor = null;
        double maxHoras = -1;
        int maxAtividades = -1;

        for (Investigador inv : investigadores.getTodos()) {

            // horas no período
            double horasPeriodo = atividades.totalHorasPorInvestigadorEntreDatas(inv.getIdUtilizador(), inicio, fim);

            // nº atividades no período
            int numAtvPeriodo = 0;
            for (Atividade a : atividades.atividadesPorInvestigador(inv.getIdUtilizador())) {
                if (!a.getData().isBefore(inicio) && !a.getData().isAfter(fim)) {
                    numAtvPeriodo++;
                }
            }

            // critério + desempate
            if (horasPeriodo > maxHoras
                    || (horasPeriodo == maxHoras && numAtvPeriodo > maxAtividades)) {

                melhor = inv;
                maxHoras = horasPeriodo;
                maxAtividades = numAtvPeriodo;
            }
        }

        return melhor;
    }

    // Método para atribuir ao Investigador do Mês (o mais ativo no período)o vale de refeição
    public Collection<Investigador> atribuirInvestigadorDoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Collection<Investigador> vencedores = new ArrayList<>();

        // Calcular o máximo de horas no período
        double maxHoras = -1;

        // Descobrir qual é o máximo
        for (Investigador inv : investigadores.getTodos()) {
            double horas = atividades.totalHorasPorInvestigadorEntreDatas(inv.getIdUtilizador(), inicio, fim);
            if (horas > maxHoras) {
                maxHoras = horas;
            }
        }

        // Se ninguém trabalhou (maxHoras = 0), retorna lista vazia
        if (maxHoras <= 0) {
            return vencedores;
        }

        // procurar por Empates
        for (Investigador inv : investigadores.getTodos()) {
            double horas = atividades.totalHorasPorInvestigadorEntreDatas(inv.getIdUtilizador(), inicio, fim);

            if (horas == maxHoras) {

                // Validação: Verificar se já recebeu vale neste mês/ano
                if (!inv.isRecebeuValeNoMes(fim.getYear(), fim.getMonthValue())) {

                    // Atribuir prémio
                    inv.atribuirValeRefeicaoComData(fim.toLocalDate());
                    vencedores.add(inv);

                    registarHistorico(inv.getIdUtilizador(),
                            "Recebeu Vale de Refeição (Investigador do Período: " + String.format("%.2f", maxHoras) + "h).");
                }
            }
        }

        return vencedores;
    }

    // metodo que verifica as horas extra acumuladas e dá 1 presente tecnológico por cada 100h extras
    public void verificarEAtribuirPresentesTecnologicos() {
        for (Investigador inv : investigadores.getTodos()) {
            int devidos = inv.presentesPorHorasExtra();
            int recebeu = inv.getPresentesTecnologicosRecebidos();

            int novos = devidos - recebeu;

            if (novos > 0) {
                inv.adicionarPresentesTecnologicos(novos);

                registarHistorico(
                        inv.getIdUtilizador(),
                        "Recebeu " + novos
                        + " presente(s) tecnológico(s)."
                );
            }
        }
    }

    // Verifica se há alguém elegível para Produtividade (tem atividades e ainda não recebeu)
    public boolean existeCandidatoProdutividade() {
        for (Investigador inv : investigadores.getTodos()) {
            if (!inv.isRecebeuPremioProdutividade() && inv.getNumeroAtividades() > 0) {
                return true;
            }
        }
        return false;
    }
    
    // metodo que atribui o Prémio de Produtividade ao investigador com mais atividades no total
    public Investigador atribuirInvestigadorMaisProdutivo() {
        Investigador melhor = null;
        int maxAtv = -1;
        for (Investigador inv : investigadores.getTodos()) {
            int n = inv.getNumeroAtividades();
            if (n > maxAtv) {
                maxAtv = n;
                melhor = inv;
            }
        }
        if (melhor != null && !melhor.isRecebeuPremioProdutividade()) {
            melhor.atribuirPremioProdutividade();
            registarHistorico(melhor.getIdUtilizador(),
                    "Recebeu Prémio de Produtividade");
        }
        return melhor;
    }

    // Verifica se há projetos elegíveis para prémio de equipa (têm atividades concluídas)
    public boolean existeCandidatoPremioEquipa() {
        for (Projeto p : projetos.listaTotalProjetos()) {
            // Se tiver pelo menos uma atividade concluída, já tem % de conclusão > 0
            if (atividades.numAtividadesPorProjeto(p.getIdProjeto()) > 0) {
                 return true; 
            }
        }
        return false;
    }
    // metodo que atribui o Prémio de Equipa ao projeto mais avançado (maior % de atividades concluídas)
    public Projeto atribuirPremioEquipaProjeto() {
        Projeto melhor = null;
        double maxPercent = -1;
        for (Projeto p : projetos.listaTotalProjetos()) {
            // calcular percentagem: (atividades concluídas / total)
            Collection<Atividade> acts = atividades.atividadesPorProjeto(p.getIdProjeto());
            if (acts.isEmpty()) {
                continue;
            }
            int total = acts.size();
            int concluidas = 0;
            for (Atividade a : acts) {
                if (a.getEstado() == EstadoAtividade.CONCLUIDA) {
                    concluidas++;
                }
            }
            double pct = (100.0 * concluidas) / total;
            if (pct > maxPercent) {
                maxPercent = pct;
                melhor = p;
            }
        }
        if (melhor != null) {
            for (Investigador inv : melhor.getInvestigadores()) {
                if (!inv.isRecebeuPremioEquipa()) {
                    inv.atribuirPremioEquipa();
                    registarHistorico(inv.getIdUtilizador(),
                            "Recebeu Prémio de Equipa pelo projeto " + melhor.getIdProjeto());
                }
            }
        }
        return melhor;
    }

    //metodo que executa todas as atribuições de prémios para um determinado período
    public void atribuirBonusDoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        // (se quiseres usar as horas no período, podes adaptar atribuirInvestigadorDoMes/Produtivo para usar atividadesEntreDatas)
        
        //Investigadores
        atribuirInvestigadorDoPeriodo(inicio, fim);
        atribuirInvestigadorMaisProdutivo();
        atribuirPremioEquipaProjeto();
        verificarEAtribuirPresentesTecnologicos();

        //Coordenadores
        atribuirPremioMelhorGestor();
        
        // reset das horas semanais para o próximo ciclo
        for (Investigador inv : investigadores.getTodos()) {
            inv.resetSemana();
        }
        registarHistorico("SYSTEM", "Reset semanal após a atribuição de bónus");
    }
    
    // Retorna lista de investigadores a quem o sistema "deve" presentes tecnológicos
    public Collection<Investigador> getInvestigadoresComPresentesPendentes() {
        ArrayList<Investigador> lista = new ArrayList<>();

        for (Investigador inv : investigadores.getTodos()) {
            int merecidos = inv.presentesPorHorasExtra();
            int recebidos = inv.getPresentesTecnologicosRecebidos();

            // Se merece mais do que já recebeu, adiciona à lista de alerta
            if (merecidos > recebidos) {
                lista.add(inv);
            }
        }
        return lista;
    }

//Metodos do bonus dos coordenadores:
    // Verifica se existe algum coordenador elegível para o prémio de Melhor Gestor
    public boolean existeCandidatoMelhorGestor() {
        for (Coordenador c : coordenadores.getTodos()) {
            for (Projeto p : c.getProjetosGeridos()) {
                if (p.getEstado() == EstadoProjeto.CONCLUIDO) {
                    return true; // Basta um coordenador com um projeto concluído para haver competição
                }
            }
        }
        return false;
    }
    
    // Atribui prémio ao Coordenador com mais projetos CONCLUÍDOS
    public Coordenador atribuirPremioMelhorGestor() {
        Coordenador melhor = null;
        int maxConcluidos = -1;

        for (Coordenador c : coordenadores.getTodos()) {
            int concluidos = 0;
            
            // Os projetos deste coordenador
            for (Projeto p : c.getProjetosGeridos()) {
                if (p.getEstado() == EstadoProjeto.CONCLUIDO) {
                    concluidos++;
                }
            }

            // Verifica se é o melhor até agora
            if (concluidos > maxConcluidos) {
                maxConcluidos = concluidos;
                melhor = c;
            }
        }
        
        // Se encontrou alguém com pelo menos 1 projeto concluído
        if (melhor != null && maxConcluidos > 0) {
            registarHistorico(melhor.getIdUtilizador(), 
                "Recebeu o Prémio de Melhor Gestor (" + maxConcluidos + " projetos concluídos).");
            return melhor;
        }
        
        return null; // Ninguém elegível
    }
    
//Estatisticas de investigadores
    public double horasTotaisInvestigador(String idInvestigador) {
        if (idInvestigador == null) return 0;
        return atividades.totalHorasPorInvestigador(idInvestigador);
    }
    
    public int numeroAtividadesInvestigador(String idInvestigador) {
        if (idInvestigador == null) return 0;
        return atividades.numAtividadesPorInvestigador(idInvestigador);
    }
    
    public int numeroProjetosInvestigador(String idInvestigador) { //Este recebe apenas o id
        if (idInvestigador == null) return 0;
        return projetos.numProjetosPorInvestigador(idInvestigador);
    }
    
    public double rankingInvestigador(Investigador inv) {
        return inv.getScoreRanking();
    }
    
    public double horasTotaisLaboratorio(Laboratorio lab) {
        if (lab == null) return 0;
        return projetos.totalHorasPorLaboratorio(lab.getIdLaboratorio());
    }
    
    
    public int totalInvestigadores() {
        return investigadores.obterTotalInvestigadores();
    }
    
      //ranking geral
    public Collection<Investigador> rankingGeral() {
        return investigadores.rankingGeral();
    }
    
    public Collection<Investigador> investigadoresLivres() {
        return investigadores.investigadoresLivres(projetos.listaTotalProjetos());
    }

    public int totalInvestigadoresLivres() {
        return investigadoresLivres().size();
    }

//Estatisticas de coordenadores
    public Collection<Projeto> projetosDoCoordenador(String idCoordenador) {
        if (idCoordenador == null) return new ArrayList<>();
        return projetos.projetosPorCoordenador(idCoordenador);
    }

    public int numeroProjetosDoCoordenador(String idCoordenador) {
        return projetos.numProjetosPorCoordenador(idCoordenador);
    }

    public double horasTotaisProjetosCoordenador(String idCoordenador) {
        Collection<Projeto> ps = projetosDoCoordenador(idCoordenador);
        double total = 0;
        for (Projeto p : ps) {
            total += atividades.totalHorasProjeto(p.getIdProjeto());
        }
        return total;
    }
     
    //metodo para dar o numero total de atividades dos projetos que Coordenador gere
    public int totalAtividadesNosProjetosCoordenador(String idCoordenador) {
        Collection<Projeto> projetosGeridos = projetosDoCoordenador(idCoordenador);
        int total = 0;

        for (Projeto p : projetosGeridos) {
            total += atividades.atividadesPorProjeto(p.getIdProjeto()).size();
        }
        return total;
    }
    
    //metodo q lista apenas o ranking dos investigadores da equipa do coordenador
    public Collection<Investigador> topInvestigadoresDoCoordenador(String idCoordenador) {

        Collection<Projeto> ps = projetosDoCoordenador(idCoordenador);
        ArrayList<Investigador> lista = new ArrayList<>();

        for (Projeto p : ps) {
            for (Investigador inv : p.getInvestigadores()) {
                if (!lista.contains(inv)) {
                    lista.add(inv);
                }
            }
        }

        lista.sort((a, b) -> Double.compare(b.getScoreRanking(), a.getScoreRanking()));
        return lista;
    }
    
    //metodo verifica se certo investigador pertence à equipa do coordenador
    public boolean investigadorPertenceAProjetosDoCoordenador(Investigador inv, Coordenador coord) {

        if (inv == null || coord == null) {
            return false;
        }

        Collection<Projeto> projs = projetosDoCoordenador(coord.getIdUtilizador());

        for (Projeto p : projs) {
            if (p.getInvestigadores().contains(inv)) {
                return true;
            }
        }
        return false;
    }

    //metodo para dar o numero total de investigadores nas equipas dos projetos que gere
    public int totalInvestigadoresNosProjetosCoordenador(String idCoordenador) {
        Collection<Projeto> projetosGeridos = projetosDoCoordenador(idCoordenador);
        ArrayList<String> ids = new ArrayList<>();

        for (Projeto p : projetosGeridos) {
            
            for (Investigador inv : p.getInvestigadores()) {
                if (!ids.contains(inv.getIdUtilizador()))
                    ids.add(inv.getIdUtilizador());  
            }
        }
        return ids.size();
    }
    
//Estaticas de projetos
    public double horasTotaisProjeto(String idProjeto) {
        if (idProjeto == null) return 0;
        return atividades.totalHorasProjeto(idProjeto);
    }

    public double horasTotaisLaboratorio(String idLaboratorio) {
        Laboratorio lab = laboratorios.procurarPorId(idLaboratorio);
        if ( lab == null) {
            return 0;
        }

        double total = 0;
        // Apenas somamos as horas dos investigadores QUE PERTENCEM a este laboratório
        for (Investigador inv : lab.getInvestigadores()) {
            total += atividades.totalHorasPorInvestigador(inv.getIdUtilizador());
        }
        return total;
    }

    public int numProjetosPorLaboratorio(String idLaboratorio) {
        return projetos.numProjetosPorLaboratorio(idLaboratorio);
    }

    public int numeroInvestigadoresLaboratorio(String idLaboratorio) {
        if (idLaboratorio == null) return 0;
        Laboratorio lab = laboratorios.procurarPorId(idLaboratorio);
        if (lab == null) return 0;
        return lab.getInvestigadores().size();
    }

    public double horasTotaisSistema() {
        double total = 0;
        for (Projeto p : projetos.listaTotalProjetos()) {
            total += atividades.totalHorasProjeto(p.getIdProjeto());
        }
        return total;
    }
    
//Estatiscas laboratorio
    public int numeroProjetosLaboratorio(String idLaboratorio) {
        if (idLaboratorio == null) return 0;

        Laboratorio lab = laboratorios.procurarPorId(idLaboratorio);
        if (lab == null) return 0;

        return lab.getProjetos().size();
    }

    public int totalAtividadesLaboratorio(String idLaboratorio) {
        if (idLaboratorio == null) return 0;

        Laboratorio lab = laboratorios.procurarPorId(idLaboratorio);
        if (lab == null) return 0;

        int total = 0;

        // Percorre diretamente a lista de projetos do laboratório
        for (Projeto p : lab.getProjetos()) {

            total += p.getAtividades().size();
        }

        return total;
    }

//Laboratorios metodos auxiliares
    // Verifica se existe MAIS ALGUM investigador deste laboratório neste projeto
    private boolean laborarioAindaTemInvestigadoresNoProjeto(Laboratorio lab, Projeto p) {
        for (Investigador membro : p.getInvestigadores()) {
            // Se encontrarmos alguém que pertença a este lab
            if (membro.getLaboratorioAtual() != null
                    && membro.getLaboratorioAtual().getIdLaboratorio().equals(lab.getIdLaboratorio())) {
                return true; 
            }
        }
        return false; // Não sobrou ninguém
    }

    // Método auxiliar para criar a ligação bidirecional (evita repetir código)
    private void criarLigacaoProjetoLaboratorio(Projeto p, Laboratorio lab) {
        if (p != null && lab != null) {
            // O Lab guarda o projeto
            lab.adicionarProjeto(p);
            // O Projeto guarda o Lab
            p.adicionarLaboratorio(lab);
        }
    }

    // Método auxiliar para destruir a ligação bidirecional
    private void desfazerLigacaoProjetoLaboratorio(Projeto p, Laboratorio lab) {
        if (p != null && lab != null) {
            lab.removerProjeto(p);
            p.removerLaboratorio(lab);
        }
    }

//Persistência de dados (Serialization):
//Guarda o estado ATUAL de todo o sistema num ficheiro binário.
    
    // Método auxiliar para atualizar os contadores estáticos após carregar do ficheiro
    private void atualizarContadoresAposCarregamento() {
        
        // Projetos (Ids tipo "PROJ1", "PROJ2"...)
        int max = 0;
        for (Projeto p : projetos.listaTotalProjetos()) {
            try {
                // "PROJ" tem 4 letras, cortamos e convertemos o resto para numero
                int id = Integer.parseInt(p.getIdProjeto().substring(4)); 
                if (id > max) max = id;
            } catch (Exception e) {} // Ignora se o ID estiver num formato estranho
        }
        Projeto.setContador(max + 1);

        // Investigadores ("INV1", "INV2"...)
        max = 0;
        for (Investigador i : investigadores.getTodos()) {
            try {
                int id = Integer.parseInt(i.getIdUtilizador().substring(3));
                if (id > max) max = id;
            } catch (Exception e) {}
        }
        Investigador.setContador(max + 1);

        // Coordenadores ("CO1", "CO2"...)
        max = 0;
        for (Coordenador c : coordenadores.getTodos()) {
            try {
                int id = Integer.parseInt(c.getIdUtilizador().substring(2));
                if (id > max) max = id;
            } catch (Exception e) {}
        }
        Coordenador.setContador(max + 1);

        // Administradores ("ADMIN1"...)
        max = 0;
        for (Administrador a : administradores.getTodos()) {
            try {
                int id = Integer.parseInt(a.getIdUtilizador().substring(5));
                if (id > max) max = id;
            } catch (Exception e) {}
        }
        Administrador.setContador(max + 1);

        // Atividades ("ATIV1"...)
        max = 0;
        for (Atividade at : atividades.getTodas()) { // ou o nome que deste à lista
             try {
                int id = Integer.parseInt(at.getIdAtividade().substring(4));
                if (id > max) max = id;
            } catch (Exception e) {}
        }
        Atividade.setContador(max + 1);
        
        // Laboratórios ("LAB1"...)
        max = 0;
        for (Laboratorio l : laboratorios.getTodos()) {
            try {
                int id = Integer.parseInt(l.getIdLaboratorio().substring(3));
                if (id > max) max = id;
            } catch (Exception e) {}
        }
        Laboratorio.setContador(max + 1);
        
        // Histórico ("H1"...)
        max = 0;
        for (Historico h : historico) {
            try {
                 int id = Integer.parseInt(h.getIdHistorico().substring(1));
                 if (id > max) max = id;
            } catch (Exception e) {}
        }
        Historico.setContador(max + 1);
    }
    
    //Guarda o objeto 'SistemaGestao' completo (incluindo todas as listas dentro dele)
    public void guardarSistema(String nomeFicheiro) {
        // O "try" (dentro dos parêntesis) cria o fluxo de dados e 
        // garante que o ficheiro é FECHADO automaticamente no fim, mesmo se houver erro.
        try (ObjectOutputStream out
                = new ObjectOutputStream(new FileOutputStream(nomeFicheiro))) {

            // A 'magia' acontece aqui: 'this' representa este objeto SistemaGestao.
            // O writeObject vai percorrer todas as listas (Investigadores, Projetos, etc.)
            // e gravar tudo em cadeia no disco.
            out.writeObject(this);
            System.out.println("Sistema guardado com sucesso!");

        } catch (Exception e) {
            // Se houver erro (ex: disco cheio, sem permissões), apanhamos aqui
            System.out.println("Erro ao guardar o sistema:");
            e.printStackTrace();
        }
    }
    /*
     * Método ESTÁTICO: É chamado sem precisar de uma instância de SistemaGestao (porque ainda não a temos!).
     * Lê o ficheiro do disco e reconstrói o objeto SistemaGestao em memória.
     */
    // Carrega o objeto 'SistemaGestao' do ficheiro
    public static SistemaGestao carregarSistema(String nomeFicheiro) {
        // Tenta abrir o ficheiro para leitura
        try (ObjectInputStream in
                = new ObjectInputStream(new FileInputStream(nomeFicheiro))) {

            // Lê o objeto genérico do ficheiro e faz o 'Casting' ((SistemaGestao))
            // para o transformar de volta no nosso tipo de classe específico.
            SistemaGestao sistema = (SistemaGestao) in.readObject();
            sistema.atualizarContadoresAposCarregamento();
            System.out.println("Sistema carregado com sucesso!");
            return sistema;

        } catch (Exception e) {

            // Se o ficheiro não existir ou estiver estragado, dá erro. 
            // Retornamos 'null' para que a Main saiba que tem de criar um sistema novo vazio.
            System.out.println("Erro ao carregar o sistema:");
            e.printStackTrace();
            return null;
        }
    }
    
}
