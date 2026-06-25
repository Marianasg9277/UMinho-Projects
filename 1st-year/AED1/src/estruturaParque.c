#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
    
void criarFicheiro(char *filename) {
    FILE *file = fopen(filename, "w");
    if (file == NULL) {
        perror("Erro ao criar o ficheiro"); //output: Erro a criar o ficheiro: PERMISSION DENIED
        return;
    } else{

        // Cabeçalho do ficheiro
        fprintf(file, "Piso,Linhas,Colunas,Lugares_2_Rodas,Lugares_Ligeiros,Lugares_Grandes,Preço\n");

        int pisos, linhas, colunas, lugares2rodas, lugaresLigeiros, lugaresGrandes;
        float preco15min, preco30min, precoHora, precoHSeguintes, precodia;

        //PISOS:
        printf("Quantos pisos tem o parque? (1-5): ");
        scanf("%d", &pisos);
        
        //Numero maximo de pisos é 5
        while ( pisos > 5 || pisos <= 0) {
            printf("Número inválido.\n Numero minimo 1 - Numero maximo 5.\n Por favor escreva o numero de pisos: ");
            scanf("%d", &pisos);
        }
        
        //LINHAS:
        printf("Número de linhas por piso (1-20): ");
        scanf("%d", &linhas);

        //Numero maximo de linhas é 20
        while (linhas > 20 || linhas <=0 ) {
            printf("Numero máximo de linhas é 20 \n Escreva novo numero de linhas: ");
            scanf("%d", &linhas);
        }
        
        //COLUNAS:
        printf("Número de colunas por piso(1-10): ");
        scanf("%d", &colunas);
        //Numero maximo de colunas é 10
        while (colunas >10 || colunas <= 0){
            printf("Numero máximo de colunas 10 \n Escreva novo numero de colunas: ");
            scanf("%d", &colunas);
        }

        //PREÇO:
        //pelos primeiros 15 minutos
        printf("Preço por 15min. (€): ");
        scanf("%f", &preco15min);
        
        //Preço não pode ser negativo ou gratuito
        while (preco15min <= 0){
            printf("Preço inválido. \n Digite o preço por hora: ");
            scanf("%f", &preco15min);
        }
        //pelos primeiros 30 minutos
        printf("Preço por 30min (€): ");
        scanf("%f", &preco30min);
        
        //Preço não pode ser negativo ou gratuito
        while (preco30min <= 0){
            printf("Preço inválido. \n Digite o preço por hora: ");
            scanf("%f", &preco30min);
        }
        //pela primeira hora
        printf("Preço por hora (€): ");
        scanf("%f", &precoHora);
        
        //Preço não pode ser negativo ou gratuito
        while (precoHora <= 0){
            printf("Preço inválido. \n Digite o preço por hora: ");
            scanf("%f", &precoHora);
        }
        //pelos horas seguintes
        printf("Preço pelas horas seguintes (€): ");
        scanf("%f", &precoHSeguintes);
        
        //Preço não pode ser negativo ou gratuito
        while (precoHSeguintes <= 0){
            printf("Preço inválido. \n Digite o preço por hora: ");
            scanf("%f", &precoHSeguintes);
        }
        //por dia completo
        printf("Preço por dia completo (€): ");
        scanf("%f", &precodia);
        
        //Preço não pode ser negativo ou gratuito
        while (precodia <= 0){
            printf("Preço inválido. \n Digite o preço por hora: ");
            scanf("%f", &precodia);
        }

        //Todos os lugares têm de ter associado 1 e apenas 1 tipo
        //Não pode existir um lugar onde nenhum dos tipos seja válido
        while ( linhas * colunas != lugares2rodas + lugaresGrandes + lugaresLigeiros){ 
            //LUGARES PARA CADA TIPO:
            printf("\nConfiguração para os lugares de estacionamento\n");
            int lugaresTotais = linhas * colunas;

            //Lugares disponiveis para ligeiros:
            printf("Lugares para veículos ligeiros: ");
            scanf("%d", &lugaresLigeiros);

            while (lugaresLigeiros < 0 || lugaresLigeiros > lugaresTotais) {
                printf("Número inválido.\n Lugares para veículos ligeiros (máximo %d): ", lugaresTotais);
                scanf("%d", &lugaresLigeiros);
            }

            lugaresTotais -= lugaresLigeiros; //total restante para os lugares do tipo 2 rodas e ligeiros Grandes

            if (lugaresTotais != 0) {
                //Lugares disponiveis para 2 rodas:
                printf("Lugares para veículos de 2 rodas: ");
                scanf("%d", &lugares2rodas);

                while (lugares2rodas < 0 || lugares2rodas > lugaresTotais) { 
                    printf("Número inválido.\nLugares para veículos de 2 rodas(máximo %d): ", lugaresTotais);
                    scanf("%d", &lugares2rodas);
                }        
                lugaresTotais -= lugares2rodas;

            }else{
                lugares2rodas = 0;
                lugaresGrandes = 0;
                printf("Não existem lugares para Veiculos de 2 rodas\n");
            }

            if (lugaresTotais !=0){
                //Lugares disponiveis para ligeiros Grandes:
                printf("Lugares para veículos ligeiros grandes: ");
                scanf("%d", &lugaresGrandes);

                while (lugaresGrandes < 0 || lugaresGrandes > lugaresTotais) {
                    printf("Número inválido.\nLugares para veículos ligeiros grandes (máximo %d): ", lugaresTotais);
                    scanf("%d", &lugaresGrandes);
                }
            } else{
                lugaresGrandes = 0;
                printf("Não existem lugares para veiculos ligeiros grandes\n");

            }
        }

        printf("\n\nConclusão:\nLugares disponiveis para ligeiros: %d \nLugares disponiveis para 2 rodas: %d \nLugares disponiveis para ligeiros grandes: %d\n"
              , lugaresLigeiros, lugares2rodas, lugaresGrandes);
        
        //Escreve no ficheiro
        fprintf(file, "%d,%d,%d,%d,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f\n", pisos, linhas, colunas, lugares2rodas, lugaresLigeiros, lugaresGrandes, preco15min, preco30min, precoHora, precoHSeguintes, precodia);
    }

    //Fechar ficheiro
    fclose(file);
    printf("Ficheiro '%s' criado com sucesso.\n", filename);
}



int main() {
    criarFicheiro("config_parque.csv");
    system("./projeto"); //Comando para executar o menu

    return 0;
}