package FrontEnd;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Consola {

    private final Scanner scanner = new Scanner(System.in);

    //Em vez de usar sout (".."); podemos usar estas funções para comunicar ao utilizador
    public void escrever(String mensagem) {
        System.out.println(mensagem);
    }

    public void escreverErro(String mensagem) {
        System.err.println(mensagem);
    }

    //em vez de estar a escrever com o sout e dps scanner podemos só usar esta metodo
    public String lerString(String mensagem) {
        escrever(mensagem);
        return scanner.nextLine();
    }

    //Este meodo faz a verificação se o q foi intrudozido pelo o utilizador é um int
    public int lerInteiro(String mensagem) {
        Integer numero = null;
        String texto;

        do {
            escrever(mensagem);
            texto = scanner.nextLine();

            try {
                numero = Integer.valueOf(texto); // so pode dar um tp de excessao
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é um número inteiro válido.");
            }

        } while (numero == null);//até o numero ser valido

        return numero;
    }

    //este metodo em vez de estarmos sempre a dar scan da opção este metodo faz a leitura e a validação
    public int lerInteiro(String[] opcoes) {
        Integer numero = null;
        String texto = "";

        do {
            escrever("Selecione uma das seguintes opcões:");
            for (int i = 0; i < opcoes.length; i++) {
                escrever((i + 1) + " - " + opcoes[i]);
            }

            try {
                texto = scanner.nextLine();
                numero = Integer.valueOf(texto);
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é uma opção válida");
            }

            if (numero == null || numero <= 0 || numero > opcoes.length) {
                numero = null;
                escreverErro(texto + " não é uma opção válida");
            }

        } while (numero == null);

        return numero;
    }
    
    
    //a unica diff com o lerInteiro(String msg) e q devolve um valor com casas decimais!!
    public double lerDecimal(String mensagem) {
        Double numero = null;
        String texto;

        do {
            escrever(mensagem);
            texto = scanner.nextLine();

            try {
                numero = Double.valueOf(texto);
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é um número decimal válido.");
            }
        } while (numero == null);

        return numero;
    }
    
    public int lerMes(String mensagem) {
        Integer mes = -1;
        String texto;

        do {
            escrever(mensagem);
            texto = scanner.nextLine();

            try {
                mes = Integer.valueOf(texto); // so pode dar um tp de excessao
                if (mes < 1 || mes > 12) {
                    escreverErro("Mês inválido. Insira um valor entre 1 e 12.");
                    mes = -1; // força novo ciclo
                }
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é um número válido. Tente novamente.");
                mes = -1;
            }
        } while (mes == -1);

        return mes;
    }
    
    public int lerAno(String mensagem) {
        Integer ano = -1;
        String texto;

        do {
            escrever(mensagem);
            texto = scanner.nextLine();

            try {
                ano = Integer.valueOf(texto); // so pode dar um tp de excessao
                if (ano < 1900 || ano > 9999) {
                    escreverErro("Ano inválido. Insira um ano entre 1900 e 9999.");
                    ano = -1;
                }
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é um número válido. Tente novamente.");
                ano = -1;
            }
        } while (ano == -1);

        return ano;
    }
    public int lerDia(String mensagem) {
        Integer dia = -1;
        String texto;

        do {
            escrever(mensagem);
            texto = scanner.nextLine();

            try {
                dia  = Integer.valueOf(texto); // so pode dar um tp de excessao
                if (dia < 1 || dia > 31) {
                    escreverErro("Dia inválido. Insira um valor entre 1 e 31.");
                    dia = -1;
                }
            } catch (NumberFormatException e) {
                escreverErro(texto + " não é um número válido. Tente novamente.");
                dia = -1;
            }
        } while (dia == -1);

        return dia;
    }
    
    public LocalDateTime lerData(String mensagem) {
        escrever(mensagem);

        while (true) {
            int ano = lerAno("Ano (AAAA): ");
            int mes = lerMes("Mês (1- 12): ");
            int dia = lerDia("Dia: ");

            try {
                // valida corretamente o dia conforme mês/ano (ex.: 29/02 em anos bissextos)
                return LocalDateTime.of(ano, mes, dia,0,0,0);
            } catch (DateTimeException e) {
                escreverErro("Data inválida. Verifique dia/mês/ano e tente novamente.");
                // volta ao início do ciclo
            }
        }
    }

    public String lerEmail(String mensagem) {
        String email = "";
        boolean valido = false;

        while (!valido) {
            email = lerString(mensagem).trim(); // .trim() remove espaços acidentais no início/fim

            //Validar se tem @
            int posicaoArroba = email.indexOf("@"); //se não tiver @ indexOf devolve -1

            //Validar se tem .
            int ultimaPosicaoPonto = email.lastIndexOf("."); // se não tiver . indexOf devolve .

            if (posicaoArroba == -1) {
                escreverErro("Email inválido: Falta o '@'.");

            } else if (email.contains(" ")) {
                escreverErro("Email inválido: Não pode conter espaços.");

            } else if (posicaoArroba == 0 || posicaoArroba == email.length() - 1) {
                escreverErro("Email inválido: O '@' não pode estar no início nem no fim.");

            } else if (ultimaPosicaoPonto < posicaoArroba) {
                // Garante que existe um ponto DEPOIS do @
                escreverErro("Email inválido: Falta o domínio (ex: .com, .pt) depois do '@'.");

            } else {
                //é válido
                valido = true;
            }
        }
        return email;
    }
}
