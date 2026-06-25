#include <locale.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define tamLinha 500

//Estrutura da encomenda
typedef struct Encomenda{
    int ID;
    char nome[100];
    char descricao[100];
    char dataDeCriacao[9]; //AAAAMMDD
    struct Encomenda *next;
}Encomenda;

//estrutura das encomendas pendentes (lista ligada)
typedef struct {
    Encomenda *inicio;
}EncomendaPendente;

//estrutura das encomendas em expedicao (filas)
typedef struct {
    Encomenda *inicio, *fim;
}EncomendaExpedida;

//estrutura do historico (pilhas)
typedef struct {
    Encomenda *top;
}Historico;

int ultimoID = 1; //Declaracao do ultimo ID
int erros = 0;

// Função para validar a data no formato AAAAMMDD
int validaData(const char *data) {
    int i;

    // Verifica se a data tem exatamente 8 caracteres
    if (strlen(data) != 8) {
        return 0; // Data inválida
    }

    // Verifica se cada caractere é um número (entre '0' e '9')
    for (i = 0; i < 8; i++) {
        if (data[i] < '0' || data[i] > '9') {
            return 0; // Data inválida
        }
    }

    // Extrai o mês (data[4] e data[5]) e converte para inteiro
    char mesStr[3] = { data[4], data[5], '\0' };
    int mes = atoi(mesStr); // atoi - converter para inteiro

    // Extrai o dia (data[6] e data[7]) e converte para inteiro
    char diaStr[3] = { data[6], data[7], '\0' };
    int dia = atoi(diaStr); // atoi - converter para inteiro

    // Verifica se o mês está entre 1 e 12
    if (mes < 1 || mes > 12) {
        return 0; // Mês inválido
    }

    // Verifica se o dia está entre 1 e 31
    if (dia < 1 || dia > 31) {
        return 0; // Dia inválido
    }

    // Verifica se os meses têm 30 dias
    if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && dia > 30) {
        return 0; // inválido
    }

    // Verifica se fevereiro tem 28 ou 29 dias
    if (mes == 2) {
        // Verifica se é um ano bissexto
        int ano = atoi(data); // Extrai o ano
        if ((ano % 4 == 0 && ano % 100 != 0) || (ano % 400 == 0)) {
            // Ano bissexto
            if (dia > 29) {
                return 0; // inválido
            }
        } else {
            // Ano não bissexto
            if (dia > 28) {
                return 0; // inválido
            }
        }
    }

    return 1; // Data válida
}

// Função para verificar se um ‘ID’ já existe nas listas
int idExisteGeral(const void *estrutura, int id, int tipo) {
    switch (tipo) {
        case 1: { // Tipo 1: Lista de Encomendas
            const Encomenda *lista = (const Encomenda *)estrutura;
            while (lista != NULL) {
                if (lista->ID == id) {
                    return 1; // ‘ID’ existe
                }
                lista = lista->next;
            }
            break;
        }
        case 2: { // Tipo 2: Lista de Encomendas Pendentes
            const EncomendaPendente *pendentes = (const EncomendaPendente *)estrutura;
            if (pendentes != NULL) {
                return idExisteGeral(pendentes->inicio, id, 1); // Reutiliza a verificação de lista
            }
            break;
        }
        case 3: { // Tipo 3: Fila de Encomendas Expedidas
            const EncomendaExpedida *expedidas = (const EncomendaExpedida *)estrutura;
            if (expedidas != NULL) {
                return idExisteGeral(expedidas->inicio, id, 1); // Reutiliza a verificação de lista
            }
            break;
        }
        case 4: { // Tipo 4: Pilha de Histórico
            const Historico *historico = (const Historico *)estrutura;
            if (historico != NULL) {
                return idExisteGeral(historico->top, id, 1); // Reutiliza a verificação de lista
            }
            break;
        }
        default:
            break;
    }
    return 0; // ‘ID’ não existe
}

int separarElementos(char *linha, int *id, char *nome, char *descricao, char *dataCriacao) {
    char *token;

    // Copiar a linha para não destruir a original
    char copiaLinha[tamLinha];
    strcpy(copiaLinha, linha);

    token = strtok(copiaLinha, ";");
    if (!token || sscanf(token, "%d", id) != 1) return 0;

    token = strtok(NULL, ";");
    if (!token || strlen(token) == 0) return 0;
    strcpy(nome, token);

    token = strtok(NULL, ";");
    if (!token || strlen(token) == 0) return 0;
    strcpy(descricao, token);

    token = strtok(NULL, ";\n");
    if (!token || strlen(token) != 8) return 0;
    strcpy(dataCriacao, token);

    return 1; // Tudo certo
}

