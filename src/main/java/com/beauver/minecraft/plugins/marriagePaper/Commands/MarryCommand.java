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

    @Subcommand("adopt")
    @CommandCompletion("@players")
    @CommandPermission("marriage.adopt")
    public void adoptPlayer(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please mention a player you want to adopt.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(Component.text("You may not turn yourself into an orphan.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        //player offline
        if(target == null) {
            player.sendMessage(Component.text("You may not adopt someone that is offline.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        //not married
        Couple couple = null;
        for(Couple c : MarriageHandler.getMarriages()){
            if(c.getPartner1().equals(player.getUniqueId()) || c.getPartner2().equals(player.getUniqueId())){
                couple = c;
                break;
            }
        }
        if(couple == null){
            player.sendMessage(Component.text("You are not married, thus you may not adopt a child").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        // cannot adopt your own partner
        if (target.getUniqueId().equals(couple.getPartner1()) || target.getUniqueId().equals(couple.getPartner2())) {
            player.sendMessage(Component.text("You may not adopt your own partner.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        // cannot adopt an existing child in this family
        if (couple.getChildren().contains(target.getUniqueId())) {
            player.sendMessage(Component.text("This player is already your child.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        AdoptRequest request = new AdoptRequest(player, target);
        AdoptRequestHandler.addAdoptionRequests(request);

        player.sendMessage(Component.text("Adoption request sent to: ").color(TextColor.fromHexString("#55FF55"))
                .append(Component.text(target.getName()).color(TextColor.fromHexString("#00AA00"))));

        target.sendMessage(Component.text(player.getName()).color(TextColor.fromHexString("#00AA00"))
                .append(Component.text(" wants to adopt you!").color(TextColor.fromHexString("#55FF55")))
                .append(Component.text("\nrun /marry adopt accept " + player.getName() + " to become their child").color(TextColor.fromHexString("#55FF55"))
                        .clickEvent(ClickEvent.runCommand("/marry adopt accept " + player.getName())))
                .hoverEvent(HoverEvent.showText(Component.text("Click this to accept the adoption request").color(TextColor.fromHexString("#55FF55"))))
                .append(Component.text("\nrun /marry adopt reject " + player.getName() + " to stay an orphan").color(TextColor.fromHexString("#FF5555"))
                        .clickEvent(ClickEvent.runCommand("/marry adopt reject " + player.getName()))
                        .hoverEvent(HoverEvent.showText(Component.text("Click this to reject the adoption request").color(TextColor.fromHexString("#FF5555"))))));
    }

    @Subcommand("adopt accept")
    @CommandCompletion("@players")
    @CommandPermission("marriage.adopt")
    public void acceptAdopt(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please mention a player you want to join their family of.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        String targetName = args[0];
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);

        //not married
        AdoptRequest request = null;
        for(AdoptRequest a : AdoptRequestHandler.getAdoptionRequests()){
            if(a.getTarget().equals(player.getUniqueId()) || a.getProposer().equals(targetOffline.getUniqueId())){
                request = a;
                break;
            }
        }
        if(request == null){
            player.sendMessage(Component.text("You do not have a pending adoption request from this player.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Couple couple = null;
        for(Couple c : MarriageHandler.getMarriages()){
            if(c.getPartner1().equals(targetOffline.getUniqueId()) || c.getPartner2().equals(targetOffline.getUniqueId())){
                couple = c;
                break;
            }
        }
        if(couple == null){
            player.sendMessage(Component.text("This player is no longer in a family.").color(TextColor.fromHexString("#FF5555")));
            AdoptRequestHandler.removeAdoptionRequests(request);
            return;
        }

        AdoptRequestHandler.removeAdoptionRequests(request);
        MarriageHandler.removeMarriage(couple);
        couple.addChild(player);
        MarriageHandler.addMarriage(couple);

        player.getServer().broadcast(Component.text(player.getName()).color(TextColor.fromHexString("#00AA00"))
                .append(Component.text(" is now part of ").color(TextColor.fromHexString("#55FF55")))
                .append(Component.text(targetOffline.getName() != null ? targetOffline.getName() : "Unknown").color(TextColor.fromHexString("#00AA00")))
                .append(Component.text("'s family!").color(TextColor.fromHexString("#55FF55"))));
    }

    @Subcommand("adopt reject")
    @CommandCompletion("@players")
    @CommandPermission("marriage.adopt")
    public void rejectAdopt(CommandSender sender, String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }
        if(args.length == 0) {
            player.sendMessage(Component.text("Please mention a player you would like to reject their adoption papers of.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        String targetName = args[0];
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);
        Player target = Bukkit.getPlayer(targetName);

        //not married
        AdoptRequest request = null;
        for(AdoptRequest a : AdoptRequestHandler.getAdoptionRequests()){
            if(a.getTarget().equals(player.getUniqueId()) || a.getProposer().equals(targetOffline.getUniqueId())){
                request = a;
                break;
            }
        }
        if(request == null){
            player.sendMessage(Component.text("You do not have a pending adoption request from this player.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Couple couple = null;
        for(Couple c : MarriageHandler.getMarriages()){
            if(c.getPartner1().equals(targetOffline.getUniqueId()) || c.getPartner2().equals(targetOffline.getUniqueId())){
                couple = c;
                break;
            }
        }
        if(couple == null){
            player.sendMessage(Component.text("This player is no longer in a family.").color(TextColor.fromHexString("#FF5555")));
            AdoptRequestHandler.removeAdoptionRequests(request);
            return;
        }

        AdoptRequestHandler.removeAdoptionRequests(request);
        player.sendMessage(Component.text("You have rejected the adoption request.").color(TextColor.fromHexString("#FF5555")));
        if(targetOffline.isOnline()){
            target.sendMessage(Component.text(player.getName()).color(TextColor.fromHexString("#AA0000"))
                    .append(Component.text(" has rejected your adoption request.").color(TextColor.fromHexString("#FF5555"))));
        }
    }

    @Subcommand("adopt leave")
    @CommandPermission("marriage.adopt")
    public void adoptLeave(CommandSender sender, String[] args){
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can run this command.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        Couple couple = null;
        for(Couple c : MarriageHandler.getMarriages()){
            if(c.getChildren().contains(player.getUniqueId())){
                couple = c;
                break;
            }
        }
        if(couple == null){
            player.sendMessage(Component.text("You are a orphan.").color(TextColor.fromHexString("#FF5555")));
            return;
        }

        MarriageHandler.removeMarriage(couple);
        couple.removeChild(player);
        MarriageHandler.addMarriage(couple);

        OfflinePlayer p1 = Bukkit.getOfflinePlayer(couple.getPartner1());
        OfflinePlayer p2 = Bukkit.getOfflinePlayer(couple.getPartner2());
        String parent1 = "Unknown";
        String parent2 = "Unknown";
        if(p1.getName() != null && !p1.getName().isEmpty()){
            parent1 = p1.getName();
        }
        if(p2.getName() != null && !p2.getName().isEmpty()){
            parent2 = p2.getName();
        }

        Bukkit.getOfflinePlayer(couple.getPartner2());
        player.getServer().broadcast(
                Component.text(player.getName())
                        .color(TextColor.fromHexString("#AA0000"))
                        .append(Component.text(" has left ").color(TextColor.fromHexString("#FF5555")))
                        .append(Component.text(parent1)
                                .color(TextColor.fromHexString("#AA0000")))
                        .append(Component.text(" and ").color(TextColor.fromHexString("#FF5555")))
                        .append(Component.text(parent2).color(TextColor.fromHexString("#AA0000")))
        );
    }

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

    private ItemStack setCustomName(ItemStack item, String name) {
        return setCustomName(item, name, true);
    }

    private ItemStack setCustomName(ItemStack item, String name, boolean alwaysRename){
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

    private @Nullable Player getPartner(Player player) {
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

}
