package com.laioffer.jupiter.recommendation;

import com.laioffer.jupiter.db.MySQLConnection;
import com.laioffer.jupiter.db.MySQLException;
import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.laioffer.jupiter.external.TwitchClient;
import com.laioffer.jupiter.external.TwitchException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemRecommender {
    //add a few constants as default limit for recommendation
    //更加方便让user更改：放到config.property里面读取
    private static final int DEFAULT_GAME_LIMIT = 3;
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;
    // Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip].
    // Add items are related to the top games provided in the argument
    //从top里面找到推荐的资源
    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames) throws RecommendationException {
        //先准备一个空的返回值，如果啥都没有，返回空的list
        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();
        //catch twitch exception
        //label
        outer_loop:
        for (Game game : topGames) {
            List<Item> items;
            try {
                items = client.searchByType(game.getId(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                //转换成我们自己的异常
                throw new RecommendationException("Failed to get recommendation result");
            }
            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    //break到outer loop
                    break outer_loop;
                }
                recommendedItems.add(item);
            }
        }
        return recommendedItems;
    }

    // Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip].
    // All items are related to the items previously favorited by the user.
    // E.g., if a user favorited some videos about game "Just Chatting", then it will return some other videos about the same game.
    private List<Item> recommendByFavoriteHistory(
            Set<String> favoritedItemIds, List<String> favoriteGameIds, ItemType type) throws RecommendationException {
        // Count the favorite game IDs from the database for the given user.
        // E.g. if the favorited game ID list is ["1234", "2345", "2345", "3456"],
        // the returned Map is {"1234": 1, "2345": 2, "3456": 1}
       // java8 parallelStream()更加highlevel
        Map<String, Long> favoriteGameIdByCount =
                favoriteGameIds.parallelStream()
                .collect(Collectors.groupingBy //拿出数据分类
                        (Function.identity(), Collectors.counting() //同一类的放在同一组，统计每一个组的个数
                        ));
        //用循环来做
//        Map<String, Long> favoriteGameIdByCount = new HashMap<>();
//        for (String id : favoriteGameIds) {
//            favoriteGameIdByCount.put(favoriteGameIdByCount.getOrDefault(id, 0) + 1);
//
//        }

        // Sort the game Id by count. E.g. if the input is {"1234": 1, "2345": 2, "3456": 1},
        // the returned Map is {"2345": 2, "1234": 1, "3456": 1} 从高往低排序
        List<Map.Entry<String, Long>> sortedFavoriteGameIdListByCount = new ArrayList<>(
                favoriteGameIdByCount.entrySet());
        sortedFavoriteGameIdListByCount.sort((Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) -> Long
                .compare(e2.getValue(), e1.getValue()));//e2放在e1的前面就是逆序排列
        // See also: https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values

        if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
            sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);
        }

        List<Item> recommendedItems = new ArrayList<>();
        TwitchClient client = new TwitchClient();

        // Search Twitch based on the favorite game IDs returned in the last step.
        outerloop:
        for (Map.Entry<String, Long> favoriteGame : sortedFavoriteGameIdListByCount) {
            List<Item> items;
            try {
                //向twitch发送请求
                items = client.searchByType(favoriteGame.getKey(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }

            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    break outerloop;
                }
                if (!favoritedItemIds.contains(item.getId())) {
                    recommendedItems.add(item);
                }
            }
        }
        return recommendedItems;
    }
    //上面2个helper func
    // Return a map of Item objects as the recommendation result. Keys of the may are [Stream, Video, Clip].
    // Each key is corresponding to a list of Items objects, each item object is a recommended item
    // based on the previous favorite records by the user.
    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        Set<String> favoriteItemIds;
        //favoriteItemIds要从该数据库中去的，只有id没有game的完整信息
        Map<String, List<String>> favoriteGameIds;
        MySQLConnection connection = null;
        try {
            connection = new MySQLConnection();
            favoriteItemIds = connection.getFavoriteItemIds(userId);
            favoriteGameIds = connection.getFavoriteGameIds(favoriteItemIds);
        } catch (MySQLException e) {
            throw new RecommendationException("Failed to get user favorite history for recommendation");
        } finally {
            connection.close();
        }

        for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
            if (entry.getValue().size() == 0) {
                //如果是空的，默认返回topGame
                TwitchClient client = new TwitchClient();
                List<Game> topGames;
                try {
                    topGames = client.topGames(DEFAULT_GAME_LIMIT);
                } catch (TwitchException e) {
                    throw new RecommendationException("Failed to get game data for recommendation");
                }
                //itemType = entry.getKey();
                recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf(entry.getKey()), topGames));
            } else {
                recommendedItemMap.put(entry.getKey(), recommendByFavoriteHistory(favoriteItemIds, entry.getValue(), ItemType.valueOf(entry.getKey())));
            }
        }
        return recommendedItemMap;
    }

    // Return a map of Item objects as the recommendation result. Keys of the map are [Stream, Video, Clip].
    // Each key is corresponding to a list of Items objects, each item object is a recommended item
    // based on the top games currently on Twitch.
    //没有user，比如用户没登录想试用一下我们的系统，我们给他推荐top game
    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        TwitchClient client = new TwitchClient();
        List<Game> topGames;
        try {
            topGames = client.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
            throw new RecommendationException("Failed to get game data for recommendation");
        }

        for (ItemType type : ItemType.values()) {
            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
        }
        return recommendedItemMap;
    }

}
