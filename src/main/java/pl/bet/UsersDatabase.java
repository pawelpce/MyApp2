package pl.bet;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

public class UsersDatabase {

    private static UsersDatabase usersDatabase = null;

    private UsersDatabase() {

    }

    public static UsersDatabase getInstance() {

        if (usersDatabase == null) {
            usersDatabase = new UsersDatabase();
        }
        return usersDatabase;
    }

    private MongoConnector mongoConnector = new MongoConnector();
    private MongoDatabase database = mongoConnector.connect();
    private MongoCollection<Document> appUsers = database.getCollection("appUsers");
    Logger mongoLogger = Logger.getLogger("org.mongodb.driver");


    public String addUser(String username, String login) {
        mongoLogger.setLevel(Level.SEVERE);

        Document user = new Document()
                .append("userName", username)
                .append("login", login);

        try {

            appUsers.insertOne(user);
            return username;
        } catch (MongoWriteException e) {
            if (e.getError().getCategory().equals(ErrorCategory.DUPLICATE_KEY)) {
                System.out.println("Username already in use: " + username);
                return "Error";
            }
            throw e;
        }
    }

    public String validateLogin(String userLogin) {
        mongoLogger.setLevel(Level.SEVERE);

        Document user = appUsers.find(eq("login", userLogin)).first();

        if (user == null) {
            System.out.println("User not in database");
            return "Error";
        }

        return user.get("userName").toString();
    }

    public boolean doesPlayerExists(String userName){

        Document user = appUsers.find(eq("userName", userName)).first();
        if (user == null){
            return false;
        }
        return true;
    }
}