//funcao de transferir o que tem no ficheiro para a lista ligada (Encomendas pendentes)
void transferirPendentes(Encomenda **apLista){
    char linha[tamLinha], nome[100], descricao[100], dataCriacao[20];
    int id;

    FILE *fPendentes = fopen("EncomendasPendentes.txt", "r");
    if (!fPendentes) {
        printf("Erro ao abrir EncomendasPendentes.txt");
        return;
    }

    // Ler cada linha de encomenda
    while (fgets(linha, sizeof(linha), fPendentes)) {
        separarElementos(linha, &id, nome, descricao, dataCriacao);

        if (!separarElementos(linha, &id, nome, descricao, dataCriacao)) {
            printf("Linha invalida (falta ID, nome, descricao ou data) no ficheiro EncomendasPendentes.txt.\n");
            erros++;
            continue;
        }

        if (!validaData(dataCriacao)) {
            printf("Data invalida na encomenda ID %d no ficheiro EncomendasPendentes.txt.\n", id);
            erros++;
        }

        // Verifica se o ‘ID’ já existe e incrementa se necessário
        while (idExisteGeral(apLista, id, 2)) {
            id++;
        }

        Encomenda *nova = (Encomenda *)malloc(sizeof(Encomenda));
        if (!nova) {
            printf("Erro ao alocar memoria.\n");
            return;
        }

        nova->ID = id;
        strcpy(nova->nome, nome);
        strcpy(nova->descricao, descricao);
        strcpy(nova->dataDeCriacao, dataCriacao);
        nova->next = *apLista;
        *apLista = nova;

        if (id >= ultimoID) {
            ultimoID = id + 1;
        }
    }

    fclose(fPendentes);
}

//funcao de transferir o que tem no ficheiro para a fila (Encomendas expedidas)
void transferirExpedidas(EncomendaExpedida *queue) {
    char linha[tamLinha], nome[100], descricao[100], dataCriacao[20];
    int id;

    FILE *fExpedidas = fopen("EncomendasExpedidas.txt", "r");
    if (!fExpedidas) {
        printf("Erro ao abrir EncomendasExpedidas.txt");
        return;
    }

    // Ler cada linha de encomenda
    while (fgets(linha, sizeof(linha), fExpedidas)) {
        separarElementos(linha, &id, nome, descricao, dataCriacao);

        if (!separarElementos(linha, &id, nome, descricao, dataCriacao)) {
            printf("Linha inválida (faltam campos obrigatórios) em EncomendasPendentes.txt: %s", linha);
            erros++;
            continue;
        }

        //verifica a data
        if (!validaData(dataCriacao)) {
            printf("Data invalida na encomenda ID %d no ficheiro EncomendasExpedidas.txt.\n", id);
            erros++;
        }

        // Verifica se o ‘ID’ já existe e incrementa se necessário
        while (idExisteGeral(queue, id, 3)) {
            id++;
        }

        Encomenda *nova = (Encomenda *)malloc(sizeof(Encomenda));
        if (nova == NULL) {
            printf("Erro ao alocar memoria para encomenda.\n");
            return;
        }

        nova->ID = id;
        strcpy(nova->nome, nome);
        strcpy(nova->descricao, descricao);
        strcpy(nova->dataDeCriacao, dataCriacao);
        nova->next = NULL;

        // Inserir na fila (fim)
        if (queue->inicio == NULL) {
            queue->inicio = queue->fim = nova;
        } else {
            queue->fim->next = nova;
            queue->fim = nova;
        }

        // Atualizar ID global se necessario
        if (id >= ultimoID) {
            ultimoID = id + 1;
        }

    }
    fclose(fExpedidas);
}

