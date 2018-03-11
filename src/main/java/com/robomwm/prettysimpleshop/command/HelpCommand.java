package com.robomwm.prettysimpleshop.command;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class HelpCommand implements CommandExecutor
{
    private ConfigManager configManager;
    public HelpCommand(PrettySimpleShop plugin)
    {
        configManager = plugin.getConfigManager();
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        sender.sendMessage("To create a shop, place a chest named " +
                ChatColor.ITALIC + configManager.getString("shopName") + ChatColor.RESET +
                " and use /setprice.\nUse /buy to buy from a shop.");
        return true;
    }
}
