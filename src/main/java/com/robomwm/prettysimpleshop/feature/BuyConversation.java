package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.LazyUtil;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/13/2018.
 *
 * I was gonna do something like an assistant or cashier or something
 * but that's all too ambiguous so here's a generic name
 *
 * @author RoboMWM
 */
public class BuyConversation implements Listener
{
    private JavaPlugin plugin;
    private Set<Player> buyPrompt = ConcurrentHashMap.newKeySet(); //thread safe????????

    public BuyConversation(JavaPlugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShopSelectWithIntent(ShopSelectEvent event)
    {
        if (!event.hasIntentToBuy())
            return;
        Player player = event.getPlayer();

        player.sendMessage(LazyUtil.buildPage(plugin.getName() + ": How many ", event.getShopInfo().getHoverableText(), ChatColor.RESET + " would you like to buy?")); //TODO: configurable
        buyPrompt.add(player);
    }

    @EventHandler(ignoreCancelled = true)
    private void onCommand(PlayerCommandPreprocessEvent event)
    {
        buyPrompt.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onChat(AsyncPlayerChatEvent event)
    {
        if (!buyPrompt.remove(event.getPlayer()))
            return;
        try
        {
            int amount = Integer.parseInt(event.getMessage());
            if (amount <= 0)
                return;
            event.setCancelled(true);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    event.getPlayer().performCommand("/buy " + amount); //TODO: call directly
                }
            }.runTask(plugin);
        }
        catch (Throwable ignored){}
    }
}

//No way to abort on failed input :c

//new ConversationFactory(plugin)
//        .withFirstPrompt(new AmountToBuy(event.getPlayer(), PrettySimpleShop.getItemName(event.getShopInfo().getItem())))
//        .withPrefix(new PluginNameConversationPrefix(plugin))
//        .withLocalEcho(false)
//        .buildConversation(event.getPlayer()).begin();
//
//        }
//
//private class AmountToBuy extends NumericPrompt
//{
//    private Player player;
//    private String itemName;
//    AmountToBuy(Player player, String itemName)
//    {
//        this.player = player;
//        this.itemName = itemName;
//    }
//
//    @Override
//    protected Prompt acceptValidatedInput(ConversationContext context, Number input)
//    {
//        if (input.intValue() > 0)
//            player.performCommand("/buy " + input.intValue()); //TODO: call method directly
//        return Prompt.END_OF_CONVERSATION;
//    }
//
//    @Override
//    public String getPromptText(ConversationContext context)
//    {
//        return "How many " + context.getSessionData("itemName") + " would you like to buy?"; //TODO: configureable
//    }
//
//    //Why do they not offer a way to cancel...
//    @Override
//    public Prompt acceptInput(ConversationContext context, String input) {
//        if (isInputValid(context, input)) {
//            return acceptValidatedInput(context, input);
//        } else {
//            String failPrompt = getFailedValidationText(context, input);
//            if (failPrompt != null) {
//                context.getForWhom().sendRawMessage(ChatColor.RED + failPrompt);
//            }
//            // Redisplay this prompt to the user to re-collect input
//            return this;
//        }
//    }
//
//}
