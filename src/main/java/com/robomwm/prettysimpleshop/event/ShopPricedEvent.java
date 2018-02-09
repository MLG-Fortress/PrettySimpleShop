package com.robomwm.prettysimpleshop.event;

import com.robomwm.prettysimpleshop.shop.ShopInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShopPricedEvent extends Event
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
    private double newPrice;

    public ShopPricedEvent(Player player, ShopInfo shopInfo, double newPrice)
    {
        this.player = player;
        this.shopInfo = shopInfo;
        this.newPrice = newPrice;
    }

    public Player getPlayer()
    {
        return player;
    }

    public ShopInfo getShopInfo()
    {
        return shopInfo;
    }

    public double getNewPrice()
    {
        return newPrice;
    }
}
