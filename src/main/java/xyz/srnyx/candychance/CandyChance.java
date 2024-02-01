package xyz.srnyx.candychance;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import org.bson.conversions.Bson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.lazylibrary.LazyCollection;
import xyz.srnyx.lazylibrary.LazyEmbed;
import xyz.srnyx.lazylibrary.LazyEmoji;
import xyz.srnyx.lazylibrary.LazyLibrary;
import xyz.srnyx.lazylibrary.settings.LazySettings;

import xyz.srnyx.candychance.listeners.MessageListener;
import xyz.srnyx.candychance.mongo.Profile;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class CandyChance extends LazyLibrary {
    @NotNull private static final Random RANDOM = new Random();

    @NotNull public final CandyConfig config = new CandyConfig(this);
    @NotNull private final ScheduledExecutorService executor_service = Executors.newSingleThreadScheduledExecutor();
    @NotNull public final Set<Long> doors = new HashSet<>();
    public long lastDoors;

    public CandyChance() {
        jda.addEventListener(new MessageListener(this));
        jda.getPresence().setActivity(Activity.customStatus("Made by srnyx.com ❤"));
        LOGGER.info("Candy Chance has finished starting!");
    }

    @Override @NotNull
    public Consumer<LazySettings> getSettings() {
        return newSettings -> newSettings
                .searchPaths("xyz.srnyx.candychance.commands")
                .gatewayIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .disabledCacheFlags(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOJI,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.STICKER,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.SCHEDULED_EVENTS)
                .mongoCollection("profiles", Profile.class)
                .embedDefault(LazyEmbed.Key.COLOR, 15762229)
                .embedDefault(LazyEmbed.Key.FOOTER_TEXT, "Candy Chance")
                .embedDefault(LazyEmbed.Key.FOOTER_ICON, "https://us-east-1.tixte.net/uploads/cdn.venox.network/candychance.png");
    }

    public void sendDoors(@NotNull MessageUnion union) {
        final long endTime = (System.currentTimeMillis() / 1000) + config.times.reactions;

        // Build embed
        final LazyEmbed embed = new LazyEmbed()
                .setTitle(":game_die: Candy Chance!")
                .setDescription("React below with the door you want to choose!\nEach door will give you a random amount of candy :candy:\n*Ending <t:" + endTime + ":R>*")
                .addField(config.guild.emojis.one, "**?** :candy:", true)
                .addField(config.guild.emojis.two, "**?** :candy:", true)
                .addField(config.guild.emojis.three, "**?** :candy:", true);

        // Send message
        final long messageId = union.sendMessage(embed.build(this))
                // Add reactions
                .flatMap(message -> message.addReaction(config.guild.emojis.getOne())
                        .flatMap(v -> message.addReaction(config.guild.emojis.getTwo()))
                        .flatMap(v -> message.addReaction(config.guild.emojis.getThree()))
                        .map(v -> message.getIdLong()))
                .complete();
        doors.add(messageId);

        // Get reward amounts
        final List<Integer> rewards = new ArrayList<>(List.of(RANDOM.nextInt(5), RANDOM.nextInt(5) + 5, RANDOM.nextInt(5) + 10));
        final int rewardOne = rewards.remove(RANDOM.nextInt(3));
        final int rewardTwo = rewards.remove(RANDOM.nextInt(2));
        final int rewardThree = rewards.get(0);

        executor_service.schedule(() -> {
            doors.remove(messageId);

            // Get message
            final Message message = union.getMessage();
            if (message == null) {
                union.editMessage(LazyEmoji.WARNING + " **An unexpected error occurred!** Please notify <@242385234992037888> with this code: `CandyChance-1`").queue();
                return;
            }

            // Get winners
            final List<User> winnersOne = message.retrieveReactionUsers(config.guild.emojis.getOne()).complete();
            final List<User> winnersTwo = message.retrieveReactionUsers(config.guild.emojis.getTwo()).complete();
            final List<User> winnersThree = message.retrieveReactionUsers(config.guild.emojis.getThree()).complete();
            final int winnersOneSize = winnersOne.size() - 1;
            final int winnersTwoSize = winnersTwo.size() - 1;
            final int winnersThreeSize = winnersThree.size() - 1;

            // Modify embed
            embed
                    .setTitle(":game_die: Candy Chance! ENDED")
                    .setDescription("The doors have opened!\nCandy is being handed out now...\n*Ended <t:" + endTime + ":R>*")
                    .clearFields()
                    .addField(config.guild.emojis.one, "**" + rewardOne + "** :candy:\n*" + winnersOneSize + " winners*", true)
                    .addField(config.guild.emojis.two, "**" + rewardTwo + "** :candy:\n*" + winnersTwoSize + " winners*", true)
                    .addField(config.guild.emojis.three, "**" + rewardThree + "** :candy:\n*" + winnersThreeSize + " winners*", true);

            // Edit message
            union.editEmbed(embed.build(this))
                    .flatMap(v -> message.clearReactions())
                    .queue();

            // Give candy
            final LazyCollection<Profile> collection = getMongoCollection(Profile.class);
            final Bson updateOne = Updates.inc("candy", rewardOne);
            final Bson updateTwo = Updates.inc("candy", rewardTwo);
            final Bson updateThree = Updates.inc("candy", rewardThree);
            final long self = jda.getSelfUser().getIdLong();
            winnersOne.forEach(user -> {
                final long id = user.getIdLong();
                if (id != self) collection.upsertOne(Filters.eq("user", user.getIdLong()), updateOne);
            });
            winnersTwo.forEach(user -> {
                final long id = user.getIdLong();
                if (id != self) collection.upsertOne(Filters.eq("user", user.getIdLong()), updateTwo);
            });
            winnersThree.forEach(user -> {
                final long id = user.getIdLong();
                if (id != self) collection.upsertOne(Filters.eq("user", user.getIdLong()), updateThree);
            });
        }, config.times.reactions - 5, TimeUnit.SECONDS);
    }

    @NotNull
    public MessageEmbed getLeaderboardEmbed(@Nullable Long userId) {
        // Get sorted profiles
        final List<Profile> sorted = new ArrayList<>();
        getMongoCollection(Profile.class).collection.aggregate(List.of(Aggregates.sort(Sorts.descending("candy")))).into(sorted);

        // Create embed
        final LazyEmbed embed = new LazyEmbed().setTitle(":trophy: Leaderboard");

        // Add user place
        if (userId != null) {
            int userPlace = 0;
            for (int i = 0; i < sorted.size(); i++) {
                if (!userId.equals(sorted.get(i).user)) continue;
                userPlace = i + 1;
                break;
            }
            embed.setDescription("You're placed **#" + userPlace + "** with **" + sorted.get(userPlace - 1).getCandy() + "** :candy:");
        }

        // Add top 10
        final List<Profile> top10 = sorted.subList(0, Math.min(sorted.size(), 10));
        for (int i = 0; i < top10.size(); i++) {
            final Profile profile = top10.get(i);
            final String emoji = switch (i) {
                case 0 -> " :first_place:";
                case 1 -> " :second_place:";
                case 2 -> " :third_place:";
                default -> "";
            };
            embed.addField("#" + (i + 1) + emoji, "<@" + profile.user + "> with **" + profile.getCandy() + "** :candy:", false);
        }

        return embed.build(this);
    }

    public static void main(@NotNull String[] arguments) {
        new CandyChance();
    }
}
