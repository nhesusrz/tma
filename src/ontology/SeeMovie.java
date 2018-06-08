package ontology;

import java.util.Date;

import jade.content.AgentAction;

public class SeeMovie implements AgentAction {

    private Movie movie;
    private Date date;

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        if (movie != null && date != null)
            return ("Title: " + movie.getName() + " Date: " + getDate());
        return "Empty";
    }
}
