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
    private boolean debug;
    private Map<String, String> messages = new HashMap<>();
    private Map<String, String> tips = new HashMap<>();
    private Set<World> whitelistedWorlds = new HashSet<>();

    private Map<Player, String> lastSeenTip = new HashMap<>();

    public ConfigManager(JavaPlugin plugin)
    {
        instance = plugin;
        config = instance.getConfig();
        config.addDefault("useWorldWhitelist", false);
        List<String> whitelist = new ArrayList<>();
        whitelist.add("mall");
        config.addDefault("worldWhitelist", whitelist);

        ConfigurationSection messageSection = config.getConfigurationSection("messages");
        if (messageSection == null)
            messageSection = config.createSection("messages");
        messageSection.addDefault("shopName", "shop");
        messageSection.addDefault("price", "Price:");
        messageSection.addDefault("sales", "Sales:");
        messageSection.addDefault("saleInfo", "{0} @ &e{1}&r. {2} available");

        ConfigurationSection tipSection = config.getConfigurationSection("tips");
        if (tipSection == null)
            tipSection = config.createSection("tips");
        tipSection.addDefault("saleInfo", "Hover for item details. /buy <quantity>");

        config.options().copyDefaults(true);
        instance.saveConfig();
        debug = config.getBoolean("debug", false);

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

        messages.put("shopName", messageSection.getString("shopName"));
        messages.put("price", messageSection.getString("price"));
        messages.put("sales", messageSection.getString("sales"));
        messages.put("saleInfo", messageSection.getString("saleInfo"));

        tips.put("saleInfo", tipSection.getString("saleInfo"));

        instance.saveConfig();
    }

    public boolean isDebug()
    {
        return debug;
    }

    public void sendTip(Player player, String key)
    {
        if (lastSeenTip.containsKey(player) && lastSeenTip.get(player).equals(key))
            return;
        lastSeenTip.put(player, key);
        String message = formatter(tips.get(key));
        if (message.isEmpty())
            return;
        player.sendMessage(message);
    }

    public String getString(String key, String... formatees)
    {
        return formatter(messages.get(key), formatees);
    }

    public String getString(String key)
    {
        return formatter(messages.get(key));
    }

    public boolean isWhitelistedWorld(World world) //may want to consider returning unmodifiable collection
    {
        return whitelistedWorlds.isEmpty() || whitelistedWorlds.contains(world);
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

    //String.format or whatever it was does weird stuff and doesn't like certain characters in the string when parsing {0} stuff so yea...

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
