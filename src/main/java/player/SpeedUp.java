package player;

import me.drakosha.speed.SpeedSimulator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SpeedUp implements Listener {
    public static Map<String, Long> speedUpForDB = new HashMap<>();
    public static Map<String, Long> totalSpeed = new HashMap<>();
    private static Map<String, Location> locationSphere = new HashMap<>();
    public static Map<String, Boolean> activeSphere = new ConcurrentHashMap<>();

    public static void initLocationSphere(){
        Location locationGreenSphere = new Location(Bukkit.getWorlds().get(0), -29, 199.500, 401.999);
        Location locationRedSphere = new Location(Bukkit.getWorlds().get(0), -29, 199.500, 410);
        Location locationBlueSphere = new Location(Bukkit.getWorlds().get(0), -29, 199.500, 406);
        Location locationYellowSphere = new Location(Bukkit.getWorlds().get(0), -29, 199.500, 414);

        locationSphere.put("green", locationGreenSphere);
        locationSphere.put("blue", locationBlueSphere);
        locationSphere.put("yellow", locationYellowSphere);
        locationSphere.put("red", locationRedSphere);

        activeSphere.put("red", true);
        activeSphere.put("blue", true);
        activeSphere.put("yellow", true);
        activeSphere.put("green", true);
    }

    private final Map<String, Location> lastPositions = new HashMap<>();

    private final Map<String, Double> accumulatedDistances = new HashMap<>();

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location currentLocation = player.getLocation();

        Location lastLocation = lastPositions.get(player.getName());

        if (lastLocation != null) {
            double dx = currentLocation.getX() - lastLocation.getX();
            double dz = currentLocation.getZ() - lastLocation.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            double totalDistance = accumulatedDistances.getOrDefault(player.getName(), 0.0) + distance;

            while (totalDistance >= 1.0) {
                long speedUp = speedUpForDB.get(player.getName()) + 1L;
                speedUpForDB.put(player.getName(), speedUp);

                long speedTotal = totalSpeed.get(player.getName()) + 1L;
                totalSpeed.put(player.getName(), speedTotal);

                totalDistance -= 1;

            }
            accumulatedDistances.put(player.getName(), totalDistance);
        }
        lastPositions.put(player.getName(), currentLocation);
    }

    public static void speedAddMove(Player player) {

        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attribute != null) {
            if (totalSpeed.get(player.getName()) == null) {
                totalSpeed.put(player.getName(), 0L);
            }
            long speed = totalSpeed.get(player.getName());
            double newSpeed = checkAndGiveSpeed(speed);

            attribute.setBaseValue(newSpeed);
        }
    }

    public static void addMoveSpeed(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setLevel(totalSpeed.get(player.getName()).intValue());
                    speedAddMove(player);
                    playerOnSphere(player);
            }

        }.runTaskTimer(SpeedSimulator.getInstance(), 0L, 1L);
    }

    private static double checkAndGiveSpeed(long totalSpeed){
        double baseSpeed = 0.01;
        double speedFlag = 40;
        double growthFactor = 0.7;
        if (totalSpeed <= 1000){
            growthFactor = 0.7;
        }else if (totalSpeed <= 3000){
            growthFactor = 0.6;
        }else if (totalSpeed <= 6000){
            growthFactor = 0.5;
        } else if (totalSpeed <= 10000){
            growthFactor = 0.4;
        }
        double calculatedSpeed = baseSpeed;
        while (totalSpeed > speedFlag) {
            calculatedSpeed += baseSpeed;
            speedFlag += speedFlag * growthFactor;
        }
        return calculatedSpeed;
    }

    public static void playerOnSphere(Player player){
        if (checkPlayerLocation(locationSphere.get("green"), "green")) {
           toggleSpeedAndGiveSpeed("green", 40, player);

        }else if (checkPlayerLocation(locationSphere.get("red"), "red")){
            toggleSpeedAndGiveSpeed("red", 80, player);

        }else if (checkPlayerLocation(locationSphere.get("yellow"), "yellow")){
            player.sendMessage("+1 опыт");
            activeSphere.put("yellow", false);
            if (!activeSphere.get("yellow")) {
                ClientSocket.sendClintPacket("yellow");
                timerSphere("yellow");
            }

        }else if (checkPlayerLocation(locationSphere.get("blue"), "blue")){
           toggleSpeedAndGiveSpeed("blue", 100, player);
        }
    }

    private static void toggleSpeedAndGiveSpeed(String color, int speedAdd, Player player){
        long totalSpeedAddForDB = speedUpForDB.get(player.getName());
        long totalSpeedAll = totalSpeed.get(player.getName());

        activeSphere.put(color, false);
        boolean value = activeSphere.get(color);
        if (!value){
            ClientSocket.sendClintPacket(color);

            totalSpeedAddForDB += speedAdd;
            speedUpForDB.put(player.getName(), totalSpeedAddForDB);

            totalSpeedAll += speedAdd;
            totalSpeed.put(player.getName(), totalSpeedAll);

            timerSphere(color);

            player.sendMessage("Вы получили " + speedAdd + " очков скорости за поднятие " + color + " сферы");
        }
    }
    public static void timerSphere(String color){

            new BukkitRunnable() {
                @Override
                public void run() {
                    activeSphere.put(color, true);
                    if (activeSphere.get(color)) {
                        ClientSocket.sendClintPacket(color);
                    }
                }
            }.runTaskLater(SpeedSimulator.getInstance(), 5 * 20);
    }
    private static boolean checkPlayerLocation(Location center, String color) {
            if (!activeSphere.getOrDefault(color, true)) return false;


        World world = center.getWorld();
        double radius = 0.5;
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof Player) {
                return true;
            }
        }
        return false;
    }

}
