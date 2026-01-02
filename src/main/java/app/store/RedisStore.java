package app.store;

import app.model.Student;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisStore {
    private final JedisPool pool;
    private final Gson gson;

    public RedisStore() {
        // Redis bağlantı havuzu (daha performanslı)
        this.pool = new JedisPool("localhost", 6379);
        this.gson = new Gson();
    }

    public void addStudent(Student student) {
        try (Jedis jedis = pool.getResource()) {
            String json = gson.toJson(student);
            jedis.set(student.getStudent_no(), json);
        }
    }

    public String getStudent(String studentNo) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(studentNo);
        }
    }
}