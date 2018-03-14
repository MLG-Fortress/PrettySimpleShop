package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 3/13/2018.
 *
 * @author RoboMWM
 */
public class AnvilBuyGUI implements Listener
{
    private JavaPlugin plugin;

    public AnvilBuyGUI(JavaPlugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShopSelectWithIntent(ShopSelectEvent event)
    {
        if (!event.hasIntentToBuy())
            return;
        Player thePlayer = event.getPlayer();

        //TODO: configurable message
        AnvilGUI anvilGUI = new AnvilGUI(plugin, thePlayer, "Input quantity to buy", (player, reply) ->
        {
            int amount;
            try
            {
                amount = Integer.parseInt(reply);
            }
            catch (Throwable rock)
            {
                return null;
            }
            if (amount <= 0)
                return null;
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    player.performCommand("/buy " + amount);
                }
            }.runTask(plugin);
            return null;
        });
    }
}
