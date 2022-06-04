package org.example.ventus.database.util;

import org.example.ventus.database.model.Config;
import org.example.ventus.database.model.SearchHistory;
import org.example.ventus.database.repository.ConfigRepository;
import org.example.ventus.database.repository.SearchHistoryRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseHelper {

    private static ConfigRepository configRepository;
    private static SearchHistoryRepository searchHistoryRepository;

    @Autowired
    public DatabaseHelper(ConfigRepository configRepository, SearchHistoryRepository searchHistoryRepository) {
        DatabaseHelper.configRepository = configRepository;
        DatabaseHelper.searchHistoryRepository = searchHistoryRepository;
    }

    public static @Nullable Long getLogChannelById(long guildId) {
        return configRepository.findById(guildId).map(Config::getLogChannelId).orElse(null);
    }

    public static @Nullable Long getMusicChannelById(long guildId) {
        return configRepository.findById(guildId).map(Config::getMusicChannelId).orElse(null);
    }

    public static List<SearchHistory> getSearchHistory(long userId) {
        return searchHistoryRepository.findById(userId)
                .filter(searchHistory -> searchHistory.getUserId() == userId)
                .stream().toList();
    }

    public static void insertSearchHistory(List<SearchHistory> histories) {
        searchHistoryRepository.saveAllAndFlush(histories);
    }

}
