package com.nr.vertx.clustered;

import com.hazelcast.config.Config;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class Starter {
    public static void main(String [] args) {
        Config config = new Config();

        config.setInstanceName("HazelcastService");
        config.getNetworkConfig().setPortAutoIncrement(true);
        config.getNetworkConfig().setPort(10555);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
        config.getNetworkConfig().getInterfaces().addInterface("127.0.0.1");
        config.getNetworkConfig().getInterfaces().setEnabled(true);
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1");

        final ClusterManager mgr = new HazelcastClusterManager(config);
        final VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.clusteredVertx(options, cluster -> {
            if (cluster.succeeded()) {
                cluster.result().deployVerticle(new Sender());
            }
        });
    }
}
