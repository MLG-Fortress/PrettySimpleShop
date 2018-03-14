package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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
        new ConversationFactory(plugin)
                .withFirstPrompt(new AmountToBuy(event.getPlayer()))
                .buildConversation(event.getPlayer()).begin();

    }

    private class AmountToBuy extends NumericPrompt
    {
        private Player player;
        AmountToBuy(Player player)
        {
            this.player = player;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input)
        {
            if (input.intValue() > 0)
                player.performCommand("/buy " + input.intValue()); //TODO: call method directly
            return Prompt.END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context)
        {
            return "How many " + context.getSessionData("itemName") + " would you like to buy?"; //TODO: configureable
        }
    }
}
