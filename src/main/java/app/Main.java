package app;

import app.model.Student;
import app.store.HazelcastStore;
import app.store.MongoStore;
import app.store.RedisStore;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Main {
    private static RedisStore redisStore;
    private static HazelcastStore hazelcastStore;
    private static MongoStore mongoStore;

    public static void main(String[] args) throws IOException {
        System.out.println("");
        
        redisStore = new RedisStore();
        hazelcastStore = new HazelcastStore();
        mongoStore = new MongoStore();

        System.out.println("10.000 KAYIT");
        generateAndLoadData(10000);
        System.out.println("");

        // Sunucuyu 8080 portunda başlat
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Endpoint tanımları
        server.createContext("/nosql-lab-rd", new RedisHandler());
        server.createContext("/nosql-lab-hz", new HazelcastHandler());
        server.createContext("/nosql-lab-mon", new MongoHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(20));
        server.start();
        System.out.println("SUNUCU: http://localhost:8080");
    }

    private static void generateAndLoadData(int count) {
        String[] departments = {"Computer Eng.", "Turkish Folk Music", "Classical Turkish Music", "Industrial Eng."};
        String[] names = {"Munip Utandi", "Nagme Yarkin", "Aysun Gultekin", "Berkay Yilmaz", "Ali Veli"};
        Random random = new Random();
        long startId = 2025000001L;

        for (int i = 0; i < count; i++) {
            String id = String.valueOf(startId + i);
            String name = names[random.nextInt(names.length)];
            String dept = departments[random.nextInt(departments.length)];
            
            Student s = new Student(id, name, dept);
            
            // Hepsine aynı anda yaz
            redisStore.addStudent(s);
            hazelcastStore.addStudent(s);
            mongoStore.addStudent(s);
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        if (response == null) {
            String msg = "Kayit bulunamadi";
            exchange.sendResponseHeaders(404, msg.length());
            try (OutputStream os = exchange.getResponseBody()) { os.write(msg.getBytes()); }
        } else {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    // URL parse edici: /student_no=xxxxx kısmını ayıklar
    private static String extractId(String path) {
        if (path.contains("student_no=")) {
            return path.split("student_no=")[1];
        }
        return null;
    }

    static class RedisHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String id = extractId(exchange.getRequestURI().toString());
            sendResponse(exchange, id != null ? redisStore.getStudent(id) : null);
        }
    }

    static class HazelcastHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String id = extractId(exchange.getRequestURI().toString());
            sendResponse(exchange, id != null ? hazelcastStore.getStudent(id) : null);
        }
    }

    static class MongoHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String id = extractId(exchange.getRequestURI().toString());
            sendResponse(exchange, id != null ? mongoStore.getStudent(id) : null);
        }
    }
}