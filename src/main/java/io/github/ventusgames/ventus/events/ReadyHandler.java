package io.github.ventusgames.ventus.events;

import io.github.ventusgames.ventus.central.Ventus;
import io.github.ventusgames.ventus.util.ResponseHelper;
import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import io.github.ventusgames.ventus.central.VentusConfig;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URI;

public class ReadyHandler extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Ventus.getInstance().getExecutor().submit(() -> {
            VentusConfig config = Ventus.getInstance().getConfig();
            Ventus.getLogger().info("Successfully started " + Ventus.getInstance().getShardManager().getShards().size() + " shards.");
            Ventus.getInstance().getInteractionCommandHandler().init();
            if (Ventus.getInstance().isDebug()) Ventus.getLogger().warn("Debug mode is enabled.");
            Ventus.getInstance().initCommandCheck();
            JdaLavalink lavalink = Ventus.getInstance().getLavalink();
            lavalink.setJdaProvider(id -> Ventus.getInstance().getShardManager().getShardById(id));
            lavalink.setUserId(event.getJDA().getSelfUser().getId());
            lavalink.addNode("node-1", URI.create(config.getLavalink().getHostUrl()), config.getLavalink().getPassword());
            MessageEmbed embed = ResponseHelper.createEmbed("Ventus is ready!", null, Color.GREEN, null).build();
            Ventus.getInstance().getWebhookClient().send(embed);
        });
    }

}
