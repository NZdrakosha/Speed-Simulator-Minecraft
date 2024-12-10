package dataBase;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;

public class MongoDB {
    private static final String DATABASE_NAME = "playerInfoDB";
    private static final String COLLECTION_NAME = "SPEED";

    private static MongoClient database;

    public static void init(Vertx vertx) {
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017/")
                .put("db_name", DATABASE_NAME);

        database = MongoClient.createShared(vertx, mongoConfig);
    }
    public static void loadPlayerInfo(String nickname, Handler<Stats> resultHandler){
        database.findOne(COLLECTION_NAME, new JsonObject().put("nickname", nickname), null, ar ->{
            if (ar.succeeded()){
                if (ar.result() != null){
                    resultHandler.handle(new Stats(ar.result()));
                }else {
                    resultHandler.handle(null);
                }
            }else {
                ar.cause().printStackTrace(System.err);
                resultHandler.handle(null);
            }
        });
    }
    public static void savePlayer(Stats stats) {
        JsonObject playerJson = new JsonObject().
                put("nickname", stats.getNickname()).
                put("speed", stats.getSpeed());

        if (stats.getId() == null) {
            String id = new ObjectId().toHexString();
            playerJson.put("_id", id);
            database.insert("SPEED", playerJson, ar -> {
                if (ar.failed()) {
                    ar.cause().printStackTrace(System.err);
                }
            });
        } else {
            playerJson.put("_id", stats.getId());
            database.replaceDocuments("players", new JsonObject().put("_id", stats.getId()), playerJson, ar -> {
                if (ar.failed()) {
                    ar.cause().printStackTrace(System.err);
                }
            });
        }
    }
    public static void updatePlayerStats (String nickname, String stat, Long increment){
        JsonObject query = new JsonObject().put("nickname", nickname);
        JsonObject update = new JsonObject().put("$inc", new JsonObject().put(stat, increment));

        database.updateCollection(COLLECTION_NAME, query, update, ar ->{
            if (ar.succeeded()){
                if (ar.result().getDocModified() > 0){
                    Bukkit.getLogger().info("Данные обновленны");
                }else {
                    Bukkit.getLogger().info("Игрок не найден");
                }
            }else {
                ar.cause().printStackTrace(System.err);
            }
        });
    }
}
