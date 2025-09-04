import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;

//-------classe de eventos--------
class Events {
    String nome;
    String endereco;
    String categoria;
    LocalDateTime horario;
    String descricao;
    ArrayList<String> participantes; 

    public Events(String nome, String endereco, String categoria, LocalDateTime horario, String descricao) {
        this.nome = nome;
        this.endereco = endereco;
        this.categoria = categoria;
        this.horario = horario;
        this.descricao = descricao;
        this.participantes = new ArrayList<>();
    }

    public String toFileString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String part = String.join(",", participantes); 
        return nome + ";" + endereco + ";" + categoria + ";" + horario.format(formatter) + ";" + descricao + ";" + part;
    }

    public static Events fromFileString(String linha) {
        try {
            String[] partes = linha.split(";", -1);
            String nome = partes[0];
            String endereco = partes[1];
            String categoria = partes[2];
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime horario = LocalDateTime.parse(partes[3], formatter);
            String descricao = partes[4];
            Events ev = new Events(nome, endereco, categoria, horario, descricao);
            if(partes.length > 5 && !partes[5].isEmpty()) {
                ev.participantes.addAll(Arrays.asList(partes[5].split(",")));
            }
            return ev;
        } catch (Exception e) {
            return null;
        }
    }
}

// -------classe de users--------
class Users {
    String email;
    String senha;
    String nome;
    String endereco;

    public Users(String email, String senha, String nome, String endereco) {
        this.email = email;
        this.senha = senha;
        this.nome = nome;
        this.endereco = endereco;
    }
}

public class SistemEvents {

    static ArrayList<Events> listaEventos = new ArrayList<>();
    static ArrayList<Users> listaUsuarios = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static final String FILE_NAME = "events.data";
    static Users usuarioLogado = null;

    public static void main(String[] args) {
        carregarEventos();

        int opcao;
        do {
            System.out.println("--EventsSystem--");
            System.out.println("-- 1- Login --");
            System.out.println("-- 2- Cadastro --");
            System.out.println("-- 3- Sair --");

            opcao = sc.nextInt();
            sc.nextLine();

            switch(opcao) {
                case 1: login(); break;
                case 2: cadastro(); break;
                case 3: System.out.println("--Até mais--"); break;
                default: System.out.println("--Opção inválida--");
            }
        } while(opcao != 3);
        sc.close();
    }

    public static void cadastro() {
        System.out.println("\n--- Cadastro ---");
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Senha: ");
        String senha = sc.nextLine();
        System.out.print("Endereço: ");
        String endereco = sc.nextLine();

        Users user = new Users(email, senha, nome, endereco);
        listaUsuarios.add(user);
        System.out.println("Cadastro concluído!\n");
        
        usuarioLogado = user;
        pgPrincipal();
    }

    public static void login() {
        System.out.println("\n--Login--");
        System.out.print("Email: ");
        String email = sc.nextLine();
        System.out.print("Senha: ");
        String senha = sc.nextLine();

        for(Users u : listaUsuarios) {
            if(u.email.equals(email) && u.senha.equals(senha)) {
                usuarioLogado = u;
                System.out.println("Login realizado com sucesso!\n");
                pgPrincipal();
                return;
            }
        }
        System.out.println("Email ou senha incorretos.\n");
    }

    public static void eventos() {
        if(listaEventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
            return;
        }

        listaEventos.sort((e1, e2) -> e1.horario.compareTo(e2.horario));
        LocalDateTime agora = LocalDateTime.now();

        System.out.println("\n--- Lista de Eventos ---");
        int i = 1;
        for(Events e : listaEventos) {
            String status;
            if(e.horario.isBefore(agora)) status = "[JÁ OCORREU]";
            else if(e.horario.isAfter(agora)) status = "[FUTURO]";
            else status = "[AGORA]";
            System.out.println(i + "- " + status + " " + e.nome + " em " + e.endereco + " - " + e.horario);
            i++;
        }

        
        System.out.println("\nDigite o número do evento para confirmar presença ou 0 para voltar:");
        int escolha = sc.nextInt();
        sc.nextLine();
        if(escolha > 0 && escolha <= listaEventos.size()) {
            Events ev = listaEventos.get(escolha-1);
            if(!ev.participantes.contains(usuarioLogado.email)) {
                ev.participantes.add(usuarioLogado.email);
                salvarEvento(ev);
                System.out.println("Presença confirmada!");
            } else {
                System.out.println("Você já confirmou presença neste evento.");
            }
        }
    }

    public static void criarEvento() {
        System.out.println("\n--Crie o seu evento--");
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        System.out.print("Endereço: ");
        String endereco = sc.nextLine();

        System.out.println("Digite um número para a CATEGORIA: 1-Esporte 2-Festa 3-Show");
        String[] categArr = {"", "Esporte", "Festa", "Show"};
        int option = sc.nextInt();
        sc.nextLine();
        String categoria = categArr[option];

        System.out.print("Data e hora (dd/MM/yyyy HH:mm): ");
        String dataHoraStr = sc.nextLine();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime horario = LocalDateTime.parse(dataHoraStr, formatter);

        System.out.print("Descrição: ");
        String descricao = sc.nextLine();

        Events ev = new Events(nome, endereco, categoria, horario, descricao);
        listaEventos.add(ev);
        salvarEvento(ev);
        System.out.println("Evento criado com sucesso e salvo!");
    }

    public static void listaEventos() {
        if(listaEventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
            return;
        }

        System.out.println("\n--- Seus Eventos Confirmados ---");
        int i = 1;
        for(Events e : listaEventos) {
            if(e.participantes.contains(usuarioLogado.email)) {
                System.out.println(i + "- " + e.nome + " em " + e.endereco + " - " + e.horario);
            }
            i++;
        }

        System.out.println("\nDigite o número do evento para cancelar participação ou 0 para voltar:");
        int escolha = sc.nextInt();
        sc.nextLine();
        if(escolha > 0 && escolha <= listaEventos.size()) {
            Events ev = listaEventos.get(escolha-1);
            if(ev.participantes.contains(usuarioLogado.email)) {
                ev.participantes.remove(usuarioLogado.email);
                salvarEvento(ev);
                System.out.println("Participação cancelada!");
            } else {
                System.out.println("Você não está participando deste evento.");
            }
        }
    }

    public static void pgPrincipal() {
        int option;
        do {
            System.out.println("\n--Bem-vindo " + usuarioLogado.nome + "--");
            System.out.println("-- 1- Eventos --");
            System.out.println("-- 2- Criar evento --");
            System.out.println("-- 3- Meus eventos confirmados --");
            System.out.println("-- 4- Sair --");

            option = sc.nextInt();
            sc.nextLine();

            switch(option) {
                case 1: eventos(); break;
                case 2: criarEvento(); break;
                case 3: listaEventos(); break;
                case 4: System.out.println("--Até mais--"); usuarioLogado = null; break;
                default: System.out.println("--Opção inválida--");
            }
        } while(option != 4);
    }

    public static void salvarEvento(Events ev) {
        try (FileWriter fw = new FileWriter(FILE_NAME, false); 
             PrintWriter pw = new PrintWriter(fw)) {

            for(Events e : listaEventos) {
                pw.println(e.toFileString());
            }

        } catch (IOException e) {
            System.out.println("Erro ao salvar evento: " + e.getMessage());
        }
    }

    public static void carregarEventos() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String linha;
            while((linha = br.readLine()) != null) {
                Events ev = Events.fromFileString(linha);
                if(ev != null) listaEventos.add(ev);
            }
        } catch (IOException e) {
            
        }
    }
}
