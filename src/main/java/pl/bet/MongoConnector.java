package pl.bet;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

public class MongoConnector {

    private MongoClient mongoClient;

    private PropertiesLoader propertiesLoader = new PropertiesLoader();


    public MongoDatabase connect() {
        MongoCredential mongoCredential = MongoCredential.
                createCredential(propertiesLoader.getUser(), propertiesLoader.getDb(), propertiesLoader.getPassword().toCharArray());

        ServerAddress serverAddress = new ServerAddress(propertiesLoader.getAddress());

        mongoClient = new MongoClient(serverAddress, prepareCredentials(mongoCredential));

        return mongoClient.getDatabase(propertiesLoader.getDb());

    }

    private List<MongoCredential> prepareCredentials(MongoCredential mongoCredential) {
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        credentials.add(mongoCredential);
        return credentials;
    }

}
