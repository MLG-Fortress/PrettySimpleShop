package com.robomwm.prettysimpleshop;

import com.robomwm.prettysimpleshop.command.BuyCommand;
import com.robomwm.prettysimpleshop.command.HelpCommand;
import com.robomwm.prettysimpleshop.command.PriceCommand;
import com.robomwm.prettysimpleshop.feature.BuyConversation;
import com.robomwm.prettysimpleshop.feature.ShowoffItem;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopListener;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.Callable;

/**
 * Created on 2/4/2018.
 *
 * @author RoboMWM
 */
public class PrettySimpleShop extends JavaPlugin
{
    private Economy economy;
    private static boolean debug;
    private ShopAPI shopAPI;
    private ConfigManager config;
    private ShowoffItem showoffItem = null;

    public void onEnable()
    {
        config = new ConfigManager(this);
        debug = config.isDebug();
        shopAPI = new ShopAPI(config.getString("shopName"), config.getString("price"), config.getString("sales"));
        try
        {
            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SimplePie("bukkit_implementation", new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return getServer().getVersion().split("-")[1];
                }
            }));

            for (final String key : getConfig().getKeys(false))
            {
                if (!getConfig().isBoolean(key) && !getConfig().isInt(key) && !getConfig().isString(key))
                    continue;
                metrics.addCustomChart(new Metrics.SimplePie(key.toLowerCase(), new Callable<String>()
                {
                    @Override
                    public String call() throws Exception
                    {
                        return getConfig().getString(key);
                    }
                }));
            }
        }
        catch (Throwable ignored) {}

        PrettySimpleShop plugin = this;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                economy = getEconomy();
                if (economy == null)
                {
                    getLogger().severe("No economy plugin was found. Disabling.");
                    getServer().getPluginManager().disablePlugin(plugin);
                    return;
                }
                ShopListener shopListener = new ShopListener(plugin, shopAPI, economy, config);
                if (config.getBoolean("showOffItems"))
                    showoffItem = new ShowoffItem(plugin, shopAPI);
                getCommand("shop").setExecutor(new HelpCommand(plugin));
                getCommand("setprice").setExecutor(new PriceCommand(shopListener));
                getCommand("buy").setExecutor(new BuyCommand(plugin, shopListener, economy));
                if (config.getBoolean("useBuyPrompt"))
                    new BuyConversation(plugin);
            }
        }.runTask(this);
    }

    public void onDisable()
    {
        if (showoffItem != null)
            showoffItem.despawnAll();
    }

    public static String getItemName(ItemStack item)
    {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return item.getItemMeta().getDisplayName();
        try
        {
            return item.getI18NDisplayName();
        }
        catch (Throwable rock)
        {
            return item.getType().name().toLowerCase().replaceAll("_", " ");
        }
    }

    public ShopAPI getShopAPI()
    {
        return shopAPI;
    }

    public ConfigManager getConfigManager()
    {
        return config;
    }

    public static void debug(Object message)
    {
        if (debug)
            System.out.println("[PrettySimpleShop debug] " + message);
    }

    private Economy getEconomy()
    {
        if (economy != null)
            return economy;
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        economy = rsp.getProvider();
        return economy;
    }


}
