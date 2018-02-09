package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.event.ShopBoughtEvent;
import com.robomwm.prettysimpleshop.event.ShopPricedEvent;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopInfo;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    public ShowoffItem(JavaPlugin plugin, ShopAPI shopAPI)
    {
        instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        cacheFile = new File(plugin.getDataFolder(), "inventorySnapshots.data");
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

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event)
    {
        final Chunk chunk = event.getChunk();
        final String chunkName = event.getWorld().getName() + event.getChunk().getX() + "," + event.getChunk().getZ();
        if (event.isNewChunk() || !cache.getStringList("chunks").contains(chunkName))
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

                if (blocksToCheck.isEmpty())
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            removeCachedChunk(chunkName);
                        }
                    }.runTask(instance);
                    return;
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
                            if (spawnItem(coordinate.getChunk().getBlock(coordinate.getX(), coordinate.getY(), coordinate.getZ()))
                                    && noShops)
                                noShops = false;
                        }
                        if (noShops)
                            removeCachedChunk(chunkName);
                    }
                }.runTask(instance);
            }
        }.runTaskAsynchronously(instance);
    }

    @EventHandler
    private void onShopPriced(ShopPricedEvent event)
    {
        updateItemFromEvent(event.getShopInfo());
    }

    @EventHandler
    private void onShopBought(ShopBoughtEvent event)
    {
        updateItemFromEvent(event.getShopInfo());
    }

    private void updateItemFromEvent(ShopInfo shopInfo)
    {

    }

    private boolean spawnItem(Block block)
    {

    }

    private void removeCachedChunk(String chunkName)
    {
        cache.getStringList("chunks").remove(chunkName);
        //TODO: save
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
