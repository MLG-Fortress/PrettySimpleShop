package com.robomwm.prettysimpleshop;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        config.addDefault("showOffItems", true);
        config.addDefault("useWorldWhitelist", false);
        config.addDefault("confirmTransactions", true);
        config.addDefault("useBuyPrompt", true);
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
        messageSection.addDefault("noSpace", "&6Warning: you might not have enough inventory space to store this item.");
        messageSection.addDefault("noShopSelected", "&eSelect a shop via left-clicking its chest.");
        messageSection.addDefault("shopModified", "&cTransaction canceled: Shop was modified. Please try again.");
        messageSection.addDefault("transactionCanceled", "&cTransaction canceled.");
        messageSection.addDefault("transactionCompleted", "Transaction completed. Bought {0} {1} for {2}");
        messageSection.addDefault("transactionCompletedWindow", "Bought {0} {1} for {2}");
        messageSection.addDefault("transactionCompleted", "Bought {0} {1} for {2}");
        messageSection.addDefault("applyPrice", "&bOpen the shop to apply your shiny new price.");
        messageSection.addDefault("setPriceCanceled", "&c/setprice canceled: opened chest is not a shop. To make this chest a shop, rename it in an anvil with the name: shop");
        messageSection.addDefault("priceApplied", "Price updated to {0}");
        messageSection.addDefault("collectRevenue", "Collected {0} in sales from this shop");
        messageSection.addDefault("tooFar", "&cYou're too far away from this shop");
        messageSection.addDefault("noShopThere", "&cThis shop has been moved or destroyed");
        messageSection.addDefault("buyPrompt", "&dPrettySimpleShop: &rHow many {0} &rwould you like to buy?");
        messageSection.addDefault("TotalCost", "for a total cost of {0}");
        messageSection.addDefault("currentBalanceAndCost", "You have {0} and will have {1} after confirming.");
        messageSection.addDefault("shopCommand", "To create a shop, place a chest named: &oshop&r\n" +
                "Tip: You can use an anvil to rename a chest.\n" +
                "Use /setprice to a set a price.\n" +
                "Use /buy to buy from a shop.");
        messageSection.addDefault("Confirm", "&2[Confirm]");
        messageSection.addDefault("Cancel", "&4[Cancel]");

        tipSection = config.getConfigurationSection("tips");
        if (tipSection == null)
            tipSection = config.createSection("tips");
        tipSection.addDefault("saleInfo", "Hover for item details. Click to /buy");
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
                    instance.getLogger().warning("World " + worldName + " does not exist.");
                else
                    whitelistedWorlds.add(world);
            }
        }

        instance.saveConfig();
    }

    public boolean getBoolean(String key)
    {
        return config.getBoolean(key);
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
