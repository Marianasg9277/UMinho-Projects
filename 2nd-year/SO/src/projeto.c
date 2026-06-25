#include <stdlib.h> // para exit()
#include <unistd.h> // para chamadas ao sistema: fork(), execvp(), etc...
#include <sys/wait.h> // para chamadas wait() e macros relacionados
#include <string.h> // para manipulação de strings
#include <fcntl.h> // necessário para open() e as flags O_WRONLY, O_CREAT, etc.
#include <sys/types.h> // definições de tipos de dados do sistema (como pid_t)
#include <sys/stat.h> // definições para atributos de ficheiros e permissões (necessário para mkfifo)

// named pipe
#define FIFO_NAME "pipe_so"
#define BUFFER_SIZE 1000 // Define o tamanho máximo do buffer para evitar overflow de memória

// Esta parte vai ser chamada sempre que o servidor receber um comando
void executarComandos(char *args[]) { 
    int status;
    pid_t pid = fork(); // Cria o processo filho (usamos pid_t em vez do int para garantir que funciona em qualquer Linux sem erros)

    if (pid < 0) {
        char msg[] = "[Erro] Falha no fork\n";
        write(2, msg, strlen(msg)); // Descritor 2 = STDERR (File Descriptor de Erros)
        return;
    }

    else if (pid == 0) {
        // Processo filho
        // printf("Eu sou o filho! O meu PID é: %d\n",getpid());
        execvp(args[0], args); // usamos execvp uma vez que não sabemos quantos argumentos vamos receber
        
        // se chegar aqui, o execvp falhou
        char msg[] = "[Erro] Comando inválido ou falha no execvp\n";
        write(2, msg, strlen(msg));
        exit(-1);
    }
        
    else {
        // Processo pai
        // printf("Eu sou o pai! O meu PID é: %d. Criei um filho com o PID: %d\n",getpid(), pid);
        waitpid(pid, &status, 0); // O pai espera que o filho com um PID x termine
        
        if (WIFEXITED(status)) { // o processo-pai verifica se o processo-filho terminou sem erros
            int exit_code = WEXITSTATUS(status); // Obtém o código de terminação

			// declaramos um buffer para montar a string do log
            char buffer_log[1024] = ""; // inicializamos vazio para usar strcat com segurança
			
			// Juntamos os argumentos no log
			for (int i = 0; args[i] != NULL; i++) { // percorre a lista de palavras que o utilizador enviou e para quando entra NULL (fim da lista)
				strcat(buffer_log, args[i]); // cola a palavra atual (ex: "ls") no fim do buffer
                strcat(buffer_log, " "); // adiciona um espaço para as palavras nao ficarem coladas
            }
			
			strcat(buffer_log, "; exit status: ");
			
			// Convertemos o exit_code (int para string)
			char s_status[12]; // pequeno buffer para guardar o numero do erro
			int idx = 0;
			int n = exit_code;

			// Se o código for 0, tratamos logo
			if (n == 0) {
				s_status[idx++] = '0';
			} else {
				// Extrai os dígitos (sai de trás para a frente)
				while (n > 0) {
					s_status[idx++] = (n % 10) + '0'; // entra primeiro o ultimo digito do numero (ex: 123 entra o 3)
					n /= 10; // remove o ultimo digito para passarmos ao seguinte (ex: 123 passa a 12)
				}
			}
			s_status[idx] = '\0'; // Fecha a string

			// Como os números saíram ao contrário, temos de inverter a string
			for (int i = 0; i < idx / 2; i++) {
				char temp = s_status[i];
				s_status[i] = s_status[idx - 1 - i];
				s_status[idx - 1 - i] = temp;
			}

			// Juntar o status ao buffer final
			strcat(buffer_log, s_status); // Cola o número do status já convertido e na ordem certa
            strcat(buffer_log, "\n");
			
            // abrir ficheiro de logs internamente
            // O_WRONLY: abre o ficheiro em modo de "apenas escrita"
	        // O_CREAT: cria o ficheiro se ele ainda não existir
            // O_APPEND: escreve no fim, sem remover o histórico de logs
            // 0644: permissões de leitura+escrita para o dono e só de leitura para o grupo e outros
            int fd_log = open("servidor.log", O_WRONLY | O_CREAT | O_APPEND, 0644);

            if (fd_log != -1) {
                // Escreve no ficheiro de logs
                if (write(fd_log, buffer_log, strlen(buffer_log)) == -1) { // se for igual a -1 falhou
                    char err_w[] = "[Erro] Falha ao escrever no ficheiro de log\n";
                    write(STDERR_FILENO, err_w, strlen(err_w));
                }
                close(fd_log); //Fechar o ficheiro depois de escrever
            } else {
                char err_o[] = "[Erro] Falha ao abrir o log\n";
                write(STDERR_FILENO, err_o, strlen(err_o));
            }
        }
    }
}