//funcao de transferir o que tem no ficheiro para a pilha (historico)
void transferirHistorico(Historico *stack) {
    char linha[tamLinha], nome[100], descricao[100], dataCriacao[20];
    int id;

    FILE *fHistorico = fopen("Historico.txt", "r");
    if (!fHistorico) {
        printf("Erro ao abrir Historico.txt");
        return;
    }

    while (fgets(linha, sizeof(linha), fHistorico)) {
        separarElementos(linha, &id, nome, descricao, dataCriacao);

        if (!separarElementos(linha, &id, nome, descricao, dataCriacao)) {
            printf("Linha invalida (faltam campos obrigatorios) em Historico.txt: %s", linha);
            erros++;
            continue;
        }

        //verifica a data
        if (!validaData(dataCriacao)) {
            printf("Data invalida na encomenda ID %d no ficheiro Historico.txt.\n", id);
            erros++;
        }

        // Verifica se o ‘ID’ já existe e incrementa se necessário
        while (idExisteGeral(stack, id, 4)) {
            id++;
        }

        Encomenda *nova = (Encomenda *)malloc(sizeof(Encomenda));
        if (nova == NULL) {
            printf("Erro ao alocar memoria para encomenda.\n");
            return;
        }

        nova->ID = id;
        strcpy(nova->nome, nome);
        strcpy(nova->descricao, descricao);
        strcpy(nova->dataDeCriacao, dataCriacao);

        // Inserir no topo da pilha
        nova->next = stack->top;
        stack->top = nova;

        if (id >= ultimoID) {
            ultimoID = id + 1;
        }
    }
    fclose(fHistorico);
}

// Funcao para capturar informacoes da encomenda
void capturaEntrada(char *prompt, char *buffer, int tamanho) {
    printf("%s", prompt);//pergunta para o cliente
    fgets(buffer, tamanho, stdin);//guarda a resposta
}

//funcao de criar o no
Encomenda *criaEncomenda(char nome[100], char descricao[100], char dataCriacao[9]){
    Encomenda *novaEncomenda = (Encomenda*)malloc(sizeof(Encomenda));
    if(novaEncomenda != NULL) {
        novaEncomenda->ID = ultimoID++; //Dá um ‘ID’ unico a nova encomenda
        strcpy(novaEncomenda->nome, nome);
        strcpy(novaEncomenda->descricao, descricao);
        strcpy(novaEncomenda->dataDeCriacao, dataCriacao);
        novaEncomenda->next=NULL; //Ainda nao existe outra encomenda
    }
    return novaEncomenda;
}

//Inserir encomendas pendentes
void insereEncomendaPendente(Encomenda **apLista) {
    char nome[100], descricao[100], dataCriacao[9];
    fflush(stdin);

    capturaEntrada("Introduza o nome do cliente: ", nome, sizeof(nome));
    capturaEntrada("Introduza a descricao da encomenda: ", descricao, sizeof(descricao));

    do {
        capturaEntrada("Introduza a data de criacao (AAAAMMDD): ", dataCriacao, sizeof(dataCriacao));
        if (!validaData(dataCriacao)) {
            printf("Formato de data invalida. Use AAAAMMDD.\n");
        }
    } while (!validaData(dataCriacao));

    nome[strcspn(nome, "\n")] = '\0';
    descricao[strcspn(descricao, "\n")] = '\0';

    Encomenda *novaEncomenda = criaEncomenda(nome, descricao, dataCriacao);

    if (novaEncomenda == NULL) {
        printf("Erro ao alocar memoria para a nova encomenda.\n");
        return;
    }

    novaEncomenda->next = *apLista; // Insere no inicio da lista
    *apLista = novaEncomenda;

    printf("Encomenda com ID %d inserida com sucesso.\n", novaEncomenda->ID);

    //colocar as informacoes da encomenda pendente no ficheiro (EncomendasPendentes.txt)
    FILE *fPendentes = fopen("EncomendasPendentes.txt", "w");
    if (!fPendentes) {
        printf("Erro ao abrir o ficheiro EncomendasPendentes.txt .\n");
        return;
    }

    Encomenda *atual = *apLista; //passa e grava no ficheiro todas as encomendas
    while (atual != NULL) {
        fprintf(fPendentes, "%d;%s;%s;%s\n", atual->ID, atual->nome, atual->descricao, atual->dataDeCriacao);//escrever as informacoes da encomenda no ficheiro
        atual = atual->next;
    }

    fclose(fPendentes);
    printf("Encomenda pendente guardada com sucesso no ficheiro EncomendasPendentes.txt .\n");
}

//listar as encomendas pendentes
void listarEncomendasPendentes(Encomenda *head) {
    if (head == NULL) {
        printf("Nao existem encomendas pendentes.\n");
        return;
    }

    printf("\n                          --- Encomendas Pendentes ---\n");

    Encomenda *atual = head;
    while (atual != NULL) {
        printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", atual->ID, atual->nome, atual->descricao, atual->dataDeCriacao);
        atual = atual->next;
    }
    printf("\n");
}

