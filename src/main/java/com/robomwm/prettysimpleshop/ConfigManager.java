package com.robomwm.prettysimpleshop;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
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
    private ConfigurationSection messageSection;
    private ConfigurationSection tipSection;
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

         messageSection = config.getConfigurationSection("messages");
        if (messageSection == null)
            messageSection = config.createSection("messages");
        messageSection.addDefault("shopName", "shop");
        messageSection.addDefault("price", "Price:");
        messageSection.addDefault("sales", "Sales:");
        messageSection.addDefault("saleInfo", "{0} @ &e{1}&r. {2} available");
        messageSection.addDefault("noPrice", "&cThis shop is not open for sale yet! &6If you are the owner, use /setprice <price> to open this shop!");
        messageSection.addDefault("noStock", "&cThis shop is out of stock!");
        messageSection.addDefault("noMoney", "&cTransaction canceled: Insufficient /money. Try again with a smaller quantity?");
        messageSection.addDefault("noSpace", "&cTransaction canceled: Insufficient inventory space. Free up some inventory slots or try again with a smaller quantity.");
        messageSection.addDefault("noShopSelected", "&eSelect a shop via left-clicking its chest.");
        messageSection.addDefault("shopModified", "&cTransaction canceled: Shop was modified. Please try again.");
        messageSection.addDefault("transactionCompleted", "Transaction completed. Bought {0} {1} for {2}");
        messageSection.addDefault("applyPrice", "&bOpen the shop to apply your shiny new price.");
        messageSection.addDefault("priceApplied", "Price updated to {0}");
        messageSection.addDefault("collectRevenue", "Collected {0} in sales from this shop");

        tipSection = config.getConfigurationSection("tips");
        if (tipSection == null)
            tipSection = config.createSection("tips");
        tipSection.addDefault("saleInfo", "Hover for item details. /buy <quantity>");
        tipSection.addDefault("noStock", "If you are the owner, take note that shops must only contain the same item in its inventory.");

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
        String message = formatter(tipSection.getString(key));
        if (message.isEmpty())
            return;
        player.sendMessage(message);
    }

    public void sendMessage(CommandSender player, String key)
    {
        String message = getString(key);
        if (!message.isEmpty())
            player.sendMessage(message);
    }

    public void sendMessage(CommandSender player, String key, String... formatees)
    {
        String message = getString(key, formatees);
        if (!message.isEmpty())
            player.sendMessage(message);
    }

    public String getString(String key, String... formatees)
    {
        return formatter(messageSection.getString(key), formatees);
    }

    public String getString(String key)
    {
        return formatter(messageSection.getString(key));
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
