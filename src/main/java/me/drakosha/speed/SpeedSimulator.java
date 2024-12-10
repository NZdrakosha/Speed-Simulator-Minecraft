package me.drakosha.speed;

import player.EventPlayer;
import dataBase.MongoDB;
import io.vertx.core.Vertx;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import player.SpeedUp;


public final class SpeedSimulator extends JavaPlugin {
    private static SpeedSimulator instance;

    @Override
    public void onEnable() {
        SpeedUp.initLocationSphere();
        instance = this;
        MongoDB.init(Vertx.vertx());
        registerEvent();
    }
    public static SpeedSimulator getInstance(){
        return instance;
    }


    @Override
    public void onDisable() {
    }
    private void registerEvent(){
        Bukkit.getPluginManager().registerEvents(new EventPlayer(), this);
        Bukkit.getPluginManager().registerEvents(new SpeedUp(), this);
    }
}
