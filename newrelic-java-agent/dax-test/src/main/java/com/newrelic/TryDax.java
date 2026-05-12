package com.newrelic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.newrelic.api.agent.Trace;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.dax.ClusterDaxAsyncClient;
import software.amazon.dax.ClusterDaxClient;
import software.amazon.dax.Configuration;

public class TryDax {
    private static final int SORT_KEY_MAX = 4;
    private static final int PARTITION_KEY_MAX = 20;

    public static void main(String[] args) throws Exception {
        // Async clients
        DynamoDbAsyncClient ddbAsyncClient = DynamoDbAsyncClient.builder().build();
        DynamoDbAsyncClient daxAsyncClient = null;

        // Sync clients
        DynamoDbClient ddbSyncClient = DynamoDbClient.builder().build();
        DynamoDbClient daxSyncClient = null;

        if (args.length >= 1) {
            String daxUrl = args[0]; // e.g. dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com
            System.out.println("Using DAX Url: " + daxUrl);

            daxAsyncClient = ClusterDaxAsyncClient.builder()
                    .overrideConfiguration(Configuration.builder()
                            .url(daxUrl)
                            .build())
                    .build();

            daxSyncClient = ClusterDaxClient.builder()
                    .overrideConfiguration(Configuration.builder()
                            .url(daxUrl)
                            .build())
                    .build();
        }

        String tableName = "TryDaxTable";

        System.out.println("Creating table...");
        createTable(tableName, ddbAsyncClient);

        System.out.println("Populating table...");
        writeData(tableName, ddbAsyncClient, PARTITION_KEY_MAX, SORT_KEY_MAX);

        DynamoDbAsyncClient testAsyncClient = (daxAsyncClient != null) ? daxAsyncClient : ddbAsyncClient;
        DynamoDbClient testSyncClient = (daxSyncClient != null) ? daxSyncClient : ddbSyncClient;

        System.out.println("Running GetItem and Query tests...");
        System.out.println("First iteration of each test will result in cache misses");
        System.out.println("Next iterations are cache hits\n");

        System.out.println("=== ASYNC CLIENT TESTS ===");
        doAllTheThingsAsync(testAsyncClient, tableName);

        System.out.println("\n=== SYNC CLIENT TESTS ===");
        doAllTheThingsSync(testSyncClient, tableName);

        System.out.println("Deleting table...");
        deleteTable(tableName, ddbAsyncClient);
    }

    @Trace(dispatcher = true)
    private static void doAllTheThingsAsync(DynamoDbAsyncClient testClient, String tableName) {
        scanTestAsync(tableName, testClient, 1);
        getItemTestAsync(tableName, testClient, PARTITION_KEY_MAX, SORT_KEY_MAX, 3);
        queryTestAsync(tableName, testClient, PARTITION_KEY_MAX, 2, 4, 3);
        batchGetItemTestAsync(tableName, testClient, PARTITION_KEY_MAX, SORT_KEY_MAX, 1);
    }

    @Trace(dispatcher = true)
    private static void doAllTheThingsSync(DynamoDbClient testClient, String tableName) {
        scanTestSync(tableName, testClient, 1);
        getItemTestSync(tableName, testClient, PARTITION_KEY_MAX, SORT_KEY_MAX, 3);
        queryTestSync(tableName, testClient, PARTITION_KEY_MAX, 2, 4, 3);
        batchGetItemTestSync(tableName, testClient, PARTITION_KEY_MAX, SORT_KEY_MAX, 1);
    }

