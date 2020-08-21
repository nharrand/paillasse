package se.kth.assertteam.exp;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExperimentLine {
	public Map<Step, STATUS> steps = new LinkedHashMap<>();
	Iterator<Step> iterator;
	Step current;
	Map<String,String> store;
	String name;
	boolean hasAborted = false;

	public ExperimentLine(List<Step> stepList, Map<String,String> initialStore, String name) {
		steps.put(new Step("NEL", new LinkedList<>(), new LinkedList<>()), STATUS.NOT_COMPLETED);
		for(Step s: stepList) {
			steps.put(s, STATUS.NOT_COMPLETED);
		}
		iterator = steps.keySet().iterator();
		current = iterator.next();
		store = initialStore;
		this.name = name;
	}

	public String getName() {
		return name;
	}


	public Step getCurrentStep() {
		return current;
	}


	public JSONObject getCurrentStepJSON() throws MalformedStepException {
		JSONObject config = new JSONObject();
		config.put("step", current.name);
		for(String p: current.parameters) {
			if(store.containsKey(p)) config.put(p,store.get(p));
			else throw new MalformedStepException("The store does not contains a value for parameter " + p);
		}
		return config;
	}

	public void abort() {
		hasAborted = true;
	}


	public boolean isOver() {
		return current == null;
	}

	/**
	 * @param results A map containing the results
	 * @Return true if the experiment line is over.
	 */
	public boolean reportResult(Map<String,Object> results, boolean success) {
		for(Map.Entry<String,Object> e: results.entrySet()) store.put(e.getKey(), e.getValue().toString());
		steps.put(current, success ? STATUS.SUCCESS : STATUS.FAILURE);
		if(iterator.hasNext() && success) {
			current = iterator.next();
			return false;
		} else {
			current = null;
			return true;
		}
	}

	public boolean isSuccess() {
		boolean success = !hasAborted;
		for(STATUS s: steps.values()) {
			success &= (s == STATUS.SUCCESS);
		}
		return success;
	}

	public void report(File resultFile, Set<String> columnOrder, String worker) {
		try {
			//Worker,Parameters,Results,Status
			String line = worker;
			for(String col: columnOrder) {
				line += "," + store.get(col);
			}
			boolean success = !hasAborted;
			for(STATUS s: steps.values()) {
				success &= s == STATUS.SUCCESS;
				line += "," + s.toString();
			}
			line+= "," + success;
			FileUtils.write(resultFile, line + "\n", Charset.defaultCharset(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reportHeader(File resultFile) {
		try {
			//Worker,Parameters,Results,Status
			String line = "Worker";
			Set<String> columns = steps.keySet().stream().flatMap(s -> s.parameters.stream()).collect(Collectors.toSet());
			columns.addAll(steps.keySet().stream().flatMap(s -> s.results.stream()).collect(Collectors.toSet()));
			columns.addAll(steps.keySet().stream().map(s -> s.name).collect(Collectors.toSet()));
			String st = columns.stream().collect(Collectors.joining(","));
			line = join(line,st);
			line = join(line,"EL_Success");
			FileUtils.write(resultFile, line + "\n", Charset.defaultCharset(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String join(String a, String b) {
		return (a == null || a.length() == 0) ? b : ((b == null || b.length() == 0) ? a : a + "," + b);
	}

	enum STATUS {SUCCESS, FAILURE, NOT_COMPLETED}
}
