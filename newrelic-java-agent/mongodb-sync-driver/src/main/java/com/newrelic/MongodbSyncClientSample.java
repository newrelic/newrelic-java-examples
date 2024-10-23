package com.newrelic;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import com.newrelic.api.agent.Trace;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongodbSyncClientSample {
    public static void main(String[] args) {
        MongodbSyncClientSample clientSample = new MongodbSyncClientSample();
        clientSample.start();

    }

    @Trace(dispatcher = true)
    private void start() {
        String uri = "mongodb://localhost:27017";
        MongoCollection<Document> collection;
        MongoClient mongoClient;
        mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("sample");
        collection = database.getCollection("video_game_characters");

        addToCollection(collection);
        fetchFromCollection(collection);
        dropCollection(collection);
        mongoClient.close();
    }

    @Trace
    private void addToCollection(MongoCollection<Document> collection) {
        List<Document> documents = new ArrayList<>();
        documents.add(new Document().append("name", "Claptrap").append("game", "Borderlands"));
        documents.add(new Document().append("name", "Handsome Jack").append("game", "Borderlands"));
        documents.add(new Document().append("name", "Lilith").append("game", "Borderlands"));
        documents.add(new Document().append("name", "Roland").append("game", "Borderlands"));
        documents.add(new Document().append("name", "Brick").append("game", "Borderlands"));

        for (Document d : documents) {
            InsertOneResult result = collection.insertOne(d);
            System.out.println("Inserted id: " + result.getInsertedId());
        }
    }

    @Trace
    private void fetchFromCollection(MongoCollection<Document> collection) {
        FindIterable<Document> docs =  collection.find();
        for (Document d : docs) {
            System.out.println(d.toJson());
            Bson deleteQuery = eq("name", d.get("name"));
            collection.findOneAndDelete(deleteQuery);
            System.out.println("Deleted");
        }
    }

    @Trace
    private void dropCollection(MongoCollection<Document> collection) {
        collection.drop();
    }
}