    private static void createTable(String tableName, DynamoDbAsyncClient client) {
        try {
            System.out.println("Attempting to create table; please wait...");

            client.createTable(CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(KeySchemaElement.builder()
                            .keyType(KeyType.HASH)
                            .attributeName("pk")
                            .build(), KeySchemaElement.builder()
                            .keyType(KeyType.RANGE)
                            .attributeName("sk")
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("pk")
                            .attributeType(ScalarAttributeType.N)
                            .build(), AttributeDefinition.builder()
                            .attributeName("sk")
                            .attributeType(ScalarAttributeType.N)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build()).get();
            client.waiter().waitUntilTableExists(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build()).get();
            System.out.println("Successfully created table.");

        } catch (Exception e) {
            System.err.println("Unable to create table: ");
            e.printStackTrace();
        }
    }

    private static void deleteTable(String tableName, DynamoDbAsyncClient client) {
        try {
            System.out.println("\nAttempting to delete table; please wait...");
            client.deleteTable(DeleteTableRequest.builder()
                    .tableName(tableName)
                    .build()).get();
            client.waiter().waitUntilTableNotExists(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build()).get();
            System.out.println("Successfully deleted table.");

        } catch (Exception e) {
            System.err.println("Unable to delete table: ");
            e.printStackTrace();
        }
    }

    private static void writeData(String tableName, DynamoDbAsyncClient client, int pkmax, int skmax) {
        System.out.println("Writing data to the table...");

        int stringSize = 1000;
        StringBuilder sb = new StringBuilder(stringSize);
        for (int i = 0; i < stringSize; i++) {
            sb.append('X');
        }
        String someData = sb.toString();

        try {
            for (int ipk = 1; ipk <= pkmax; ipk++) {
                System.out.println(("Writing " + skmax + " items for partition key: " + ipk));
                for (int isk = 1; isk <= skmax; isk++) {
                    client.putItem(PutItemRequest.builder()
                            .tableName(tableName)
                            .item(Map.of("pk", attr(ipk), "sk", attr(isk), "someData", attr(someData)))
                            .build()).get();
                }
            }
        } catch (Exception e) {
            System.err.println("Unable to write item:");
            e.printStackTrace();
        }
    }

    private static AttributeValue attr(int n) {
        return AttributeValue.builder().n(String.valueOf(n)).build();
    }

    private static AttributeValue attr(String s) {
        return AttributeValue.builder().s(s).build();
    }

    @Trace
    private static void getItemTestAsync(String tableName, DynamoDbAsyncClient client, int pk, int sk, int iterations) {
        long startTime, endTime;
        System.out.println("GetItem ASYNC test - partition key 1-" + pk + " and sort keys 1-" + sk);

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            try {
                for (int ipk = 1; ipk <= pk; ipk++) {
                    for (int isk = 1; isk <= sk; isk++) {
                        client.getItem(GetItemRequest.builder()
                                .tableName(tableName)
                                .key(Map.of("pk", attr(ipk), "sk", attr(isk)))
                                .build()).get();
                    }
                }
            } catch (Exception e) {
                System.err.println("Unable to get item:");
                e.printStackTrace();
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, pk * sk);
        }
    }

