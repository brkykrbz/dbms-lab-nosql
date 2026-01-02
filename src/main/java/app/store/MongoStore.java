package app.store;

import app.model.Student;
import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;

public class MongoStore {
    private final MongoCollection<Document> collection;

    public MongoStore() {
        // MongoDB yerel bağlantısı
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("nosql_lab");
        this.collection = database.getCollection("students");
    }

    public void addStudent(Student student) {
        Document doc = new Document("student_no", student.getStudent_no())
                .append("name", student.getName())
                .append("department", student.getDepartment());
        collection.insertOne(doc);
    }

    public String getStudent(String studentNo) {
        Document doc = collection.find(eq("student_no", studentNo)).first();
        if (doc != null) {
            doc.remove("_id"); // MongoDB'nin kendi ID'sini gizle
            return doc.toJson();
        }
        return null;
    }
}