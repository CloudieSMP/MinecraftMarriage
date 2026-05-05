package com.beauver.minecraft.plugins.marriagePaper.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.beauver.minecraft.plugins.marriagePaper.MarriagePaper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static com.beauver.minecraft.plugins.marriagePaper.Util.MarriageUtil.getPartner;
import static com.beauver.minecraft.plugins.marriagePaper.Util.MarriageUtil.setCustomName;

@CommandAlias("marry")
public class MarriageInteractionCommand extends BaseCommand {
    @Subcommand("kiss")
    @CommandPermission("marriage.marry.kiss")
    public void marryKiss(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Player target = getPartner(player);
        if(target == null){
            player.sendMessage(Component.text("Your partner is not online. I'm afraid you can't kiss the air.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        target.sendActionBar(Component.text("Your partner grabbed you closely and kissed you on the lips!").color(TextColor.fromHexString("#FFAA00")));
        player.sendActionBar(Component.text("You kissed your partner!").color(TextColor.fromHexString("#FFAA00")));

        target.getWorld().spawnParticle(Particle.HEART, target.getLocation(), 10, 0.5,0.5,0.5);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 10, 0.5,1,0.5);
    }

    @Subcommand("hug")
    @CommandPermission("marriage.marry.hug")
    public void marryHug(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Player target = getPartner(player);
        if(target == null){
            player.sendMessage(Component.text("Your partner is not online. I'm afraid you can't hug the air.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        target.sendActionBar(Component.text("Your partner hugged you tightly!").color(TextColor.fromHexString("#FFAA00")));
        player.sendActionBar(Component.text("You hugged your partner!").color(TextColor.fromHexString("#FFAA00")));

        target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation(), 10, 0.5, 0.5, 0.5);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 10, 0.5, 1, 0.5);
    }

    @Subcommand("tp")
    @CommandPermission("marriage.marry.tp")
    public void marryTp(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Player target = getPartner(player);
        if(target == null){
            player.sendMessage(Component.text("Your partner is not online. I'm afraid you can't tp to an unknown location.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        int delayInSeconds = 3;

        for (int i = delayInSeconds; i > 0; i--) {
            final int secondsLeft = i;
            Bukkit.getScheduler().runTaskLater(MarriagePaper.plugin, () -> {
                target.sendActionBar(
                        Component.text("Your partner will tp to you in " + secondsLeft + " seconds...")
                                .color(TextColor.fromHexString("#FFAA00")));
                player.sendActionBar(
                        Component.text("You will be tped in " + secondsLeft + " seconds...")
                                .color(TextColor.fromHexString("#FFAA00")));
            }, (delayInSeconds - secondsLeft) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(MarriagePaper.plugin, () -> {
            player.teleport(target.getLocation());
            player.sendActionBar(Component.text("You have been tped to ur partner!").color(TextColor.fromHexString("#FFAA00")));
            target.sendActionBar(Component.text("Your partner has tped to you!").color(TextColor.fromHexString("#FFAA00")));
        }, delayInSeconds * 20L);
    }


    @Subcommand("inventory")
    @CommandCompletion("@nothing")
    @CommandPermission("marriage.marry.inventory")
    public void marryInventory(CommandSender sender){
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Player target = getPartner(player);
        if (target == null) {
            player.sendMessage(Component.text("You do not have a partner or they're not online. I'm afraid you can't see their inventory.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Inventory copy = Bukkit.createInventory(player, 45, Component.text(target.getName() + "'s Inventory"));
        player.openInventory(copy);

        // keep inventory read-only and updated
        Listener listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getWhoClicked().equals(player) && event.getInventory().equals(copy)) {
                    event.setCancelled(true);
                }
            }

            @EventHandler
            public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
                if (event.getPlayer().equals(player) && event.getInventory().equals(copy)) {
                    InventoryCloseEvent.getHandlerList().unregister(this);
                    InventoryClickEvent.getHandlerList().unregister(this);
                }
            }
        };

        player.getServer().getPluginManager().registerEvents(listener, MarriagePaper.plugin);

        // schedule periodic updates while the inventory is open
        Bukkit.getScheduler().runTaskTimer(MarriagePaper.plugin, () -> {
            if (!player.isOnline() || !player.getOpenInventory().getTopInventory().equals(copy)) {
                return;
            }

            copy.setContents(target.getInventory().getContents());
            copy.setItem(36, setCustomName(target.getInventory().getItemInMainHand(), "Main Hand"));
            copy.setItem(37, setCustomName(target.getInventory().getItemInOffHand(), "Off Hand"));
            copy.setItem(38, setCustomName(target.getInventory().getHelmet(), "Helmet", false));
            copy.setItem(39, setCustomName(target.getInventory().getChestplate(), "Chestplate", false));
            copy.setItem(40, setCustomName(target.getInventory().getLeggings(), "Leggings", false));
            copy.setItem(41, setCustomName(target.getInventory().getBoots(), "Boots", false));
            copy.setItem(42, setCustomName(new ItemStack(Material.BARRIER), "Unused"));
            copy.setItem(43, setCustomName(new ItemStack(Material.BARRIER), "Unused"));
            copy.setItem(44, setCustomName(new ItemStack(Material.BARRIER), "Unused"));
        }, 0L, 20L);
    }

    @Subcommand("gift")
    @CommandCompletion("@nothing")
    @CommandPermission("marriage.marry.gift")
    public void marryGift(CommandSender sender){
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Player target = getPartner(player);
        if(target == null){
            player.sendMessage(Component.text("You do not have a partner or their not online. You don't wanna waste your gift now!").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.isEmpty()){
            player.sendMessage(Component.text("You're not holding anything in your hand. It's a bit sad to gift air isn't it?").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        if (target.getInventory().firstEmpty() == -1) {
            player.sendMessage(Component.text("The target's inventory is full. We will not gift your gift!").color(TextColor.fromHexString("#FF5555")));
            target.sendMessage(Component.text("Your partner tried to gift you something, but your inventory is full!").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        player.sendMessage(Component.text("You have gifted your partner: ").color(TextColor.fromHexString("#55FF55"))
                .append(Component.text(PlainTextComponentSerializer.plainText().serialize(item.displayName())).color(TextColor.fromHexString("#00AA00"))));
        target.sendMessage(Component.text("Your partner has gifted you: ").color(TextColor.fromHexString("#55FF55"))
                .append(Component.text(PlainTextComponentSerializer.plainText().serialize(item.displayName())).color(TextColor.fromHexString("#00AA00"))));

        target.getInventory().addItem(item);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
    }

    @Subcommand("fuck")
    @CommandCompletion("@nothing")
    @CommandPermission("marriage.marry.fuck")
    public void marryFuck(CommandSender sender){
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Player target = getPartner(player);
        if(target == null){
            player.sendMessage(Component.text("You might wanna use your hand instead, as you don't have a partner or they are offline.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        target.sendActionBar(Component.text("Your partner is a little too silly, go and get a room! Have fun of course! ;p").color(TextColor.fromHexString("#FFAA00")));
        player.sendActionBar(Component.text("You're a little silly, luckily there's a room nearby, have fun! ;p").color(TextColor.fromHexString("#FFAA00")));
        target.getWorld().spawnParticle(Particle.HEART, target.getLocation(), 10, 0.5,0.5,0.5);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 10, 0.5,1,0.5);

        if(new Random().nextInt(250) == 1){
            player.getServer().broadcast(Component.text(player.getName()).color(TextColor.fromHexString("#AA0000"))
                    .append(Component.text(" and ").color(TextColor.fromHexString("#FF5555")))
                    .append(Component.text(target.getName()).color(TextColor.fromHexString("#AA0000")))
                    .append(Component.text(" have been caught in the act?! Get a room!").color(TextColor.fromHexString("#FF5555"))));
        }
    }
}
