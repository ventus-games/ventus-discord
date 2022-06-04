package org.example.ventus.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.example.ventus.database.model.SearchHistory;
import org.example.ventus.database.util.DatabaseHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record AudioResultHandler(SlashCommandInteractionEvent event, AudioScheduler scheduler) implements AudioLoadResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AudioResultHandler.class);

    @Override
    public void trackLoaded(AudioTrack track) {
        scheduler.queue(track);
        handle(track);
        LOGGER.debug("Track loaded: {}", track.getInfo().title);
    }

    @Override
    public void playlistLoaded(@NotNull AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            AudioTrack track = playlist.getTracks().get(0);
            scheduler.queue(track);
            handle(track);
            LOGGER.debug("Track loaded: {}", track.getInfo().title);
        } else {
            playlist.getTracks().forEach(scheduler::queue);
            handlePlaylist(playlist);
            LOGGER.debug("Playlist loaded: {}", playlist.getName());
        }
    }

    @Override
    public void noMatches() {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("No matches found")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
        LOGGER.debug("No matches found");
    }

    @Override
    public void loadFailed(@NotNull FriendlyException exception) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("An error occurred while loading the track")
                .setColor(Color.RED)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
        LOGGER.debug("An error occurred while loading the track: {}", exception.getMessage());
    }

    private void handle(@NotNull AudioTrack track) {
        AudioTrackInfo info = track.getInfo();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Added to queue:")
                .setDescription(String.format("[%s](%s) by %s", info.title,
                        info.uri, info.author))
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
        DatabaseHelper.insertSearchHistory(List.of(
                new SearchHistory(event.getUser().getIdLong(), info.title, info.uri, track.getIdentifier())
        ));
    }

    private void handlePlaylist(@NotNull AudioPlaylist playlist) {
        StringBuilder description = new StringBuilder("Tracks:\n");
        int trackList = playlist.getTracks().size();
        int trackCount = Math.min(trackList, 10);
        for (int i = 0; i < trackCount; i++) {
            AudioTrack track = playlist.getTracks().get(i);
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
        if (trackList > trackCount) {
            description.append("And ")
                    .append("`")
                    .append(trackList - trackCount)
                    .append("`")
                    .append(" more tracks...");
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Added to queue:")
                .setDescription(description)
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now());
        if (trackList == 0) {
            embed.setDescription("The queue is empty.");
        } else {
            embed.setDescription(description);
        }
        event.getHook().sendMessageEmbeds(embed.build()).queue();
        ArrayList<SearchHistory> histories = new ArrayList<>();
        playlist.getTracks().forEach(audioTrack -> {
            AudioTrackInfo info = audioTrack.getInfo();
            histories.add(new SearchHistory(event.getUser().getIdLong(), info.title, info.uri, audioTrack.getIdentifier()));
            DatabaseHelper.insertSearchHistory(histories);
        });
    }

}
