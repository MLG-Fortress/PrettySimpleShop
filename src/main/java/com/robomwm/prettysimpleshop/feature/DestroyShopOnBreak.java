package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopBreakEvent;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 11/9/2019.
 *
 * @author RoboMWM
 */
public class DestroyShopOnBreak implements Listener
{
    private PrettySimpleShop plugin;

    public DestroyShopOnBreak(PrettySimpleShop plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShopBreak(ShopBreakEvent event)
    {
        Location location = event.getBaseEvent().getBlock().getLocation();
        location.getWorld().dropItemNaturally(location, new ItemStack(event.getBaseEvent().getBlock().getType()));
        event.getBaseEvent().setDropItems(false);
    }
}
