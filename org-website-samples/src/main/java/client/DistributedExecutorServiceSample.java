package client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import java.io.Serializable;

public class DistributedExecutorServiceSample {
    public static void main(String[] args) {
        // Enable Code Deployment from this Client classpath to the Cluster Members classpath
        // User Code Deployment needs to be enabled on the Cluster Members as well.
        ClientConfig config = new ClientConfig();
        ClientUserCodeDeploymentConfig userCodeDeploymentConfig = config.getUserCodeDeploymentConfig();
        userCodeDeploymentConfig.setEnabled(true);
        userCodeDeploymentConfig.addClass(DistributedExecutorServiceSample.MessagePrinter.class);
        // Start the Hazelcast Client and connect to an already running Hazelcast Cluster on 127.0.0.1
        HazelcastInstance hz1 = HazelcastClient.newHazelcastClient(config);
        // Get the Distributed Executor Service
        IExecutorService ex = hz1.getExecutorService("my-distributed-executor");
        // Submit the MessagePrinter Runnable to a random Hazelcast Cluster Member
        ex.submit(new MessagePrinter("message to any node"));
        // Get the first Hazelcast Cluster Member
        Member firstMember = hz1.getCluster().getMembers().iterator().next();
        // Submit the MessagePrinter Runnable to the first Hazelcast Cluster Member
        ex.executeOnMember(new MessagePrinter("message to very first member of the cluster"), firstMember);
        // Submit the MessagePrinter Runnable to all Hazelcast Cluster Members
        ex.executeOnAllMembers(new MessagePrinter("message to all members in the cluster"));
        // Submit the MessagePrinter Runnable to the Hazelcast Cluster Member owning the key called "key"
        ex.executeOnKeyOwner(new MessagePrinter("message to the member that owns the following key"), "key");
        // Shutdown this Hazelcast Cluster Member
        hz1.shutdown();
    }

    // This MessagePrinter class must be available on the Hazelcast Cluster Members Classpath
    static class MessagePrinter implements Runnable, Serializable {
        final String message;

        MessagePrinter(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            System.out.println(message);
        }
    }
}