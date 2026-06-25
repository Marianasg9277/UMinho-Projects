package BackEnd;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

public abstract class Investigador extends Utilizador implements Serializable {

    private String areaEspecializacao;
    private Laboratorio laboratorioAtual;

    private double horasSemanaAtual; // horas semanais
    private double horasExtrasAcumuladas; // horas extra acumuladas
    private int folgas;// folgas acumuladas em minutos
    private int presentesTecnologicosRecebidos; // presente tecnologico de 100 em 100 horas extras acumuladas
    private boolean recebeuPremioProdutividade; // premio de produtividade (para investigador com mais atividades)
    private boolean recebeuValeRefeicao; // vale refeicao para o investigador do mes
    private boolean recebeuPremioEquipa; //premio simbolico para equipa com projeto mais avançado

    private Collection<Projeto> listaProjetos;
    private Collection<Atividade> listaAtividades;

    public static final int HORAS_MINIMAS_SEMANA = 40; //numeros de horas semanais minimas por lei
    public static final int HORAS_MAXIMAS_SEMANA = 60; //numeros de horas semanais maximas que os inv podem trabalhar com horas extras incluidas

    private static int contador = 1;

    //metodo PRIVADO para incrementar o contador e atribuir automaticamente um id ao utilizador
    private static String gerarId() {
        return "INV" + (contador++);
    }

    private LocalDate dataUltimoValeRefeicao = null;

    public Investigador(String email, String nome, String username, String password, String areaEspecializacao) {
        super(gerarId(), email, nome, username, password);

        this.areaEspecializacao = areaEspecializacao;
        this.laboratorioAtual = null;

        this.listaAtividades = new ArrayList<>();
        this.listaProjetos = new ArrayList<>();

        this.horasSemanaAtual = 0;
        this.horasExtrasAcumuladas = 0;
        this.folgas = 0;

        this.presentesTecnologicosRecebidos = 0;
        this.recebeuPremioProdutividade = false;
        this.recebeuValeRefeicao = false;
        this.recebeuPremioEquipa = false;
    }

    public static void setContador(int valor) {
        contador = valor;
    }

    //getters
    public String getAreaEspecializacao() {
        return areaEspecializacao;
    }

    public Laboratorio getLaboratorioAtual() {
        return laboratorioAtual;
    }

    public double getHorasExtrasAcumuladas() {
        return horasExtrasAcumuladas;
    }

    public int getFolgas() {
        return folgas;
    }

    //Número de atividades realizadas
    public int getNumeroAtividades() {
        return listaAtividades.size();
    }

    //Número de projetos em que participa
    public int getNumeroProjetos() {
        return listaProjetos.size();
    }

    //Cálculo do "Score" do Ranking
    public double getScoreRanking() {
        return getTotalHoras() * 0.6 + getNumeroAtividades() * 0.3 + getNumeroProjetos() * 0.1;
    }

    public double getHorasSemanaAtual() {
        return horasSemanaAtual;
    }

    public Collection<Projeto> getListaProjetos() {
        return listaProjetos;
    }

    public Collection<Atividade> getListaAtividades() {
        return listaAtividades;
    }

    //Método para calcular o total de horas trabalhadas
    public double getTotalHoras() {
        double total = 0;
        for (Atividade atv : listaAtividades) {
            total += atv.getDuracao();
        }
        return total;
    }

    //setters
    public void setAreaEspecializacao(String areaEspecializacao) {
        this.areaEspecializacao = areaEspecializacao;
    }

    public void setLaboratorioAtual(Laboratorio laboratorioAtual) {
        this.laboratorioAtual = laboratorioAtual;
    }

    //método abstrato: como não tem implementação passamos para os filhos
    public abstract String getTipoInvestigador();

    //metodo para adicionar um projeto a investigador
    public void adicionarProjeto(Projeto projeto) {
        if (projeto != null && !listaProjetos.contains(projeto)) {
            listaProjetos.add(projeto);
        }
    }

    //metodo para adicionar atividade a lista de atividades do investigador
    public void adicionarAtividade(Atividade atividade) {
        if (atividade != null && !listaAtividades.contains(atividade)) {
            listaAtividades.add(atividade);
            registarHoras(atividade.getDuracao());
        }
    }

    //metodo para remover o projeto do investigadoor
    public void removerProjeto(Projeto projeto) {
        if (projeto != null && listaProjetos.contains(projeto)) {
            listaProjetos.remove(projeto);
        }
    }

    //metodo para remover uma das atividades programadas para o investigador
    public void removerAtividade(Atividade atividade) {
        if (atividade != null && listaAtividades.contains(atividade)) {
            listaAtividades.remove(atividade);

            // Reverter as horas da semana
            this.horasSemanaAtual -= atividade.getDuracao();

            // Segurança para não ficar negativo (ex: erros de arredondamento)
            if (this.horasSemanaAtual < 0) {
                this.horasSemanaAtual = 0;
            }
        }
    }

