package org.example.ventus.central;

import org.example.ventus.commands.misc.ShutdownCommand;
import org.example.ventus.util.ApplicationContextProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("org.example.ventus")
@EntityScan("org.example.ventus.database.model")
@EnableJpaRepositories("org.example.ventus.database.repository")
public class Application {

    private static Application instance;
    @Autowired private ApplicationContextProvider provider;

    public Application() {
        instance = this;
    }

    public static void main(String... args) {
        Thread.currentThread().setName("Ventus Main Thread");
        SpringApplication application = new SpringApplication(Application.class);
        application.setBanner((environment, sourceClass, out) -> {
            String banner = """
                    ____   ____             __
                    \\   \\ /   /____   _____/  |_ __ __  ______
                     \\   Y   // __ \\ /    \\   __\\  |  \\/  ___/
                      \\     /\\  ___/|   |  \\  | |  |  /\\___ \\
                       \\___/  \\___  >___|  /__| |____//____  >
                                  \\/     \\/                \\/
                    """;
            out.println(banner);
        });
        application.addListeners((ApplicationListener<ContextClosedEvent>) event -> {
            Ventus.getLogger().info("Shutting down Ventus...");
            Ventus.getInstance().getWebhookClient().send(ShutdownCommand.shutdownEmbed);
            Ventus.getLogger().info("Goodbye!");
        });
        application.run(args);
    }

    public static Application getInstance() {
        return instance;
    }

    public ApplicationContextProvider getProvider() {
        return provider;
    }

}
