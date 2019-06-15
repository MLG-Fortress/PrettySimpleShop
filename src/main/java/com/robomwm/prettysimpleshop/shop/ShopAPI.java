package com.robomwm.prettysimpleshop.shop;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

    /**
     * Returns a copy of the item sold in this shop.
     * The total quantity available is stored in ItemStack#getAmount
     * 
     * Will return null if !item#isSimilar to another in the container.
     * 
     * @param container
     * @return the item sold, or null if either no item exists or multiple types of items are present in the container
     */
    public ItemStack getItemStack(Container container)
    {
        Validate.notNull(container);
        Inventory inventory = container.getInventory();
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

    public boolean isShop(Container container)
    {
        return isShop(container, true);
    }

    public boolean isShop(Container container, boolean includeNew)
    {
        return isShopFormat(getName(container), includeNew);
    }

    public boolean isDoubleChest(Container container)
    {
        return container.getInventory().getHolder() instanceof DoubleChest;
    }


    private boolean isShopFormat(String theName, boolean includeNew)
    {
        if (theName == null || theName.isEmpty())
            return false;
        String[] name = theName.split(" ");
        if (name.length == 1 && name[0].equalsIgnoreCase(shopKey) && includeNew) //new shop
            return true;
        return name.length == 5 && name[4].startsWith("\u00A7\u00A7"); //could also check price and sales keyword too
    }

    public boolean setPrice(Container container, double newPrice)
    {
        String theName = getName(container);
        PrettySimpleShop.debug("setPrice:" + theName + ";");
        //Now any container can be a shop. May add configuration for this if desired.
//        if (!isShopFormat(theName, true))
//            return false;

        String[] name = null;
        if (theName != null)
            name = theName.split(" ");

//        PrettySimpleShop.debug("setPrice:" + name.length);
//        if (name.length == 1 && name[0].equalsIgnoreCase(shopKey))
//            return setName(container, priceKey + " " + Double.toString(newPrice) + " " + salesKey + " 0 \u00A7\u00A7");
//        else if (!name[0].equals(priceKey))
//            return false;

        //Shop not named, or named something else (i.e. basically turn it into a shop no matter what)
        if (name == null || !name[0].equals(priceKey))
            return setName(container, priceKey + " " + Double.toString(newPrice) + " " + salesKey + " 0 \u00A7\u00A7");

        //Otherwise, just change the price portion of the string
        name[1] = Double.toString(newPrice);

        return setName(container, StringUtils.join(name, " "));
    }

    public double getPrice(Container container)
    {
        String theName = getName(container);
        if (theName == null || theName.isEmpty())
            return -1;
        String[] name = theName.split(" ");
        if (name.length < 2 || !name[0].equals(priceKey))
            return -1;
        else
            return Double.parseDouble(name[1]);
    }

    public double getRevenue(Container container, boolean reset)
    {
        String theName = getName(container);
        double revenue;
        if (theName == null || theName.isEmpty())
            return -1;
        String[] name = theName.split(" ");
        PrettySimpleShop.debug("getRevenue:" + String.join(" ", name));
        if (name.length < 5 || name[4].length() < 2 || !name[4].substring(0, 2).equals("\u00A7\u00A7"))
            return -1;
        if (name[4].length() < 3)
            return 0;
        revenue = Double.parseDouble(name[4].substring(2));
        if (reset)
        {
            name[4] = "\u00A7\u00A7";
            if (!setName(container, StringUtils.join(name, " ")))
                return 0;
        }
        return revenue;
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

    /**
     * If the block at the given location is a nameable container, its state will be returned as a Container.
     * @param location
     * @return the block's state as a Container, or null if not a Nameable Container.
     */
    public Container getContainer(Location location)
    {
        if (location.getBlock() == null)
            return null;
        BlockState state = location.getBlock().getState();
        if (state instanceof Nameable && state instanceof Container)
            return (Container)state;
        return null;
    }


    /**
     * Returns the location of a shop
     * DoubleChest conveniently returns the middle between the two chests.
     * @param container
     * @return
     */
    public Location getLocation(Container container)
    {
        if (isDoubleChest(container))
            return ((DoubleChest)container.getInventory().getHolder()).getLocation();
        return container.getLocation();
    }

    private String getName(Container container)
    {
        if (!(container instanceof Nameable))
            return null;

        if (isDoubleChest(container))
        {
            DoubleChest doubleChest = (DoubleChest)container.getInventory().getHolder();
            //Left side takes precedence, but we'll override if the right side is a shop and the left side isn't
            if (!isShopFormat(((Chest)doubleChest.getLeftSide()).getCustomName(), false) && isShopFormat((((Chest)doubleChest.getRightSide())).getCustomName(), true))
                return ((Chest)doubleChest.getRightSide()).getCustomName();
            return ((Chest)doubleChest.getLeftSide()).getCustomName();
        }

        return ((Nameable)container).getCustomName();
    }

    private boolean setName(Container actualContainer, String name)
    {
        if (!(actualContainer instanceof Nameable))
            return false;
        Nameable containerName = (Nameable)actualContainer;
        if (!isDoubleChest(actualContainer))
        {
            PrettySimpleShop.debug("setName: " + name);
            containerName.setCustomName(name);
            return actualContainer.update();
        }
        //Thanks Bukkit
        DoubleChest doubleChest = (DoubleChest)actualContainer.getInventory().getHolder();
        Chest leftChest = ((Chest)doubleChest.getLeftSide());
        leftChest.setCustomName(name);
        Chest rightChest = ((Chest)doubleChest.getRightSide());
        rightChest.setCustomName(name);
        PrettySimpleShop.debug("setName: " + name);
        return leftChest.update() && rightChest.update();
    }

    /**
     * Removes items from the shop - performs the transaction
     * @param requestedItem
     * @param price
     * @return amount sold
     */
    public ItemStack performTransaction(Container container, ItemStack requestedItem, double price)
    {
        //Verify price
        PrettySimpleShop.debug(Double.toString(getPrice(container)) + " " + price);
        if (getPrice(container) != price)
            return null;
        PrettySimpleShop.debug("price validated");
        //Verify item type
        ItemStack shopItem = getItemStack(container);
        if (!isSimilar(requestedItem, shopItem))
            return null;
        PrettySimpleShop.debug(shopItem.toString() + requestedItem.toString());
        PrettySimpleShop.debug("item validated");
        //Verify stock - cap to max stock remaining
        //We use and return the shopItem since this is already a cloned ItemStack (instead of also cloning item)
        //(This is why we're modifying `shopItem` to the request amount, unless it is larger.
        if (requestedItem.getAmount() < shopItem.getAmount())
            shopItem.setAmount(requestedItem.getAmount());

        //Update statistics/revenue first, otherwise will overwrite inventory changes
        String[] name = getName(container).split(" ");
        name[3] = Long.toString(Long.valueOf(name[3]) + shopItem.getAmount());
        double revenue = getRevenue(container, false);
        PrettySimpleShop.debug("rev" + revenue);
        revenue += shopItem.getAmount() * price;
        name[4] = "\u00A7\u00A7" + Double.toString(revenue);
        if (!setName(container, StringUtils.join(name, " ")))
            return null;

        Inventory inventory = container.getInventory();
        inventory.removeItem(shopItem);

        return shopItem;
    }
}
