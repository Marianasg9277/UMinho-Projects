#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>
#include <string.h>



//Em qualquer parque que use este software, no maximo, podem existir 5 pisos e 200 lugares por piso.
#define MAX_PISOS 5
#define MAX_LUGARES 200
#define MAX_CLIENTES  500 

//enum permite associar os tipos a um valor inteiro DUAS_RODAS = 0; LIGEIRO = 1; LIGEIRO_GRANDE = 2
typedef enum { DUAS_RODAS, LIGEIRO, LIGEIRO_GRANDE } TipoVeiculo;

typedef struct {
    int id; //é um identificador para cada lugar, isto é, por coordenada (0,0) id =0; (0,1) id=1; (0,2) id=3 etc.
    TipoVeiculo tipo; //tipo de lugar
    bool ocupado; //isto é se estiver ocupado ent 1(verdadeiro) caso contrario 0 (falso)
    char matricula[15]; //string da matricula não pode ter mais do que 15 elementos
    char dataEntrada[11]; 
    char horaEntrada[6];  
} Lugar;


typedef struct {
    int numeroPisos;
    int linhas;
    int colunas;
    int lugares2rodas[MAX_PISOS]; //numero total de lugares do tipo 2 rodas por piso
    int lugaresLigeiros[MAX_PISOS]; //numero total de lugares do tipo ligeiro por piso
    int lugaresGrandes[MAX_PISOS]; //numero total de lugares do tipo ligeiro grande por piso
    //preços:
    float min15;
    float min30;
    float hora;
    float horasSeguintes;
    float dia;
    Lugar lugares[MAX_PISOS][MAX_LUGARES]; 
} Parque;


typedef struct {
    char nome[50];
    char morada[100];
    char telefone[15];
    char email[50];
    char veiculos[5][15]; // Máximo de 5 veículos por cliente e a matricula de cada um desses veículos
} Cliente;

Cliente clientes[MAX_CLIENTES];
Parque parque;
bool clientesIniciados = false;
int totalClientes = 0;


// Função para verificar se o ficheiro existe
int ficheiroExiste(char *filename) {
    FILE *file = fopen(filename, "r");
    if (file) {
        fclose(file);
        return 1; //Se conseguiu abrir com sucesso quer dizer que o ficheiro existe, logo verdade 1
    }
    return 0;
}


// Função para o estado inicial do parque com base no ficheiro de configuração, isto é, todos os luagres vazios
void inicializarParque(char *filename) {
    FILE *file = fopen(filename, "r");
    if (!file) {
        perror("Erro ao abrir o ficheiro de configuração"); //Mensagem de erro
        exit(1); //A função exit() força a saída do programa exit(0) EXIT_SUCCESS; exit(1) EXIT_FAILURE
    }

    char linha[100];

    fgets(linha, sizeof(linha), file); // Ignorar o cabeçalho do ficheiro excell

    fgets(linha, sizeof(linha), file);// Obter a informção relativa aos total de pisos, linhas, colunas, lugares2rodas, lugaresLigeiros, lugaresGrandes e ao preco
    int linhas, colunas, pisos, lugares2rodas, lugaresGrandes, lugaresLigeiros;
    sscanf(linha, "%d,%d,%d,%d,%d,%d,%f,%f,%f,%f,%f",&pisos, &linhas, &colunas, &lugares2rodas, &lugaresLigeiros, &lugaresGrandes, 
            &parque.min15, &parque.min30, &parque.hora, &parque.horasSeguintes, &parque.dia);

    parque.linhas          = linhas;
    parque.colunas         = colunas;
    parque.numeroPisos     = pisos;
    

    //Iterar por cada lugar
    for (int i = 0; i < linhas; i++) {
        for (int j = 0; j < colunas; j++) {
            for (int w=0; w < pisos; w++){
                
                parque.lugares2rodas[w]   = lugares2rodas;
                parque.lugaresLigeiros[w] = lugaresLigeiros;
                parque.lugaresGrandes[w]  = lugaresGrandes;

                Lugar *lugar   = &parque.lugares[w][i * colunas + j];
                lugar->id      = i * colunas + j;
                lugar->ocupado = false;

                strcpy(lugar->matricula, "");
                strcpy(lugar->dataEntrada, "");
                strcpy(lugar->horaEntrada, "");

            }
        }
    }
    fclose(file);
}

// Função para inicializar os clientes a partir do ficheiro
void inicializarClientes(const char *filename) {
    FILE *file = fopen(filename, "r");
    if (!file) {
        perror("Erro ao abrir o ficheiro de clientes");
        totalClientes = 0;
        return;
    }

    char linha[256];
    fgets(linha, sizeof(linha), file); // Ignorar cabeçalhos

    while (fgets(linha, sizeof(linha), file)) {
        if (totalClientes >= MAX_CLIENTES) {
            printf("Limite máximo de clientes (%d) atingido.\n", MAX_CLIENTES);
            break;
        }

        Cliente *cliente = &clientes[totalClientes];
        sscanf(linha, "%s,%s,%s,%s,%s,%s,%s,%s,%s", cliente->nome, cliente->morada, cliente->telefone, cliente->email,
               cliente->veiculos[0], cliente->veiculos[1], cliente->veiculos[2], cliente->veiculos[3], cliente->veiculos[4]);

        totalClientes++;
    }

    fclose(file);
    printf("Clientes inicializados com sucesso. Total: %d\n", totalClientes);
}
//Fucão auxiliar
void atualizarFicheiroClientes(char *filename) {
    FILE *file = fopen(filename, "w");
    if (!file) {
        perror("Erro ao atualizar o ficheiro de clientes");
        return;
    }

    // Escrever cabeçalho
    fprintf(file, "Nome,Morada,Telefone,Email,Veiculo1,Veiculo2,Veiculo3,Veiculo4,Veiculo5\n");

    // Escrever clientes
    for (int i = 0; i < totalClientes; i++) {
        Cliente *cliente = &clientes[i];
        fprintf(file, "%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                cliente->nome, cliente->morada, cliente->telefone, cliente->email,
                cliente->veiculos[0], cliente->veiculos[1], cliente->veiculos[2],
                cliente->veiculos[3], cliente->veiculos[4]);
    }

    fclose(file);
    printf("Ficheiro atualizado com sucesso.\n");
}

