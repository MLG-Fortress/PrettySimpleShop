package com.robomwm.prettysimpleshop.command;

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
    private PrettySimpleShop plugin;
    public HelpCommand(PrettySimpleShop plugin)
    {
        this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "PrettySimpleShop version " + plugin.getDescription().getVersion() + " by " + ChatColor.RED + "RoboMWM");
        sender.sendMessage("shopCommand");
        return true;
    }
}
