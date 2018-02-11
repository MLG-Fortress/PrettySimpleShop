package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopBoughtEvent;
import com.robomwm.prettysimpleshop.event.ShopBreakEvent;
import com.robomwm.prettysimpleshop.event.ShopOpenCloseEvent;
import com.robomwm.prettysimpleshop.event.ShopPricedEvent;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private JavaPlugin instance;
    private ShopAPI shopAPI;
    private YamlConfiguration cache;
    private File cacheFile;
    private Map<Location, Item> spawnedItems = new HashMap<>();
    private ConfigManager config;

    public ShowoffItem(PrettySimpleShop plugin, ShopAPI shopAPI)
    {
        instance = plugin;
        config = plugin.getConfigManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        cacheFile = new File(plugin.getDataFolder(), "chunksContainingShops.data");
        if (!cacheFile.exists())
        {
            try
            {
                cacheFile.createNewFile();
                cache = YamlConfiguration.loadConfiguration(cacheFile);
                cache.set("chunks", new ArrayList<String>());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                cache = new YamlConfiguration();
            }
        }
        else
            cache = YamlConfiguration.loadConfiguration(cacheFile);
    }

    private void saveCache()
    {
        try
        {
            cache.save(cacheFile);
        }
        catch (Throwable rock)
        {
            instance.getLogger().warning("Unable to save cache file: " + rock.getMessage());
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event)
    {
        if (!config.isWhitelistedWorld(event.getWorld()))
            return;
        final Chunk chunk = event.getChunk();
        if (event.isNewChunk() || !cache.getStringList("chunks").contains(getChunkName(chunk)))
            return;
        final ChunkSnapshot chunkSnapshot = event.getChunk().getChunkSnapshot();
        //This nesting is crazy lol I haven't nested like this in years
        new BukkitRunnable()
        {
            @Override
            public void run()
            {

                Set<Coordinate> blocksToCheck = new HashSet<>();
                for (int x = 0; x < 16; x++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        for (int y = 0; y < 256; y++)
                        {
                            if (chunkSnapshot.getBlockType(x, y, z) == Material.CHEST)
                            {
                                blocksToCheck.add(new Coordinate(chunk, x, y, z));
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
                        for (Coordinate coordinate : blocksToCheck)
                        {
                            if (!coordinate.getChunk().isLoaded())
                                return;
                            if (spawnItem((Chest)coordinate.getChunk().getBlock(coordinate.getX(), coordinate.getY(), coordinate.getZ()).getState())
                                    && noShops)
                                noShops = false;
                        }
                        if (noShops)
                            removeCachedChunk(chunk);
                    }
                }.runTask(instance);
            }
        }.runTaskAsynchronously(instance);
    }

    @EventHandler(ignoreCancelled = true)
    private void onChunkUnload(ChunkUnloadEvent event)
    {
        if (!config.isWhitelistedWorld(event.getWorld()))
            return;
        if (event.getChunk().getEntities().length < 1) //Assuming this is already calculated? If not, should remove this check
            return;
        Iterator<Location> locations = spawnedItems.keySet().iterator();
        while (locations.hasNext()) //can optimize later via mapping chunks if needed
        {
            Location location = locations.next();
            if (location.getChunk() == event.getChunk())
            {
                spawnedItems.get(location).remove();
                locations.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPickup(EntityPickupItemEvent event)
    {
        if (!config.isWhitelistedWorld(event.getItem().getLocation().getWorld()))
            return;
        event.setCancelled(event.getItem().hasMetadata("NO_PICKUP"));
    }

    @EventHandler
    private void onShopPriced(ShopPricedEvent event)
    {
        spawnItem((Chest)event.getLocation().getBlock().getState());
    }

    @EventHandler
    private void onShopBought(ShopBoughtEvent event)
    {
        spawnItem((Chest)event.getShopInfo().getLocation().getBlock().getState());
    }
    @EventHandler
    private void onShopSelect(ShopSelectEvent event)
    {
        spawnItem((Chest)event.getShopInfo().getLocation().getBlock().getState());
    }
    @EventHandler
    private void onShopOpen(ShopOpenCloseEvent event)
    {
        spawnItem((Chest)event.getShopInfo().getLocation().getBlock().getState());
    }

    @EventHandler
    private void onShopBreak(ShopBreakEvent event)
    {
        despawnItem(event.getShopInfo().getLocation());
    }
    @EventHandler
    private void onItemDespawn(ItemDespawnEvent event)
    {
        if (event.getEntity().hasMetadata("NO_PICKUP"))
            event.setCancelled(true);
    }

    private boolean spawnItem(Chest chest)
    {
        if (!shopAPI.isShop(chest))
            return false;
        Location location = shopAPI.getLocation(chest).add(0.5, 1.2, 0.5);
        ItemStack itemStack = shopAPI.getItemStack(chest);
        despawnItem(location);
        if (itemStack == null)
            return false;
        String name = PrettySimpleShop.getItemName(itemStack); //TODO: make configurable
        itemStack.setAmount(1);
        itemStack.getItemMeta().setDisplayName(String.valueOf(ThreadLocalRandom.current().nextInt())); //Prevents merging (idea from SCS) though metadata might be sufficient?
        Item item = location.getWorld().dropItem(location, itemStack);
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setCustomName(name);
        item.setCustomNameVisible(true);
        item.setVelocity(new Vector(0, 0.01, 0));
        item.setMetadata("NO_PICKUP", new FixedMetadataValue(instance, this));
        spawnedItems.put(location, item);
        cacheChunk(location.getChunk());
        try
        {
            item.setCanMobPickup(false);
        }
        catch (Throwable rock){} //switch to Paper
        return true;
    }

    //Modifies Map as well, hence why it's not used in chunkUnloadEvent when we iterate through locations.
    private void despawnItem(Location location)
    {
        if (spawnedItems.containsKey(location))
            spawnedItems.remove(location).remove();
    }

    private void cacheChunk(Chunk chunk)
    {
        if (cache.getStringList("chunks").contains(getChunkName(chunk)))
            return;
        cache.getStringList("chunks").add(getChunkName(chunk));
        saveCache();
    }

    private void removeCachedChunk(Chunk chunk)
    {
        cache.getStringList("chunks").remove(getChunkName(chunk));
        saveCache();
    }

    private String getChunkName(Chunk chunk)
    {
        return chunk.getWorld().getName() + chunk.getX() + "," + chunk.getZ();
    }

    public void despawnAll()
    {
        for (Item item : spawnedItems.values())
            item.remove();
        spawnedItems.clear();
    }
}

class Coordinate
{
    private Chunk chunk;
    private int x;
    private int y;
    private int z;

    Coordinate(Chunk chunk, int x, int y, int z)
    {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Chunk getChunk()
    {
        return chunk;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }
}