//Função para registar cliente
void registarCliente(const char *filename, const char *matriculaVeiculo) {
    // Inicializar clientes se ainda não foi feito
    if (!clientesIniciados) {
        inicializarClientes(filename);
        clientesIniciados = true;
    }

    // Verificar se o cliente já está registrado
    char nome[50];
    printf("\n--- Registo de Cliente ---\n");
    printf("Nome: ");
    getchar(); // Limpa o buffer do teclado
    fgets(nome, sizeof(nome), stdin);
    nome[strcspn(nome, "\n")] = '\0'; // Remove o '\n' do final da string

    Cliente *clienteExistente = NULL;
    for (int i = 0; i < totalClientes; i++) {
        if (strcmp(clientes[i].nome, nome) == 0) {
            clienteExistente = &clientes[i];
            break;
        }
    }

    if (clienteExistente) {
        // Verificar limite de veículos
        int veiculosAtuais = 0;
        for (int i = 0; i < 5; i++) {
            if (strlen(clienteExistente->veiculos[i]) > 0) {
                veiculosAtuais++;
            }
        }

        if (veiculosAtuais >= 5) {
            printf("O cliente não pode registar mais nenhum veículo.\n");
            return;
        }

        // Adicionar veículo ao cliente existente
        for (int i = 0; i < 5; i++) {
            if (strlen(clienteExistente->veiculos[i]) == 0) {
                strcpy(clienteExistente->veiculos[i], matriculaVeiculo);
                printf("Veículo adicionado ao cliente existente: %s\n", clienteExistente->nome);
                break;
            }
        }
    } else {
        // Registrar novo cliente
        if (totalClientes >= MAX_CLIENTES) {
            printf("Erro: Limite máximo de clientes (%d) atingido.\n", MAX_CLIENTES);
            return;
        }

        Cliente *novoCliente = &clientes[totalClientes];
        strcpy(novoCliente->nome, nome);

        printf("Morada: ");
        fgets(novoCliente->morada, sizeof(novoCliente->morada), stdin);
        novoCliente->morada[strcspn(novoCliente->morada, "\n")] = '\0';

        printf("Telefone: ");
        fgets(novoCliente->telefone, sizeof(novoCliente->telefone), stdin);
        novoCliente->telefone[strcspn(novoCliente->telefone, "\n")] = '\0';

        printf("Email: ");
        fgets(novoCliente->email, sizeof(novoCliente->email), stdin);
        novoCliente->email[strcspn(novoCliente->email, "\n")] = '\0';

        for (int i = 0; i < 5; i++) {
            strcpy(novoCliente->veiculos[i], (i == 0) ? matriculaVeiculo : "");
        }

        totalClientes++;
        printf("Novo cliente registado com sucesso: %s\n", novoCliente->nome);
    }

    // Atualizar o ficheiro
    atualizarFicheiroClientes("clientes.csv");
}

//Para a opção 0
// Função para apresentar o estado do parque 
void apresentarParque(char *filename) {
    //Se parque.numeroPisos ainda estiver com o valor de zero quer dizer que esta foi a primeira opção que o utilizador escolheu no menu, portanto tem de se dar a inicialização do parque
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }

    printf("\nEstado do Parque:\n");
    //Iteramos por cada lugar
    for (int piso = 0; piso < parque.numeroPisos; piso++) {
        printf("Piso %d:\n", piso + 1);
        for (int i = 0; i < parque.linhas; i++) {
            for (int j = 0; j < parque.colunas; j++) {
                //Se o lugar estiver ocupado marcar com X se não dexar vazio
                Lugar *lugar = &parque.lugares[piso][i * parque.colunas + j];
                printf("[%c] ", lugar->ocupado ? 'X' : ' ');
            }
            printf("\n");
        }
        printf("\n");
    }
}

