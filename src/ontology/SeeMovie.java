package ontology;

import java.util.Date;

import jade.content.AgentAction;

public class SeeMovie implements AgentAction {

	private static final long serialVersionUID = 3984072537199280137L;
	private Movie movie;
	private Date date;

	public SeeMovie(Movie movie, Date date) {
		this.movie = movie;
		this.date = date;
	}

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
			return ("Title: " + movie.getName() + " Date: " + date.toString());
		return "Empty";
	}

}
