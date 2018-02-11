package com.robomwm.prettysimpleshop.event;

import com.robomwm.prettysimpleshop.shop.ShopInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/11/2018.
 *
 * Convenience event to determine when a player is opening or closing a shop.
 *
 * @author RoboMWM
 */
public class ShopOpenCloseEvent extends Event
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
    private boolean open;

    public ShopOpenCloseEvent(Player player, ShopInfo shopInfo, boolean open)
    {
        this.player = player;
        this.shopInfo = shopInfo;
        this.open = open;
    }

    public Player getPlayer()
    {
        return player;
    }

    public ShopInfo getShopInfo()
    {
        return shopInfo;
    }

    public boolean isOpen()
    {
        return open;
    }
}
