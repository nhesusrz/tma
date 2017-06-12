package ontology;

import jade.content.Concept;

public class Movie implements Concept {

	private static final long serialVersionUID = 4243753784362292136L;
	private String name;
	private String year;
	private String director;
	private String actors;

	private int utility;

	public Movie(String name, String year, String director, String actors, int utility) {
		this.name = name;
		this.year = year;
		this.director = director;
		this.actors = actors;
		this.utility = utility;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
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

	public int getUtility() { return utility; }

	public void setUtility(int utility) { this.utility = utility; }

	@Override
	public String toString(){
		return "Name: " + name + " Year: " + year + "Utility: " + utility;
	}
}
