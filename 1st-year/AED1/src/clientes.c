#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

// Função para criar um ficheiro Excel para armazenar os dados dos clientes
void criarFicheiroClientes(const char *filename) {
    FILE *file = fopen(filename, "w");
    if (!file) {
        perror("Erro ao criar o ficheiro");
        return;
    }

    // Cabeçalho do ficheiro
    fprintf(file, "Nome,Morada,Telefone,Email,Veiculo1,Veiculo2,Veiculo3,Veiculo4,Veiculo5\n");

    int numClientes;
    printf("Quantos clientes deseja cadastrar? ");
    scanf("%d", &numClientes);//emeplo: 1\n (depois vai ser preciso apagar o \n residual)

    while (numClientes <= 0) {
        printf("Número inválido. Por favor insira um valor maior que zero: ");
        scanf("%d", &numClientes);
    }

    for (int i = 0; i < numClientes; i++) {
        char nome[50], morada[100], telefone[15], email[50], veiculos[5][15];
        int numVeiculos;

        // Nome
        printf("\nCliente %d:\n", i + 1);
        printf("Nome: ");
        getchar(); // Limpa o buffer do teclado para apagar o \n residual
        fgets(nome, sizeof(nome), stdin);
        //a função strcspn(s,r) devolve o comprimento de s que consiste dos elementos que não ocorrem em r 
        nome[strcspn(nome, "\n")] = '\0'; // Remove o '\n' do final da string

        // Morada
        printf("Morada: ");
        fgets(morada, sizeof(morada), stdin);
        morada[strcspn(morada, "\n")] = '\0';// Remove o '\n' do final da string

        // Telefone
        printf("Telefone: ");
        fgets(telefone, sizeof(telefone), stdin);
        telefone[strcspn(telefone, "\n")] = '\0';// Remove o '\n' do final da string

        // Email
        printf("Email: ");
        fgets(email, sizeof(email), stdin);
        email[strcspn(email, "\n")] = '\0';// Remove o '\n' do final da string

        // Número de veículos
        printf("Quantos veículos este cliente possui? (máximo 5): ");
        scanf("%d", &numVeiculos);

        while (numVeiculos < 0 || numVeiculos > 5) {
            printf("Número inválido. Digite um valor entre 0 e 5: ");
            scanf("%d", &numVeiculos);
        }

        // Veículos
        for (int j = 0; j < numVeiculos; j++) {
            printf("  Matrícula do veículo %d: ", j + 1);
            getchar(); // Limpa o buffer do teclado
            fgets(veiculos[j], sizeof(veiculos[j]), stdin);
            veiculos[j][strcspn(veiculos[j], "\n")] = '\0'; // Remove o '\n' do final da string
        }

        // Preencher ficheiro com a informação do cliennte
        fprintf(file, "%s,%s,%s,%s,", nome, morada, telefone, email);
        //
        for (int j = 0; j < 5; j++) {
            if (j < numVeiculos) {
                fprintf(file, "%s", veiculos[j]); // Preencher matrícula do veículo
            } else {
                fprintf(file, " "); // Deixar vazio se não houver veículo
            }
            if (j < 4) {
                fprintf(file, ","); // Adicionar separador se não for o último veículo
            }
        }
        fprintf(file, "\n"); // Finalizar linha do cliente
    }

    fclose(file);
    printf("Ficheiro '%s' criado com sucesso.\n", filename);
}

// Função principal para testar a criação do ficheiro de clientes
int main() {
    criarFicheiroClientes("clientes.csv");
    system("./projeto"); //Comando para executar o menu

    return 0;
}
