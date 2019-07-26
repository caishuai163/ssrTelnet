package tools.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;

public class MongoUtils implements AutoCloseable {
    private MongoClient create;

    public void getConnect() {
        create = MongoClients.create("mongodb://127.0.0.1:27017");
        MongoIterable<String> listDatabaseNames = create.listDatabaseNames();
        for (String string : listDatabaseNames) {
            System.out.println(string);
        }
    }

    @Override
    public void close() throws Exception {
        if (create != null) {
            create.close();
        }
    }

}
