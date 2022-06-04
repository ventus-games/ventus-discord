package org.example.ventus.commands.database;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.example.ventus.core.SlashCommand;
import org.example.ventus.database.model.Tag;
import org.example.ventus.database.repository.TagRepository;
import org.example.ventus.util.ResponseHelper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
public class TagSlashCommand extends SlashCommand {

    @Autowired private TagRepository tagRepository;

    public TagSlashCommand() {
        setCommandData(
                Commands.slash("tag", "Manage server tags.")
                        .addSubcommands(
                                new SubcommandData("add", "Add a new tag.").addOptions(
                                        new OptionData(OptionType.STRING, "name", "Tag name", true, false),
                                        new OptionData(OptionType.STRING, "title", "Tag Title", true),
                                        new OptionData(OptionType.STRING, "value", "Tag Value", true)
                                ), new SubcommandData("delete", "Delete a tag.").addOptions(
                                        new OptionData(OptionType.STRING, "name", "Tag name", true, true)
                                ), new SubcommandData("get", "Get a tag.").addOptions(
                                        new OptionData(OptionType.STRING, "name", "Tag name", true, true)
                                )
                        )
        );
        setEphemeral(false);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();
        switch (subcommandName) {
            case "add" -> {
                String tagName = event.getOption("name").getAsString();
                String tagTitle = event.getOption("title").getAsString();
                String tagValue = event.getOption("value").getAsString();
                tagRepository.findById(tagName).ifPresentOrElse(tag -> {
                    MessageEmbed tagAlreadyPresent = ResponseHelper.createEmbed(null, "Tag already present", Color.RED, event.getUser()).build();
                    event.getHook().sendMessageEmbeds(tagAlreadyPresent).queue();
                }, () -> {
                    tagRepository.save(new Tag(tagName, tagTitle, tagValue));
                    MessageEmbed tagSaved = ResponseHelper.createEmbed(null, "Tag saved", Color.GREEN, event.getUser()).build();
                    event.getHook().sendMessageEmbeds(tagSaved).queue();
                });
            }
            case "delete" -> {
                String tagName = event.getOption("name").getAsString();
                tagRepository.findById(tagName).ifPresentOrElse(tag -> {
                    tagRepository.deleteById(tagName);
                    MessageEmbed tagDeleted = ResponseHelper.createEmbed(null, "Tag deleted", Color.GREEN, event.getUser()).build();
                    event.getHook().sendMessageEmbeds(tagDeleted).queue();
                }, () -> {
                    MessageEmbed notExist = ResponseHelper.createEmbed(null, "Tag does not exist", Color.RED, event.getUser()).build();
                    event.getHook().sendMessageEmbeds(notExist).queue();
                });
            }
            case "get" -> {
                String tagName = event.getOption("name").getAsString();
                tagRepository.findById(tagName).ifPresentOrElse(tag -> {
                    MessageEmbed tagEmbed = ResponseHelper.createEmbed(tag.getTitle(), tag.getValue(), Color.CYAN, event.getUser()).build();
                    event.getHook().sendMessageEmbeds(tagEmbed).queue();
                }, () -> {
                    MessageEmbed notExist = ResponseHelper.createEmbed(null, "Tag does not exist", Color.RED, event.getUser()).build();
                    event.getHook().sendMessageEmbeds(notExist).queue();
                });
            }
        }
    }

    @Override
    public void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        String value = focusedOption.getValue();
        if (!event.getSubcommandName().equals("add") && focusedOption.getName().equals("name")) {
            List<Command.Choice> choices;
            if (value.isEmpty()) {
                choices = tagRepository.findAll().stream()
                        .limit(25)
                        .map(tag -> new Command.Choice(tag.getName(), tag.getName()))
                        .toList();
            } else {
                choices = tagRepository.findAll().stream()
                        .limit(25)
                        .filter(tag -> tag.getTitle().contains(value.toLowerCase()))
                        .map(tag -> new Command.Choice(tag.getName(), tag.getName()))
                        .toList();
            }
            event.replyChoices(choices).queue();
        }
    }

}
