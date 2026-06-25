package BackEnd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class ListaUtilizadores implements Serializable {
    private HashMap<String, Utilizador> utilizadoresPorId;
    private HashMap<String, Utilizador> utilizadoresPorUsername;

//construtor
    //construtor com capacidade inicial
    public ListaUtilizadores(int capacidade) {
        utilizadoresPorId = new HashMap<>(capacidade);
        utilizadoresPorUsername = new HashMap<>(capacidade);
    }

    //HashMap tem capacidade inicial default de 15
    public ListaUtilizadores() {
        utilizadoresPorId = new HashMap<>();
        utilizadoresPorUsername = new HashMap<>();
    }

    //construtor recebe uma Collection e faz uma copia
    public ListaUtilizadores(Collection<Utilizador> lista) {
        HashMap<String, Utilizador> porId = new HashMap<>(lista.size());
        HashMap<String, Utilizador> porUsername = new HashMap<>(lista.size());
        
        for (Utilizador u : lista) {
            porId.put(u.getIdUtilizador(), u);
            porUsername.put(u.getIdUtilizador(), u);
        }
        
        utilizadoresPorId = porId;
        utilizadoresPorUsername = porUsername;
    }
    
//metodospara adicionar
    //metodo para adicionar utilizador
    public void adicionarUtilizador(Utilizador utilizador){
        if(utilizador != null){
            utilizadoresPorId.put(utilizador.getIdUtilizador(), utilizador);
            utilizadoresPorUsername.put(utilizador.getUsername(), utilizador);
        }
    }
    
//metodos para remover:
    //metodo para remover utilizador a 
    public void removerUtilizador(Utilizador utilizador){
        if(utilizador != null){
            utilizadoresPorId.remove(utilizador.getIdUtilizador(), utilizador);
            utilizadoresPorUsername.remove(utilizador.getUsername(), utilizador);
        }
    }
    
    //metodo para remover por id 
    public void removerPorId(String id) {
        Utilizador u = utilizadoresPorId.remove(id); //remove da lista e devolve para u o utilizador removido

        //remove também do hashmap q guarda com os usernames
        if (u != null) {
            utilizadoresPorUsername.remove(u.getUsername());    
        }
        
    }

    //metodo para remover por username
    public void removerPorUsername(String username) {
        Utilizador u = utilizadoresPorUsername.remove(username);

        //remove também do hashmap que guarda os utilizadores por ids
        if (u != null) {
            utilizadoresPorId.remove(u.getIdUtilizador());
          
        }
    }
    
//metodos para procurar:
    //procurar um utilizador pelo id
    public Utilizador procurarPorId (String id){
        return utilizadoresPorId.get(id);
    }
    
    //procurar um utilizador pelo username
    public Utilizador procuraPorUsername(String user){
        return utilizadoresPorUsername.get(user);
    }
    
    //procurar por email o utilizador
    public Utilizador procurarPorEmail(String email) {
        //percorre a lista de utilizadores
       for (Utilizador u : utilizadoresPorId.values()) {
           if (u.getEmail().equalsIgnoreCase(email)) { //emails não interassa se maiscula se é minuscula
               return u;
           }
       }
       return null;
    }
    
    //procurar pelo nome mas como o nome não é unico tem de devolver uma lista
    public Collection<Utilizador> procurarPorNome(String nome) {
       Collection<Utilizador> resultado = new ArrayList<>();
       //percorre a lista de utilizadores
        for (Utilizador u : utilizadoresPorId.values()) {
            
            if (u.getNome().equalsIgnoreCase(nome)) {
                resultado.add(u);
            }
        }
    
        return resultado;
    }

    //devolve num de utilizadores
    public int tamanhoListaUtilizadores(){
        return utilizadoresPorId.size();
    }
    
    //metodo para autenticar um utilizador
    public Utilizador autenticar(String username, String password) {
        Utilizador u = utilizadoresPorUsername.get(username);

        if (u == null) return null; // username não existe
        if (u.isBloqueado()) return null; //user esta bloqueado
        if (!u.validarPassword(password)){
            u.falhaLogin();
            return null;
        } // password errada
        
        // limpar tentativas pois auteticou com sucesso
        u.resetTentativas();
        return u; // sucesso
    }

//utilizadores por tipo    
    //lista utilizadores por tipo Coordenador Investigador ou coordenador
    public Collection<Utilizador> utilizadoresPorTipo(String tipo) {
        Collection<Utilizador> resultado = new ArrayList<>();
        
         for (Utilizador u : utilizadoresPorId.values()) {
            if (u.getClass().getName().equalsIgnoreCase(tipo)) {
                resultado.add(u);
            }
        }
        return resultado;
    }
    
    //num total de utilizadores de certo tipo
    public int totalUtilizadoresPorTipo(String tipo){
        return utilizadoresPorTipo(tipo).size();
        
    }
            
    //verifica devovendo true ou false se certo utilizador existe
    public boolean existeUsername(String username) {
        return utilizadoresPorUsername.containsKey(username);
    }

    
    public void atualizarUsername(Utilizador u, String novoUsername) {
        if (u == null || novoUsername == null) return;

        // remover chave antiga
        utilizadoresPorUsername.remove(u.getUsername());

        // atualizar objeto
        u.setUsername(novoUsername);

        // inserir nova chave
        utilizadoresPorUsername.put(novoUsername, u);
    }

    public boolean existeEmail(String email) {
        if (email == null) {
            return false;
        }

        for (Utilizador u : utilizadoresPorId.values()) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public void atualizarEmail(Utilizador u, String novoEmail) {
        if (u == null || novoEmail == null) return;
        u.setEmail(novoEmail);
    }
    
    public void atualizarPassword(Utilizador u, String novaPassword) {
        if (u == null || novaPassword == null) return;
        u.setPassword(novaPassword);
    }





}