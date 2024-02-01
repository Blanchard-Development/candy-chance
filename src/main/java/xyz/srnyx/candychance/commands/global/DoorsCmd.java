package xyz.srnyx.candychance.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.candychance.MessageUnion;
import xyz.srnyx.candychance.CandyConfig;
import xyz.srnyx.candychance.CandyChance;


@CommandMarker
public class DoorsCmd extends ApplicationCommand {
    @Dependency private CandyChance candyChance;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "doors",
            description = "MANAGER-ONLY | Start a new random doors round in the current channel")
    public void commandDoors(@NotNull GlobalSlashEvent event) {
        final CandyConfig.GuildNode guild = candyChance.config.guild;
        if (guild.roles.checkDontHaveRole(event, guild.roles.manager)) return;
        if (guild.channels.general == event.getChannel().getIdLong()) candyChance.lastDoors = System.currentTimeMillis();
        candyChance.sendDoors(new MessageUnion(event));
    }
}
