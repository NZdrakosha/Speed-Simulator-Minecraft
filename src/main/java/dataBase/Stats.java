package dataBase;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Stats {
    final String nickname;
    final String id;
    long speed;
    public Player handle;

    public Stats(JsonObject json){
        id = json.getString("_id");
        nickname = json.getString("nickname");
        speed = json.getLong("speed");
        handle = Bukkit.getPlayerExact(nickname);
    }
    public Stats(String nickname){
        id = null;
        this.nickname = nickname;
        speed = 0L;
        handle = Bukkit.getPlayerExact(nickname);
    }
}
