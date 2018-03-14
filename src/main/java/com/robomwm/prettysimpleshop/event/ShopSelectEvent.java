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
public class ShopSelectEvent extends Event
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
    private boolean intentToBuy;

    public ShopSelectEvent(Player player, ShopInfo shopInfo, boolean intentToBuy)
    {
        this.player = player;
        this.shopInfo = shopInfo;
        this.intentToBuy = intentToBuy;
    }

    /**
     * @return whether the player's selection indicates an interest to buy
     */
    public boolean hasIntentToBuy()
    {
        return intentToBuy;
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
