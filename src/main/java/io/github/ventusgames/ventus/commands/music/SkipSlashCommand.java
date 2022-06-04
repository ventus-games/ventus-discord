package io.github.ventusgames.ventus.commands.music;

import io.github.ventusgames.ventus.central.Ventus;
import io.github.ventusgames.ventus.core.CommandFlag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import io.github.ventusgames.ventus.core.SlashCommand;
import io.github.ventusgames.ventus.music.AudioScheduler;
import io.github.ventusgames.ventus.music.GuildAudioPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class SkipSlashCommand extends SlashCommand {

    public SkipSlashCommand() {
        setCommandData(Commands.slash("skip", "Skips the current song."));
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Ventus.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        scheduler.nextTrack();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Skipped the current song.")
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

}
