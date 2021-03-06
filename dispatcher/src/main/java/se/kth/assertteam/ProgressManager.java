package se.kth.assertteam;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.kth.assertteam.exp.ExperimentLine;
import se.kth.assertteam.exp.MalformedStepException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ProgressManager {

	ConcurrentHashMap<String,ExperimentLine> hostTasks = new ConcurrentHashMap<>();
	Queue<ExperimentLine> remainingTasks;
	int successes = 0;
	int failures = 0;

	File logFile;
	File resultsFile;

	public Set<String> resultsColumnOrder;



	public ProgressManager(Queue<ExperimentLine> tasks, File logs, File results) {
		this.remainingTasks = tasks;
		this.logFile = logs;
		this.resultsFile = results;

		//Set report column order
		if(!tasks.isEmpty()) {
			//First column is worker name.

			//Set of parameters
			resultsColumnOrder = new LinkedHashSet<String>();
			resultsColumnOrder.addAll(tasks.peek().steps.keySet().stream().flatMap(s -> s.parameters.stream()).collect(Collectors.toSet()));

			//Set of output values
			resultsColumnOrder.addAll(tasks.peek().steps.keySet().stream().flatMap(s -> s.results.stream()).collect(Collectors.toSet()));

			//Step status
			String steps = tasks.peek().steps.keySet().stream().map(s -> s.name).collect(Collectors.joining(","));

			//Last column is EL success
			try {
				FileUtils.write(resultsFile,
						"Worker," + resultsColumnOrder.stream().collect(Collectors.joining(",")) + "," + steps + ",EL_success\n",
						Charset.defaultCharset(),
						false
				);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	int workerCount = 0;
	synchronized public String getNewHostName() {
		String r = "Worker-" + workerCount;
		workerCount++;
		return r;
	}

	synchronized public JSONObject getConfig(String hostname) {
		//Continue experiment line if it exist and it is not over
		if(hostTasks.containsKey(hostname)) {
			try {
				return hostTasks.get(hostname).getCurrentStepJSON();
			} catch (MalformedStepException e) {
				ExperimentLine el = hostTasks.get(hostname);
				System.out.println("[Progress Manager] abort task " + el);
				try {
					FileUtils.write(logFile, "[Progress Manager] abort task " + el, Charset.defaultCharset(), true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				el.abort();
			}
		}

		while(!remainingTasks.isEmpty()) {
			try {
				ExperimentLine el = remainingTasks.poll();
				JSONObject config = el.getCurrentStepJSON();
				hostTasks.put(hostname, el);
				return config;
			} catch (MalformedStepException e) {
				e.printStackTrace();
				try {
					FileUtils.write(logFile, "[Progress Manager] abort task ", Charset.defaultCharset(), true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}

	synchronized public void postResults(String hostname, JSONObject results) {
		System.err.println("[postResults] from " + hostname);
		ExperimentLine el = hostTasks.get(hostname);
		if(el != null) {
			boolean success = !results.containsKey("failure");
			el.reportResult(results, success);
			if(el.isOver()) {
				hostTasks.remove(hostname);
				el.report(resultsFile, resultsColumnOrder, hostname);
				if(el.isSuccess()) successes++;
				else failures++;
			}
		}
		System.err.println("[postResults] done");
	}


	synchronized public JSONObject getOverview() {
		JSONObject res = new JSONObject();
		res.put("pending", hostTasks.size());
		res.put("successes", successes);
		res.put("failures", + failures);
		res.put("remaining", remainingTasks.size());
		JSONArray hostStatuses = new JSONArray();

		for(String host: hostTasks.keySet()) {
			JSONObject h = new JSONObject();
			ExperimentLine cfg = hostTasks.get(host);

			h.put("tname", cfg.getName());
			h.put("host", host);
			h.put("step", (cfg.getCurrentStep() == null ? "null" : cfg.getCurrentStep().name));
			hostStatuses.add(h);

		}
		res.put("hoststatuses", hostStatuses);
		return res;
	}


	synchronized public String getHtmlOverview() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<html>");
		stringBuilder.append("<header>");
		stringBuilder.append("<title>Paillasse Dashboard</title>");
		stringBuilder.append("</header>");


		stringBuilder.append("<body>");
		stringBuilder.append("<h2>Paillasse Dashboard</h2>");

		stringBuilder.append("<h3>Pending (" + hostTasks.size() + ") | Successes (" + successes + ") | Failures (" + failures + ") | To Do (" + remainingTasks.size() + ")</h3>");

		stringBuilder.append("<table>");
		stringBuilder.append("<thead>");
		stringBuilder.append("<td>Name</td><td>Host</td><td>Step</td>");
		stringBuilder.append("</thead>");

		for(String host: hostTasks.keySet()) {
			ExperimentLine cfg = hostTasks.get(host);
			stringBuilder.append("<tr><td>" + cfg.getName() + "</td><td>" + host + "</td><td>" + (cfg.getCurrentStep() == null ? "null" : cfg.getCurrentStep().name) + "</td></tr>");
		}
		stringBuilder.append("</table>");

		stringBuilder.append("</body>");

		stringBuilder.append("</html>");
		return stringBuilder.toString();
	}
}