    @Trace
    private static void getItemTestSync(String tableName, DynamoDbClient client, int pk, int sk, int iterations) {
        long startTime, endTime;
        System.out.println("GetItem SYNC test - partition key 1-" + pk + " and sort keys 1-" + sk);

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            try {
                for (int ipk = 1; ipk <= pk; ipk++) {
                    for (int isk = 1; isk <= sk; isk++) {
                        client.getItem(GetItemRequest.builder()
                                .tableName(tableName)
                                .key(Map.of("pk", attr(ipk), "sk", attr(isk)))
                                .build());
                    }
                }
            } catch (Exception e) {
                System.err.println("Unable to get item:");
                e.printStackTrace();
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, pk * sk);
        }
    }

    @Trace
    private static void queryTestAsync(String tableName, DynamoDbAsyncClient client, int pk, int sk1, int sk2, int iterations) {
        long startTime, endTime;
        System.out.println("Query ASYNC test - partition key 1-" + pk + " and sort keys between " + sk1 + " and " + sk2);

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            for (int ipk = 1; ipk <= pk; ipk++) {
                try {
                    client.queryPaginator(QueryRequest.builder()
                            .tableName(tableName)
                            .keyConditionExpression("pk = :pkval and sk between :skval1 and :skval2")
                            .expressionAttributeValues(Map.of(":pkval", attr(ipk), ":skval1", attr(sk1), ":skval2", attr(sk2)))
                            .build()).items().subscribe((item) -> {
                    }).get();
                } catch (Exception e) {
                    System.err.println("Unable to query table:");
                    e.printStackTrace();
                }
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, pk);
        }
    }

    @Trace
    private static void queryTestSync(String tableName, DynamoDbClient client, int pk, int sk1, int sk2, int iterations) {
        long startTime, endTime;
        System.out.println("Query SYNC test - partition key 1-" + pk + " and sort keys between " + sk1 + " and " + sk2);

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            for (int ipk = 1; ipk <= pk; ipk++) {
                try {
                    client.queryPaginator(QueryRequest.builder()
                            .tableName(tableName)
                            .keyConditionExpression("pk = :pkval and sk between :skval1 and :skval2")
                            .expressionAttributeValues(Map.of(":pkval", attr(ipk), ":skval1", attr(sk1), ":skval2", attr(sk2)))
                            .build()).items().forEach(item -> {});
                } catch (Exception e) {
                    System.err.println("Unable to query table:");
                    e.printStackTrace();
                }
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, pk);
        }
    }

    @Trace
    private static void scanTestAsync(String tableName, DynamoDbAsyncClient client, int iterations) {
        long startTime, endTime;
        System.out.println("Scan ASYNC test");

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            try {
                client.scanPaginator(ScanRequest.builder()
                        .tableName(tableName)
                        .build()).items().subscribe(item -> {}).get();
            } catch (Exception e) {
                System.err.println("Unable to scan table:");
                e.printStackTrace();
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, 1);
        }
    }

    @Trace
    private static void scanTestSync(String tableName, DynamoDbClient client, int iterations) {
        long startTime, endTime;
        System.out.println("Scan SYNC test");

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            try {
                client.scanPaginator(ScanRequest.builder()
                        .tableName(tableName)
                        .build()).items().forEach(item -> {});
            } catch (Exception e) {
                System.err.println("Unable to scan table:");
                e.printStackTrace();
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, 1);
        }
    }

    @Trace
    private static void batchGetItemTestAsync(String tableName, DynamoDbAsyncClient client, int pk, int sk, int iterations) {
        long startTime, endTime;
        System.out.println("BatchGetItem ASYNC test - partition key 1-" + pk + " and sort keys 1-" + sk);

        List<Map<String, AttributeValue>> keys = buildKeysList(pk, sk);

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            try {
                client.batchGetItem(BatchGetItemRequest.builder()
                        .requestItems(Map.of(tableName, KeysAndAttributes.builder()
                                .keys(keys)
                                .build()))
                        .build()).get();
            } catch (Exception e) {
                System.err.println("Unable to batch get items:");
                e.printStackTrace();
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, pk * sk);
        }
    }

    @Trace
    private static void batchGetItemTestSync(String tableName, DynamoDbClient client, int pk, int sk, int iterations) {
        long startTime, endTime;
        System.out.println("BatchGetItem SYNC test - partition key 1-" + pk + " and sort keys 1-" + sk);

        List<Map<String, AttributeValue>> keys = buildKeysList(pk, sk);

        for (int i = 0; i < iterations; i++) {
            startTime = System.nanoTime();
            try {
                client.batchGetItem(BatchGetItemRequest.builder()
                        .requestItems(Map.of(tableName, KeysAndAttributes.builder()
                                .keys(keys)
                                .build()))
                        .build());
            } catch (Exception e) {
                System.err.println("Unable to batch get items:");
                e.printStackTrace();
            }
            endTime = System.nanoTime();
            printTime(startTime, endTime, pk * sk);
        }
    }

    private static List<Map<String, AttributeValue>> buildKeysList(int pk, int sk) {
        List<Map<String, AttributeValue>> keys = new ArrayList<>();
        for (int ipk = 1; ipk <= pk; ipk++) {
            for (int isk = 1; isk <= sk; isk++) {
                keys.add(Map.of("pk", attr(ipk), "sk", attr(isk)));
            }
        }
        return keys;
    }

    private static void printTime(long startTime, long endTime, int iterations) {
        System.out.format("\tTotal time: %.3f ms - ", (endTime - startTime) / (1000000.0));
        System.out.format("Avg time: %.3f ms\n", (endTime - startTime) / (iterations * 1000000.0));
    }
}