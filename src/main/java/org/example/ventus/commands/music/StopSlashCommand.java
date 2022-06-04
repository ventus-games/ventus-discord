package org.example.ventus.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.ventus.central.Ventus;
import org.example.ventus.core.CommandFlag;
import org.example.ventus.core.SlashCommand;
import org.example.ventus.music.AudioScheduler;
import org.example.ventus.music.GuildAudioPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class StopSlashCommand extends SlashCommand {

    public StopSlashCommand() {
        setCommandData(Commands.slash("stop", "Stops playing."));
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Ventus.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        scheduler.destroy();
        event.getJDA().getDirectAudioController().disconnect(event.getGuild());
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Stopped playing.")
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

}
