package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.SqlPredicate;

import java.io.Serializable;
import java.util.Collection;

public class QuerySample {
    public static void main(String[] args) {
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz = HazelcastClient.newHazelcastClient();
        // Get a Distributed Map called "users"
        IMap<String, User> users = hz.getMap("users");
        // Add some users to the Distributed Map
        generateUsers(users);
        // Create a Predicate from a String (a SQL like Where clause)
        Predicate sqlQuery = new SqlPredicate("active AND age BETWEEN 18 AND 21)");
        // Creating the same Predicate as above but with a builder
        Predicate criteriaQuery = Predicates.and(
                Predicates.equal("active", true),
                Predicates.between("age", 18, 21)
        );
        // Get result collections using the two different Predicates
        Collection<User> result1 = users.values(sqlQuery);
        Collection<User> result2 = users.values(criteriaQuery);
        // Print out the results
        System.out.println(result1);
        System.out.println(result2);
        // Shutdown the Hazelcast Cluster Member
        hz.shutdown();
    }

    private static void generateUsers(IMap<String, User> users) {
        users.put("Rod", new User("Rod",19,true));
        users.put("Jane", new User("Jane",20,true));
        users.put("Freddy", new User("Freddy",23,true));
    }

    /**
     * The User class that is a value object in the "users" Distributed Map
     * This Class must be available on the Classpath of the Hazelcast Cluster Members
     */
    public static class User implements Serializable {
        public User(String username, int age, boolean active) {
            this.username = username;
            this.age = age;
            this.active = active;
        }

        String username;
        int age;
        boolean active;

        @Override
        public String toString() {
            return "User{" +
                    "username='" + username + '\'' +
                    ", age=" + age +
                    ", active=" + active +
                    '}';
        }
    }
}
