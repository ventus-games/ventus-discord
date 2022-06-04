package io.github.ventusgames.ventus.central;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.github.ventusgames.ventus.music.AudioManager;
import lavalink.client.io.jda.JdaLavalink;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import io.github.ventusgames.ventus.core.CommandHandler;
import io.github.ventusgames.ventus.core.GenericCommand;
import io.github.ventusgames.ventus.core.InteractionCommandHandler;
import io.github.ventusgames.ventus.events.EventManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
public class Ventus implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ventus.class);
    private static Ventus instance;
    private ShardManager shardManager;
    private boolean isDebug;

    private final ExecutorService commandExecutor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                    new ThreadFactoryBuilder()
                            .setNameFormat("Ventus Command Thread %d")
                            .setUncaughtExceptionHandler((thread, throwable) -> {
                                LOGGER.error("An uncaught error occurred on the command thread-pool! (Thread {})", thread.getName(), throwable);
                            }).build());

    private final ScheduledExecutorService scheduledExecutor =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("Ventus Scheduled Executor Thread")
                    .setUncaughtExceptionHandler((thread, throwable) -> {
                        LOGGER.error("An uncaught error occurred on the scheduled executor!", throwable);
                    }).build());

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("Ventus Executor Thread")
                    .setUncaughtExceptionHandler((thread, throwable) -> {
                        LOGGER.error("An uncaught error occurred on the executor!", throwable);
                    }).build());

    private InteractionCommandHandler interactionCommandHandler;
    private CommandHandler commandHandler;
    private EventManager eventManager;
    private EventWaiter eventWaiter;
    private JdaLavalink lavalink;
    private AudioManager audioManager;
    private JDAWebhookClient webhookClient;
    private final VentusConfig config;

    private Ventus() {
        instance = this;
        this.config = Application.getInstance().getProvider().getApplicationContext().getBean(VentusConfig.class);
    }

    private void init() throws LoginException {
        this.interactionCommandHandler = new InteractionCommandHandler();
        this.commandHandler = new CommandHandler();
        this.isDebug = config.isDebugState();
        this.eventWaiter = new EventWaiter();
        this.lavalink = new JdaLavalink(null, 1, null);
        this.audioManager = new AudioManager();
        this.eventManager = new EventManager();
        this.shardManager = DefaultShardManagerBuilder.createDefault(config.getToken())
                .enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_MEMBERS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setShardsTotal(-1)
                .setStatus(OnlineStatus.ONLINE)
                .setVoiceDispatchInterceptor(lavalink.getVoiceInterceptor())
                .setEventManagerProvider(i -> eventManager)
                .addEventListeners(lavalink)
                .build();
        this.webhookClient = new WebhookClientBuilder(config.getWebhookUrl())
                .setThreadFactory(r -> {
                    Thread thread = new Thread(r);
                    thread.setName("Ventus Webhook Thread");
                    thread.setDaemon(true);
                    return thread;
                }).setWait(true)
                .buildJDA();
        eventManager.init();
    }

    @Override
    public void run(String... args) {
        try {
            Ventus ventus = new Ventus();
            ventus.init();
        } catch (Exception e) {
            if (e instanceof LoginException) {
                Ventus.getLogger().error("Failed to login to Discord!", e);
            } else {
                Ventus.getLogger().error("Failed to start Ventus!", e);
            }
        }
    }

    public static Ventus getInstance() {
        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ExecutorService getCommandExecutor() {
        return commandExecutor;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public InteractionCommandHandler getInteractionCommandHandler() {
        return interactionCommandHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public VentusConfig getConfig() {
        return config;
    }

    public EventWaiter getEventWaiter() {
        return eventWaiter;
    }

    public JdaLavalink getLavalink() {
        return lavalink;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public JDAWebhookClient getWebhookClient() {
        return webhookClient;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public void initCommandCheck() {
        LOGGER.info("Checking for outdated command cache...");
        getCommandExecutor().submit(() -> {
            if (getInstance().isDebug()) {
                Guild guild = getInstance().getShardManager().getGuildById(config.getGuildId());
                if (guild == null) {
                    LOGGER.error("Debug guild does not exist!");
                    return;
                }
                guild.retrieveCommands().queue(discordCommands -> {
                    List<GenericCommand> localCommands = getInstance().getInteractionCommandHandler()
                            .getRegisteredGuildCommands()
                            .get(guild.getIdLong());
                    handleCommandUpdates(discordCommands, localCommands);
                });
                return;
            }
            getInstance().getShardManager().getShards().get(0).retrieveCommands().queue(discordCommands -> {
                List<GenericCommand> localCommands = getInstance().getInteractionCommandHandler()
                        .getRegisteredCommands()
                        .stream()
                        .filter(GenericCommand::isGlobal)
                        .toList();
                handleCommandUpdates(discordCommands, localCommands);
            });
        });
    }

    private static void handleCommandUpdates(@NotNull Collection<? extends Command> discordCommands, @NotNull Collection<? extends GenericCommand> localCommands) {
        boolean commandRemovedOrAdded = localCommands.size() != discordCommands.size();
        if (commandRemovedOrAdded) {
            if (localCommands.size() > discordCommands.size()) {
                LOGGER.warn("New command(s) has/have been added! Updating Discord's cache...");
            } else {
                LOGGER.warn("Command(s) has/have been removed! Updating Discord's cache...");
            }
            getInstance().getInteractionCommandHandler().updateCommands(commands -> {
                LOGGER.info("Updated {} commands!", commands.size());
            }, throwable -> {});
            return;
        }
        boolean outdated = false;
        for (GenericCommand localCommand : localCommands) {
            Command discordCommand = discordCommands.stream()
                    .filter(command -> command.getName().equalsIgnoreCase(localCommand.getData().getName()))
                    .findFirst()
                    .orElse(null);
            CommandData localCommandData = localCommand.getData();
            CommandData discordCommandData = CommandData.fromCommand(discordCommand);
            if (!localCommandData.equals(discordCommandData)) {
                outdated = true;
                break;
            }
        }
        if (outdated) {
            getInstance().getInteractionCommandHandler().updateCommands(commands -> {
                LOGGER.info("Updated {} commands!", commands.size());
            }, throwable -> {});
        } else {
            LOGGER.info("No outdated commands found!");
        }
    }

}
