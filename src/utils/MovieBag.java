package utils;

import ontology.Movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MovieBag {

    private String name;
    private ArrayList<Movie> movies;

    public MovieBag(String name, Object[] list) {
        this.name = "BAG_" + name;
        TmaLogger.get().debug("Creating the bag: " + this.name);
        movies = createMovieList(list);
        Random seed = new Random();
        Collections.shuffle(movies, seed);
        TmaLogger.get().debug("Movie bag " + this.name + " shuffled using seed: " + seed);
    }

    public boolean isEmpty() {
        if (movies != null)
            movies.isEmpty();
        return false;
    }

    public Movie pickUp() {
        if (!movies.isEmpty()) {
            Movie movie = movies.get(0);
            movies.remove(0);
            TmaLogger.get().debug("Pick Up - Movie bag: " + this.name + " Movie: " + movie.getName() + " Utility: " + movie.getUtility());
            return movie;
        }
        return null;
    }

    private ArrayList<Movie> createMovieList(Object[] list) {
        String[] string_list = Arrays.copyOf(list, list.length, String[].class);
        ArrayList<Movie> movies = new ArrayList<Movie>();
        for (int i = 0; i < string_list.length; i = i + 5) {
            Movie movie = new Movie(string_list[i], string_list[i + 1], string_list[i + 2], string_list[i + 3], Integer.parseInt(string_list[i + 4]));
            movies.add(movie);
        }
        TmaLogger.get().debug("Movie bag created: " + movies.toString());
        return movies;
    }
}
