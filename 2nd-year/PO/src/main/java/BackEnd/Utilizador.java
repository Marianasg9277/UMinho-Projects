package BackEnd;

import BackEnd.exceptions.DadosInvalidosException;
import java.io.Serializable;

public class Utilizador implements Serializable{
    private String idUtilizador;
    private String email;
    private String nome;
    private String username;
    private String password;
    
    //para a autenticacao
    private int tentativasFalhadas = 0;
    private boolean bloqueado = false;

  
//Construtor    
    protected Utilizador(String idUtilizador, String email, String nome, String username, String password){
        this.idUtilizador = idUtilizador;
        this.email        = email;
        this.nome         = nome;
        this.username     = username;
        this.password     = password;
    }
    
// Funcoes get nao temos getPassword para evitar leaks
    public String getIdUtilizador(){
        return idUtilizador;
    }
    
    public String getEmail(){
        return email;
    }
    
    public String getNome(){
        return nome;
    }
    
    public String getUsername(){
        return username;
    }
    

        
// Funcoes set nao existe setIdUtilizador; Utilizador não deve conseguir alterar o ID manualmente. 
    public void setEmail(String email) throws DadosInvalidosException {
        if (email == null || !email.contains("@"))
            throw new DadosInvalidosException("Email inválido: " + email);

        this.email = email;
    }

    
    public void setNome(String nome){
        this.nome = nome;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
//Metodos para autenticar utilizador
    //metodo valida a password
    public boolean validarPassword(String tentativa) {
        return password.equals(tentativa);
    }
    
    //metodo que verifica se o utilizador ultrapassou o num de tentativas de log in maximas
    public void falhaLogin() {
        tentativasFalhadas++;
        if (tentativasFalhadas >= 3) { //após 3 tentativas o utilizador bloqueia
            bloqueado = true;
        }
    }

    //apos ter sido desbloqueado o utilizador pode fazer novas 3 tentativas de login
    public void resetTentativas() {
        tentativasFalhadas = 0;
        bloqueado = false; // desbloqueia automaticamente
    }

    //verifica se o utilizador está bloqueado
    public boolean isBloqueado() {
        return bloqueado;
    }

    
}
