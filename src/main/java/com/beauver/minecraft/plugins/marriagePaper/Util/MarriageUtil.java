package com.beauver.minecraft.plugins.marriagePaper.Util;

import com.beauver.minecraft.plugins.marriagePaper.Classes.Couple;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class MarriageUtil {

    public static @Nullable Player getPartner(Player player) {
        Couple couple = null;
        boolean isFirst = true;
        for (Couple c : MarriageHandler.getMarriages()) {
            if (c.getPartner1().equals(player.getUniqueId())) {
                couple = c;
                isFirst = false;
                break;
            } else if (c.getPartner2().equals(player.getUniqueId())) {
                couple = c;
                break;
            }
        }
        if (couple == null) {
            return null;
        }

        Player target;
        if (isFirst) {
            target = Bukkit.getPlayer(couple.getPartner1());
        } else {
            target = Bukkit.getPlayer(couple.getPartner2());
        }
        return target;
    }

    public static ItemStack setCustomName(ItemStack item, String name, boolean alwaysRename){
        if(item == null){
            item = new ItemStack(Material.BARRIER);
        }else{
            item = item.clone();
        }

        ItemMeta meta = item.getItemMeta();
        if(meta == null){
            item = new ItemStack(Material.BARRIER);
            meta = item.getItemMeta();
        }

        if (alwaysRename || item.getType().equals(Material.BARRIER)) {
            meta.displayName(Component.text(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static ItemStack setCustomName(ItemStack item, String name) {
        return setCustomName(item, name, true);
    }
}