//remover das encomendas pendentes e adicionar encomenda a expidicao
char enqueue(Encomenda** apLista, EncomendaExpedida *queue) {
    int id;
    if (*apLista == NULL) {
        printf("Nao ha encomendas pendentes.\n");
        return 0;
    }

    printf("Introduza o ID da encomenda a expedir: ");
    scanf("%d", &id);

    Encomenda *atual = *apLista;
    Encomenda *anterior = NULL;

    // Procurar a encomenda na lista ligada
    while (atual != NULL && atual->ID != id) {
        anterior = atual;
        atual = atual->next;
    }

    if (atual == NULL) {
        printf("Encomenda com ID %d nao encontrada na lista de encomendas pendentes.\n", id);
        return 0;
    }

    // Remover da lista pendente
    if (anterior == NULL) { // é o primeiro elemento
        *apLista = atual->next;
    } else {
        anterior->next = atual->next;
    }

    atual->next = NULL; // preparar para inserir na fila

    // Inserir na fila (expedição)
    if (queue->inicio == NULL) {
        queue->inicio = queue->fim = atual;
    } else {
        queue->fim->next = atual;
        queue->fim = atual;
    }

    printf("Encomenda com ID %d expedida com sucesso.\n", id);

    //Atualizar o ficheiro de pendentes (reescrever toda a lista atualizada)
    FILE *fPendentes = fopen("EncomendasPendentes.txt", "w");
    if (!fPendentes) {
        printf("Erro ao abrir EncomendasPendentes.txt");
        return 0;
    }

    Encomenda *temp = *apLista;
    while (temp != NULL) {
        fprintf(fPendentes, "%d;%s;%s;%s\n", temp->ID, temp->nome, temp->descricao, temp->dataDeCriacao);
        temp = temp->next;
    }
    fclose(fPendentes);

    // Atualizar o ficheiro de expedidas (adicionar a nova encomenda no fim)
    FILE *fExpedidas = fopen("EncomendasExpedidas.txt", "w");
    if (!fExpedidas) {
        printf("Erro ao abrir EncomendasExpedidas.txt");
        return 0;
    }

    if (queue->inicio != NULL) {
        Encomenda *tempExp = queue->inicio;
        while (tempExp != NULL) {
            fprintf(fExpedidas, "%d;%s;%s;%s\n", tempExp->ID, tempExp->nome, tempExp->descricao, tempExp->dataDeCriacao);
            tempExp = tempExp->next;
        }
    }
    fclose(fExpedidas);

    return 1;
}

//listar as encomendas na fila de expedicao
void front(const EncomendaExpedida *queue) {
    if (queue->inicio == NULL){
        printf("Fila vazia.\n");
    }else{
        printf("\n                          --- Encomendas Expedidas ---\n");

        Encomenda *current = queue->inicio;
        while (current != NULL) {
            printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", current->ID, current->nome, current->descricao, current->dataDeCriacao);
            current = current->next;
        }
        printf("\n");
    }
}

