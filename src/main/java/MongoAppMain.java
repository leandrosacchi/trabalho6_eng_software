import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import dto.Produto;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class MongoAppMain {

    public static void main(String[] args) throws Exception {
        MongoClient mongoClient = conectar("mongodb://localhost:27017");

        fechaConexao(mongoClient);

    }

    public static MongoClient conectar(String connectionString) throws Exception {
        MongoClient client = null;

        try {
            System.out.println("Conectando com o MongoDB");
            client = MongoClients.create(connectionString);
        } catch (Exception e) {
            System.out.println("Erro ao conectar ao banco de dados");
            e.printStackTrace();
        }

        return client;
    }

    public static Iterable<Document> listaProdutos(MongoClient client) {
        Iterable<Document> documents = null;

        try {
            System.out.println("Imprimindo produtos");
            documents = getCollection(client, "produtos").find();
            for (Document produto : documents) {
                ObjectId id = produto.get("_id", ObjectId.class);
                String nome = produto.getString("nome");
                String descricao = produto.getString("descricao");
                Double valor = produto.getDouble("valor");
                String estado = produto.getString("estado");
                System.out.println(id.toString() + " -- " + nome + " -- " + descricao + " -- " + valor + " -- " + estado);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar produtos:" + e);
            e.printStackTrace();
        }

        return documents;
    }

    public static void insereProduto(MongoClient client, Produto produto) {
        try {
            Document document = new Document("nome", produto.getNome())
                    .append("valor", produto.getValor())
                    .append("estado", produto.getEstado())
                    .append("descricao", produto.getDescricao());

            InsertOneResult result = getCollection(client, "produtos").insertOne(document);

            System.out.println("Produto [" + produto.getNome() + "] inserido com o id: " + result.getInsertedId().asObjectId().getValue());
        } catch (Exception e) {
            System.out.println("Erro ao inserir produto, " + e);
            e.printStackTrace();
        }
    }

    public static void alteraValorProduto(MongoClient client, String idProduto, Double novoValor) {
        try {
            Bson query = eq("_id", new ObjectId(idProduto));
            Document document = retornaProdutoPorId(client, idProduto);
            document.put("valor", novoValor);

            UpdateResult result = getCollection(client, "produtos").replaceOne(query, document);
            long count = result.getModifiedCount();
            System.out.println("Quantidade de produtos atualizados: "+count);

            if (count != 0) {
                System.out.println("Produto atualizado para:");
                System.out.println(document.get("_id")+ " -- " +document.get("nome")+ " -- "
                        +document.get("descricao")+ " -- " +document.get("valor")+ " -- " + document.get("estado"));
            }

        } catch (Exception e) {
            System.out.println("Erro ao atualizar valor do produto, " + e);
            e.printStackTrace();
        }
    }

    public static Document retornaProdutoPorId(MongoClient client, String idProduto) {
        Document document = null;
        try {
            Bson query = eq("_id", new ObjectId(idProduto));
            document = getCollection(client, "produtos").find(query).first();
        } catch (Exception e) {
            System.out.println("Erro ao apagar produto, " + e);
            e.printStackTrace();
        }

        return document;
    }

    public static void apagaProduto(MongoClient client, String idProduto) {
        try {
            Bson query = eq("_id", new ObjectId(idProduto));
            DeleteResult result = getCollection(client, "produtos").deleteOne(query);
            System.out.println("Quantidade de documentos deletados: " + result.getDeletedCount());
        } catch (Exception e) {
            System.out.println("Erro ao apagar produto, " + e);
            e.printStackTrace();
        }
    }

    private static MongoCollection<Document> getCollection(MongoClient client, String colecao) {
        MongoCollection<Document> collection = null;
        try {
            MongoDatabase db = client.getDatabase("loja");
            collection = db.getCollection(colecao);
        } catch (Exception e) {
            System.out.println("Erro ao recuperar a collection, " + e);
            e.printStackTrace();
        }
        return collection;
    }

    public static void fechaConexao(MongoClient client) {
        System.out.println("Fechando conexão");
        client.close();
    }
}
