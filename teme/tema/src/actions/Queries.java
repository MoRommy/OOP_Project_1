package actions;

import actor.ActorsAwards;
import fileio.Input;
import fileio.ActionInputData;
import fileio.MovieInputData;
import fileio.SerialInputData;
import fileio.UserInputData;
import fileio.ActorInputData;

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
            case "shows" -> getShows(input, action);
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
        int n = action.getNumber();
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
        return createUsersStringResult(users, n);
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
        int n = action.getNumber();

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
        return createActorsStringResult(actors, n);
    }

    private static String getActorsByAwardsAndDescription(final Input input,
                                                          final ActionInputData action) {
        int n = action.getNumber();

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
        return createActorsStringResult(actors, n);
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
     * @param action action
     * @return result
     */
    public static String getShows(final Input input, final ActionInputData action) {
        //filterShows(input, action.getFilters());

        return null;
    }

    /**
     *
     * @param input database
     * @param filters filters
     */
    private static void filterShows(final Input input, final List<List<String>> filters) {
        int year = 0;
        if (filters.get(0).get(0) != null) {
            year = Integer.parseInt(filters.get(0).get(0));
        }
        String genre = filters.get(1).get(0);
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

}
