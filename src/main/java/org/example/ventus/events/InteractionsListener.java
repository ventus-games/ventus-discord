package org.example.ventus.events;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.ventus.central.Ventus;
import org.example.ventus.central.VentusConfig;
import org.example.ventus.util.ResponseHelper;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class InteractionsListener extends ListenerAdapter {

    private VentusConfig config;

    public InteractionsListener() {
        this.config = Ventus.getInstance().getConfig();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Ventus.getInstance().isDebug() && !(config.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!",
                    Color.RED, event.getUser()).build()).setEphemeral(true).queue();
        } else {
            Ventus.getInstance().getInteractionCommandHandler().handleSlashCommand(event, event.getMember());
        }
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Ventus.getInstance().isDebug() && !(config.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!",
                    Color.RED, event.getUser()).build()).setEphemeral(true).queue();
        } else {
            Ventus.getInstance().getInteractionCommandHandler().handleMessageContextCommand(event);
        }
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (Ventus.getInstance().isDebug() && !(config.getOwnerId() == (event.getUser().getIdLong()))) {
            event.replyEmbeds(ResponseHelper.createEmbed(null, "I am in debug mode! Only my owner can use commands!",
                    Color.RED, event.getUser()).build()).setEphemeral(true).queue();
        } else {
            Ventus.getInstance().getInteractionCommandHandler().handleUserContextCommand(event);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.isFromGuild()) return;
        Ventus.getInstance().getInteractionCommandHandler().handleAutoComplete(event);
    }

}
