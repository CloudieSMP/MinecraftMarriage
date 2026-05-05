package com.beauver.minecraft.plugins.marriagePaper.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.beauver.minecraft.plugins.marriagePaper.Classes.AdoptRequest;
import com.beauver.minecraft.plugins.marriagePaper.Classes.Couple;
import com.beauver.minecraft.plugins.marriagePaper.Classes.ProposalRequest;
import com.beauver.minecraft.plugins.marriagePaper.Enums.RelationshipType;
import com.beauver.minecraft.plugins.marriagePaper.MarriagePaper;
import com.beauver.minecraft.plugins.marriagePaper.Util.AdoptRequestHandler;
import com.beauver.minecraft.plugins.marriagePaper.Util.MarriageHandler;
import com.beauver.minecraft.plugins.marriagePaper.Util.ProposalRequestHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;


@CommandAlias("marry")
public class MarryCommand extends BaseCommand {

    @Subcommand("propose")
    @CommandCompletion("@players")
    @CommandPermission("marriage.marry")
    public void marryPlayer(CommandSender sender, String[] args) {
        //checks
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please mention a player you want to marry.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(Component.text("You may not marry yourself.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        //player offline
        if(target == null) {
            player.sendMessage(Component.text("You may not marry someone that is offline.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        //already outgoing proposal to targer
        for(ProposalRequest r : ProposalRequestHandler.getProposals()){
            if(r.getProposer().equals(player.getUniqueId()) || r.getProposer().equals(target.getUniqueId())){
                player.sendMessage(Component.text("You already have an outgoing proposal to this player.").color(TextColor.fromHexString("#FF5555")));
                return;
            }
        }

        //is already married
        for(Couple c : MarriageHandler.getMarriages()){
            if(c.getPartner1().equals(player.getUniqueId()) || c.getPartner2().equals(player.getUniqueId())) {
                player.sendMessage(Component.text("You're already married, don't try and have a affair!").color(TextColor.fromHexString("#FF5555")));
                return;
            }
        }

        ProposalRequest request = new ProposalRequest(player, target);
        ProposalRequestHandler.addProposal(request);

        player.sendMessage(Component.text("You proposed to ").color(TextColor.fromHexString("#55FF55"))
                .append(Component.text(targetName + "!").color(TextColor.fromHexString("#00AA00"))));

        target.sendMessage(Component.text(player.getName()).color(TextColor.fromHexString("#00AA00"))
                .append(Component.text(" proposed to you!").color(TextColor.fromHexString("#55FF55")))
                .append(Component.text("\nRun /marry accept " + player.getName() + " to accept the proposal").color(TextColor.fromHexString("#55FF55"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click here to accept the proposal").color(TextColor.fromHexString("#55FF55"))))
                        .clickEvent(ClickEvent.runCommand("/marry accept " + player.getName())))
                .append(Component.text("\nRun /marry reject " + player.getName() + " to reject the proposal").color(TextColor.fromHexString("#FF5555"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click here to reject the proposal").color(TextColor.fromHexString("#FF5555"))))
                        .clickEvent(ClickEvent.runCommand("/marry reject " + player.getName()))));
    }

    @Subcommand("accept")
    @CommandCompletion("@players")
    @CommandPermission("marriage.marry")
    public void acceptMarry(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please mention a player you want to accept their proposal.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        String targetName = args[0];
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
        ProposalRequest request = null;

        for(ProposalRequest r : ProposalRequestHandler.getProposals()){
            if(r.getTarget().equals(player.getUniqueId()) && r.getProposer().equals(targetOffline.getUniqueId())){
                request = r;
                break;
            }
        }
        if(request == null){
            player.sendMessage(Component.text("You do not have a proposal request from this player").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Couple couple = new Couple(player.getUniqueId(), targetOffline.getUniqueId());
        MarriageHandler.addMarriage(couple);
        ProposalRequestHandler.removeProposal(request);

        String offlineName = "Unkown";
        if(targetOffline.getName() != null && !targetOffline.getName().isEmpty()){
            offlineName = targetOffline.getName();
        }

        player.getServer().broadcast(Component.text(player.getName()).color(TextColor.fromHexString("#00AA00"))
                .append(Component.text(" and ").color(TextColor.fromHexString("#55FF55")))
                .append(Component.text(offlineName).color(TextColor.fromHexString("#00AA00")))
                .append(Component.text(" are now officially married!").color(TextColor.fromHexString("#55FF55"))));
    }

    @Subcommand("reject")
    @CommandCompletion("@players")
    @CommandPermission("marriage.marry")
    public void rejectMarry(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please mention a player you want to reject their proposal.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
        ProposalRequest request = null;

        for(ProposalRequest r : ProposalRequestHandler.getProposals()){
            if(r.getTarget().equals(player.getUniqueId()) && r.getProposer().equals(targetOffline.getUniqueId())){
                request = r;
                break;
            }
        }
        if(request == null){
            player.sendMessage(Component.text("You do not have a proposal request from this player").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        ProposalRequestHandler.removeProposal(request);

        if(targetOffline.isOnline() && target != null){
            target.sendMessage(Component.text(player.getName() + " has rejected your proposal.").color(TextColor.fromHexString("#FF5555")));
        }

        player.sendMessage(Component.text("You rejected " + targetOffline.getName() + "'s proposal.").color(TextColor.fromHexString("#FF5555")));
    }

    @Subcommand("divorce")
    @CommandPermission("marriage.marry")
    public void marryDivorce(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
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
            player.sendMessage(Component.text("You're already a single pringle!").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        MarriageHandler.removeMarriage(couple);
        player.getServer().broadcast(Component.text(player.getName()).color(TextColor.fromHexString("#AA0000"))
                .append(Component.text(" has filed for a divorce and left their children to be orphaned.").color(TextColor.fromHexString("#FF5555"))));
    }

    @Subcommand("modify")
    @CommandCompletion("@nothing")
    @CommandPermission("marriage.marry")
    public void modifyRelationship(CommandSender sender, String[] args) {
        sender.sendMessage(Component.text("Invalid command.").color(TextColor.fromHexString("#FF5555")));
    }

    @Subcommand("modify relationship")
    @CommandCompletion("straight|gay|lesbian")
    @CommandPermission("marriage.marry")
    public void marryChangeRelationshipType(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please state if you want your marriage to be: Straight, Gay, Lesbian").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        String relationshipType = args[0];
        Couple couple = null;
        for(Couple c : MarriageHandler.getMarriages()){
            if(c.getPartner1().equals(player.getUniqueId()) || c.getPartner2().equals(player.getUniqueId())){
                couple = c;
                break;
            }
        }
        if(couple == null){
            player.sendMessage(Component.text("You can't change your relationship type if you're not in a married.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        RelationshipType relationshipEnum;

        try{
            relationshipEnum = RelationshipType.valueOf(relationshipType.toUpperCase());
        }catch (IllegalArgumentException e){
            player.sendMessage(Component.text("Invalid relationship type! [Straight, Gay, Lesbian]").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        MarriageHandler.removeMarriage(couple);
        couple.setRelationshipType(relationshipEnum);
        MarriageHandler.addMarriage(couple);

        player.sendMessage(Component.text("Changed your relationship type to: ").color(TextColor.fromHexString("#55FF55"))
                .append(Component.text(relationshipType).color(TextColor.fromHexString("#00AA00"))));

    }

    @Subcommand("list")
    @CommandPermission("marriage.marry")
    public void marryList(CommandSender sender){
        if(!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        Component marryList = Component.text("Married People:\n").color(TextColor.fromHexString("#FFAA00"));

        for(Couple c : MarriageHandler.getMarriages()){
            OfflinePlayer p1 = Bukkit.getOfflinePlayer(c.getPartner1());
            OfflinePlayer p2 = Bukkit.getOfflinePlayer(c.getPartner2());
            String p1Name = "Unknown";
            String p2Name = "Unknown";
            if (p1.getName() != null && !p1.getName().isEmpty()) {
                p1Name = p1.getName();
            }
            if (p2.getName() != null && !p2.getName().isEmpty()) {
                p2Name = p2.getName();
            }

            marryList = marryList.append(Component.text(p1Name).color(TextColor.fromHexString("#FFAA00")));

            TextColor heartColor;
            switch (c.getRelationshipType()){
                case STRAIGHT -> heartColor = TextColor.fromHexString("#FF5555");
                case GAY -> heartColor = TextColor.fromHexString("#55FFFF");
                case LESBIAN -> heartColor = TextColor.fromHexString("#FF55FF");
                default -> heartColor = TextColor.fromHexString("#FF5555");
            }

            marryList = marryList.append(Component.text(" ❤ ").color(heartColor));
            marryList = marryList.append(Component.text(p2Name).color(TextColor.fromHexString("#FFAA00")));
            marryList = marryList.append(Component.text(" (Days Married: " + c.getDaysMarried() + ")").color(TextColor.fromHexString("#5555FF")));

            if(!c.getChildren().isEmpty()){
                marryList = marryList.append(Component.text("\n- "));
            }else{
                marryList = marryList.append(Component.text("\n"));
            }

            int counter = 0;
            for(UUID uuid : c.getChildren()){
                counter++;
                if(counter == c.getChildren().size()){
                    OfflinePlayer child = Bukkit.getOfflinePlayer(uuid);
                    String childName = "Unknown";
                    if (child.getName() != null && !child.getName().isEmpty()) {
                        childName = child.getName();
                    }

                    marryList = marryList.append(Component.text(childName + "\n").color(TextColor.fromHexString("#AAAAAA")));
                }else{
                    OfflinePlayer child = Bukkit.getOfflinePlayer(uuid);
                    String childName = "Unknown";
                    if (child.getName() != null && !child.getName().isEmpty()) {
                        childName = child.getName();
                    }

                    marryList = marryList.append(Component.text(childName + ", ").color(TextColor.fromHexString("#AAAAAA")));
                }
            }
        }
        sender.sendMessage(marryList);
    }
}
