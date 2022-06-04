package org.example.ventus.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.example.ventus.central.Ventus;
import org.example.ventus.core.CommandFlag;
import org.example.ventus.core.SlashCommand;
import org.example.ventus.music.AudioScheduler;
import org.example.ventus.music.GuildAudioPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class LoopSlashCommand extends SlashCommand {

    public LoopSlashCommand() {
        setCommandData(
                Commands.slash("loop", "Loop the current song.")
                        .addOption(OptionType.BOOLEAN, "loop",
                                "Whether to loop the current song.", true)
        );
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Ventus.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        boolean loop = event.getOption("loop").getAsBoolean();
        if (loop) {
            scheduler.setRepeat(true);
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Looping")
                    .setDescription("Looping is now enabled")
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        } else {
            scheduler.setRepeat(false);
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("No longer looping")
                    .setDescription("Looping is now disabled")
                    .setColor(0x9F90CF)
                    .setTimestamp(Instant.now())
                    .build();
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

}