// Funcao auxiliar
// Transforma "ls -l" em {"ls", "-l", NULL}
void preparar_e_executar(char *comando_completo) {
    char *args[10]; // array de ponteiros para os argumentos
    int arg_idx = 0;
    int k = 0; //indice para percorrer a string

    // Enquanto nao chegarmos ao fim da string e nao enchermos o array
    while (comando_completo[k] != '\0' && arg_idx < 9) {
        
        //Saltar espaços em branco
        while (comando_completo[k] == ' ') {
            k++;
        }

        // ao chegar ao fim break
        if (comando_completo[k] == '\0') break;

        args[arg_idx] = &comando_completo[k];
        arg_idx++;

        //vancamos ate encontrar o fim da palavra
        while (comando_completo[k] != ' ' && comando_completo[k] != '\0') {
            k++;
        }

        // ao encontrar um espaco, substituimos por '\0'
        if (comando_completo[k] == ' ') {
            comando_completo[k] = '\0';
            k++; //prox posicao para continuar o ciclo
        }
    }

    // O ultimo elemento do array TEM de ser NULL para o execvp saber que acabou
    args[arg_idx] = NULL;

    if (arg_idx > 0) {
        executarComandos(args);
    }
}

//SERVIDOR
int mainServidor(){
    int fd;
    char buffer[BUFFER_SIZE];
    char comando_atual[BUFFER_SIZE];
    
    // cria o named pipe com permissoes 0666
    if (mkfifo(FIFO_NAME, 0666) == -1) {
        // Escreve no descritor 2 (STDERR_FILENO)
        char msg_erro[] = "[Erro] mkfifo falhou\n";
        write(STDERR_FILENO, msg_erro, strlen(msg_erro));
        
        exit(-1);
    }
    
    // Escreve no descritor 1 (STDOUT_FILENO)
    char msg_pronto[] = "[Servidor] Pronto para iniciar o processamento.\n";
    write(STDOUT_FILENO, msg_pronto, strlen(msg_pronto));

    while (1){
        // abre para leitura
        fd = open(FIFO_NAME, O_RDONLY);
        
        if (fd == -1) {
            // Escreve no descritor 2 (STDERR_FILENO)
            char msg_erro[] = "[Erro] Falha ao abrir o pipe\n";
            write(STDERR_FILENO, msg_erro, strlen(msg_erro));
            
            exit(-2);
        }

        //Limpar o bbufer
        memset(buffer, 0, sizeof(buffer)); //Enche o buffer todo com zeros
        
        //ler do pipe
        if (read(fd, buffer, sizeof(buffer)) > 0){
            buffer[strcspn(buffer, "\n")]= 0;//remove \n se existir
            
            write(STDOUT_FILENO, "Received: ", 10); 
            write(STDOUT_FILENO, buffer, strlen(buffer));
            write(STDOUT_FILENO, "\n", 1);

            int j = 0;

            // Percorre a string recebida
            for (int i = 0; i <= strlen(buffer); i++) {
                
                if (buffer[i] == ';' || buffer[i] == '\0') {
                    
                    // Fechar a string do comando atual
                    comando_atual[j] = '\0';
                    
                    // Verifica se nao esta vazia (caso haja ;; seguidos)
                    if (j > 0) {
                        write(STDOUT_FILENO, "Comando isolado: '", 18);
                        write(STDOUT_FILENO, comando_atual, strlen(comando_atual));
                        write(STDOUT_FILENO, "'\n", 2);
                        
                        preparar_e_executar(comando_atual);
                    }
                    
                    //reiniciar j para guardar o proximo comando
                    j = 0;

                } else {
                    // Se for uma letra normal, copia para o comando_atual
                    comando_atual[j] = buffer[i];
                    j++;
                }
            }   
        }
        
        close(fd);
    }


    //fecha o pipe_SO
    if (unlink(FIFO_NAME) == 0) {
        char msg_sucesso[] = "[Servidor] Pipe removido com sucesso.\n";
        write(STDOUT_FILENO, msg_sucesso, strlen(msg_sucesso));
    } else {
        char msg_erro[] = "[Servidor] Erro ao remover o pipe\n";
        write(STDERR_FILENO, msg_erro, strlen(msg_erro));
    }
    
    return 0;
}

