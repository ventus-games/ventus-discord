package io.github.ventusgames.ventus.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.github.ventusgames.ventus.central.Ventus;
import io.github.ventusgames.ventus.core.CommandFlag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import io.github.ventusgames.ventus.core.SlashCommand;
import io.github.ventusgames.ventus.music.GuildAudioPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueSlashCommand extends SlashCommand {

    public QueueSlashCommand() {
        setCommandData(
                Commands.slash("queue", "Queue a song")
                        .addSubcommands(
                                new SubcommandData("list", "List the current queue."),
                                new SubcommandData("clear", "Clear the current queue.")
                        )
        );
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Ventus.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        BlockingQueue<AudioTrack> queue = guildAudioPlayer.getScheduler().getQueue();
        switch (event.getSubcommandName()) {
            case "list" -> {
                StringBuilder description = new StringBuilder("Queue:\n");
                int trackSize = queue.size();
                int trackCount = Math.min(trackSize, 10);
                List<AudioTrack> trackList = queue.stream().toList();
                for (int i = 0; i < trackCount; i++) {
                    AudioTrack track = trackList.get(i);
                    AudioTrackInfo info = track.getInfo();
                    description.append("`#")
                            .append(i + 1)
                            .append("` [")
                            .append(info.title)
                            .append("](")
                            .append(info.uri)
                            .append(") by ")
                            .append(info.author)
                            .append("\n");
                }
                if (trackSize > trackCount) {
                    description.append("And ")
                            .append("`")
                            .append(trackSize - trackCount)
                            .append("`")
                            .append(" more tracks...");
                }
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Queue")
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now());
                if (trackSize == 0) {
                    embed.setDescription("The queue is empty.");
                } else {
                    embed.setDescription(description);
                }
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
            case "clear" -> {
                queue.clear();
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Queue")
                        .setDescription("Queue cleared")
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                event.getHook().sendMessageEmbeds(embed).queue();
            }
        }
    }

}
