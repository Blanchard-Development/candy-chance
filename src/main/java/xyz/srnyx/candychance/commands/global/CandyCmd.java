package xyz.srnyx.candychance.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;

import xyz.srnyx.candychance.CandyConfig;
import xyz.srnyx.candychance.CandyChance;
import xyz.srnyx.candychance.mongo.Profile;


@CommandMarker
public class CandyCmd extends ApplicationCommand {
    @Dependency private CandyChance candyChance;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "candy",
            subcommand = "get",
            description = "How much candy do you got in your bag?!")
    public void commandGet(@NotNull GlobalSlashEvent event,
                           @AppOption(description = "The user to get the candy of") @Nullable User user) {
        final long eventUserId = event.getUser().getIdLong();
        final long userId = user != null ? user.getIdLong() : eventUserId;
        boolean otherUser = eventUserId != userId;
        final Profile profile = candyChance.getMongoCollection(Profile.class).findOne("user", userId);
        final int candy = profile == null ? 0 : profile.getCandy();
        event.replyEmbeds(new LazyEmbed().setTitle(":candy: Candy Amount").setDescription((otherUser ? user.getAsMention() + " has" : "You have") + " **" + candy + "** :candy: in " + (otherUser ? "their" : "your") + " bag!").build(candyChance)).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "candy",
            subcommand = "give",
            description = "MANAGER-ONLY | Give candy to another user")
    public void commandGive(@NotNull GlobalSlashEvent event,
                            @AppOption(description = "The user to give the candy to") @NotNull User user,
                            @AppOption(description = "The amount of candy to give") int amount) {
        final CandyConfig.GuildNode.RolesNode roles = candyChance.config.guild.roles;
        if (roles.checkDontHaveRole(event, roles.manager)) return;

        // Get new profile
        final Profile profile = candyChance.getMongoCollection(Profile.class).findOneAndUpsert(Filters.eq("user", user.getIdLong()), Updates.inc("candy", amount));
        if (profile == null) {
            event.reply(LazyEmoji.NO + " An unexpected error occurred!").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.replyEmbeds(new LazyEmbed().setTitle(":candy: Give Candy").setDescription("You gave **" + amount + "** :candy: to " + user.getAsMention()).addField("New amount", profile.candy + " :candy:", true).build(candyChance)).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "candy",
            subcommand = "take",
            description = "MANAGER-ONLY | Take candy from another user")
    public void commandTake(@NotNull GlobalSlashEvent event,
                            @AppOption(description = "The user to take the candy from") @NotNull User user,
                            @AppOption(description = "The amount of candy to take") int amount) {
        final CandyConfig.GuildNode.RolesNode roles = candyChance.config.guild.roles;
        if (roles.checkDontHaveRole(event, roles.manager)) return;

        // Get new profile
        final Profile profile = candyChance.getMongoCollection(Profile.class).findOneAndUpsert(Filters.eq("user", user.getIdLong()), Updates.inc("candy", -amount));
        if (profile == null) {
            event.reply(LazyEmoji.NO + " An unexpected error occurred!").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.replyEmbeds(new LazyEmbed().setTitle(":candy: Take Candy").setDescription("You took **" + amount + "** :candy: from " + user.getAsMention()).addField("New amount", profile.candy + " :candy:", true).build(candyChance)).queue();
    }

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "candy",
            subcommand = "set",
            description = "MANAGER-ONLY | Set the amount of candy of another user")
    public void commandSet(@NotNull GlobalSlashEvent event,
                           @AppOption(description = "The user to set the candy of") @NotNull User user,
                           @AppOption(description = "The amount of candy to set") int amount) {
        final CandyConfig.GuildNode.RolesNode roles = candyChance.config.guild.roles;
        if (roles.checkDontHaveRole(event, roles.manager)) return;

        // Get new profile
        final Profile profile = candyChance.getMongoCollection(Profile.class).findOneAndUpsert(Filters.eq("user", user.getIdLong()), Updates.set("candy", amount));
        if (profile == null) {
            event.reply(LazyEmoji.NO + " An unexpected error occurred!").setEphemeral(true).queue();
            return;
        }

        // Reply
        event.replyEmbeds(new LazyEmbed().setTitle(":candy: Set Candy").setDescription("You set " + user.getAsMention() + "'s candy to **" + amount + "** :candy:").build(candyChance)).queue();
    }
}
