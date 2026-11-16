import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class ConectaCatador {

    // Lista segura para acesso simultâneo entre Terminal e Navegador
    private static final List<Descarte> listaDescartes = new CopyOnWriteArrayList<>();
    private static final AtomicLong contadorId = new AtomicLong(1);

    // Classe modelo integrada no mesmo arquivo para evitar subpastas
    static class Descarte {
        private Long id;
        private String nomeMorador;
        private String telefone;
        private String bairro;
        private String tipoMaterial;
        private String quantidade;
        private String status;
        private String nomeColetor;

        public Descarte(Long id, String nomeMorador, String telefone, String bairro, String tipoMaterial, String quantidade) {
            this.id = id;
            this.nomeMorador = nomeMorador;
            this.telefone = telefone;
            this.bairro = bairro;
            this.tipoMaterial = tipoMaterial;
            this.quantidade = quantidade;
            this.status = "DISPONIVEL";
            this.nomeColetor = "Nenhum";
        }

        public Long getId() { return id; }
        public String getNomeMorador() { return nomeMorador; }
        public String getTelefone() { return telefone; }
        public String getBairro() { return bairro; }
        public String getTipoMaterial() { return tipoMaterial; }
        public String getQuantidade() { return quantidade; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getNomeColetor() { return nomeColetor; }
        public void setNomeColetor(String nomeColetor) { this.nomeColetor = nomeColetor; }
    }

    public static void main(String[] args) throws IOException {
        // Dados iniciais de exemplo
        listaDescartes.add(new Descarte(contadorId.getAndIncrement(), "Maria Silva", "(11) 98888-1111", "Centro", "Papelão", "3 caixas grandes"));
        listaDescartes.add(new Descarte(contadorId.getAndIncrement(), "João Santos", "(11) 97777-2222", "Pinheiros", "Garrafas PET", "2 sacos"));

        // Inicia o servidor HTTP puro na porta 8080
        HttpServer server = iniciarServidorWeb(8080);

        Scanner scanner = new Scanner(System.in);
        boolean rodando = true;

        System.out.println("\n**************************************************");
        System.out.println(" 🌐 SISTEMA ONLINE! Acesse aqui: http://localhost:8080");
        System.out.println("**************************************************");

        while (rodando) {
            System.out.println("\n==================================================");
            System.out.println("            ♻️  CONECTA CATADOR - TERMINAL           ");
            System.out.println("   Link ativo: http://localhost:8080              ");
            System.out.println("==================================================");
            System.out.println("1. 📋 Listar / Filtrar Descartes");
            System.out.println("2. 📦 Cadastrar Novo Descarte (Morador)");
            System.out.println("3. 🤝 Aceitar Coleta (Coletor)");
            System.out.println("0. 🚪 Sair");
            System.out.print("\nEscolhe uma opção: ");

            String opcao = scanner.nextLine();

            switch (opcao.trim()) {
                case "1":
                    listarDescartes(scanner);
                    break;
                case "2":
                    cadastrarDescarte(scanner);
                    break;
                case "3":
                    aceitarColeta(scanner);
                    break;
                case "0":
                    rodando = false;
                    server.stop(0); // Encerra o link web junto com o terminal
                    System.out.println("\n👋 A encerrar o sistema e o link web. Até logo!");
                    break;
                default:
                    System.out.println("\n⚠️ Opção inválida! Escolhe entre 0 e 3.");
                    pausa(scanner);
            }
        }
        scanner.close();
    }

    // =========================================================================
    // SERVIDOR HTTP PURO (TEXTO SIMPLES, SEM HTML)
    // =========================================================================
    private static HttpServer iniciarServidorWeb(int porta) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);

        // Rota principal: Lista descartes e mostra comandos via URL
        server.createContext("/", exchange -> {
            Map<String, String> params = lerParametrosUrl(exchange.getRequestURI().getRawQuery());
            String bairroFiltro = params.getOrDefault("bairro", "").toLowerCase();

            StringBuilder sb = new StringBuilder();
            sb.append("==================================================\n");
            sb.append("          ♻️  CONECTA CATADOR - MODO LINK          \n");
            sb.append("==================================================\n\n");
            sb.append("COMO USAR PELO LINK (copie e cole na barra de endereços):\n");
            sb.append("-> Listar todos:   http://localhost:8080/\n");
            sb.append("-> Filtrar bairro: http://localhost:8080/?bairro=Centro\n");
            sb.append("-> Novo cadastro:  http://localhost:8080/cadastrar?nome=Ana&tel=99999&bairro=Centro&material=Vidro&qtd=5\n");
            sb.append("-> Aceitar coleta: http://localhost:8080/aceitar?id=1&coletor=Carlos\n\n");
            sb.append("--------------------------------------------------\n");
            sb.append("LISTA DE DESCARTES ATUAL:\n");
            sb.append("--------------------------------------------------\n");

            boolean encontrou = false;
            for (Descarte item : listaDescartes) {
                if (bairroFiltro.isEmpty() || item.getBairro().toLowerCase().contains(bairroFiltro)) {
                    encontrou = true;
                    sb.append("ID: ").append(item.getId())
                      .append(" | Material: ").append(item.getTipoMaterial())
                      .append(" (").append(item.getQuantidade()).append(")\n");
                    sb.append("Bairro: ").append(item.getBairro()).append("\n");
                    sb.append("Morador: ").append(item.getNomeMorador()).append(" | Tel: ").append(item.getTelefone()).append("\n");
                    sb.append("Status: ").append(item.getStatus());
                    if ("ACEITO".equals(item.getStatus())) {
                        sb.append(" (Coletor: ").append(item.getNomeColetor()).append(")");
                    }
                    sb.append("\n--------------------------------------------------\n");
                }
            }

            if (!encontrou) {
                sb.append("Nenhum descarte encontrado.\n");
            }

            enviarTexto(exchange, 200, sb.toString());
        });

        // Rota para cadastrar via link: /cadastrar?nome=...&tel=...&bairro=...&material=...&qtd=...
        server.createContext("/cadastrar", exchange -> {
            Map<String, String> params = lerParametrosUrl(exchange.getRequestURI().getRawQuery());

            String nome = params.get("nome");
            String tel = params.get("tel");
            String bairro = params.get("bairro");
            String material = params.get("material");
            String qtd = params.get("qtd");

            if (nome == null || bairro == null || material == null) {
                enviarTexto(exchange, 400, "ERRO: Faltam dados na URL.\nExemplo correto:\nhttp://localhost:8080/cadastrar?nome=Ana&tel=11999990000&bairro=Centro&material=Papelao&qtd=2_caixas");
                return;
            }

            Descarte novo = new Descarte(
                contadorId.getAndIncrement(),
                nome,
                tel != null ? tel : "Não informado",
                bairro,
                material,
                qtd != null ? qtd : "1 unidade"
            );
            listaDescartes.add(novo);

            enviarTexto(exchange, 200, "✅ Descarte cadastrado com sucesso! ID: " + novo.getId() + "\nVolte para: http://localhost:8080/");
        });

        // Rota para aceitar coleta via link: /aceitar?id=1&coletor=Carlos
        server.createContext("/aceitar", exchange -> {
            Map<String, String> params = lerParametrosUrl(exchange.getRequestURI().getRawQuery());
            String idStr = params.get("id");
            String coletor = params.get("coletor");

            if (idStr == null || coletor == null) {
                enviarTexto(exchange, 400, "ERRO: Informe o ID e o nome do coletor.\nExemplo correto:\nhttp://localhost:8080/aceitar?id=1&coletor=Carlos");
                return;
            }

            try {
                long id = Long.parseLong(idStr);
                for (Descarte item : listaDescartes) {
                    if (item.getId().equals(id)) {
                        if ("ACEITO".equals(item.getStatus())) {
                            enviarTexto(exchange, 200, "⚠️ Este descarte já foi aceito por " + item.getNomeColetor() + "!\nVolte para: http://localhost:8080/");
                            return;
                        }
                        item.setStatus("ACEITO");
                        item.setNomeColetor(coletor);
                        enviarTexto(exchange, 200, "✅ Coleta do ID " + id + " aceita por " + coletor + "!\nVolte para: http://localhost:8080/");
                        return;
                    }
                }
                enviarTexto(exchange, 404, "❌ Erro: ID " + id + " não encontrado.\nVolte para: http://localhost:8080/");
            } catch (NumberFormatException e) {
                enviarTexto(exchange, 400, "❌ Erro: O ID precisa ser um número.");
            }
        });

        server.setExecutor(null);
        server.start();
        return server;
    }

    private static void enviarTexto(HttpExchange exchange, int status, String resposta) throws IOException {
        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> lerParametrosUrl(String query) {
        Map<String, String> mapa = new HashMap<>();
        if (query == null || query.isEmpty()) return mapa;

        for (String par : query.split("&")) {
            String[] partes = par.split("=", 2);
            if (partes.length == 2) {
                String chave = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
                String valor = URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
                mapa.put(chave, valor);
            }
        }
        return mapa;
    }

    // =========================================================================
    // MÉTODOS ORIGINAIS DO TERMINAL
    // =========================================================================
    private static void listarDescartes(Scanner scanner) {
        System.out.print("Filtrar por bairro (deixa em branco para ver todos): ");
        String bairroFiltro = scanner.nextLine().trim().toLowerCase();

        System.out.println("\n==================================================");
        System.out.println("               LISTA DE DESCARTES                 ");
        System.out.println("==================================================");

        boolean encontrado = false;
        for (Descarte item : listaDescartes) {
            if (bairroFiltro.isEmpty() || item.getBairro().toLowerCase().contains(bairroFiltro)) {
                encontrado = true;
                System.out.println("ID: " + item.getId() + " | Material: " + item.getTipoMaterial() + " (" + item.getQuantidade() + ")");
                System.out.println(" 📍 Bairro: " + item.getBairro());
                System.out.println(" 👤 Morador: " + item.getNomeMorador() + " | Tel: " + item.getTelefone());
                System.out.println(" 📌 Status: " + item.getStatus() + (item.getStatus().equals("ACEITO") ? " (Coletor: " + item.getNomeColetor() + ")" : ""));
                System.out.println("--------------------------------------------------");
            }
        }

        if (!encontrado) {
            System.out.println("Nenhum descarte encontrado.");
        }
        pausa(scanner);
    }

    private static void cadastrarDescarte(Scanner scanner) {
        System.out.println("\n--- CADASTRO DE NOVO DESCARTE ---");
        System.out.print("Teu Nome: ");
        String nome = scanner.nextLine();
        System.out.print("WhatsApp / Telefone: ");
        String tel = scanner.nextLine();
        System.out.print("Bairro: ");
        String bairro = scanner.nextLine();
        System.out.print("Tipo de Material (ex: Papelão, Óleo, Vidro): ");
        String material = scanner.nextLine();
        System.out.print("Quantidade (ex: 2 sacolas): ");
        String qtd = scanner.nextLine();

        Descarte novo = new Descarte(contadorId.getAndIncrement(), nome, tel, bairro, material, qtd);
        listaDescartes.add(novo);
        System.out.println("\n✅ Descarte cadastrado com sucesso! ID gerado: " + novo.getId());
        pausa(scanner);
    }

    private static void aceitarColeta(Scanner scanner) {
        System.out.print("Digite o ID do descarte que queres recolher: ");
        try {
            long id = Long.parseLong(scanner.nextLine());
            System.out.print("Teu Nome (Coletor): ");
            String coletor = scanner.nextLine();

            boolean achou = false;
            for (Descarte item : listaDescartes) {
                if (item.getId().equals(id)) {
                    if (item.getStatus().equals("ACEITO")) {
                        System.out.println("\n⚠️ Este descarte já foi aceito por " + item.getNomeColetor() + "!");
                        achou = true;
                        break;
                    }
                    item.setStatus("ACEITO");
                    item.setNomeColetor(coletor);
                    achou = true;
                    System.out.println("\n✅ Coleta aceita com sucesso! Combina a recolha com o morador.");
                    break;
                }
            }

            if (!achou) {
                System.out.println("\n❌ Erro: ID não encontrado.");
            }
        } catch (NumberFormatException e) {
            System.out.println("\n❌ Erro: Digita um ID numérico válido.");
        }
        pausa(scanner);
    }

    private static void pausa(Scanner scanner) {
        System.out.print("\nPressiona ENTER para continuar...");
        scanner.nextLine();
    }
}