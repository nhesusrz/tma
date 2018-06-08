package ontology;

import jade.content.Concept;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Movie implements Concept {

    private String name;
    private Date year;
    private String director;
    private String actors;
    private Float utility;

    private DateFormat format = new SimpleDateFormat("yyyy", Locale.ENGLISH);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getYear() {
        return year;
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public Float getUtility() {
        return utility;
    }

    public void setUtility(Float utility) {
        this.utility = utility;
    }

    @Override
    public String toString() {
        return "Name: " + name + " Year: " + year + " Director: " + director + " Actors: " + actors + " Utility: " + utility;
    }
}
