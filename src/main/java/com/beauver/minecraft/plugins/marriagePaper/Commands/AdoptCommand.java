package com.beauver.minecraft.plugins.marriagePaper.Commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.beauver.minecraft.plugins.marriagePaper.Classes.AdoptRequest;
import com.beauver.minecraft.plugins.marriagePaper.Classes.Couple;
import com.beauver.minecraft.plugins.marriagePaper.Util.AdoptRequestHandler;
import com.beauver.minecraft.plugins.marriagePaper.Util.MarriageHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("marry")
public class AdoptCommand extends BaseCommand {

    @Subcommand("adopt invite")
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

}
