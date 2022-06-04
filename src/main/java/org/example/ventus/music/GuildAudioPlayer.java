package org.example.ventus.music;

import lavalink.client.io.jda.JdaLink;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import org.example.ventus.central.Ventus;

public class GuildAudioPlayer {

    private final LavalinkPlayer player;
    private final AudioScheduler scheduler;
    private final long guildId;
    private final JdaLink link;

    public GuildAudioPlayer(long guildId) {
        Guild guild = Ventus.getInstance().getShardManager().getGuildById(guildId);
        this.guildId = guild.getIdLong();
        this.link = Ventus.getInstance().getLavalink().getLink(guild);
        this.player = link.getPlayer();
        this.scheduler = new AudioScheduler(player, guildId);
    }

    public LavalinkPlayer getPlayer() {
        return player;
    }

    public JdaLink getLink() {
        return link;
    }

    public AudioScheduler getScheduler() {
        return scheduler;
    }

    public long getGuildId() {
        return guildId;
    }

    public void destroy() {
        link.destroy();
        scheduler.destroy();
    }

}
