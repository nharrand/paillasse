package se.kth.assertteam.exp;

import java.util.List;

public class Step {
	public String name;
	public List<String> parameters;
	public List<String> results;

	public Step(String name, List<String> parameters, List<String> results) {
		this.name=name;
		this.parameters=parameters;
		this.results=results;
	}
}
