package io.github.ventusgames.ventus.commands.music;

import io.github.ventusgames.ventus.central.Ventus;
import io.github.ventusgames.ventus.core.CommandFlag;
import io.github.ventusgames.ventus.core.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import io.github.ventusgames.ventus.music.AudioScheduler;
import io.github.ventusgames.ventus.music.GuildAudioPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class ShuffleSlashCommand extends SlashCommand {

    public ShuffleSlashCommand() {
        setCommandData(Commands.slash("shuffle", "Shuffle the current queue."));
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Ventus.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        scheduler.shuffle();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Shuffled the queue.")
                .setColor(0x9F90CF)
                .setTimestamp(Instant.now())
                .build();
        event.getHook().sendMessageEmbeds(embed).queue();
    }

}