//remover encomenda a expidicao (enviar para o cliente) e acrescentar encomenda ao historico
void push(EncomendaExpedida *queue, Historico *stack) {
    int r, contador=0;

    if (queue->inicio == NULL) {
        printf("Nao ha encomendas expedidas.\n");
        return;
    }

    const Encomenda *fila = queue->inicio;
    while (fila != NULL)  {
        fila=fila->next;
        contador++;
    }

    printf("Quantas encomendas pretende enviar? ");
    do {
        scanf("%d", &r);
        if (r < 0 || r > contador)
            printf("Apenas %d encomendas em expedicao disponiveis para envio, introduza novamente ", contador);
    }while (r < 0 || r > contador);

    while (r > 0 && queue->inicio != NULL) {
        Encomenda *enviada = queue->inicio;  // Encomenda a ser enviada
        queue->inicio = enviada->next;       // Avançar a fila

        if (queue->inicio == NULL) {
            queue->fim = NULL; // Se a fila ficar vazia
        }

        // Inserir no topo da pilha (histórico)
        enviada->next = stack->top;
        stack->top = enviada;

        printf("Encomenda com ID %d enviada para o cliente e movida para o historico.\n", enviada->ID);

        r--; // Reduzir o numero de encomendas restantes a enviar
    }

    if (r > 0) {
        printf("Apenas existiam %d encomendas para enviar.\n", r);
    }

    // Atualizar o ficheiro de encomendas expedidas
    FILE *fExpedidas = fopen("EncomendasExpedidas.txt", "w");
    if (!fExpedidas) {
        printf("Erro ao abrir EncomendasExpedidas.txt");
        return;
    }

    if (queue->inicio != NULL) {
        Encomenda *temp = queue->inicio;
        while (temp != NULL) {
            fprintf(fExpedidas, "%d;%s;%s;%s\n", temp->ID, temp->nome, temp->descricao, temp->dataDeCriacao);
            temp = temp->next;
        }
    }
    fclose(fExpedidas);

    // Atualizar o ficheiro do histórico
    FILE *fHistorico = fopen("Historico.txt", "w");
    if (!fHistorico) {
        printf("Erro ao abrir Historico.txt");
        return;
    }

    Encomenda *tempHist = stack->top;
    while (tempHist != NULL) {
        fprintf(fHistorico, "%d;%s;%s;%s\n", tempHist->ID, tempHist->nome, tempHist->descricao, tempHist->dataDeCriacao);
        tempHist = tempHist->next;
    }
    fclose(fHistorico);

}

void idHistorico(const Historico* stack) {
    int idInicio = 0, idFim = 9999, encontrou = 0;
    char buffer[10];

    fflush(stdin);

    do {
        printf("Introduza o ID de inicio: ");
        fgets(buffer, sizeof(buffer), stdin);
        if (strlen(buffer) > 1) { // Se o utilizador não pressionou apenas Enter
            idInicio = atoi(buffer);
        }

        printf("Introduza o ID de fim: ");
        fgets(buffer, sizeof(buffer), stdin);
        if (strlen(buffer) > 1) {
            idFim = atoi(buffer);
        }

        if (idInicio > idFim || idInicio < 0 || idFim < 0) {
            printf("IDs invalidos. Tente novamente.\n");
        }
    } while (idInicio > idFim || idInicio < 0 || idFim < 0);

    // Percorrer a lista e imprimir encomendas dentro do intervalo
    Encomenda* atual = stack->top;

    printf("\n                          --- Encomendas no intervalo de IDs %d - %d ---\n", idInicio, idFim);

    while (atual != NULL) {
        if (atual->ID >= idInicio && atual->ID <= idFim) {
            printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", atual->ID, atual->nome, atual->descricao, atual->dataDeCriacao);
            encontrou = 1;
        }
        atual = atual->next;
    }

    if (!encontrou) {
        printf("Nenhuma encomenda encontrada no intervalo %d - %d.\n", idInicio, idFim);
    }
}

void datasHistorico(const Historico* stack) {
    char dataInicio[9] = "20000101", dataFim[9] = "99991231", buffer[10];;
    int encontrou = 0;

    fflush(stdin);

    // Obter e validar data de início
    do {
        printf("Introduza a data de inicio (AAAAMMDD): ");
        fgets(buffer, sizeof(buffer), stdin);
        if (strlen(buffer) > 1) {
            strcpy(dataInicio, buffer);
            dataInicio[strcspn(dataInicio, "\n")] = '\0'; // Remove newline
        }

        if (!validaData(dataInicio)) {
            printf("Data invalida. Tente novamente.\n");
        }
    } while (!validaData(dataInicio));

    // Obter e validar data de fim
    do {
        printf("Introduza a data de fim (AAAAMMDD): ");
        fgets(buffer, sizeof(buffer), stdin);
        if (strlen(buffer) > 1) {
            strcpy(dataFim, buffer);
            dataFim[strcspn(dataFim, "\n")] = '\0'; // Remove newline
        }

        if (!validaData(dataFim)) {
            printf("Data invalida. Tente novamente.\n");
        } else if (strcmp(dataInicio, dataFim) > 0) {
            printf("A data de fim deve ser posterior ou igual a data de inicio.\n");
        }
    } while (!validaData(dataFim) || strcmp(dataInicio, dataFim) > 0);

    // Percorrer histórico e mostrar encomendas no intervalo
    Encomenda* atual = stack->top;
    printf("\n                          --- Encomendas entre as datas %s e %s ---\n", dataInicio, dataFim);

    while (atual != NULL) {
        if (strcmp(atual->dataDeCriacao, dataInicio) >= 0 && strcmp(atual->dataDeCriacao, dataFim) <= 0) {
            printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", atual->ID, atual->nome, atual->descricao, atual->dataDeCriacao);
            encontrou = 1;
        }
        atual = atual->next;
    }

    if (!encontrou) {
        printf("Nenhuma encomenda encontrada no intervalo de datas.\n");
    }
}

