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
public class BuyCommand implements CommandExecutor
{
    private ShopListener shopListener;

    public BuyCommand(ShopListener shopListener)
    {
        this.shopListener = shopListener;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player) || args.length < 1)
            return false;

        int quantity;

        try
        {
            quantity = Integer.valueOf(args[0]);
        }
        catch (Throwable rock)
        {
            return false;
        }

        shopListener.buyCommand((Player)sender, quantity);
        return true;
    }
}
