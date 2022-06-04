package io.github.ventusgames.ventus.commands.misc;

import io.github.ventusgames.ventus.central.Ventus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import io.github.ventusgames.ventus.core.Command;
import io.github.ventusgames.ventus.core.CommandContext;
import io.github.ventusgames.ventus.core.CommandFlag;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownCommand extends Command {

    public static final MessageEmbed shutdownEmbed = new EmbedBuilder()
            .setTitle("Shutting down Ventus...")
            .setColor(Color.RED)
            .setTimestamp(Instant.now())
            .build();

    public ShutdownCommand() {
        super("shutdown", "Shuts down the bot", "shutdown");
        addCommandFlags(CommandFlag.DEVELOPER_ONLY, CommandFlag.PRIVATE);
        addAllowedGuilds(Ventus.getInstance().getConfig().getGuildId());
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, CommandContext ctx) {
        ScheduledExecutorService executor = Ventus.getInstance().getScheduledExecutor();
        event.getMessage().replyEmbeds(shutdownEmbed).queue();
        Ventus.getInstance().getShardManager().shutdown();
        executor.schedule(() -> System.exit(0), 10, TimeUnit.SECONDS);
    }

}
