package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * Created on 8/4/2019.
 *
 * @author RoboMWM
 */
public class ActionBarItemDetails implements Listener
{
    private Plugin plugin;
    private ShopAPI shopAPI;
    private ConfigManager config;
    private Economy economy;

    public ActionBarItemDetails(PrettySimpleShop plugin, ShopAPI shopAPI, Economy economy)
    {
        try
        {
            Player.Spigot test;
        }
        catch (Throwable rock)
        {
            plugin.getLogger().warning("The blah blah feature requires at least spigot or paper.");
            return;
        }

        this.plugin = plugin;
        config = plugin.getConfigManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        this.economy = economy;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player player : plugin.getServer().getOnlinePlayers())
                {
                    sendShopDetails(player);
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    public boolean sendShopDetails(Player player)
    {
        if (!config.isWhitelistedWorld(player.getWorld()))
            return false;

        Block block = player.getTargetBlockExact(7);

        if (!shopAPI.isShop(block, false))
            return false;

        Container shopBlock = (Container)block.getState();

        ItemStack item = shopAPI.getItemStack(shopBlock);

        if (item == null)
            return false;

        String textToSend = config.getString("saleInfo", PrettySimpleShop.getItemName(item), economy.format(shopAPI.getPrice(shopBlock)), Integer.toString(item.getAmount()));

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(textToSend));
        return true;
    }
}
