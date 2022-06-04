package org.example.ventus.commands.music;

import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import org.example.ventus.central.Ventus;
import org.example.ventus.core.CommandFlag;
import org.example.ventus.core.SlashCommand;
import org.example.ventus.database.util.DatabaseHelper;
import org.example.ventus.music.AudioResultHandler;
import org.example.ventus.music.AudioScheduler;
import org.example.ventus.music.GuildAudioPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.example.ventus.util.MiscUtil.isUrl;

public class PlaySlashCommand extends SlashCommand {

    public PlaySlashCommand() {
        setCommandData(Commands.slash("play", "Plays a song.").addOptions(
                new OptionData(OptionType.STRING, "song", "The song to play.", true, true),
                new OptionData(OptionType.STRING, "provider", "Provider to search in. (Ignore if link)", false)
                        .addChoice("YouTube (Default)", "ytsearch")
                        .addChoice("Spotify", "spsearch")
                        .addChoice("SoundCloud", "scsearch")
                        .addChoice("YouTube Music", "ytmsearch")));
        addCommandFlags(CommandFlag.MUSIC);
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        GuildAudioPlayer guildAudioPlayer = Ventus.getInstance().getAudioManager().getAudioPlayer(event.getGuild().getIdLong());
        AudioScheduler scheduler = guildAudioPlayer.getScheduler();
        JdaLink link = guildAudioPlayer.getScheduler().getLink();
        String input = event.getOption("song").getAsString();
        OptionMapping providerOption = event.getOption("provider");
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioManager manager = event.getGuild().getAudioManager();
        String query;
        String provider;
        if (providerOption == null) {
            provider = "ytsearch";
        } else {
            provider = providerOption.getAsString();
        }
        if (isUrl(input)) {
            query = input;
        } else {
            query = String.format("%s:%s", provider, input);
        }
        if (manager.getConnectedChannel() == null) {
            scheduler.setChannelId(event.getChannel().getIdLong());
            link.connect(voiceState.getChannel());
            link.getRestClient().loadItem(query, new AudioResultHandler(event, scheduler));
        }
    }

    @Override
    public void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        String value = focusedOption.getValue();
        if (focusedOption.getName().equals("song")) {
            List<Command.Choice> choices;
            if (value.isEmpty()) {
                choices = DatabaseHelper.getSearchHistory(event.getUser().getIdLong())
                        .stream()
                        .limit(25)
                        .map(history -> new Command.Choice(history.getName(), history.getUrl()))
                        .toList();
            } else {
                choices = DatabaseHelper.getSearchHistory(event.getUser().getIdLong())
                        .stream()
                        .limit(25)
                        .filter(history -> history.getName().contains(value.toLowerCase()))
                        .map(history -> new Command.Choice(history.getName(), history.getUrl()))
                        .toList();
            }
            event.replyChoices(choices).queue();
        }
    }

}
