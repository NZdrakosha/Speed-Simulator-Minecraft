package player;

import dataBase.MongoDB;
import dataBase.Stats;
import me.drakosha.speed.SpeedSimulator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;



public class EventPlayer implements Listener {
    public static Map<String, Boolean> activeTask = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if (player != null){
            Bukkit.getScheduler().runTaskAsynchronously(SpeedSimulator.getInstance(), () -> {
                MongoDB.loadPlayerInfo(player.getName(), result -> {
                    if (result != null) {
                        SpeedUp.speedUpForDB.put(player.getName(), 0L);
                        player.sendMessage("Ваша скорость = " + result.getSpeed());
                        SpeedUp.totalSpeed.put(player.getName(), result.getSpeed());
                        activeTask.put(player.getName(), true);
                        player.setLevel((int) result.getSpeed());
                        SpeedUp.addMoveSpeed(player);
                    } else {
                        Stats newPlayer = new Stats(player.getName());
                        MongoDB.savePlayer(newPlayer);
                        Bukkit.getLogger().info("Игрока не было в базе данных. Новый профиль создан ");
                        player.sendMessage("Ваша скорость = 0");
                        SpeedUp.speedUpForDB.put(player.getName(), 0L);
                        SpeedUp.totalSpeed.put(player.getName(), 0L);
                        activeTask.put(player.getName(), true);
                        SpeedUp.addMoveSpeed(player);
                    }
                });
            });        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if (player != null){
            activeTask.put(player.getName(), false);
            Bukkit.getScheduler().runTaskAsynchronously(SpeedSimulator.getInstance(), () -> {
                long speed = SpeedUp.speedUpForDB.get(player.getName());
                MongoDB.updatePlayerStats(player.getName(), "speed", speed);
            });
        }
    }
}
