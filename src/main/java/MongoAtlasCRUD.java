import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.Scanner;

public class MongoAtlasCRUD {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private final Scanner scanner = new Scanner(System.in);

    // Construtor com conexão ao Atlas
    public MongoAtlasCRUD() {
        // Substitua pela sua connection string do Atlas
        String connectionString = "mongodb+srv://user:usuario246@cluster0.awzh8.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        this.mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase("crudAtlasDB");
        this.collection = database.getCollection("produtos");
        System.out.println("Conectado ao MongoDB Atlas!");
    }

    // CREATE
    public void criarProduto() {
        System.out.println("\n--- CRIAR PRODUTO ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Preço: ");
        double preco = scanner.nextDouble();
        scanner.nextLine(); // Limpar buffer

        System.out.print("Categoria: ");
        String categoria = scanner.nextLine();

        Document produto = new Document("nome", nome)
                .append("preco", preco)
                .append("categoria", categoria)
                .append("estoque", 0); // Valor padrão

        collection.insertOne(produto);
        System.out.println("Produto criado com ID: " + produto.get("_id"));
    }

    // READ
    public void listarProdutos() {
        System.out.println("\n--- LISTAR PRODUTOS ---");
        FindIterable<Document> produtos = collection.find();

        for (Document produto : produtos) {
            System.out.println(produto.toJson());
        }
    }

    // UPDATE
    public void atualizarProduto() {
        System.out.println("\n--- ATUALIZAR PRODUTO ---");
        System.out.print("ID do produto: ");
        String id = scanner.nextLine();
        
        System.out.print("Novo preço: ");
        double novoPreco = scanner.nextDouble();
        scanner.nextLine(); // Limpar buffer

        Bson filtro = Filters.eq("_id", new org.bson.types.ObjectId(id));
        Bson atualizacao = Updates.set("preco", novoPreco);
        
        UpdateResult result = collection.updateOne(filtro, atualizacao);
        System.out.println(result.getModifiedCount() + " produto(s) atualizado(s)");
    }

    // DELETE
    public void deletarProduto() {
        System.out.println("\n--- DELETAR PRODUTO ---");
        System.out.print("ID do produto: ");
        String id = scanner.nextLine();
        
        Bson filtro = Filters.eq("_id", new org.bson.types.ObjectId(id));
        DeleteResult result = collection.deleteOne(filtro);
        
        System.out.println(result.getDeletedCount() + " produto(s) deletado(s)");
    }

    // OPERAÇÕES AVANÇADAS
    public void operacoesAvancadas() {
        System.out.println("\n--- OPERAÇÕES AVANÇADAS ---");
        
        // 1. Criar índice
        collection.createIndex(Indexes.ascending("nome"));
        System.out.println("Índice criado no campo 'nome'");
        
        // 2. Agregação - média de preço por categoria
        System.out.println("\nMédia de preço por categoria:");
        collection.aggregate(Arrays.asList(
            Aggregates.group("$categoria", 
                Accumulators.avg("mediaPreco", "$preco")
            )
        )).forEach(doc -> System.out.println(doc.toJson()));
    }

    public void fecharConexao() {
        mongoClient.close();
        scanner.close();
        System.out.println("Conexão encerrada.");
    }

    public static void main(String[] args) {
        MongoAtlasCRUD crud = new MongoAtlasCRUD();
        
        try {
            boolean executando = true;
            while (executando) {
                System.out.println("\n=== MENU ===");
                System.out.println("1. Criar produto");
                System.out.println("2. Listar produtos");
                System.out.println("3. Atualizar produto");
                System.out.println("4. Deletar produto");
                System.out.println("5. Operações avançadas");
                System.out.println("0. Sair");
                System.out.print("Opção: ");
                
                int opcao = Integer.parseInt(crud.scanner.nextLine());
                
                switch (opcao) {
                    case 1 -> crud.criarProduto();
                    case 2 -> crud.listarProdutos();
                    case 3 -> crud.atualizarProduto();
                    case 4 -> crud.deletarProduto();
                    case 5 -> crud.operacoesAvancadas();
                    case 0 -> executando = false;
                    default -> System.out.println("Opção inválida!");
                }
            }
        } finally {
            crud.fecharConexao();
        }
    }
}