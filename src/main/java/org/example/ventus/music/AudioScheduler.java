package org.example.ventus.music;

import com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.example.ventus.central.Ventus;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.example.ventus.util.MiscUtil.toMinutesAndSeconds;

public class AudioScheduler extends PlayerEventListenerAdapter {

    private final LavalinkPlayer player;
    private final JdaLink link;
    private final BlockingQueue<AudioTrack> queue;
    private final long guildId;
    private long channelId;
    private boolean repeat = false;

    public AudioScheduler(@NotNull LavalinkPlayer player, long guildId) {
        this.guildId = guildId;
        this.queue = new LinkedBlockingQueue<>();
        this.link = Ventus.getInstance().getLavalink().getLink(String.valueOf(guildId));
        this.player = link.getPlayer();
        player.addListener(this);
    }

    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() != null) {
            queue.offer(track);
        } else {
            player.playTrack(track);
        }
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();
        if (track != null) {
            player.playTrack(track);
        } else {
            if (queue.size() == 0) {
                TextChannel channel = Ventus.getInstance().getShardManager().getTextChannelById(channelId);
                MessageEmbed embed = new EmbedBuilder()
                        .setDescription("There are no more tracks in the queue.")
                        .setColor(Color.RED)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
            }
        }
    }

    public void skipTo(int index) {
        AudioTrack[] tracks = queue.toArray(new AudioTrack[0]);
        AudioTrack track = tracks[index];
        for (int i = -1; i < index; i++) { // send help
            queue.poll();
        }
        if (track != null) {
            player.playTrack(track);
        }
    }

    public void shuffle() {
        AudioTrack[] tracks = queue.toArray(new AudioTrack[0]);
        for (int i = tracks.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            AudioTrack tmp = tracks[index];
            tracks[index] = tracks[i];
            tracks[i] = tmp;
        }
        queue.clear();
        queue.addAll(List.of(tracks));
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getChannelId() {
        return channelId;
    }

    public JdaLink getLink() {
        return link;
    }

    @Override
    public void onTrackStart(IPlayer player, @NotNull AudioTrack track) {
        TextChannel channel = Ventus.getInstance().getShardManager().getTextChannelById(channelId);
        long length = track.getInfo().length;
        String duration = toMinutesAndSeconds(length);
        if (!repeat) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Now Playing:")
                    .setDescription(String.format("[%s](%s) by %s", track.getInfo().title,
                            track.getInfo().uri, track.getInfo().author))
                    .addField("Duration:", duration, true)
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now());
            if (track instanceof YoutubeAudioTrack) {
                embed.setThumbnail(String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", track.getInfo().identifier));
            } else if (track instanceof ISRCAudioTrack isrcAudioTrack) {
                embed.setThumbnail(isrcAudioTrack.getArtworkURL());
            }
            channel.sendMessageEmbeds(embed.build()).setActionRow(
                        Button.secondary(String.format("music:pause:%s:%s",
                                channelId, track.getIdentifier()), "Play/Pause"),
                        Button.secondary(String.format("music:save:%s:%s",
                                channelId, track.getIdentifier()), "Save to library")
                    ).queue();
        }
    }

    @Override
    public void onTrackEnd(IPlayer player, AudioTrack track, @NotNull AudioTrackEndReason endReason) {
        TextChannel channel = Ventus.getInstance().getShardManager().getTextChannelById(channelId);
        if (endReason.mayStartNext) {
            if (repeat) {
                player.playTrack(track);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Looping:")
                        .setDescription(String.format("[%s](%s) by %s", track.getInfo().title,
                                track.getInfo().uri, track.getInfo().author))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
            } else {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Track Ended:")
                        .setDescription(String.format("[%s](%s) by %s", track.getInfo().title,
                                track.getInfo().uri, track.getInfo().author))
                        .setColor(0x9F90CF)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue();
                nextTrack();
            }
        }
    }

    @Override
    public void onTrackException(IPlayer player, @NotNull AudioTrack track, Exception exception) {
        TextChannel channel = Ventus.getInstance().getShardManager().getTextChannelById(channelId);
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while playing the track:")
                .setDescription(track.getInfo().title)
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        channel.sendMessageEmbeds(embed).queue();
    }

    public long getGuildId() {
        return guildId;
    }

    public void destroy() {
        player.stopTrack();
        queue.clear();
    }

}
