package actions;

import actor.ActorsAwards;

import fileio.Input;
import fileio.ActionInputData;
import fileio.MovieInputData;
import fileio.SerialInputData;
import fileio.UserInputData;
import fileio.ActorInputData;
import fileio.ShowInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static common.Constants.MAX_INT_COUNTER;
import static common.Constants.WORDS_ID;
import static common.Constants.AWARDS_ID;

public final class Queries {

    private Queries() {

    }

    /**
     * Switches the query and run it
     * @param input database
     * @param action action
     * @return result
     */
    public static String act(final Input input, final ActionInputData action) {
        return switch (action.getObjectType()) {
            case "users" -> getUsers(input, action);
            case "actors" -> getActors(input, action);
            case "shows", "movies" -> getShows(input, action);
            default -> null;
        };
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String getUsers(final Input input, final ActionInputData action) {
        List<UserInputData> users = new ArrayList<>();
        for (UserInputData user : input.getUsers()) {
            if (user.getRatedMovies().size() > 0) {
                users.add(user);
            }
        }
        users.sort(Comparator.comparingInt(o -> o.getRatedMovies().size()));
        if (action.getSortType().equals("desc")) {
            Collections.reverse(users);
        }
        return createUsersStringResult(users, action.getNumber());
    }

    /**
     *
     * @param users users
     * @param n number of users
     * @return string with the users
     */
    private static String createUsersStringResult(final List<UserInputData> users, final int n) {
        String res = "Query result: [";
        int m = n;
        if (n == 0) {
            m = MAX_INT_COUNTER;
        }
        for (int i = 0; i < Math.min(m - 1, users.size() - 1); i++) {
            res = res.concat(users.get(i).getUsername() + ", ");
        }
        if (users.size() > 0) {
            String lastUser = users.get(Math.min(m - 1, users.size() - 1)).getUsername();
            return res + lastUser + "]";
        }
        return res + "]";
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String getActors(final Input input, final ActionInputData action) {
        if (action.getCriteria().equals("filter_description")) {
            return getActorsByAwardsAndDescription(input, action);
        }
        if (action.getCriteria().equals("awards")) {
            return getActorsByAwardsAndDescription(input, action);
        }
        if (action.getCriteria().equals("average")) {
            return getActorsByAverage(input, action);
        }
        return null;
    }

    /**
     *
     * @param input input
     * @param action action
     * @return result
     */
    private static String getActorsByAverage(final Input input, final ActionInputData action) {
        calculateActorRating(input);
        List<ActorInputData> actors = filterActors(input, action.getFilters());

        actors.sort((o1, o2) -> {
            if (o1.getRating() > o2.getRating()) {
                return 1;
            }
            if (o1.getRating() < o2.getRating()) {
                return -1;
            }
            return o1.getName().compareTo(o2.getName());
        });

        actors.removeIf(actor -> actor.getRating() == 0.0);

        if (action.getSortType().equals("desc")) {
            Collections.reverse(actors);
        }
        return createActorsStringResult(actors, action.getNumber());
    }

    private static String getActorsByAwardsAndDescription(final Input input,
                                                          final ActionInputData action) {

        List<ActorInputData> actors = filterActors(input, action.getFilters());
        if (action.getCriteria().equals("awards")) {
            actors.sort((o1, o2) -> {
                if (o1.getAwards().size() > o2.getAwards().size()) {
                    return 1;
                }
                if (o1.getAwards().size() < o2.getAwards().size()) {
                    return -1;
                }
                return o1.getName().compareTo(o2.getName());
            });
        } else {
            actors.sort(Comparator.comparing(ActorInputData::getName));
        }
        if (action.getSortType().equals("desc")) {
            Collections.reverse(actors);
        }
        return createActorsStringResult(actors, action.getNumber());
    }

    /**
     *
     * @param actors actors
     * @param n number of actors
     * @return string with the actors
     */
    private static String createActorsStringResult(final List<ActorInputData> actors, final int n) {
        String res = "Query result: [";
        int m = n;
        if (n == 0) {
            m = MAX_INT_COUNTER;
        }
        for (int i = 0; i < Math.min(m - 1, actors.size() - 1); i++) {
            res = res.concat(actors.get(i).getName() + ", ");
        }
        if (actors.size() > 0) {
            String lastActor = actors.get(Math.min(m - 1, actors.size() - 1)).getName();
            return res + lastActor + "]";
        }
        return res + "]";
    }

    /**
     *
     * @param input databaase
     */
    private static void calculateActorRating(final Input input) {
        for (ActorInputData actor : input.getActors()) {
            double rating = 0.0;
            int numberOfMovies = 0;
            for (MovieInputData movie : input.getMovies()) {
                for (String cast : movie.getCast()) {
                    if (cast.equals(actor.getName())) {
                        double r = movie.getRating();
                        if (r > 0) {
                            rating += r;
                            numberOfMovies++;
                        }
                        break;
                    }
                }
            }
            for (SerialInputData serial : input.getSerials()) {
                for (String cast : serial.getCast()) {
                    if (cast.equals(actor.getName())) {
                        double r = serial.getRating();
                        if (r > 0) {
                            rating += r;
                            numberOfMovies++;
                        }
                        break;
                    }
                }
            }
            if (numberOfMovies > 0) {
                actor.setRating(rating / numberOfMovies);
            } else {
                actor.setRating(0.0);
            }
        }
    }

    /**
     *
     * @param input database
     * @param filters filters
     * @return list with filtered actors
     */
    public static List<ActorInputData> filterActors(final Input input,
                                                    final List<List<String>> filters) {
        List<ActorInputData> actors = new ArrayList<>();

        List<String> words = filters.get(WORDS_ID);
        List<String> awards = filters.get(AWARDS_ID);

        for (ActorInputData actor : input.getActors()) {
            int ok = 1;
            if (words != null) {
                for (String word : words) {
                    if (!actor.getCareerDescription().contains(word)) {
                        ok = 0;
                        break;
                    }
                }
            }
            if (awards != null) {
                for (String award : awards) {
                    ActorsAwards aw = ActorsAwards.valueOf(award);
                    if (!actor.getAwards().containsKey(aw)) {
                        ok = 0;
                        break;
                    }
                }
            }
            if (ok == 0) {
                continue;
            }
            actors.add(actor);
        }
        return actors;
    }

    /**
     *
     * @param input database
     * @param action action
     * @return shows
     */
    public static String getShows(final Input input, final ActionInputData action) {
        if (action.getCriteria().equals("longest")) {
            return getShowsByLongest(input, action);
        }
        if (action.getCriteria().equals("favorite")) {
            return getShowsByFavorite(input, action);
        }
        if (action.getCriteria().equals("most_viewed")) {
            return getShowsByMostViewed(input, action);
        }
        if (action.getCriteria().equals("ratings")) {
            return getShowsByRatings(input, action);
        }
        return "asdasdsadasd";
    }

    /**
     *
     * @param input database
     * @param action action
     * @return shows
     */
    private static String getShowsByLongest(final Input input, final ActionInputData action) {
        List<ShowInput> shows = filterShows(input, action.getFilters());
        shows.sort((o1, o2) -> {
            if (o1.getDuration() > o2.getDuration()) {
                return 1;
            }
            if (o1.getDuration() < o2.getDuration()) {
                return -1;
            }
            return o1.getTitle().compareTo(o2.getTitle());
        });
        shows.removeIf(show -> show.getDuration() == 0.0);
        if (action.getSortType().equals("desc")) {
            Collections.reverse(shows);
        }
        return createShowsStringResult(shows, action.getNumber());
    }

    /**
     *
     * @param input database
     * @param action action
     * @return shows
     */
    private static String getShowsByFavorite(final Input input, final ActionInputData action) {
        List<ShowInput> shows = filterShows(input, action.getFilters());
        shows.sort((o1, o2) -> {
            int movieso1 = 0, movieso2 = 0;
            for (UserInputData user : input.getUsers()) {
                for (String movie : user.getFavoriteMovies()) {
                    if (movie.equals(o1.getTitle())) {
                        movieso1++;
                    }
                    if (movie.equals(o2.getTitle())) {
                        movieso2++;
                    }
                }
            }
            if (movieso1 > movieso2) {
                return 1;
            }
            if (movieso1 < movieso2) {
                return -1;
            }
            return o1.getTitle().compareTo(o2.getTitle());
        });

        for (int i = 0; i < shows.size(); i++) {
            ShowInput video = shows.get(i);
            int ok = 0;
            for (UserInputData user : input.getUsers()) {
                for (String movie : user.getFavoriteMovies()) {
                    if (movie.equals(video.getTitle())) {
                        ok = 1;
                        break;
                    }
                }
            }
            if (ok == 0) {
                shows.remove(video);
            }
        }

        if (action.getSortType().equals("desc")) {
            Collections.reverse(shows);
        }
        return createShowsStringResult(shows, action.getNumber());
    }

    /**
     *
     * @param input database
     * @param action action
     * @return shows
     */
    private static String getShowsByMostViewed(final Input input, final ActionInputData action) {
        List<ShowInput> shows = filterShows(input, action.getFilters());
        shows.sort((o1, o2) -> {
            int movieso1 = 0, movieso2 = 0;
            for (UserInputData user : input.getUsers()) {
                if (user.getHistory().containsKey(o1.getTitle())) {
                    movieso1 += user.getHistory().get(o1.getTitle());
                }
                if (user.getHistory().containsKey(o2.getTitle())) {
                    movieso2 += user.getHistory().get(o2.getTitle());
                }
            }
            if (movieso1 > movieso2) {
                return 1;
            }
            if (movieso1 < movieso2) {
                return -1;
            }
            return o1.getTitle().compareTo(o2.getTitle());
        });

        for (int i = 0; i < shows.size(); i++) {
            ShowInput video = shows.get(i);
            int ok = 0;
            for (UserInputData user : input.getUsers()) {
                if (user.getHistory().containsKey(video.getTitle())) {
                    ok = 1;
                    break;
                }
            }
            if (ok == 0) {
                shows.remove(video);
            }
        }

        if (action.getSortType().equals("desc")) {
            Collections.reverse(shows);
        }
        return createShowsStringResult(shows, action.getNumber());
    }

    /**
     *
     * @param input database
     * @param action action
     * @return shows
     */
    private static String getShowsByRatings(final Input input, final ActionInputData action) {
        List<ShowInput> shows = filterShows(input, action.getFilters());
        shows.sort((o1, o2) -> {
            if (o1.getRating() > o2.getRating()) {
                return 1;
            }
            if (o1.getRating() < o2.getRating()) {
                return -1;
            }
            return o1.getTitle().compareTo(o2.getTitle());
        });
        shows.removeIf(show -> show.getRating() == 0.0);
        if (action.getSortType().equals("desc")) {
            Collections.reverse(shows);
        }
        return createShowsStringResult(shows, action.getNumber());
    }

    /**
     *
     * @param shows shows
     * @param n number of shows
     * @return string with shows
     */
    private static String createShowsStringResult(final List<ShowInput> shows, final int n) {
        String res = "Query result: [";
        int m = n;
        if (n == 0) {
            m = MAX_INT_COUNTER;
        }
        for (int i = 0; i < Math.min(m - 1, shows.size() - 1); i++) {
            res = res.concat(shows.get(i).getTitle() + ", ");
        }
        if (shows.size() > 0) {
            String lastActor = shows.get(Math.min(m - 1, shows.size() - 1)).getTitle();
            return res + lastActor + "]";
        }
        return res + "]";
    }

    /**
     *
     * @param input database
     * @param filters filters
     */
    private static List<ShowInput> filterShows(final Input input,
                                               final List<List<String>> filters) {
        List<ShowInput> shows = new ArrayList<>();
        int year = 0;
        if (filters.get(0).get(0) != null) {
            year = Integer.parseInt(filters.get(0).get(0));
        }
        String showGenre = filters.get(1).get(0);

        for (ShowInput show : input.getMovies()) {
            if (show.getYear() == year || year == 0) {
                for (String genre : show.getGenres()) {
                    if (genre.equals(showGenre)) {
                        shows.add(show);
                        break;
                    }
                }
            }
        }
        for (ShowInput show : input.getSerials()) {
            if (show.getYear() == year || year == 0) {
                for (String genre : show.getGenres()) {
                    if (genre.equals(showGenre)) {
                        shows.add(show);
                        break;
                    }
                }
            }
        }
        return shows;
    }

}
