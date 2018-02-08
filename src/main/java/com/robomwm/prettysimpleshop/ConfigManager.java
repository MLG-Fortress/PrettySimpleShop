package com.robomwm.prettysimpleshop;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2/16/2017.
 *
 * @author RoboMWM
 */
public class ConfigManager
{
    private JavaPlugin instance;
    private FileConfiguration config;
    private Map<String, String> messages = new HashMap<>();
    private Set<World> whitelistedWorlds = new HashSet<>();


    public ConfigManager(JavaPlugin plugin)
    {
        instance = plugin;
        config = instance.getConfig();
        config.addDefault("useWorldWhitelist", true);
        List<String> whitelist = new ArrayList<>();
        whitelist.add("mall");
        config.addDefault("worldWhitelist", whitelist);

        config.options().copyDefaults(true);
        instance.saveConfig();

        if (config.getBoolean("useWorldWhitelist"))
        {
            for (String worldName : config.getStringList("worldWhitelist"))
            {
                World world = instance.getServer().getWorld(worldName);
                if (world == null)
                    instance.getLogger().warning("World " + worldName + " does not exist. We advise removing this from the worldWhitelist in the config for DeathSpectating");
                else
                    whitelistedWorlds.add(world);
            }
        }

        //Messages
        ConfigurationSection messageSection = config.getConfigurationSection("messages");
        if (messageSection == null)
            messageSection = config.createSection("messages");

        messages.put("shopName", messageSection.getString("shopName", "shop"));
        messages.put("price", messageSection.getString("price", "Price:"));
        messages.put("sales", messageSection.getString("sales", "Sales:"));

        instance.saveConfig();
    }

    public String getString(String key)
    {
        return formatter(messages.get("key"));
    }

    public Set<World> getWhitelistedWorlds() //may want to consider returning unmodifiable collection
    {
        return whitelistedWorlds;
    }

    /*Utility methods*/
    private String formatter(String stringToFormat, String... formatees)
    {
        return formatter(argParser(stringToFormat, formatees));
    }

    private String formatter(String stringToFormat)
    {
        return ChatColor.translateAlternateColorCodes('&', stringToFormat);
    }

    /*Private methods, for now*/

    private String argParser(String stringToFill, String... args)
    {
        int i = 0;
        for (String arg : args)
        {
            stringToFill = stringToFill.replaceAll("\\{" + i + "}", arg);
            i++;
        }
        return stringToFill;
    }
}
