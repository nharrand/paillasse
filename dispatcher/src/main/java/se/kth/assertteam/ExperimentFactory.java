package se.kth.assertteam;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import se.kth.assertteam.exp.ExperimentLine;
import se.kth.assertteam.exp.Step;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExperimentFactory {

	public static Queue<ExperimentLine> buildExperiment(String pathToConfig, String pathToData) throws IOException, ParseException {
		Queue<ExperimentLine> experimentLines = new ConcurrentLinkedDeque<>();
		List<String> keys = new ArrayList<>();

		JSONParser parser = new JSONParser();
		JSONObject config = (JSONObject) parser.parse(
				Files.lines(Paths.get(pathToConfig), StandardCharsets.UTF_8).collect(Collectors.joining("\n"))
		);

		List<Step> stepList = (List<Step>)(((JSONArray) config.get("steps")).stream()
				.map(s -> { JSONObject step = (JSONObject) s;
						return new Step(
								(String) step.get("step"),
								(List<String>) step.get("parameters"),
								(List<String>) step.get("output")
						);
					})
				.collect(Collectors.toList()));
		try (Stream<String> stream = Files.lines(Paths.get(pathToData), StandardCharsets.UTF_8)) {
			Spliterator<String> lines = stream.spliterator();

			lines.tryAdvance(
					h -> {
						String rawKeys[] = h.split(",");
						for (String k: rawKeys) {
							keys.add(k);
						}
					}
			);

			lines.forEachRemaining(s -> {
				Map<String,String> store = new LinkedHashMap<>();

				String fields[] = s.split(",");
				for(int i = 0; i < keys.size(); i++) {
					store.put(keys.get(i), fields[i]);
				}

				ExperimentLine l = new ExperimentLine(stepList, store, fields[0]);

				experimentLines.add(l);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		return experimentLines;
	}
}