//CLIENTE
int mainCliente(int argc, char *argv[]) { 
    
    // Verifica se o utilizador passou argumentos (argc < 2 significa que só tem o nome do programa)
    if (argc < 1) { 
        char erro[] = "Uso: ./projeto <comando1> [args] ...\n"; // Mensagem de erro formatada
        write(STDERR_FILENO, erro, strlen(erro)); // Escreve a mensagem no descritor de erro 
        exit(-1);  // Termina o processo com erro
    } // Fim da verificação de segurança

    char encomenda[BUFFER_SIZE] = ""; // Declara um array de caracteres (buffer) na stack e inicializa-o vazio


    // Ciclo para percorrer os argumentos passados no terminal (começa no i=1, pois o 0 é o executável)
    for (int i = 0; i < argc; i++) { 
        
        // Verifica se a junção do argumento atual não ultrapassa o limite do buffer (proteção de memória)
        if (strlen(encomenda) + strlen(argv[i]) + 2 < BUFFER_SIZE) { 
            
            strcat(encomenda, argv[i]); // Concatena (junta) o argumento atual à string "encomenda"
            
            // Verifica se este não é o último argumento
            if (i < argc - 1) { 

                strcat(encomenda, " "); //para argumentos do mesmo comando funcionarem (ex: ls -l)
            
            } // Fim do if do separador
        } // Fim da verificação de tamanho 
    } // Fim do ciclo de construção da string

    // Tenta abrir o Named Pipe para escrita (O_WRONLY). Bloqueia se o servidor não estiver a ler.
    int fd = open(FIFO_NAME, O_WRONLY); 
    
    // Verifica se a abertura do ficheiro especial (pipe) falhou
    if (fd == -1) { 
        char erro[] = "[Erro] O Pipe não existe ou o servidor não está ativo\n";  // Mensagem de erro
        write(STDERR_FILENO, erro, strlen(erro)); // Escreve no stderr
        exit(-2); // Termina o processo com código de erro -2
    } // Fim da verificação do open

    // Transfere os dados da string "encomenda" para o buffer do kernel associado ao pipe
    // strlen + 1 é usado para enviar também o terminador nulo '\0'
    write(fd, encomenda, strlen(encomenda) + 1); 
    
    // Imprime no ecrã o que foi enviado para confirmação do utilizador
    write(STDOUT_FILENO, "[Cliente] comando enviado: ", 27);
    write(STDOUT_FILENO, encomenda, strlen(encomenda));
    write(STDOUT_FILENO, "\n", 1);
   
    // Fecha o descritor de ficheiro, libertando o recurso no Sistema Operativo
    close(fd); 

    return 0; 
}

//MAIN PRINCIPAL
int main(int argc, char *argv[]) {
    // Se o utilizador escrever: ./projeto servidor
    if (argc > 1 && strcmp(argv[1], "servidor") == 0) {
        return mainServidor();
    }
    // Se o utilizador escrever ex: ./projeto ls -l
    else if (argc > 1) {
        // Passamos apenas os argumentos depois do nome do executável para a função cliente
        // &argv[1] significa que o array começa no argumento 1 (o comando)
        return mainCliente(argc - 1, &argv[1]);
    }
    else {
        
        char msg_titulo[] = "Modo de uso:\n";
        write(STDOUT_FILENO, msg_titulo, strlen(msg_titulo));

        // printf("  1. Iniciar servidor: %s servidor\n", argv[0]);
        write(STDOUT_FILENO, "  1. Iniciar servidor: ", 23); // Texto fixo
        write(STDOUT_FILENO, argv[0], strlen(argv[0]));      // Variável (nome do programa)
        write(STDOUT_FILENO, " servidor\n", 10);             // Fim da linha

        //
        // printf("  2. Enviar comando:   %s <comando> [argumentos]\n", argv[0]);
        write(STDOUT_FILENO, "  2. Enviar comando:   ", 23); // Texto fixo
        write(STDOUT_FILENO, argv[0], strlen(argv[0]));      // Variável (nome do programa)
        write(STDOUT_FILENO, " <comando> [argumentos]\n", 24); // Fim da linha

        return 1;
    }
}