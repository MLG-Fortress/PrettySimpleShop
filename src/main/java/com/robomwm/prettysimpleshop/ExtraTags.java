package com.robomwm.prettysimpleshop;

import org.bukkit.DyeColor;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 10/5/2018.
 *
 * @author RoboMWM
 */
public enum ExtraTags
{
    SHULKER_BOX,
    TERRACOTTA;

    ExtraTags()
    {
        for (Material material : Material.values())
        {
            if (material.name().equals(this.name()))
                materials.add(material);
            else for (DyeColor color : DyeColor.values())
                if (material.name().equals(color.name() + "_" + this.name()))
                {
                    materials.add(material);
                    continue;
                }
        }
    }

    private Set<Material> materials = new HashSet<>();

    public boolean isTagged(Material material)
    {
        return materials.contains(material);
    }

    public Collection<Material> getMaterials()
    {
        return Collections.unmodifiableSet(materials);
    }
}
