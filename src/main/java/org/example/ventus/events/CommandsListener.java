package org.example.ventus.events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.example.ventus.central.Ventus;
import org.example.ventus.central.VentusConfig;
import org.jetbrains.annotations.NotNull;

public class CommandsListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.isWebhookMessage() || event.getAuthor().isBot()) return;
        String content = event.getMessage().getContentRaw();
        VentusConfig config = Ventus.getInstance().getConfig();
        String prefix = config.getPrefix();
        if (content.startsWith(prefix) || content.startsWith(String.format("<@%s> ", event.getJDA().getSelfUser().getId()))) {
            Ventus.getInstance().getCommandHandler().handleCommand(event);
        }
    }

}
