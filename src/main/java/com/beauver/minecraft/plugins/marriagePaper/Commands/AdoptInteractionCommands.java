package com.beauver.minecraft.plugins.marriagePaper.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.beauver.minecraft.plugins.marriagePaper.Classes.Couple;
import com.beauver.minecraft.plugins.marriagePaper.Util.MarriageHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("marry")
public class AdoptInteractionCommands extends BaseCommand {

    @Subcommand("adopt pat")
    @CommandCompletion("@players")
    @CommandPermission("marriage.adopt.pat")
    public void hatpatAdopted(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please mention which child which you want to headpat.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null){
            player.sendMessage(Component.text("Player is not online.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Couple couple = null;
        for(Couple c : MarriageHandler.getMarriages()){
            if(c.getPartner1().equals(player.getUniqueId()) || c.getPartner2().equals(player.getUniqueId())){
                couple = c;
                break;
            }
        }
        if(couple == null){
            player.sendMessage(Component.text("You are not married to anyone.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(!couple.getChildren().contains(target.getUniqueId())){
            player.sendMessage(Component.text("This player is not your child.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        player.sendActionBar(Component.text("You head-patted your child!").color(TextColor.fromHexString("#FFAA00")));
        target.sendActionBar(Component.text(player.getName() + " has pat your head!").color(TextColor.fromHexString("#FFAA00")));

        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 10, 0.5,0.5,0.5);
        target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation(), 10, 0.5,0.5,0.5);
    }
}
