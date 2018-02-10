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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.Iterator;

/**
 * Created on 2/6/2018.
 * Provides convenience methods to access and operate upon a shop, given a location
 *
 * We do not store Shop objects since we store everything inside the Chests' inventoryName
 *
 * You have no idea how much time I wasted trying to figure out stupid DoubleChests just to find out they store a stupid copy of a Chest that you can't update so yea thanks Bukkit/spigot
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
        return isShop(chest, true);
    }

    public boolean isShop(Chest chest, boolean includeNew)
    {
        return isShop(getName(chest), includeNew);
    }

    private boolean isShop(String theName, boolean includeNew)
    {
        if (theName == null || theName.isEmpty())
            return false;
        String[] name = theName.split(" ");
        if (name.length == 1 && name[0].equals(shopKey) && includeNew) //new shop
            return true;
        return name.length == 5 && name[4].startsWith("\u00A7\u00A7");
    }

    public boolean setPrice(Chest chest, double newPrice)
    {
        String theName = getName(chest);
        if (theName.isEmpty())
            return false;
        String[] name = chest.getCustomName().split(" ");
        if (name.length == 1 && name[0].equals(shopKey))
            return setName(chest, priceKey + " " + Double.toString(newPrice) + " " + salesKey + " 0 \u00A7\u00A7");
        else if (!name[0].equals(priceKey))
            return false;

        name[1] = Double.toString(newPrice);

        return setName(chest, StringUtils.join(name, " "));
    }

    public double getPrice(Chest chest)
    {
        String theName = getName(chest);
        if (theName == null || theName.isEmpty())
            return -1;
        String[] name = theName.split(" ");
        if (name.length < 2 || !name[0].equals(priceKey))
            return -1;
        else
            return Double.parseDouble(name[1]);
    }

    public double getRevenue(Chest chest, boolean reset)
    {
        String theName = getName(chest);
        double revenue;
        if (theName == null || theName.isEmpty())
            return -1;
        String[] name = theName.split(" ");
        PrettySimpleShop.debug(String.join(" ", name));
        if (name.length < 5 || name[4].length() < 2 || !name[4].substring(0, 2).equals("\u00A7\u00A7"))
            return -1;
        if (name[4].length() < 3)
            return 0;
        revenue = Double.parseDouble(name[4].substring(2));
        if (reset)
        {
            name[4] = "\u00A7\u00A7";
            if (!setName(chest, StringUtils.join(name, " ")))
                return 0;
        }
        return revenue;
    }

    /**
     * Apparently DoubleChests will return its inventory when doing this even though it's a chest idk it's stupid
     * @param chest
     * @return
     */
    private Inventory getInventory(Chest chest)
    {
        return chest.getInventory();
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

    //helper/convenience method
    public Chest getChest(Location location)
    {
        if (location.getBlock() == null)
            return null;
        if (!(location.getBlock().getState() instanceof Chest))
            return null;
        return (Chest)location.getBlock().getState();
    }

    //helper method that returns the location of a shop - DoubleChest conveniently returns the middle between the two chests.
    public Location getLocation(Chest chest)
    {
        if (!(chest.getInventory().getHolder() instanceof DoubleChest))
        {
            return chest.getLocation();
        }
        return ((DoubleChest)chest.getInventory().getHolder()).getLocation();
    }

    private String getName(Chest chest)
    {
        if (!(chest.getInventory().getHolder() instanceof DoubleChest))
        {
            return chest.getCustomName();
        }
        DoubleChest doubleChest = (DoubleChest)chest.getInventory().getHolder();
        //Left side takes precedence, but we'll override if the right side is a shop and the left side isn't
        if (!isShop(((Chest)doubleChest.getLeftSide()).getCustomName(), false) && isShop((((Chest)doubleChest.getRightSide())).getCustomName(), true))
            return ((Chest)doubleChest.getRightSide()).getCustomName();
        return ((Chest)doubleChest.getLeftSide()).getCustomName();
    }

    private boolean setName(Chest chest, String name)
    {
        if (!(chest.getInventory().getHolder() instanceof DoubleChest))
        {
            chest.setCustomName(name);
            return chest.update();
        }
        //Thanks Bukkit
        DoubleChest doubleChest = (DoubleChest)chest.getInventory().getHolder();
        Chest leftChest = (Chest)((Chest)doubleChest.getLeftSide()).getBlock().getState();
        leftChest.setCustomName(name);
        Chest rightChest = (Chest)((Chest)doubleChest.getRightSide()).getBlock().getState();
        rightChest.setCustomName(name);
        return leftChest.update() && rightChest.update();
    }

    /**
     * Removes items from the shop - performs the transaction
     * @param item
     * @param price
     * @return amount sold
     */
    public ItemStack performTransaction(Chest chest, ItemStack item, double price)
    {
        //Verify price
        PrettySimpleShop.debug(Double.toString(getPrice(chest)) + " " + price);
        if (getPrice(chest) != price)
            return null;
        PrettySimpleShop.debug("price validated");
        //Verify item type
        ItemStack shopItem = getItemStack(chest);
        PrettySimpleShop.debug(shopItem.toString() + item.toString());
        if (!isSimilar(item, shopItem))
            return null;
        PrettySimpleShop.debug("item validated");
        //Verify stock - cap to max stock remaining
        if (item.getAmount() > shopItem.getAmount())
            item.setAmount(shopItem.getAmount());

        //Update statistics/revenue first, otherwise will overwrite inventory changes
        String[] name = getName(chest).split(" ");
        name[3] = Long.toString(Long.valueOf(name[3]) + item.getAmount());
        double revenue = getRevenue(chest, false);
        PrettySimpleShop.debug("rev" + revenue);
        revenue += item.getAmount() * price;
        name[4] = "\u00A7\u00A7" + Double.toString(revenue);
        if (!setName(chest, StringUtils.join(name, " ")))
            return null;

        Inventory inventory = getInventory(chest);
        inventory.removeItem(item);

        return item;
    }
}
