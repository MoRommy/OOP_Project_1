package actions;

import entertainment.Season;
import fileio.Input;
import fileio.ActionInputData;
import fileio.MovieInputData;
import fileio.SerialInputData;
import fileio.UserInputData;

import java.util.ArrayList;

public final class Commands {

    private Commands() {

    }

    /**
     * Switches the command and run it
     * @param input database
     * @param action action
     * @return result
     */
    public static String act(final Input input, final ActionInputData action) {
        return switch (action.getType()) {
            case "view" -> viewVideo(input, action);
            case "favorite" -> favoriteVideo(input, action);
            case "rating" -> setRating(input, action);
            default -> null;
        };
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String viewVideo(final Input input, final ActionInputData action) {
        String video = action.getTitle();
        String username = action.getUsername();
        for (UserInputData user : input.getUsers()) {
            if (user.getUsername().equals(username)) {
                user.getHistory().merge(video, 1, Integer::sum);
                return "success -> " + video
                       + " was viewed with total views of " + user.getHistory().get(video);
            }
        }
        return "User not found!";
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String favoriteVideo(final Input input, final ActionInputData action) {
        String video = action.getTitle();
        String username = action.getUsername();
        for (UserInputData user : input.getUsers()) {
            if (user.getUsername().equals(username)) {
                for (String movie : user.getFavoriteMovies()) {
                    if (movie.equals(video)) {
                        return "error -> " + video + " is already in favourite list";
                    }
                }
                if (!user.getHistory().containsKey(video)) {
                    return "error -> " + video + " is not seen";
                }
                user.addToFavourite(video);
                return "success -> " + video + " was added as favourite";
            }
        }
        return "User not found!";
    }

    /**
     *
     * @param input database
     * @param action action
     * @return result
     */
    public static String setRating(final Input input, final ActionInputData action) {
        String video = action.getTitle();
        String username = action.getUsername();
        double rating = action.getGrade();
        int seasonNumber = action.getSeasonNumber();
        String video2 = video;
        if (seasonNumber > 0) {
            video2 += String.valueOf(seasonNumber);
        }
        for (UserInputData user : input.getUsers()) {
            if (user.getUsername().equals(username)) {
                if (!user.getHistory().containsKey(video)) {
                    return "error -> " + video + " is not seen";
                }
                for (String movie: user.getRatedMovies()) {
                    if (movie.equals(video2)) {
                        return "error -> " + video + " has been already rated";
                    }
                }
                ArrayList<String> ratedMovies = user.getRatedMovies();
                ratedMovies.add(video2);
                user.setRatedMovies(ratedMovies);
                if (seasonNumber > 0) {
                    for (SerialInputData serial : input.getSerials()) {
                        if (serial.getTitle().equals(video)) {
                            Season season = serial.getSeasons().get(seasonNumber - 1);
                            season.addRating(rating);
                            return "success -> " + video + " was rated with " + rating + " by "
                                                                                    + username;
                        }
                    }
                }
                for (MovieInputData movie : input.getMovies()) {
                    if (movie.getTitle().equals(video)) {
                        movie.getRatings().add(rating);
                        return "success -> " + video + " was rated with " + rating + " by "
                                                                                    + username;
                    }
                }
            }
        }
        return "User not found!";
    }
}
