package xyz.srnyx.candychance.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.lazylibrary.LazyListener;

import xyz.srnyx.candychance.MessageUnion;
import xyz.srnyx.candychance.CandyChance;

import java.util.HashSet;
import java.util.Set;


public class MessageListener extends LazyListener {
    @NotNull private final CandyChance candyChance;
    private long lastCheck;

    public MessageListener(@NotNull CandyChance candyChance) {
        this.candyChance = candyChance;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!(event.getChannel() instanceof TextChannel channel) || event.getAuthor().isBot() || channel.getIdLong() != candyChance.config.guild.channels.general) return;
        final long now = System.currentTimeMillis();

        // Check last doors & check
        if (now - candyChance.lastDoors < candyChance.config.times.doorsCooldown || now - lastCheck < candyChance.config.activityCheck.cooldown) return;
        lastCheck = now;

        // Check activity (get messages from last minutes, record each unique user)
        final long oldest = now - candyChance.config.activityCheck.messages.oldest;
        final long requiredUsers = candyChance.config.activityCheck.requiredUsers;
        int messages = candyChance.config.activityCheck.messages.maximum;
        final Set<Long> users = new HashSet<>();
        for (final Message message : channel.getIterableHistory()) {
            if (messages == 0 || message.getTimeCreated().toInstant().toEpochMilli() < oldest) return;
            final User user = message.getAuthor();
            if (user.isBot()) continue;

            // Update
            messages--;
            users.add(user.getIdLong());

            // Send
            if (users.size() >= requiredUsers) {
                candyChance.lastDoors = now;
                candyChance.sendDoors(new MessageUnion(channel));
                return;
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (candyChance.doors.contains(event.getMessageIdLong())) event.retrieveUser().queue(user -> {
            if (!user.isBot()) event.retrieveMessage().queue(message -> {
                final CustomEmoji emojiOne = candyChance.config.guild.emojis.getOne();
                final CustomEmoji emojiTwo = candyChance.config.guild.emojis.getTwo();
                final CustomEmoji emojiThree = candyChance.config.guild.emojis.getThree();
                final MessageReaction reactionOne = message.getReaction(emojiOne);
                final MessageReaction reactionTwo = message.getReaction(emojiTwo);
                final MessageReaction reactionThree = message.getReaction(emojiThree);
                if (reactionOne == null || reactionTwo == null || reactionThree == null) return;
                final MessageReaction reaction = event.getReaction();
                final String reactionName = reaction.getEmoji().getName();

                // Only allow the user to react with one emoji
                if (reactionName.equals(emojiOne.getName())) {
                    reactionTwo.removeReaction(user).queue();
                    reactionThree.removeReaction(user).queue();
                    return;
                }
                if (reactionName.equals(emojiTwo.getName())) {
                    reactionOne.removeReaction(user).queue();
                    reactionThree.removeReaction(user).queue();
                    return;
                }
                if (reactionName.equals(emojiThree.getName())) {
                    reactionOne.removeReaction(user).queue();
                    reactionTwo.removeReaction(user).queue();
                    return;
                }

                // Remove the reaction if it's not one of the three
                reaction.removeReaction(user).queue();
            });
        });
    }
}
