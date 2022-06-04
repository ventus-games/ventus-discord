package io.github.ventusgames.ventus.core;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.*;

public abstract class SlashCommand implements GenericCommand {

    private SlashCommandData commandData;
    private final List<Permission> requiredUserPermissions;
    private final List<Permission> requiredBotPermissions;
    private boolean isGlobal;
    private boolean isEphemeral;
    private final List<Long> enabledGuilds;
    private boolean runnableInDM;
    private final Set<CommandFlag> commandFlags;

    public boolean isRunnableInDM() {
        return runnableInDM;
    }

    public void setRunnableInDM(boolean runnableInDM) {
        this.runnableInDM = runnableInDM;
    }

    public String getCommandName() {
        return commandData.getName();
    }

    public String getCommandDescription() {
        return commandData.getDescription();
    }

    public List<OptionData> getOptions() {
        return commandData.getOptions();
    }

    @Override
    public SlashCommandData getData() {
        return commandData;
    }

    public void setCommandData(SlashCommandData commandData) {
        this.commandData = commandData;
    }

    public List<Permission> getRequiredUserPermissions() {
        return requiredUserPermissions;
    }

    public Set<CommandFlag> getCommandFlags() {
        return commandFlags;
    }

    public void setRequiredUserPermissions(Permission... permissions) {
        this.requiredUserPermissions.addAll(Arrays.asList(permissions));
    }

    public List<Permission> getRequiredBotPermissions() {
        return requiredBotPermissions;
    }

    public void setRequiredBotPermissions(Permission... permissions) {
        this.requiredBotPermissions.addAll(Arrays.asList(permissions));
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean isEphemeral() {
        return isEphemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        isEphemeral = ephemeral;
    }

    public List<Long> getEnabledGuilds() {
        return enabledGuilds;
    }

    public void setEnabledGuilds(Long... enabledGuilds) {
        this.enabledGuilds.addAll(Arrays.asList(enabledGuilds));
    }

    public void addCommandFlags(CommandFlag... flags) {
        Checks.notEmpty(flags, "Flags");
        commandFlags.addAll(Set.of(flags));
    }

    public SlashCommand() {
        this.requiredBotPermissions = new ArrayList<>();
        this.requiredUserPermissions = new ArrayList<>();
        this.commandData = null;
        this.isGlobal = true;
        this.isEphemeral = false;
        this.enabledGuilds = new ArrayList<>();
        this.runnableInDM = false;
        this.commandFlags = new HashSet<>();
    }

    /**
     * Executes requested slash command
     * @param event  The SlashCommandInteractionEvent
     */
    public abstract void executeCommand(SlashCommandInteractionEvent event);

    public void handleAutoComplete(CommandAutoCompleteInteractionEvent event) {}

}
