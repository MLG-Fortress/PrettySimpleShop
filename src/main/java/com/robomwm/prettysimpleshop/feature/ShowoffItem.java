package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopBoughtEvent;
import com.robomwm.prettysimpleshop.event.ShopBreakEvent;
import com.robomwm.prettysimpleshop.event.ShopOpenCloseEvent;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopInfo;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShowoffItem implements Listener
{
    private PrettySimpleShop plugin;
    private ShopAPI shopAPI;
    private YamlConfiguration cache;
    private File cacheFile;
    private Map<Location, Item> spawnedItems = new HashMap<>();
    private ConfigManager config;
    private boolean showItemName;

    public ShowoffItem(PrettySimpleShop plugin, ShopAPI shopAPI, boolean showItemName)
    {
        this.plugin = plugin;
        config = plugin.getConfigManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        this.showItemName = showItemName;
        cacheFile = new File(plugin.getDataFolder(), "chunksContainingShops.data");
        cache = YamlConfiguration.loadConfiguration(cacheFile);
        for (World world : plugin.getServer().getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                loadShopItemsInChunk(chunk);
    }

    private void saveCache()
    {
        try
        {
            cache.save(cacheFile);
        }
        catch (Throwable rock)
        {
            plugin.getLogger().warning("Unable to save cache file: " + rock.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onChunkLoad(ChunkLoadEvent event)
    {
        if (!config.isWhitelistedWorld(event.getWorld()))
            return;
        final Chunk chunk = event.getChunk();
        if (event.isNewChunk())
            return;
        loadShopItemsInChunk(chunk);
    }

    private void loadShopItemsInChunk(Chunk chunk)
    {
        if (!cache.contains(getChunkName(chunk)))
            return;
        final ChunkSnapshot chunkSnapshot = chunk.getChunkSnapshot();
        new BukkitRunnable()
        {
            @Override
            public void run()
            {

                Set<Location> blocksToCheck = new HashSet<>();
                for (int x = 0; x < 16; x++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        for (int y = 0; y < 256; y++)
                        {
                            if (config.isShopBlock(chunkSnapshot.getBlockType(x, y, z)))
                            {
                                blocksToCheck.add(new Location(chunk.getWorld(), x + (chunk.getX() * 16), y, z + (chunk.getZ() * 16)));
                            }
                        }
                    }
                }

                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        boolean noShops = true;
                        for (Location location : blocksToCheck)
                        {
                            if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                                return;
                            }
                            Container container = shopAPI.getContainer(location);
                            if (container == null || !shopAPI.isShop(container, false))
                                continue;
                            ItemStack item = shopAPI.getItemStack(container);
                            if (item == null)
                                continue;
                            if (spawnItem(new ShopInfo(shopAPI.getLocation(container), item, shopAPI.getPrice(container)))) {
                                noShops = false; //Shops exist in this chunk
                            }
                        }
                        if (noShops)
                            removeCachedChunk(chunk);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onChunkUnload(ChunkUnloadEvent event)
    {
        if (!config.isWhitelistedWorld(event.getWorld()))
            return;

        //Remove showcased items for shops that are about to be unloaded
        Iterator<Location> locations = spawnedItems.keySet().iterator();
        while (locations.hasNext()) //can optimize later via mapping chunks if needed
        {
            Location location = locations.next();
            if (location.getBlockX() >> 4 == event.getChunk().getX() && location.getBlockZ() >> 4 == event.getChunk().getZ())
            {
                Item item = spawnedItems.get(location);
                item.remove();
                item.removeMetadata("NO_PICKUP", plugin);
                locations.remove();
            }
        }

        //Cleanup dropped items that may have been moved away from the chunk they spawned at
        for (Entity entity : event.getChunk().getEntities())
        {
            if (entity.getType() == EntityType.DROPPED_ITEM && entity.hasMetadata("NO_PICKUP")) {
                entity.remove();
                entity.removeMetadata("NO_PICKUP", plugin);
            }
        }
    }

    //despawn items when a shop chest becomes a doublechest
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onDoubleChest(BlockPlaceEvent event)
    {
        if (!config.isWhitelistedWorld(event.getBlock().getWorld()))
            return;
        if (!config.isShopBlock(event.getBlock().getType()))
            return;

        if(event.getBlockPlaced().getType() != Material.CHEST && event.getBlockPlaced().getType() != Material.TRAPPED_CHEST)
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                Container container = shopAPI.getContainer(event.getBlock().getLocation());
                if(!shopAPI.isShop(container)){
                    return;
                }
                InventoryHolder holder = container.getInventory().getHolder();
                if (!(holder instanceof DoubleChest))
                    return;
                DoubleChest doubleChest = (DoubleChest)holder;
                despawnItem(((Chest)(doubleChest.getLeftSide())).getLocation().add(0.5, 1.2, 0.5));
                despawnItem(((Chest)(doubleChest.getRightSide())).getLocation().add(0.5, 1.2, 0.5));
                spawnItem(new ShopInfo(shopAPI.getLocation(container), shopAPI.getItemStack(container), shopAPI.getPrice(container)));
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPickup(EntityPickupItemEvent event)
    {
        if (!config.isWhitelistedWorld(event.getItem().getLocation().getWorld()))
            return;
        event.setCancelled(event.getItem().hasMetadata("NO_PICKUP"));
    }
    @EventHandler(ignoreCancelled = true)
    private void onHopperPickup(InventoryPickupItemEvent event)
    {
        if (event.getInventory().getType() == InventoryType.HOPPER)
            event.setCancelled(event.getItem().hasMetadata("NO_PICKUP"));
    }

    @EventHandler
    private void onShopBought(ShopBoughtEvent event)
    {
        spawnItem(event.getShopInfo());
    }
    @EventHandler
    private void onShopSelect(ShopSelectEvent event)
    {
        spawnItem(event.getShopInfo());
    }
    @EventHandler
    private void onShopOpen(ShopOpenCloseEvent event)
    {
        spawnItem(event.getShopInfo());
    }

    @EventHandler
    private void onShopBreak(ShopBreakEvent event)
    {
        despawnItem(event.getShopInfo().getLocation().add(0.5, 1.2, 0.5));
        Container container = shopAPI.getContainer(event.getShopInfo().getLocation());
        InventoryHolder holder = container.getInventory().getHolder();
        if (!(holder instanceof DoubleChest)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnItem(new ShopInfo(shopAPI.getLocation(container), shopAPI.getItemStack(container), shopAPI.getPrice(container)));
            }
        }.runTaskLater(plugin, 1L);
    }
    @EventHandler
    private void onItemDespawn(ItemDespawnEvent event)
    {
        if (event.getEntity().hasMetadata("NO_PICKUP")){
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    despawnItem(event.getEntity().getLocation());
                    Container container = shopAPI.getContainer(event.getLocation().subtract(0,0.5,0));
                    if(container != null) {
                        spawnItem(new ShopInfo(shopAPI.getLocation(container), shopAPI.getItemStack(container), shopAPI.getPrice(container)));
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }
    @EventHandler
    private void onItemMerge(ItemMergeEvent event)
    {
        if (event.getEntity().hasMetadata("NO_PICKUP"))
            event.setCancelled(true);
    }

    private boolean spawnItem(ShopInfo shopInfo)
    {
        Location location = shopInfo.getLocation().add(0.5, 1.2, 0.5);
        ItemStack itemStack = shopInfo.getItem();
        despawnItem(location);
        if (itemStack == null)
            return false;
        itemStack.setAmount(1);
        itemStack.getItemMeta().setDisplayName(String.valueOf(ThreadLocalRandom.current().nextInt())); //Prevents merging (idea from SCS) though metadata might be sufficient? Though we could also just use the ItemMergeEvent too but this is probably simpler and more performant.
        Item item = location.getWorld().dropItem(location, itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        if (showItemName)
        {
            String name = PrettySimpleShop.getItemName(itemStack); //TODO: make configurable
            item.setCustomName(name);
            item.setCustomNameVisible(true);
        }
        item.setVelocity(new Vector(0, 0.01, 0));
        item.setMetadata("NO_PICKUP", new FixedMetadataValue(plugin, "NO_PICKUP"));
        spawnedItems.put(location, item);
        cacheChunk(location.getChunk());
        try //spigot compat (switch to Paper!)
        {
            item.setCanMobPickup(false);
        }
        catch (Throwable rock){}
        return true;
    }

    //Modifies Map as well, hence why it's not used in chunkUnloadEvent when we iterate through locations.
    private void despawnItem(Location location)
    {
        PrettySimpleShop.debug("Checking for item at " + location);
        if (spawnedItems.containsKey(location))
        {
            Item item = spawnedItems.remove(location);
            item.remove();
            item.removeMetadata("NO_PICKUP", plugin);
            PrettySimpleShop.debug("removed item at " + location);
        }
    }

    private void cacheChunk(Chunk chunk)
    {
        if (cache.contains(getChunkName(chunk)))
            return;
        cache.set(getChunkName(chunk), true);
        saveCache();
    }

    private void removeCachedChunk(Chunk chunk)
    {
        cache.set(getChunkName(chunk), null);
        saveCache();
    }

    private String getChunkName(Chunk chunk)
    {
        return chunk.getWorld().getName() + chunk.getX() + "," + chunk.getZ();
    }

    public void despawnAll()
    {
        for (Item item : spawnedItems.values()) {
            item.remove();
            item.removeMetadata("NO_PICKUP", plugin);
        }
        spawnedItems.clear();
    }
}