//Para a opção 2 do "sub-menu" 
// Função para registar a entrada de um veículo
void entradaVeiculo() {

    int piso, linha, coluna, tipo, pisodis, cont= 0;
    char matricula[15];

    printf("Escreva a matrícula do veículo: ");
    scanf("%s", matricula);
    
    printf("Qual o tipo de veículo (0: Duas Rodas, 1: Ligeiro, 2: Ligeiro Grande): ");
    scanf("%d", &tipo);

    while (tipo!= 0 && tipo != 1 && tipo !=2){
        printf("Numero Invalido\nQual o tipo de veículo (0: Duas Rodas, 1: Ligeiro, 2: Ligeiro Grande): ");
        scanf("%d", &tipo);
    }

    printf("Qual o piso (1-%d): ", parque.numeroPisos);
    scanf("%d", &piso);


    while (piso < 1 || piso > parque.numeroPisos) {
        printf("Piso inválido.\nQual o piso (1-%d): ",  parque.numeroPisos);
        scanf("%d", &piso);
    }
    
    int totallugares;
    //Iterar por todos os lugares do piso
    for (int i = 0; i < parque.linhas; i++) {
        for (int j = 0; j < parque.colunas; j++) {
            Lugar *lugar   = &parque.lugares[piso][i * parque.colunas + j];
            if (lugar->ocupado)
                totallugares++;
        }
    }
    //Verificar se todos os lugares do piso estão ocupados
    if (totallugares == parque.colunas* parque.linhas){
        printf("Não há lugares disponiveis neste piso");
        return;
    }

    switch (tipo){ 
        case DUAS_RODAS:
            if (parque.lugares2rodas[piso-1] >  0){ //Verificar se há lugares disponiveis para o tipo naquele piso
                parque.lugares2rodas[piso-1]--;

                printf("Escreva a linha: ");
                scanf("%d", &linha);
                printf("Escreva a coluna: ");
                scanf("%d", &coluna);

                while (linha < 1 || linha > parque.linhas || coluna < 1 || coluna > parque.colunas) {
                    printf("Posição inválida.\n");
                    printf("Escreva nova linha: ");
                    scanf("%d", &linha);
                    printf("Escreva nova coluna: ");
                    scanf("%d", &coluna);        
                }
                
                Lugar *lugar = &parque.lugares[piso - 1][(linha-1) * parque.colunas + (coluna-1)]; //lugar escolhido
                //Verifica se o lugar está ocupado
                if (lugar->ocupado) {
                    printf("Lugar já ocupado. Escolha outro.\n");
                    return;
                }

                lugar->ocupado = true; //Registar o lugar escolhido como ocupado
                strcpy(lugar->matricula, matricula); //Regista a matricula do carro 
                lugar->tipo = tipo; //Registar o tipo de veiculo estacionado naquele lugar

                char data[11], hora[6];
                time_t t = time(NULL);
                struct tm tm = *localtime(&t);
                sprintf(data, "%02d/%02d/%04d", tm.tm_mday, tm.tm_mon + 1, tm.tm_year + 1900);
                sprintf(hora, "%02d:%02d", tm.tm_hour, tm.tm_min);

                strcpy(lugar->dataEntrada, data);//Registar a data atual
                strcpy(lugar->horaEntrada, hora);//Registar a hora atual

                printf("Veículo estacionado no piso %d, linha %d, coluna %d.\n", piso, linha, coluna);
                // Perguntar se deseja registrar o cliente
                int opcao;
                do{
                    printf("Deseja registar este cliente? (1-Sim; 0-Não): ");
                    scanf("%d", &opcao);

                        switch (opcao){
                            case 1:
                                registarCliente("clientes.csv", matricula);
                                break;
                            case 0:
                                return;
                            default:
                                printf("Opção invalida.");
                                break;
                            }
                }while (opcao !=0);

                       
            }else{
                for (int w = 0; w < parque.numeroPisos; w++){
                    if (parque.lugares2rodas[w]>0){
                        cont++;
                        pisodis = w +1;
                    }
                }
                if (cont>0){ //Verificar se existe lugares disponiveis noutros pisos
                    printf("Não existem lugares disponiveis para o veiculo  neste piso\nAinda existem lugares disponiveis (por exemplo no piso %d)\n", pisodis);
                }else{
                    printf("Não existem lugares disponiveis para o tipo de veiculo neste estacinoamento\n");
                }
                    
            }
            break;

        case LIGEIRO:
            if (parque.lugaresLigeiros[piso-1] >  0){
                parque.lugaresLigeiros[piso-1]--;

                printf("Escreva a linha: ");
                scanf("%d", &linha);
                printf("Escreva a coluna: ");
                scanf("%d", &coluna);

                while (linha < 1 || linha > parque.linhas || coluna < 1 || coluna > parque.colunas) {
                    printf("Posição inválida.\n");
                    printf("Escreva nova linha: ");
                    scanf("%d", &linha);
                    printf("Escreva nova coluna: ");
                    scanf("%d", &coluna);        
                }
                Lugar *lugar = &parque.lugares[piso - 1][(linha-1) * parque.colunas + (coluna-1)];
                if (lugar->ocupado) {
                    printf("Lugar já ocupado. Escolha outro.\n");
                    return;
                }

                lugar->ocupado = true;
                strcpy(lugar->matricula, matricula);
                lugar->tipo = tipo;

                char data[11], hora[6];
                time_t t = time(NULL);
                struct tm tm = *localtime(&t);
                sprintf(data, "%02d/%02d/%04d", tm.tm_mday, tm.tm_mon + 1, tm.tm_year + 1900);
                sprintf(hora, "%02d:%02d", tm.tm_hour, tm.tm_min);

                strcpy(lugar->dataEntrada, data);
                strcpy(lugar->horaEntrada, hora);

                printf("Veículo estacionado no piso %d, linha %d, coluna %d.\n", piso, linha, coluna);
                // Perguntar se deseja registrar o cliente
                int opcao;
                do{
                    printf("Deseja registar este cliente? (1-Sim; 0-Não): ");
                    scanf("%d", &opcao);

                    switch (opcao){
                        case 1:
                            registarCliente("clientes.csv", matricula);
                            break;
                        case 0:
                            return;
                        default:
                            printf("Opção invalida.");
                            break;
                    }
                }while (opcao !=0);

                       
            }else{ 
                for (int w = 0; w < parque.numeroPisos; w++){
                    if (parque.lugaresLigeiros[w]>0){
                        cont++;
                        pisodis = w + 1;
                    }
                }
                if (cont>0){
                    printf("Não existem lugares disponiveis para o veiculo  neste piso\nAinda existem lugares disponiveis (por exemplo no piso %d)\n", pisodis);
                }else{
                    printf("Não existem lugares disponiveis para o tipo de veiculo neste estacinoamento\n");
                }
                  
            }             

            break;

        case LIGEIRO_GRANDE:
            if (parque.lugaresGrandes[piso-1] >  0){
                parque.lugaresGrandes[piso-1]--;

                printf("Escreva a linha: ");
                scanf("%d", &linha);
                printf("Escreva a coluna: ");
                scanf("%d", &coluna);

                while (linha < 1 || linha > parque.linhas || coluna < 1 || coluna > parque.colunas) {
                    printf("Posição inválida.\n");
                    printf("Escreva nova linha: ");
                    scanf("%d", &linha);
                    printf("Escreva nova coluna: ");
                    scanf("%d", &coluna);        
                }
                
                Lugar *lugar = &parque.lugares[piso - 1][(linha-1) * parque.colunas + (coluna-1)];
                if (lugar->ocupado) {
                    printf("Lugar já ocupado. Escolha outro.\n");
                    return;
                }

                lugar->ocupado = true;
                strcpy(lugar->matricula, matricula);
                lugar->tipo = tipo;

                char data[11], hora[6];
                time_t t = time(NULL);
                struct tm tm = *localtime(&t);
                sprintf(data, "%02d/%02d/%04d", tm.tm_mday, tm.tm_mon + 1, tm.tm_year + 1900);
                sprintf(hora, "%02d:%02d", tm.tm_hour, tm.tm_min);

                strcpy(lugar->dataEntrada, data);
                strcpy(lugar->horaEntrada, hora);

                printf("Veículo estacionado no piso %d, linha %d, coluna %d.\n", piso, linha, coluna);
                // Perguntar se deseja registrar o cliente
                int opcao;
                do{
                    printf("Deseja registar este cliente? (1-Sim; 0-Não): ");
                    scanf("%d", &opcao);

                    switch (opcao){
                        case 1:
                            registarCliente("clientes.csv", matricula);
                            break;
                        case 0:
                            return;
                        default:
                            printf("Opção invalida.");
                            break;
                    }
                }while (opcao !=0);

                       
            }else{ 
                for (int w = 0; w < parque.numeroPisos; w++){
                    if (parque.lugaresGrandes[w]>0){
                        cont++;
                        pisodis = w+1;
                    }
                }
                if (cont>0){
                    printf("Não existem lugares disponiveis para o veiculo  neste piso\nAinda existem lugares disponiveis (por exemplo no piso %d)\n", pisodis);
                }else{
                    printf("Não existem lugares disponiveis para o tipo de veiculo neste estacinoamento\n");
                }
                  
            }                
            
            break;
        default: 
            printf("Erro no registo do tipo");
            break;

        }
}

