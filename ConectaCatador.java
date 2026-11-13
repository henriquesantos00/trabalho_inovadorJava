import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConectaCatador {
    private static List<Descarte> listaDescartes = new ArrayList<>();
    private static long contadorId = 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean rodando = true;

        // Dados iniciais de exemplo para teste rápido
        listaDescartes.add(new Descarte(contadorId++, "Maria Silva", "(11) 98888-1111", "Centro", "Papelão", "3 caixas grandes"));
        listaDescartes.add(new Descarte(contadorId++, "João Santos", "(11) 97777-2222", "Pinheiros", "Garrafas PET", "2 sacos"));

        while (rodando) {
            System.out.println("\n==================================================");
            System.out.println("            ♻️  CONECTA CATADOR - TERMINAL           ");
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
                    System.out.println("\n👋 A encerrar o sistema. Até logo!");
                    break;
                default:
                    System.out.println("\n⚠️ Opção inválida! Escolhe entre 0 e 3.");
                    pausa(scanner);
            }
        }
        scanner.close();
    }

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

        Descarte novo = new Descarte(contadorId++, nome, tel, bairro, material, qtd);
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
                // Correção: Uso de .equals() para comparar o ID corretamente
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