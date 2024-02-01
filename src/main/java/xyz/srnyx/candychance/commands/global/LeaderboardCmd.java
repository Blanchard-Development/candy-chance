package xyz.srnyx.candychance.commands.global;

import com.freya02.botcommands.api.annotations.CommandMarker;
import com.freya02.botcommands.api.annotations.Dependency;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.candychance.CandyChance;


@CommandMarker
public class LeaderboardCmd extends ApplicationCommand {
    @Dependency private CandyChance candyChance;

    @JDASlashCommand(
            scope = CommandScope.GLOBAL,
            name = "leaderboard",
            description = "View the leaderboard of the users with the most candy!")
    public void commandLeaderboard(@NotNull GlobalSlashEvent event) {
        event.replyEmbeds(candyChance.getLeaderboardEmbed(event.getUser().getIdLong())).queue();
    }
}
