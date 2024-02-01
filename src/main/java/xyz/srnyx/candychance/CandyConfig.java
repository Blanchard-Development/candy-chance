package xyz.srnyx.candychance;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.configurate.ConfigurationNode;

import xyz.srnyx.lazylibrary.LazyEmbed;


public class CandyConfig {
    @NotNull private final CandyChance candyChance;
    @NotNull public final TimesNode times;
    @NotNull public final ActivityCheckNode activityCheck;
    @NotNull public final GuildNode guild;

    public CandyConfig(@NotNull CandyChance candyChance) {
        this.candyChance = candyChance;

        final ConfigurationNode yaml = candyChance.settings.fileSettings.file.yaml;
        this.times = new TimesNode(yaml.node("times"));
        this.activityCheck = new ActivityCheckNode(yaml.node("activity-check"));
        this.guild = new GuildNode(yaml.node("guild"));
    }

    public static class TimesNode {
        /**
         * seconds
         */
        public final long reactions;
        /**
         * milliseconds
         */
        public final long doorsCooldown;

        public TimesNode(@NotNull ConfigurationNode node) {
            this.reactions = node.node("reactions").getLong();
            this.doorsCooldown = node.node("doors-cooldown").getLong() * 60000;
        }
    }

    public static class ActivityCheckNode {
        /**
         * milliseconds
         */
        public final long cooldown;
        public final int requiredUsers;
        @NotNull public final MessagesNode messages;

        public ActivityCheckNode(@NotNull ConfigurationNode node) {
            this.cooldown = node.node("cooldown").getLong() * 1000;
            this.requiredUsers = node.node("required-users").getInt();
            this.messages = new MessagesNode(node.node("messages"));
        }

        public static class MessagesNode {
            /**
             * milliseconds
             */
            public final long oldest;
            public final int maximum;

            public MessagesNode(@NotNull ConfigurationNode node) {
                this.oldest = node.node("oldest").getLong() * 60000;
                this.maximum = node.node("maximum").getInt();
            }
        }
    }

    public class GuildNode {
        public final long id;
        @NotNull public final EmojisNode emojis;
        @NotNull public final RolesNode roles;
        @NotNull public final ChannelsNode channels;

        public GuildNode(@NotNull ConfigurationNode node) {
            this.id = node.node("id").getLong();
            this.emojis = new EmojisNode(node.node("emojis"));
            this.roles = new RolesNode(node.node("roles"));
            this.channels = new ChannelsNode(node.node("channels"));
        }

        @Nullable
        public Guild getGuild() {
        return candyChance.jda.getGuildById(id);
    }

        public static class EmojisNode {
            @NotNull public final String one;
            @NotNull public final String two;
            @NotNull public final String three;

            public EmojisNode(@NotNull ConfigurationNode node) {
                this.one = node.node("one").getString("<:orangeopen:1166163806909648897>");
                this.two = node.node("two").getString("<:purpleopen:1166163808105017394>");
                this.three = node.node("three").getString("<:greenopen:1166163805034782771>");
            }

            @NotNull
            public CustomEmoji getOne() {
                return Emoji.fromFormatted(one).asCustom();
            }

            @NotNull
            public CustomEmoji getTwo() {
                return Emoji.fromFormatted(two).asCustom();
            }

            @NotNull
            public CustomEmoji getThree() {
                return Emoji.fromFormatted(three).asCustom();
            }
        }

        public class RolesNode {
            public final long manager;

            public RolesNode(@NotNull ConfigurationNode node) {
                this.manager = node.node("manager").getLong();
            }

            @Nullable
            public Role getRole(long id) {
                final Guild jdaGuild = getGuild();
                return jdaGuild == null ? null : jdaGuild.getRoleById(id);
            }

            public boolean hasRole(long user, long role) {
                final Role jdaRole = getRole(role);
                if (jdaRole == null) return false;
                final Member member = jdaRole.getGuild().retrieveMemberById(user).complete();
                return member != null && member.getRoles().contains(jdaRole);
            }

            public boolean checkDontHaveRole(@NotNull GenericCommandInteractionEvent event, long role) {
                final boolean doesntHaverole = !hasRole(event.getUser().getIdLong(), role);
                if (doesntHaverole) event.replyEmbeds(LazyEmbed.noPermission("<@&" + role + ">").build(candyChance)).setEphemeral(true).queue();
                return doesntHaverole;
            }
        }

        public static class ChannelsNode {
            public final long general;

            public ChannelsNode(@NotNull ConfigurationNode node) {
                this.general = node.node("general").getLong();
            }
        }
    }
}
