package fileio;

import java.util.ArrayList;

/**
 * Information about a movie, retrieved from parsing the input test files
 * <p>
 * DO NOT MODIFY
 */
public final class MovieInputData extends ShowInput {
    /**
     * Duration in minutes of a season
     */
    private final int duration;
    private final ArrayList<Double> rating;

    public MovieInputData(final String title, final ArrayList<String> cast,
                          final ArrayList<String> genres, final int year,
                          final int duration) {
        super(title, year, cast, genres);
        this.duration = duration;
        this.rating = new ArrayList<>();
    }

    public int getDuration() {
        return duration;
    }

    public ArrayList<Double> getRatings() {
        return rating;
    }

    /**
     *
     * @return
     */
    public Double getRating() {
        Double rate = 0.0;
        for (Double r : getRatings()) {
            if (r > 0) {
                rate += r;
            }
        }
        if (rate > 0) {
            return rate / getRatings().size();
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "MovieInputData{" + "title= "
                + super.getTitle() + "year= "
                + super.getYear() + "duration= "
                + duration + "cast {"
                + super.getCast() + " }\n"
                + "genres {" + super.getGenres() + " }\n ";
    }
}
