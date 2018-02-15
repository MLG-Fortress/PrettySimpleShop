package com.robomwm.prettysimpleshop.command;

import com.robomwm.prettysimpleshop.shop.ShopListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class PriceCommand implements CommandExecutor
{
    private ShopListener shopListener;

    public PriceCommand(ShopListener shopListener)
    {
        this.shopListener = shopListener;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;
        if (args.length < 1)
        {
            shopListener.priceCommand((Player)sender, null);
            return false;
        }

        double price;

        try
        {
            price = Integer.valueOf(args[0]);
        }
        catch (Throwable rock)
        {
            shopListener.priceCommand((Player)sender, null);
            return false;
        }

        shopListener.priceCommand((Player)sender, price);
        return true;
    }
}