void nomeHistorico(const Historico* stack) {
    int opcao, encontrarNome=0, encontrou = 0;
    int idInicio = 0, idFim = 9999;
    char dataInicio[9] = "20000101", dataFim[9] = "99991231", buffer[10];
    char nome[100];;

    Encomenda* atual = stack->top;

    do {
        getchar(); // Limpar ‘buffer’

        printf("\n--- Pesquisa por Nome ---\n");
        printf("1 - Intervalo de IDs\n");
        printf("2 - Intervalo de Datas\n");
        printf("3 - Pesquisa simplesmente pelo nome\n");
        printf("0 - Voltar atras\n");
        printf("Escolha uma opcao: ");
        scanf("%d", &opcao);

        fflush(stdin);

        if (opcao!=0) {
            // Validação para garantir que o nome existe na pilha
            do {
                printf("Introduza o nome do cliente (ou parte do nome): ");
                fgets(nome, sizeof(nome), stdin);
                nome[strcspn(nome, "\n")] = '\0';  // remover newline

                encontrarNome=0;
                Encomenda* nova = stack->top;

                while (nova != NULL) {
                    if (strstr(nova->nome, nome) != NULL) {
                        encontrarNome = 1;
                        break;
                    }
                    nova = nova->next;
                }

                if (!encontrarNome) {
                    printf("Nenhuma encomenda encontrada com esse nome. Tente novamente.\n");
                }
            } while (!encontrarNome);

            switch (opcao) {
                case 1: 

                fflush(stdin);

                do {
                    char buffer[10];
                    printf("Introduza o ID de inicio: ");
                    fgets(buffer, sizeof(buffer), stdin);
                    if (strlen(buffer) > 1) { // Se o utilizador não pressionou apenas Enter
                        idInicio = atoi(buffer);
                    }

                    printf("Introduza o ID de fim: ");
                    fgets(buffer, sizeof(buffer), stdin);
                    if (strlen(buffer) > 1) {
                        idFim = atoi(buffer);
                    }

                    if (idInicio > idFim || idInicio < 0 || idFim < 0) {
                        printf("IDs invalidos. Tente novamente.\n");
                    }
                } while (idInicio > idFim || idInicio < 0 || idFim < 0);

                printf("\n                          --- Encomendas de %s entre os IDs %d e %d ---\n", nome, idInicio, idFim);
                while (atual != NULL) {
                    if (strstr(atual->nome, nome) != NULL && atual->ID >= idInicio && atual->ID <= idFim) {
                        printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", atual->ID, atual->nome, atual->descricao, atual->dataDeCriacao);
                        encontrou = 1;
                        fflush(stdin);
                    }
                    atual = atual->next;
                }
                if (!encontrou) {
                    printf("Nenhuma encomenda encontrada com esse nome nesse intervalo de IDs.\n");
                }
                break;
                case 2: 

                fflush(stdin);

                // Obter e validar data de início
                do {
                    printf("Introduza a data de inicio (AAAAMMDD): ");
                    fgets(buffer, sizeof(buffer), stdin);
                    if (strlen(buffer) > 1) {
                        strcpy(dataInicio, buffer);
                        dataInicio[strcspn(dataInicio, "\n")] = '\0'; // Remove newline
                    }

                    if (!validaData(dataInicio)) {
                        printf("Data invalida. Tente novamente.\n");
                    }
                } while (!validaData(dataInicio));

                // Obter e validar data de fim
                do {
                    printf("Introduza a data de fim (AAAAMMDD): ");
                    fgets(buffer, sizeof(buffer), stdin);
                    if (strlen(buffer) > 1) {
                        strcpy(dataFim, buffer);
                        dataFim[strcspn(dataFim, "\n")] = '\0'; // Remove newline
                    }

                    if (!validaData(dataFim)) {
                        printf("Data invalida. Tente novamente.\n");
                    } else if (strcmp(dataInicio, dataFim) > 0) {
                        printf("A data de fim deve ser posterior ou igual a data de inicio.\n");
                    }
                } while (!validaData(dataFim) || strcmp(dataInicio, dataFim) > 0);

                Encomenda* atual2 = stack->top;
                printf("\n                          --- Encomendas de %s entre %s e %s ---\n", nome, dataInicio, dataFim);
                while (atual2 != NULL) {
                    if (strstr(atual2->nome, nome) != NULL && strcmp(atual2->dataDeCriacao, dataInicio) >= 0 && strcmp(atual2->dataDeCriacao, dataFim) <= 0) {
                        printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", atual2->ID, atual2->nome, atual2->descricao, atual2->dataDeCriacao);
                        encontrou = 1;
                    }
                    atual2 = atual2->next;
                }
                if (!encontrou) {
                    printf("Nenhuma encomenda encontrada com esse nome nesse intervalo de datas.\n");
                }
                break;
                case 3: encontrou = 0;
                Encomenda* atual3 = stack->top;
                printf("\n                          --- Encomendas com o nome \"%s\" ---\n", nome);
                while (atual3 != NULL) {
                    if (strstr(atual3->nome, nome) != NULL) {
                        printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", atual3->ID, atual3->nome, atual3->descricao, atual3->dataDeCriacao);
                        encontrou = 1;
                    }
                    atual3 = atual3->next;
                }
                if (!encontrou) {
                    printf("Nenhuma encomenda encontrada para o nome \"%s\".\n", nome);
                }
                break;
                default: printf("Opcao invalida. Tente novamente.\n");
            }
        }else {
            return;
        }
    } while (opcao != 0);

}