//Para a opção 1 do "sub-menu"
void lugarMaisProximo(){
    int piso, linha, coluna, tipo, pisodis;
    char matricula[15];
    Lugar *lugar;
    bool estacionado = false;

    //Matricula
    printf("Escreva a matrícula do veículo: ");
    scanf("%s", matricula);
    
    //Tipo
    printf("Qual o tipo de veículo (0: Duas Rodas, 1: Ligeiro, 2: Ligeiro Grande): ");
    scanf("%d", &tipo);

    while (tipo!= 0 && tipo != 1 && tipo !=2){
        printf("Numero Invalido\nQual o tipo de veículo (0: Duas Rodas, 1: Ligeiro, 2: Ligeiro Grande): ");
        scanf("%d", &tipo);
    }
    // Procurar pelo lugar mais próximo valido
    for (int piso = 0; piso < parque.numeroPisos; piso++) { // Começa pelo piso 1
        for (int i = 0; i < parque.linhas; i++) { //Começa pela a primeira linha
            for (int j = 0; j < parque.colunas; j++) { // Começa pela primeira coluna
                lugar = &parque.lugares[piso][i * parque.colunas + j];

                // Verificar se o lugar está livre e se é compatível com o tipo do veículo
                if (!lugar->ocupado && 
                    ((tipo == DUAS_RODAS && parque.lugares2rodas[piso] > 0) ||
                     (tipo == LIGEIRO && parque.lugaresLigeiros[piso] > 0) ||
                     (tipo == LIGEIRO_GRANDE && parque.lugaresGrandes[piso] > 0))) {

                        // Atualizar o lugar para ocupado
                        lugar->ocupado = true;
                        strcpy(lugar->matricula, matricula);//Registar matricula
                        lugar->tipo = tipo;//Registar tipo de veiculo estacionado

                        //Para obter a data e hora atual
                        char data[11], hora[6];
                        time_t t = time(NULL);
                        struct tm tm = *localtime(&t);
                        sprintf(data, "%02d/%02d/%04d", tm.tm_mday, tm.tm_mon + 1, tm.tm_year + 1900); //Obter data atual
                        sprintf(hora, "%02d:%02d", tm.tm_hour, tm.tm_min); //Obter hora atual

                        strcpy(lugar->dataEntrada, data);//Registar a data atual
                        strcpy(lugar->horaEntrada, hora);//Registar a hora atual
                        
                        // Atualizar os contadores de lugares
                        if (tipo == DUAS_RODAS) {
                            parque.lugares2rodas[piso]--;
                        } else if (tipo == LIGEIRO) {
                            parque.lugaresLigeiros[piso]--;
                        } else if (tipo == LIGEIRO_GRANDE) {
                            parque.lugaresGrandes[piso]--;
                        }

                        printf("Veículo estacionado no piso %d, linha %d, coluna %d.\n", piso + 1, i+1, j+1);
                        estacionado = true; //O carro ficou estacionado

                        // Perguntar se deseja registrar o cliente
                        int opcao;
                        do{
                            printf("Deseja registar este cliente? (1-Sim; 0-Não): ");
                            scanf("%d", &opcao);

                            switch (opcao){
                                case 1:
                                    registarCliente("clientes.csv", matricula);
                                    break;
                                case 0:
                                    return;
                                default:
                                    printf("Opção invalida.");
                                    break;
                            }
                        }while (opcao !=0);

                       
                }
                        return; // Fechar após encontrar o lugar mais próximo
            }
        }
    }
    

    // Se nenhum lugar foi encontrado
    if (!estacionado){
        printf("Não há lugares disponíveis para este tipo de veículo.\n");
    }
}

