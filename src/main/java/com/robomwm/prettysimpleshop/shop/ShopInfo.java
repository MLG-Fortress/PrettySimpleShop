package com.robomwm.prettysimpleshop.shop;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 2/8/2018.
 *
 * @implNote Used to store info about a selected shop to compare to when performing a transaction
 *
 * @author RoboMWM
 */
public class ShopInfo
{
    private Location location;
    private ItemStack item;
    private double price;
    private BaseComponent hoverableText;

    public ShopInfo(Location location, ItemStack item, double price)
    {
        this.location = location;
        this.item = item;
        this.price = price;
    }

    public void setHoverableText(BaseComponent hoverableText)
    {
        this.hoverableText = hoverableText;
    }

    public BaseComponent getHoverableText()
    {
        return hoverableText;
    }

    public Location getLocation()
    {
        return location;
    }

    public double getPrice()
    {
        return price;
    }

    public ItemStack getItem()
    {
        return item;
    }

    @Override
    public boolean equals(Object other)
    {
        if (super.equals(other))
            return true;
        ShopInfo otherShopInfo = (ShopInfo)other;
        return otherShopInfo.location.equals(location) && otherShopInfo.item.isSimilar(item) && otherShopInfo.price == price;
    }
}
