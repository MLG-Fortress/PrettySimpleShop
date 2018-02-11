package com.robomwm.prettysimpleshop.event;

import com.robomwm.prettysimpleshop.shop.ShopInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/11/2018.
 *
 * Convenience event to determine when a player broke a shop
 *
 * @author RoboMWM
 */
public class ShopBreakEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private Player player;
    private ShopInfo shopInfo;

    public ShopBreakEvent(Player player, ShopInfo shopInfo)
    {
        this.player = player;
        this.shopInfo = shopInfo;
    }

    public Player getPlayer()
    {
        return player;
    }

    public ShopInfo getShopInfo()
    {
        return shopInfo;
    }
}