//Para a opção 1
//Função apresenta um "sub-menu" onde o utilizador escolhe 1 das 3 opções
void subMenuEntrada(char *filename){
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }
    int op;
    do{ 
        printf("\n\nEntrada de Veiculo");
        printf("\n0. Voltar ao menu");
        printf("\n1. Escolher lugar mais próximo livre");
        printf("\n2. Escolher lugar por coordenadas\n\nOpção: ");
        scanf("%d", &op);

        switch (op){
            case 0:
                return;
            case 1:
                lugarMaisProximo();
                break;
            case 2:
                entradaVeiculo();
                break;
            default: 
                printf("Opção invalida");
                break;
        }
        
    }while (op != 0);
}

//Função auxiliar para calcular os preços
float calcularCusto(double diffSegundos) {
    int minutosTotais = diffSegundos / 60;
    int horas = (minutosTotais - 60) / 60;
    int dias = minutosTotais / (24 * 60);


    if (minutosTotais <= 15) {
        return parque.min15;
    } else if (minutosTotais <= 30) {
        return parque.min30;
    } else if (minutosTotais <= 60) {
        return parque.hora;
    } else if (minutosTotais <= 24 * 60) {
        return parque.hora + horas * parque.horasSeguintes;
    } else {
        return dias * parque.dia;
    }
}


//Para a opção 2
// Função para registar a saída de um veículo
void saidaVeiculo(char *filename) {
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }

    char matricula[15];
    printf("Escreva a matrícula do veículo que está de saída: ");
    scanf("%s", matricula);

    bool veiculoEncontrado = false; // Indicador de que o veículo foi encontrado

    for (int piso = 0; piso < parque.numeroPisos && !veiculoEncontrado; piso++) {
        for (int i = 0; i < parque.linhas && !veiculoEncontrado; i++) {
            for (int j = 0; j < parque.colunas && !veiculoEncontrado; j++) {
                Lugar *lugar = &parque.lugares[piso][i * parque.colunas + j];
                if (lugar->ocupado && strcmp(lugar->matricula, matricula) == 0) {
                    veiculoEncontrado = true; // Veículo encontrado

                    // Obter data e hora de entrada
                    int diaEntrada, mesEntrada, anoEntrada, horaEntrada, minutoEntrada;
                    sscanf(lugar->dataEntrada, "%d/%d/%d", &diaEntrada, &mesEntrada, &anoEntrada);
                    sscanf(lugar->horaEntrada, "%d:%d", &horaEntrada, &minutoEntrada);

                    // Obter data e hora atuais
                    time_t t = time(NULL);
                    struct tm tempoAtual = *localtime(&t);

                    struct tm entrada = {0};
                    entrada.tm_mday = diaEntrada;
                    entrada.tm_mon = mesEntrada - 1; // Meses são de 0 a 11
                    entrada.tm_year = anoEntrada - 1900; // Anos são desde 1900
                    entrada.tm_hour = horaEntrada;
                    entrada.tm_min = minutoEntrada;

                    time_t entradaTime = mktime(&entrada);
                    if (entradaTime == -1) {
                        printf("Erro ao calcular o tempo de entrada.\n");
                        return;
                    }

                    // Calcular custo
                    float custo = calcularCusto(difftime(t, entradaTime));


                    // Mostrar informações
                    printf("Veículo com matrícula %s saiu do parque.\n", matricula);
                    printf("Custo total: %.2f Euros\n", custo);

                    // Libertar lugar
                    lugar->ocupado = 0; // Lugar agora está livre
                    strcpy(lugar->matricula, "");
                    strcpy(lugar->dataEntrada, "");
                    strcpy(lugar->horaEntrada, "");

                    // Atualizar contadores de lugares
                    if (lugar->tipo == DUAS_RODAS) {
                        parque.lugares2rodas[piso]++;
                    } else if (lugar->tipo == LIGEIRO) {
                        parque.lugaresLigeiros[piso]++;
                    } else if (lugar->tipo == LIGEIRO_GRANDE) {
                        parque.lugaresGrandes[piso]++;
                    }
                }
            }
        }
    }

    if (!veiculoEncontrado) {
        printf("Veículo com matrícula %s não encontrado no parque.\n", matricula);
    }
}

