package com.robomwm.prettysimpleshop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import java.util.regex.Matcher;

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
    private Set<Material> shopBlocks = new HashSet<>();

    private Map<Player, String> lastSeenTip = new HashMap<>();

    public ConfigManager(JavaPlugin plugin)
    {
        instance = plugin;
        config = instance.getConfig();
        config.addDefault("showOffItems", true);
        config.addDefault("showItemDetailsInActionBar", true);
        config.addDefault("useWorldWhitelist", false);
        config.addDefault("confirmTransactions", true);
        config.addDefault("useBuyPrompt", true);
        config.addDefault("alwaysShowBuyPrompt", true);

        List<String> whitelist = new ArrayList<>();
        whitelist.add("mall");
        config.addDefault("worldWhitelist", whitelist);

        List<String> shopBlockList = new ArrayList<>();
        shopBlockList.add("CHEST");
        shopBlockList.add("TRAPPED_CHEST");
        for (Material material : ExtraTags.SHULKER_BOX.getMaterials())
            shopBlockList.add(material.name());
        config.addDefault("shopBlocks", shopBlockList);

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
        messageSection.addDefault("applyPrice", "&bOpen the shop to apply your shiny new price, or use /setprice again to cancel.");
        messageSection.addDefault("setPriceCanceled", "&c/setprice canceled");
        messageSection.addDefault("priceApplied", "Price updated to {0}");
        messageSection.addDefault("collectRevenue", "Collected {0} in sales from this shop");
        messageSection.addDefault("tooFar", "&cYou're too far away from this shop");
        messageSection.addDefault("noShopThere", "&cThis shop has been moved or destroyed");
        messageSection.addDefault("buyPrompt", "&dPrettySimpleShop: &rHow many {0} &rwould you like to buy?");
        messageSection.addDefault("TotalCost", "for a total cost of {0}");
        messageSection.addDefault("currentBalanceAndCost", "You have {0} and will have {1} after confirming.");
        messageSection.addDefault("shopCommand", "Selling:\nTo create a shop, put items of the same type in a chest, and use /setprice to set the price per item.\n" +
                "Make sure your shop is protected from access or destruction to prevent theft!\n" +
                "Buying:\n" +
                "Punch a shop to view the item. Hover over it in chat for item details.\n" +
                "Click the message, /buy, or double-punch a shop to buy from a shop\n");
        messageSection.addDefault("Confirm", "&2[Confirm]");
        messageSection.addDefault("Cancel", "&4[Cancel]");
        messageSection.addDefault("buyCommandForConfirmationBook", "/buy");

        tipSection = config.getConfigurationSection("tips");
        if (tipSection == null)
            tipSection = config.createSection("tips");
        tipSection.addDefault("saleInfo", "Hover for item details. Click to /buy");
        tipSection.addDefault("noStock", "If you are the owner, make sure there is only a single item type in the chest.");

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

        for (String blockName : config.getStringList("shopBlocks"))
        {
            Material material = Material.matchMaterial(blockName);
            if (material == null)
                instance.getLogger().warning(blockName + " is not a valid Material name.");
            else
                shopBlocks.add(material);
        }

        config.options().header("showOffItems spawns a display item above each shop.\n" +
                "confirmTransactions shows the buyer a preview the transaction via a book GUI before committing to the purchase.\n" +
                "showBuyPrompt prompts the buyer to input the quantity they wish to buy in chat, instead of requiring them to use the /buy command.\n" +
                "shopBlocks contains blocks you allow to be used as shops. Only blocks that are a Nameable Container can be used as a shop.\n" +
                "Valid Material names can be found here https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html\n" +
                "Block types that are Containers are listed as subinterfaces here (some are not Nameable, you can check by clicking the subinterface of interest) https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Container.html");

        //Spigot-4441
        //Basically, getting a block's/item's custom name will not return the reset color code.
        if (getString("shopName").contains(ChatColor.RESET.toString()))
            messageSection.set("shopName", messageSection.getString("shopName").replaceAll("(?i)&r", "&0"));
        if (getString("price").contains(ChatColor.RESET.toString()))
            messageSection.set("price", messageSection.getString("price").replaceAll("(?i)&r", "&0"));
        if (getString("sales").contains(ChatColor.RESET.toString()))
            messageSection.set("sales", messageSection.getString("sales").replaceAll("(?i)&r", "&0"));

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

    public boolean isShopBlock(Material material)
    {
        return shopBlocks.contains(material);
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
            stringToFill = stringToFill.replaceAll("\\{" + i + "}", Matcher.quoteReplacement(arg));
            i++;
        }
        return stringToFill;
    }
}
