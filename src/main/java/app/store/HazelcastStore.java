package app.store;

import app.model.Student;
import com.google.gson.Gson;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HazelcastStore {
    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, String> map;
    private final Gson gson;

    public HazelcastStore() {
        // Hazelcast sunucusunu uygulamanın içinde başlatıyoruz
        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
        this.map = hazelcastInstance.getMap("students");
        this.gson = new Gson();
    }

    public void addStudent(Student student) {
        String json = gson.toJson(student);
        map.put(student.getStudent_no(), json);
    }

    public String getStudent(String studentNo) {
        return map.get(studentNo);
    }
}