//Para a opção 3
//Função que vê qual carro está em certa posição
void verCarroEmCoordenadas(char *filename) {
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }

    int piso, linha, coluna;

    printf("Qual o piso (1-%d): ", parque.numeroPisos);
    scanf("%d", &piso);


    while (piso < 1 || piso > parque.numeroPisos) {
        printf("Piso inválido.\nQual o piso (1-%d): ",  parque.numeroPisos);
        scanf("%d", &piso);
    }


    printf("Escreva a linha: ");
    scanf("%d", &linha);
    printf("Escreva a coluna: ");
    scanf("%d", &coluna);

    while (linha < 1 || linha > parque.linhas || coluna < 1 || coluna > parque.colunas) {
        printf("Posição inválida.\n");
        printf("Escreva nova linha: ");
        scanf("%d", &linha);
        printf("Escreva nova coluna: ");
        scanf("%d", &coluna);        
    }

    // Obter o lugar nas coordenadas fornecidas
    Lugar *lugar = &parque.lugares[piso -1 ][(linha-1) * parque.colunas + (coluna-1)];

    // Verificar se o lugar está ocupado
    if (lugar->ocupado) {
        printf("Detalhes do veículo:\n");
        printf("\tMatrícula: %s\n", lugar->matricula); 
        printf("\tTipo: %s\n", lugar->tipo == DUAS_RODAS ? "Duas Rodas" :
                               lugar->tipo == LIGEIRO    ? "Ligeiro" : "Ligeiro Grande");
        printf("\tData de entrada: %s\n", lugar->dataEntrada);
        printf("\tHora de entrada: %s\n", lugar->horaEntrada);
    } else {
        printf("O lugar no piso %d, linha %d, coluna %d está vazio.\n", piso, linha, coluna);
    }
}


//Para opção 4
//Função para listar os Veiculos
void apresentarListaCarros(char *filename) {
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }

    printf("\nLista de Veículos Estacionados:\n");
    //Iterar por piso
    for (int piso = 0; piso < parque.numeroPisos; piso++) {
        printf("Piso %d:\n", piso + 1);
        bool veiculosNoPiso = false;

        //Iterar por cada lugar de um piso
        for (int i = 0; i < parque.linhas; i++) {
            for (int j = 0; j < parque.colunas; j++) {
                Lugar *lugar = &parque.lugares[piso][i * parque.colunas + j];
                if (lugar->ocupado) {
                    char *tipoVeiculo[] = {"Duas Rodas", "Ligeiro", "Ligeiro Grande"}; //Traduzir o tipo para a sua string
                    printf("  - Matrícula: %s | Tipo: %s | Entrada: %s às %s\n",
                           lugar->matricula, tipoVeiculo[lugar->tipo], lugar->dataEntrada, lugar->horaEntrada);
                    veiculosNoPiso = true;
                }
            }
        }
        //Verifica se o piso esrá vazio
        if (!veiculosNoPiso) {
            printf("  Nenhum veículo estacionado neste piso.\n");
        }
        printf("\n");
    }
}

//Para a opcao 5
//Funcao devolve Tempo de estacionamento de um carro 
void estacionamento_Tempo(char *filename) {
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }

    char matricula[15];
    printf("\nEscreva a matrícula do veículo: ");
    scanf("%s", matricula);

    // Procurar o veículo no parque
    bool encontrado = false;
    Lugar *lugarEncontrado = NULL;

    for (int piso = 0; piso < parque.numeroPisos; piso++) {
        for (int linha = 0; linha < parque.linhas; linha++) {
            for (int coluna = 0; coluna < parque.colunas; coluna++) {
                Lugar *lugar = &parque.lugares[piso][linha * parque.colunas + coluna];
                if (lugar->ocupado && strcmp(lugar->matricula, matricula) == 0) {
                    encontrado = true;
                    lugarEncontrado = lugar;
                    break;
                }
            }
        }
    }

    if (!encontrado) {
        printf("Veículo com matrícula %s não encontrado no parque.\n", matricula);
        return;
    }

    // Calcular tempo de estacionamento
    struct tm entrada = {0};
    sscanf(lugarEncontrado->dataEntrada, "%d/%d/%d", &entrada.tm_mday, &entrada.tm_mon, &entrada.tm_year);
    sscanf(lugarEncontrado->horaEntrada, "%d:%d", &entrada.tm_hour, &entrada.tm_min);

    entrada.tm_mon -= 1;             // Ajustar mês (0-11)
    entrada.tm_year -= 1900;         // Ajustar ano (a partir de 1900)

    time_t tempoEntrada = mktime(&entrada);
    if (tempoEntrada == -1) {
        printf("Erro ao processar a data/hora de entrada.\n");
        return;
    }

    time_t tempoAtual = time(NULL);

    if (difftime(tempoAtual, tempoEntrada) < 0) {
        printf("Erro: o tempo de entrada parece ser no futuro.\n");
        return;
    }

    int dias = difftime(tempoAtual, tempoEntrada) / (3600 * 24);
    int horas = ((int)difftime(tempoAtual, tempoEntrada) % (3600 * 24)) / 3600;
    int minutos = ((int)difftime(tempoAtual, tempoEntrada) % 3600) / 60;

    // Exibir o tempo de estacionamento
    printf("\nTempo de estacionamento para o veículo %s:\n", matricula);
    printf("%d dias, %d horas, %d minutos\n", dias, horas, minutos);
}