//listar encomendas expedidas
void listarHistorico(const Historico* stack) {
    int r;


    if (stack->top == NULL) {
        printf("Historico vazio.\n");
        return;
    }

    do {
        printf("\n--- Escolha uma opcao para listar o historico ---\n");
        printf("1-Intervalo de IDS\n");
        printf("2-Intervalo de datas\n");
        printf("3-Pesquisar por nome\n");
        printf("4-Mostrar tudo\n");
        printf("0-Voltar atras\n");
        scanf("%d", &r);

        switch (r) {
            case 1: idHistorico(stack);
                    break;

            case 2: datasHistorico(stack);
                    break;

            case 3: nomeHistorico(stack);
                    break;

            case 4: printf("\n                          --- Historico de Encomendas ---\n");
                    Encomenda *atual = stack->top;
                    while (atual != NULL) {
                        printf("ID-%-10d Nome-%-20s Descricao-%-30s Data-%-10s\n", atual->ID, atual->nome, atual->descricao, atual->dataDeCriacao);
                        atual = atual->next;
                    }
                    printf("\n");
                    break;

            case 0: printf("obrigada\n");
                    break;

            default:printf("Opcao invalida. Introduza de 1 a 4");
                    break;
        }
    }while (r!=0);
}

int main() {
    int res;
    setlocale(LC_ALL, "pt-PT.UTF-8");

    //listas ligadas
    Encomenda *head = NULL;

    //filas
    EncomendaExpedida EE;
    //inicializar a fila
    EE.inicio = NULL;
    EE.fim = NULL;

    //pilhas
    Historico stack;
    stack.top = NULL;

    //transeferir os ficheiros para as respetivas areas
    transferirPendentes(&head);
    transferirExpedidas(&EE);
    transferirHistorico(&stack);

    if (erros > 0) {
        printf("\nForam encontrados %d erros ao carregar os ficheiros. Corrija-os e tente novamente.\n", erros);
        exit(1);
    }

    do {
        printf("--- Escolha uma opcao ---\n");
        printf("1-Introduzir uma encomenda\n");
        printf("2-Listar encomendas pendentes\n");
        printf("3-Expeditar uma encomenda\n");
        printf("4-Listar encomendas em expedicao\n");
        printf("5-Enviar encomenda\n");
        printf("6-Listar as encomendas enviadas\n");
        printf("0-Sair\n");
        scanf("%d", &res);

        switch (res) {
            case 1: insereEncomendaPendente(&head);
                    break;
            case 2: listarEncomendasPendentes(head);
                    break;
            case 3: enqueue(&head, &EE);
                    break;
            case 4: front(&EE);
                    break;
            case 5: push(&EE, &stack);
                    break;
            case 6: listarHistorico(&stack);
                    break;
            case 0: printf("Obrigada");
                    break;
            default: printf("Erro, introduza um numero entre 0 e 6.\n");
        }
    }while (res!=0);
    return 0;
}
