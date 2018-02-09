package com.robomwm.prettysimpleshop;

import com.robomwm.prettysimpleshop.command.BuyCommand;
import com.robomwm.prettysimpleshop.command.HelpCommand;
import com.robomwm.prettysimpleshop.command.PriceCommand;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Created on 2/4/2018.
 *
 * @author RoboMWM
 */
public class PrettySimpleShop extends JavaPlugin
{
    private Economy economy;
    private ShopListener shopListener;
    private static Logger logger;
    private static boolean debug;

    public void onEnable()
    {
        logger = getLogger();
        economy = getEconomy();
        if (economy == null)
        {
            getLogger().severe("No economy plugin has been loaded yet. Please let us know which economy plugin you use!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ConfigManager config = new ConfigManager(this);
        debug = config.isDebug();
        ShopAPI shopAPI = new ShopAPI(config.getString("shopName"), config.getString("price"), config.getString("sales"));
        shopListener = new ShopListener(this, shopAPI, getEconomy(), config.getWhitelistedWorlds());
        getCommand("shop").setExecutor(new HelpCommand());
        getCommand("price").setExecutor(new PriceCommand(shopListener));
        getCommand("buy").setExecutor(new BuyCommand(shopListener));
    }

    public static void debug(String message)
    {
        if (debug)
            logger.info(message);
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