//Para a opção 1 do "sub-menu" de Gestão de clientes
// Função para adicionar um cliente
void addcliente(char *filename) {
    char matricula[15] = ""; // Para compatibilidade com registarCliente
    registarCliente(filename, matricula); // Reutiliza a função já criada
}

//Para a opção 3 do "sub-menu" de Gestão de clientes
//Função para remover um cliente da lista de clientes
void apagarCliente(char *filename) {
    if (!clientesIniciados) {
        inicializarClientes(filename);
        clientesIniciados = true;
    }

    char nome[50];
    printf("Nome do cliente a remover: ");
    getchar(); // Limpar buffer
    fgets(nome, sizeof(nome), stdin);
    nome[strcspn(nome, "\n")] = '\0';

    bool encontrado =false;
    int indice;
    for (int i = 0; i < totalClientes; i++) {
        if (strcmp(clientes[i].nome, nome) == 0) {
            encontrado = true; 
            indice = i; //Guardar o indice do cliente
            break;
        }
    }

    if (!encontrado) {
        printf("Cliente não encontrado.\n");
        return;
    }

    // Remover cliente
    for (int i = indice; i < totalClientes - 1; i++) {
        clientes[i] = clientes[i + 1]; //eliminar a linha do cliente indice
    }
    totalClientes--;//atualizar contagclienteIndexem de clientes

    atualizarFicheiroClientes(filename);
    printf("Cliente removido com sucesso.\n");
}

//para a opção 2 do "sub-menu" de Gestão de Clientes
//Função para editar um cliente
void editarCliente(char *filename) {
    if (!clientesIniciados) {
        inicializarClientes(filename);
        clientesIniciados = true;
    }

    char nome[50];
    printf("Nome do cliente a editar: ");
    getchar(); // Limpar buffer
    fgets(nome, sizeof(nome), stdin);
    nome[strcspn(nome, "\n")] = '\0';

    Cliente *cliente;
    bool encontrado = false;
    for (int i = 0; i < totalClientes; i++) {
        if (strcmp(clientes[i].nome, nome) == 0) {
            cliente = &clientes[i];
            encontrado = true;
        }
    }

    if (!encontrado) {
        printf("Cliente não encontrado.\n");
        return;
    }

    printf("\nEditar Cliente\n");
    printf("Nome (%s): ", cliente->nome);//apresenta nome atual
    fgets(cliente->nome, sizeof(cliente->nome), stdin);//substitui nome
    cliente->nome[strcspn(cliente->nome, "\n")] = '\0';// elimina o '\n' do final da string

    printf("Morada (%s): ", cliente->morada);//morada atual
    fgets(cliente->morada, sizeof(cliente->morada), stdin);
    cliente->morada[strcspn(cliente->morada, "\n")] = '\0';// apaga o '\n' do final

    printf("Telefone (%s): ", cliente->telefone);//telefone atual
    fgets(cliente->telefone, sizeof(cliente->telefone), stdin);
    cliente->telefone[strcspn(cliente->telefone, "\n")] = '\0';

    printf("Email (%s): ", cliente->email);//emailatual
    fgets(cliente->email, sizeof(cliente->email), stdin);
    cliente->email[strcspn(cliente->email, "\n")] = '\0';

    atualizarFicheiroClientes(filename);
    printf("Cliente atualizado com sucesso.\n");
}

//Para a opcao 6
// Função para o menu de gestão de clientes
void menuGestaoClientes(char *filename) {
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }
    int opcao;

    do {
        printf("\n\n0. Voltar ao Menu Principal\n");
        printf("1. Registrar um Cliente\n");
        printf("2. Editar um Cliente\n");
        printf("3. Remover um Cliente\n");
        printf("\nOpção: ");
        scanf("%d", &opcao);

        switch (opcao) {
            case 0:
                break;

            case 1:
                addcliente("clientes.csv");
                break;

            case 2:
                editarCliente("clientes.csv");
                break;

            case 3:
                apagarCliente("clientes.csv");
                break;
            
            default:
                printf("Opção inválida. Tente novamente.\n");
                break;
        }
    } while (opcao != 0);
}


