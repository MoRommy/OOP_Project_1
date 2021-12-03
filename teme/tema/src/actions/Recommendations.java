package actions;

import fileio.ActionInputData;
import fileio.Input;
import fileio.ShowInput;
import fileio.UserInputData;

import java.util.*;

public final class Recommendations {

    private Recommendations() {

    }

    /**
     * Switches the query and run it
     * @param input database
     * @param action action
     * @return result
     */
    public static String act(final Input input, final ActionInputData action) {
        return switch (action.getType()) {
            case "standard" -> recommendStandard(input, action);
            case "search" -> recommendSearch(input, action);
            case "favorite" -> recommendFavorite(input, action);
            case "popular" -> recommendPopular(input, action);
            case "best_unseen" -> recommendBestUnseen(input, action);
            default -> null;
        };
    }

    /**
     *
     * @param userName user name
     * @param input database
     * @return the user object with the given name
     */
    public static UserInputData getUser(final String userName, final Input input) {
        for (UserInputData user : input.getUsers()) {
            if (user.getUsername().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    /**
     *
     * @param input database
     * @param user user
     * @return unseen videos
     */
    public static List<ShowInput> getUnseenVideos(final Input input, final UserInputData user) {
        List<ShowInput> unseenVideos = new ArrayList<>();
        for (ShowInput video : input.getMovies()) {
            assert user != null;
            if (!user.getHistory().containsKey(video.getTitle())) {
                unseenVideos.add(video);
            }
        }
        for (ShowInput video : input.getSerials()) {
            assert user != null;
            if (!user.getHistory().containsKey(video.getTitle())) {
                unseenVideos.add(video);
            }
        }
        return unseenVideos;
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String recommendStandard(final Input input, final ActionInputData action) {
        UserInputData user = getUser(action.getUsername(), input);
        if (user != null) {
            List<ShowInput> unseenVideos = getUnseenVideos(input, user);

            if (unseenVideos.size() > 0) {
                return "StandardRecommendation result: " + unseenVideos.get(0).getTitle();
            }
        }
        return "StandardRecommendation cannot be applied!";
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String recommendBestUnseen(final Input input, final ActionInputData action) {
        UserInputData user = getUser(action.getUsername(), input);
        if (user != null) {
            List<ShowInput> unseenVideos = getUnseenVideos(input, user);
            sortByRating(unseenVideos, "");
            if (unseenVideos.size() > 0) {
                return "BestRatedUnseenRecommendation result: " + unseenVideos.get(0).getTitle();
            }
        }
        return "BestRatedUnseenRecommendation cannot be applied!";
    }

    /**
     *
     * @param unseenVideos videos
     */
    private static void sortByRating(final List<ShowInput> unseenVideos, final String secondCryteria) {
        unseenVideos.sort((o1, o2) -> {
            if (o1.getRating() > o2.getRating()) {
                return 1;
            }
            if (o1.getRating() < o2.getRating()) {
                return -1;
            }
            if (secondCryteria.equals("name")) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
            return 0;
        });
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String recommendPopular(final Input input, final ActionInputData action) {
        UserInputData user = getUser(action.getUsername(), input);
        if (user != null && user.getSubscriptionType().equals("PREMIUM")) {
            List<String> genres = getMostViewedGenres(input);
            for (String genre : genres) {
                for (ShowInput video : input.getMovies()) {
                    if (video.getGenres().contains(genre)) {
                        if (!user.getHistory().containsKey(video.getTitle())) {
                            return "PopularRecommendation result: " + video.getTitle();
                        }
                    }
                }
                for (ShowInput video : input.getSerials()) {
                    if (video.getGenres().contains(genre)) {
                        if (!user.getHistory().containsKey(video.getTitle())) {
                            return "PopularRecommendation result: " + video.getTitle();
                        }
                    }
                }
            }
            return "PopularRecommendation cannot be applied!";
        }
        return "PopularRecommendation cannot be applied!";
    }

    public static Map<String, Integer> sortByValue(Map<String, Integer> hm)
    {
        List<Map.Entry<String, Integer> > list = new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);
        Map<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static void increment(Map<String, Integer> map, String key, int n) {
        Integer count = map.getOrDefault(key, 0);
        map.put(key, count + n);
    }

    private static List<String> getMostViewedGenres(Input input) {
        Map<String, Integer> genresViews = new LinkedHashMap<>();
        for (ShowInput video : input.getMovies()) {
            int totalVideoViews = 0;
            for (UserInputData user : input.getUsers()) {
                if (user.getHistory().containsKey(video.getTitle())) {
                    totalVideoViews += user.getHistory().get(video.getTitle());
                }
            }
            for (String videoGenre : video.getGenres()) {
                increment(genresViews, videoGenre, totalVideoViews);
            }
        }
        for (ShowInput video : input.getSerials()) {
            int totalVideoViews = 0;
            for (UserInputData user : input.getUsers()) {
                if (user.getHistory().containsKey(video.getTitle())) {
                    totalVideoViews += user.getHistory().get(video.getTitle());
                }
            }
            for (String videoGenre : video.getGenres()) {
                increment(genresViews, videoGenre, totalVideoViews);
            }
        }
        genresViews = sortByValue(genresViews);
        return new ArrayList<>(genresViews.keySet());
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String recommendFavorite(final Input input, final ActionInputData action) {
        UserInputData user = getUser(action.getUsername(), input);
        String favoriteVideo = "";
        int maxFavourites = 0;
        if (user != null && user.getSubscriptionType().equals("PREMIUM")) {
            for (ShowInput video : input.getMovies()) {
                if (!user.getHistory().containsKey(video.getTitle())) {
                    int favourites = 0;
                    for (UserInputData u : input.getUsers()) {
                        if (u.getFavoriteMovies().contains(video.getTitle())) {
                            favourites++;
                        }
                    }
                    if (favourites > maxFavourites) {
                        maxFavourites = favourites;
                        favoriteVideo = video.getTitle();
                    }
                }
            }
            if (!favoriteVideo.equals("")) {
                return "FavoriteRecommendation result: " + favoriteVideo;
            }
        }
        return "FavoriteRecommendation cannot be applied!";
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String recommendSearch(final Input input, final ActionInputData action) {
        UserInputData user = getUser(action.getUsername(), input);
        if (user != null && user.getSubscriptionType().equals("PREMIUM")) {
            List<ShowInput> videos = getUnseenVideos(input, user);
            filterVideosByGenre(videos, action.getGenre());
            sortByRating(videos, "name");
            if (videos.size() > 0) {
                return "SearchRecommendation result: " + createVideosStringResult(videos);
            }
        }
        return "SearchRecommendation cannot be applied!";
    }

    /**
     *
     * @param videos videos
     * @return string with the videos
     */
    private static String createVideosStringResult(final List<ShowInput> videos) {
        String res = "[";
        for (int i = 0; i < videos.size() - 1; i++) {
            res = res.concat(videos.get(i).getTitle() + ", ");
        }
        if (videos.size() > 0) {
            String lastvideo = videos.get(videos.size() - 1).getTitle();
            return res + lastvideo + "]";
        }
        return res + "]";
    }

    /**
     * remove from the videos list those who does not have that particular genre
     * @param videos videos
     * @param genre genre
     */
    public static void filterVideosByGenre(final List<ShowInput> videos, String genre) {
        videos.removeIf(video -> !video.getGenres().contains(genre));
    }

}
