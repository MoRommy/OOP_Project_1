package fileio;

import entertainment.Season;

import java.util.ArrayList;

/**
 * Information about a tv show, retrieved from parsing the input test files
 * <p>
 * DO NOT MODIFY
 */
public final class SerialInputData extends ShowInput {
    /**
     * Number of seasons
     */
    private final int numberOfSeasons;
    /**
     * Season list
     */
    private final ArrayList<Season> seasons;

    public SerialInputData(final String title, final ArrayList<String> cast,
                           final ArrayList<String> genres,
                           final int numberOfSeasons, final ArrayList<Season> seasons,
                           final int year) {
        super(title, year, cast, genres);
        this.numberOfSeasons = numberOfSeasons;
        this.seasons = seasons;
    }

    /**
     *
     * @return
     */
    public int getDuration() {
        int sum = 0;
        for (Season s : seasons) {
            sum += s.getDuration();
        }
        return sum;
    }

    /**
     *
     * @return
     */
    public int getNumberSeason() {
        return numberOfSeasons;
    }

    /**
     *
     * @return
     */
    public Double getRating() {
        double rating = 0.0;
        for (Season season : getSeasons()) {
            double r = season.getRating();
            if (r > 0) {
                rating += r;
            }
        }
        if (rating > 0) {
            return rating / seasons.size();
        }
        return 0.0;
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

    @Override
    public String toString() {
        return "SerialInputData{" + " title= "
                + super.getTitle() + " " + " year= "
                + super.getYear() + " cast {"
                + super.getCast() + " }\n" + " genres {"
                + super.getGenres() + " }\n "
                + " numberSeason= " + numberOfSeasons
                + ", seasons=" + seasons + "\n\n" + '}';
    }
}
