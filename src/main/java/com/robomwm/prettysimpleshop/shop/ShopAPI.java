package com.robomwm.prettysimpleshop.shop;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Iterator;

/**
 * Created on 2/6/2018.
 * Provides convenience methods to access and operate upon a shop, given a location
 *
 * We do not store Shop objects since we store everything inside the Chests' inventoryName
 *
 * @author RoboMWM
 */
public class ShopAPI
{
    private String shopKey;
    private String priceKey;
    private String salesKey;

    public ShopAPI(String shopKey, String priceKey, String salesKey)
    {
        this.shopKey = shopKey;
        this.priceKey = priceKey;
        this.salesKey = salesKey;
    }

    public ItemStack getItemStack(Chest chest)
    {
        Validate.notNull(chest);
        Inventory inventory = getInventory(chest);
        if (inventory == null)
            return null;
        ItemStack item = null;
        for (ItemStack itemStack : inventory)
        {
            if (itemStack == null || itemStack.getType() == Material.AIR)
                continue;
            if (item == null) //Set item
                item = itemStack.clone();
            else if (!item.isSimilar(itemStack)) //Shop must contain only one type of item
                return null;
            else
                item.setAmount(item.getAmount() + itemStack.getAmount());
        }
        return item;
    }

    public boolean isShop(Chest chest)
    {
        if (chest == null || chest.getCustomName() == null || chest.getCustomName().isEmpty())
            return false;
        String[] name = chest.getCustomName().split(" ");
        if (name.length == 1 && name[0].equals(shopKey))
            return true;
        else if (name.length == 4 && name[0].equals(priceKey) && name[2].equals(salesKey))
            return true;
        return false;
    }

    public boolean setPrice(Chest chest, double newPrice)
    {
        if (chest == null || chest.getCustomName() == null || chest.getCustomName().isEmpty())
            return false;
        String[] name = chest.getCustomName().split(" ");
        if (name.length == 1 && name[0].equals(shopKey))
        {
            chest.setCustomName(priceKey + " " + Double.toString(newPrice) + " " + salesKey + " 0"); //TODO: include total revenue, if feasible
            return chest.update();
        }
        else if (!name[0].equals(priceKey))
            return false;

        name[1] = Double.toString(newPrice);

        chest.setCustomName(StringUtils.join(name, " "));

        return chest.update();
    }

    public double getPrice(Chest chest)
    {
        if (chest == null || chest.getCustomName() == null || chest.getCustomName().isEmpty())
            return -1;
        String[] name = chest.getCustomName().split(" ");
        if (name.length < 2 || !name[0].equals(priceKey))
            return -1;
        else
            return Double.parseDouble(name[1]);
    }

    /**
     * If chest is part of a DoubleChest, it's only gonna return half of it - so get the inventoryholder's inventory.
     * @param chest
     * @return null if not a shop
     */
    private Inventory getInventory(Chest chest)
    {
        if (!isShop(chest))
            return null;
        Inventory inventory;
        inventory = chest.getBlockInventory();
        //Might not need to do this check; could just get the holder's inventory regardless if double or not...?
        if (inventory.getHolder() instanceof DoubleChest)
            inventory = inventory.getHolder().getInventory();
        return inventory;
    }

    /**
     * In case what qualifies as "similar" must be modified in the future...
     * @param item1 cannot be null, else will always return false
     * @param item2
     * @return
     */
    private boolean isSimilar(ItemStack item1, ItemStack item2)
    {
        return item1 != null && item1.isSimilar(item2);
    }

    public Chest getChest(Location location)
    {
        if (location.getBlock() == null)
            return null;
        if (!(location.getBlock().getState() instanceof Chest))
            return null;
        return (Chest)location.getBlock().getState();
    }

    /**
     * Removes items from the shop - performs the transaction
     * @param item
     * @param price
     * @return amount sold
     */
    public ItemStack performTransaction(Chest chest, ItemStack item, double price)
    {
        Validate.notNull(chest);
        //Verify price
        if (getPrice(chest) != price)
            return null;
        PrettySimpleShop.debug("price validated");
        //Verify item type
        ItemStack shopItem = getItemStack(chest);
        PrettySimpleShop.debug(item.toString());
        if (isSimilar(item, shopItem))
            return null;
        PrettySimpleShop.debug("item validated");
        //Verify stock - cap to max stock remaining
        if (item.getAmount() > shopItem.getAmount())
            item.setAmount(shopItem.getAmount());

        Inventory inventory = getInventory(chest);
        Iterator inventoryIterator = inventory.iterator();
        int amount = item.getAmount();

        while (amount > 0)
        {
            ItemStack itemStack = (ItemStack)inventoryIterator.next();
            if (itemStack == null || itemStack.getType() == Material.AIR)
                continue;
            if (itemStack.getAmount() <= amount)
            {
                amount -= itemStack.getAmount();
                inventoryIterator.remove();
            }
            else
            {
                amount -= amount; //Basically amount = 0
                itemStack.setAmount(itemStack.getAmount() - amount);
            }
        }

        //TODO: Is retrieved inventory a copy? Else must setContents

        //Update statistics (currently just total sales)
        String[] name = chest.getCustomName().split(" ");
        long sales = Long.valueOf(name[5]); //TODO: update total revenue, if feasible
        sales += item.getAmount();
        chest.setCustomName(StringUtils.join(name, " "));

        if (!chest.update())
            return null;

        return item;
    }
}
