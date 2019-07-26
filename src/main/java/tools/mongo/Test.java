package tools.mongo;

import java.util.Date;

import org.bson.Document;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        try (MongoClient create = MongoClients
                .create("mongodb://127.0.0.1:27017");) {
            // ClientSession startSession = create.startSession();
            MongoDatabase database = create.getDatabase("mytest");
            MongoCollection<Document> collection = database
                    .getCollection("post");
            collection.insertOne(new Document("test", 1));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Thread.sleep(5000);
        System.out.println(new Date());
    }
}