    //verifica se pode adicionar mais horas esta semana (nao pode exceder o limite de 60 horas totais por semana)
    public boolean podeAdicionarHoras(double horas) {
        return (horasSemanaAtual + horas) <= HORAS_MAXIMAS_SEMANA;
    }

    //metodo para registar o numero de horas da semana e o numero de horas extras
    public void registarHoras(double horas) {
        double antes = horasSemanaAtual;

        horasSemanaAtual += horas;

        if (antes < HORAS_MINIMAS_SEMANA) {
            double limite = HORAS_MINIMAS_SEMANA - antes;

            if (horas > limite) {
                double extra = horas - limite;
                adicionarHorasExtra(extra);
            }
        } else {
            adicionarHorasExtra(horas);
        }
    }

    //metodo para resetar o numero de horas dadas por cada semana
    public void resetSemana() {
        horasSemanaAtual = 0;
    }

    //metodo para calcular horas extras e folgas
    public void adicionarHorasExtra(double horas) {
        if (horas <= 0) {
            return;
        }
        int antes = presentesPorHorasExtra();
        horasExtrasAcumuladas += horas;
        // 15 minutos de folga por hora extra
        folgas += (int) (horas * 15);
        int depois = presentesPorHorasExtra();
        if (depois > antes) {
            presentesTecnologicosRecebidos += (depois - antes);
        }
    }

    public int getPresentesTecnologicosRecebidos() {
        return presentesTecnologicosRecebidos;
    }

    public void adicionarPresentesTecnologicos(int t) {
        if (t > 0) {
            this.presentesTecnologicosRecebidos += t;
        }
    }

    // metodo que diz quantos presentes deveriam ter com base nas horas
    public int presentesPorHorasExtra() {
        return (int) (horasExtrasAcumuladas / 100);
    }

    public boolean isRecebeuValeRefeicao() {
        return recebeuValeRefeicao;
    }

    public void atribuirValeRefeicao() {
        this.recebeuValeRefeicao = true;
        this.dataUltimoValeRefeicao = LocalDate.now();
    }

    public void atribuirValeRefeicaoComData(LocalDate data) {
        if (data == null) {
            data = LocalDate.now();
        }
        this.recebeuValeRefeicao = true;
        this.dataUltimoValeRefeicao = data;
    }

    public boolean isRecebeuValeNoMes(int ano, int mes) {
        if (dataUltimoValeRefeicao == null) {
            return false;
        }
        return (dataUltimoValeRefeicao.getYear() == ano) && (dataUltimoValeRefeicao.getMonthValue() == mes);
    }

    public boolean isRecebeuPremioProdutividade() {
        return recebeuPremioProdutividade;
    }

    public void atribuirPremioProdutividade() {
        this.recebeuPremioProdutividade = true;
    }

    public boolean isRecebeuPremioEquipa() {
        return recebeuPremioEquipa;
    }

    public void atribuirPremioEquipa() {
        this.recebeuPremioEquipa = true;
    }

    public int horasExtraEmFaltaParaProximoPresente() {
        int resto = ((int) getHorasExtrasAcumuladas() % 100);

        if (resto == 0) {
            return 0;
        } else {
            return 100 - resto;
        }
    }

//GESTÃO SALARIAL
    // diferentes tipos de investigador têm differentes salários base
    public abstract double getSalarioBase();

    //Método que Calcula quantas horas REAIS o investigador completou num mês específico.
    public double getHorasTrabalhadasNoMes(int ano, int mes) {
        double totalHorasReais = 0;

        for (Atividade a : listaAtividades) {
            // Verifica se atividade pertence a este mês/ano
            boolean dataCorreta = (a.getData().getYear() == ano && a.getData().getMonthValue() == mes);

            // Verifica se a atividade está concluída (Só pagamos trabalho fechado)
            boolean estaConcluida = (a.getEstado() == EstadoAtividade.CONCLUIDA);

            if (dataCorreta && estaConcluida) {
                // Aqui usamos o getDuracao() que devolve a duracaoReal
                totalHorasReais += a.getDuracao();
            }
        }
        return totalHorasReais;
    }

    //metodo que calcula o valor a receber no final do mês.
    public double calcularSalarioDoMes(int ano, int mes) {
        double salarioBase = getSalarioBase();

        // Obter horas efetivamente trabalhadas e fechadas
        double horasTotaisMes = getHorasTrabalhadasNoMes(ano, mes);

        // Definir valor hora
        double horasNormaisMensais = 160.0; // Padrão
        double valorHoraNormal = salarioBase / horasNormaisMensais;
        double valorHoraExtra = valorHoraNormal * (1 + 1); // horas extra valem 100% das normais

        if (horasTotaisMes <= horasNormaisMensais) {
            // Se trabalhou menos ou igual a 160h, recebe o base fixo
            return salarioBase;
        } else {
            // Se ultrapassou as 160h, recebe o base + diferença
            double horasExtrasDoMes = horasTotaisMes - horasNormaisMensais;
            double valorExtra = horasExtrasDoMes * valorHoraExtra;

            return salarioBase + valorExtra;
        }
    }

}