//Para a opção 7
//Função para modificar o preço
void modificacaoPreco(char *filename) {
    if (parque.numeroPisos == 0) {
        inicializarParque(filename);
    }

    int opcao;
    float novoValor;

    do {
        // Submenu para modificar os preços
        printf("\n\nModificação dos Preços: \n");
        printf("Preços atuais:\n");
        printf("0. Voltar ao menu\n");
        printf("1. Primeiros 15 minutos: %.2f\n", parque.min15);
        printf("2. Primeiros 30 minutos: %.2f\n", parque.min30);
        printf("3. Primeira hora: %.2f\n", parque.hora);
        printf("4. Horas seguintes: %.2f\n", parque.horasSeguintes);
        printf("5. Dia completo: %.2f\n", parque.dia);
        printf("\nOpção: ");
        scanf("%d", &opcao);

        switch (opcao) {
            case 1:
                printf("Novo preço para os primeiros 15 minutos: ");
                scanf("%f", &novoValor);
                if (novoValor >= 0) {
                    parque.min15 = novoValor;
                } else {
                    printf("Erro: O preço deve ser maior ou igual a 0.\n");
                }
                break;

            case 2:
                printf("Novo preço para os primeiros 30 minutos: ");
                scanf("%f", &novoValor);
                if (novoValor >= 0) {
                    parque.min30 = novoValor;
                } else {
                    printf("Erro: O preço deve ser maior ou igual a 0.\n");
                }
                break;

            case 3:
                printf("Novo preço para a primeira hora: ");
                scanf("%f", &novoValor);
                if (novoValor >= 0) {
                    parque.hora = novoValor;
                } else {
                    printf("Erro: O preço deve ser maior ou igual a 0.\n");
                }
                break;

            case 4:
                printf("Novo preço para as horas seguintes: ");
                scanf("%f", &novoValor);
                if (novoValor >= 0) {
                    parque.horasSeguintes = novoValor;
                } else {
                    printf("Erro: O preço deve ser maior ou igual a 0.\n");
                }
                break;

            case 5:
                printf("Novo preço para o dia completo: ");
                scanf("%f", &novoValor);
                if (novoValor >= 0) {
                    parque.dia = novoValor;
                } else {
                    printf("Erro: O preço deve ser maior ou igual a 0.\n");
                }
                break;

            case 0:
                printf("Alterações concluídas.\n");
                break;

            default:
                printf("Opção inválida. Tente novamente.\n");
                break;
        }
    } while (opcao != 0);

    // Atualizar o ficheiro de configuração
    FILE *file = fopen(filename, "w");
    if (!file) {
        perror("Erro ao atualizar o ficheiro de configuração");
        return;
    }

    // Reescrever o cabeçalho e os dados atualizados
    fprintf(file, "Piso,Linhas,Colunas,Lugares_2_Rodas,Lugares_Ligeiros,Lugares_Grandes,15_Minutos,30_Minutos,Horas,Seguintes,Dia\n");
    fprintf(file, "%d,%d,%d,%d,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f\n", parque.numeroPisos, parque.linhas, parque.colunas, parque.lugares2rodas, parque.lugaresLigeiros, parque.lugaresGrandes,
            parque.min15, parque.min30, parque.hora, parque.horasSeguintes, parque.dia);

    fclose(file);
    printf("Ficheiro '%s' atualizado com sucesso.\n", filename);
}


//Para a opcao 8
//Função para ecerrar o programa
void encerrarPrograma(){
    int op;
    do{
        printf("Tem a certeza que desja sair do programa? (Sim-1; Nao-0)");
        scanf("%d", &op);
        
        switch (op) {
            case 0:
                main();
                break;
            
            case 1:  
                printf("Volte sempre!\n");
                exit(0); //Exit com sucesso
                break;
            
            default: 
                printf("Opcao invalida!\n");
                break;
        }
    }while(op!=0);
}



int main(){
    int opcao;
    //Verifica se o ficheiro excell existe. Se não existir, então cria.
    if (!ficheiroExiste("config_parque.csv")){
        system("gcc estruturaParque.c -o estruturaParque && ./estruturaParque"); //Comandos para compilar e executar
    }

    do{
            printf("\nMENU\n\n");
            printf("-------------------------------------------\n");
            printf("0. Apresentar Parque");//FEITO
            printf("\n1. Entrada de veiculo"); //FEITO
            printf("\n2. Saida do veiculo");//FEITO
            printf("\n3. Ver qual carro se encontra em certas coordenadas"); //FEITO
            printf("\n4. Apresentar lista de carros");//FEITO
            printf("\n5. Apresentar tempo de estacionamento de um carro");//FEITO
            printf("\n6. Gestão de clientes");//RYANAIR
            printf("\n7. Modificar o preco");// FEITO
            printf("\n8. Sair\n\nOpcao: ");//FEITO
            scanf("%i", &opcao); 
 

            switch(opcao){

                    

                case 0:
                    apresentarParque("config_parque.csv");
                    break;    

                case 1:
                    subMenuEntrada("config_parque.csv");
                    break;                
        
                case 2:
                    saidaVeiculo("config_parque.csv");
                    break;
                    
                case 3:
                    verCarroEmCoordenadas("config_parque.csv");
                    break;

                case 4: 
                    apresentarListaCarros("config_parque.csv");
                    break;

                case 5:
                    estacionamento_Tempo("config_parque.csv");
                    break;

                case 6: 
                    //apresentar_preco(parque);
                    break;
            
                case 7:
                    modificacaoPreco("config_parque.csv");
                    break;
                    
                
                case 8:
                    encerrarPrograma();
                    break;

                
                default:
                    printf("Opcao invalida!\nEnter para voltar ao menu\n");
                    
                }
        }while(opcao!=8);

    return 0;

}
