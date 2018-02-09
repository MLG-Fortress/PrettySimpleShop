package com.robomwm.prettysimpleshop.shop;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class ShopListener implements Listener
{
    private ShopAPI shopAPI;
    private Set<World> allowedWorlds;
    private Economy economy;
    private Map<Player, ShopInfo> selectedShop = new HashMap<>();

    public ShopListener(JavaPlugin plugin, ShopAPI shopAPI, Economy economy, Set<World> enabledWorlds)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        this.allowedWorlds = enabledWorlds;
        this.economy = economy;
    }

    @EventHandler
    private void cleanup(PlayerQuitEvent event)
    {
        selectedShop.remove(event.getPlayer());
    }

    private boolean isEnabledWorld(World world)
    {
        if (allowedWorlds.isEmpty())
            return true;
        return allowedWorlds.contains(world);
    }

    //We don't watch BlockDamageEvent as player may be in adventure (but uh this event probably doesn't fire in adventure either so... uhm yea... hmmm.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onLeftClickChest(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || player.isSneaking())
            return;
        if (!isEnabledWorld(event.getPlayer().getWorld()))
            return;

        Block block = event.getClickedBlock();
        if (block.getType() != Material.CHEST)
            return;

        Chest chest = (Chest)block.getState();
        if (!shopAPI.isShop(chest))
            return;
        ItemStack item = shopAPI.getItemStack(chest);
        double price = shopAPI.getPrice(chest);

        if (price < 0)
        {
            selectedShop.put(player, new ShopInfo(block.getLocation(), item, price));
            player.sendMessage("Shop selected.");
            return;
        }
        else if (item == null)
        {
            player.sendMessage("This shop is out of stock!");
            return;
        }

        selectedShop.put(player, new ShopInfo(block.getLocation(), item, price));

        //TODO: Use fancy json, potentially fire event for custom plugins (e.g. anvil GUI, if we can manage to stick itemstack in it)

        player.sendMessage(item.getI18NDisplayName() + " @ " + economy.format(price) + " each. " + item.getAmount() + " available.");
        player.sendMessage("/buy <quantity>");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onOpenInventory(InventoryOpenEvent event)
    {
        if (event.getPlayer().getType() != EntityType.PLAYER)
            return;
        if (event.getInventory().getLocation() == null)
            return;
        if (!(event.getInventory().getHolder() instanceof Chest || event.getInventory().getHolder() instanceof DoubleChest))
            return;
        double deposit = shopAPI.getRevenue(shopAPI.getChest(event.getInventory().getLocation()), true);
        if (deposit <= 0)
            return;
        Player player = (Player)event.getPlayer();
        economy.depositPlayer(player, deposit);
        player.sendMessage("Collected " + economy.format(deposit) + " in sales from this shop.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBreakShop(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        if (block.getType() != Material.CHEST)
            return;
        Chest chest = (Chest)block.getState();
        if (!shopAPI.isShop(chest))
            return;
        double deposit = shopAPI.getRevenue(chest, true);
        if (deposit <= 0)
            return;
        Player player = event.getPlayer();
        economy.depositPlayer(player, deposit);
        player.sendMessage("Collected " + economy.format(deposit) + " in sales from this shop.");
    }

    public void buyCommand(Player player, int amount)
    {
        ShopInfo shopInfo = selectedShop.remove(player);
        if (shopInfo == null)
        {
            player.sendMessage("Select a shop via left-clicking its chest.");
            return;
        }

        if (shopInfo.getPrice() < 0)
        {
            player.sendMessage("This shop is not open for sale yet! If you are the owner, use /price <price> to set the price per item.");
            return;
        }

        if (economy.getBalance(player) < amount * shopInfo.getPrice())
        {
            player.sendMessage("Transaction canceled: Insufficient /money. Try again with a smaller quantity?");
            return;
        }

        shopInfo.getItem().setAmount(amount);

        if (!hasInventorySpace(player, shopInfo.getItem()))
        {
            player.sendMessage("Transaction canceled: Insufficient inventory space. Free up some inventory slots or try again with a smaller quantity.");
            return;
        }

        ItemStack itemStack = shopAPI.performTransaction(shopAPI.getChest(shopInfo.getLocation()), shopInfo.getItem(), shopInfo.getPrice());
        if (itemStack == null)
        {
            player.sendMessage("Transaction canceled: Shop was modified. Please try again.");
            return;
        }

        economy.withdrawPlayer(player, itemStack.getAmount() * shopInfo.getPrice());

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(itemStack);

        player.sendMessage("Transaction completed. Bought " + itemStack.getAmount() + " " + itemStack.getType().name() + " for " + economy.format(itemStack.getAmount() * shopInfo.getPrice()));

        if (!leftovers.isEmpty())
        {
            player.sendMessage("Somehow you bought more than you can hold and we didn't detect this. Please report this issue with the following debug info:");
            for (ItemStack itemStack1 : leftovers.values())
                player.sendMessage(itemStack1.toString());
            return;
        }
    }

    public void priceCommand(Player player, double price)
    {
        ShopInfo shopInfo = selectedShop.remove(player);
        if (shopInfo == null || !shopAPI.setPrice(shopAPI.getChest(shopInfo.getLocation()), price))
        {
            player.sendMessage("Select a shop via left-clicking its chest.");
            return;
        }
        player.sendMessage("Price updated. Open chest and view title to confirm.");
    }

    //https://www.spigotmc.org/threads/detecting-when-a-players-inventory-is-almost-full.132061/#post-1401285
    public boolean hasInventorySpace(Player p, ItemStack item) {
        int free = 0;
        for (ItemStack i : p.getInventory()) {
            if (i == null) {
                free += item.getMaxStackSize();
            } else if (i.isSimilar(item)) {
                free += item.getMaxStackSize() - i.getAmount();
            }
        }
        return free >= item.getAmount();
    }
}
