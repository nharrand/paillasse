package se.kth.assertteam;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import spark.Request;
import spark.Response;

import java.io.File;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class Server {

    @Parameter(names = {"--help", "-h"}, help = true, description = "Display this message.")
    private boolean help;

	@Parameter(names = {"--experiment", "-e"}, description = "Path to configuration file describing the experiment to distribute")
	private String experimentConfig;

	@Parameter(names = {"--data", "-d"}, description = "Input data.")
	private String dataPath;

	@Parameter(names = {"--log", "-l"}, description = "Path to log file (Default: stdo)")
	private String logPath = "./pailasse.log";

	@Parameter(names = {"--result", "-r"}, description = "Path to result file (Default: results.csv)")
	private String resultsPath = "resources/results-paillasse.csv";

	@Parameter(names = {"--static-files", "-s"}, description = "Path to static files (Default: ./resources)")
	private String staticFilesPath = "resources";

	@Parameter(names = {"--port", "-p"}, description = "Port of the server. (Default 8080)")
	private int port = 8080;



	public static void log(Request req) {
		System.out.println("[" + req.ip() + "] " + req.pathInfo() + " -> " + req.body());
	}


	public static void main(String[] args) throws Exception {

		//Parse Arguments
		Server serverConfig = new Server();
		JCommander jc = new JCommander(serverConfig);
		jc.parse(args);
		if(serverConfig.help || serverConfig.experimentConfig == null || serverConfig.dataPath == null) {
			jc.usage();
			System.exit(-1);
		}
		int SERVER_PORT = serverConfig.port;

		File logs = new File(serverConfig.logPath);
		File results = new File(serverConfig.resultsPath);
		JSONParser p = new JSONParser();
		ProgressManager progressManager = new ProgressManager(ExperimentFactory.buildExperiment(serverConfig.experimentConfig, serverConfig.dataPath), logs, results);



		//Init config generator

		//Setup routes
		staticFiles.externalLocation(serverConfig.staticFilesPath);
		port(SERVER_PORT);

		//Dashboard
		get("/", (Request req, Response res) -> {
			res.type("text/html");
			res.status(200);
			return progressManager.getHtmlOverview();
		});


		//Register worker
		get("/getHostName", (Request req, Response res) -> {
			log(req);
			res.status(200);
			JSONObject json = new JSONObject();
			json.put("workerName", progressManager.getNewHostName());

			res.type("application/json");
			return json.toString();
		});

		//Get experience configuration (potentially multi steps)
		get("/getConfiguration", (Request req, Response res) -> {
			log(req);
			String worker = req.headers("workerName");

			JSONObject cfg = progressManager.getConfig(worker);
			if(cfg == null) {
				res.status(204);
				return new JSONObject();
			}

			res.status(200);

			System.out.println("[Server] Sent config: " + cfg.toJSONString());


			res.type("application/json");
			return cfg.toString();
		});

		//Post step results
		post("/postResult", (Request req, Response res) -> {
			log(req);
			String worker = req.headers("workerName");
			String raw = req.body();
			Object parsed = null;
			try {
				parsed = p.parse(raw);
			} catch (Exception e) {
				System.err.println("Error while parsing json");
			}
			if(!(parsed instanceof  JSONObject)) {
				res.status(400);
				return "Error, unable to parse JSON.";
			}
			JSONObject configResults = (JSONObject) parsed;
			progressManager.postResults(worker, (JSONObject) configResults.get("result"));
			System.err.println("Ok");
			res.status(200);
			return "";
		});

		//Post new experiment line
		post("/postNEL", (Request req, Response res) -> {
			log(req);
			String raw = req.body();
			Object parsed = null;
			try {
				parsed = p.parse(raw);
			} catch (Exception e) {
				System.err.println("Error while parsing json");
			}
			if(!(parsed instanceof  JSONObject)) {
				res.status(400);
				return "Error, unable to parse JSON.";
			}
			JSONObject initialStore = (JSONObject) parsed;
			boolean success = progressManager.addNEL(initialStore);
			System.err.println(success ? "Ok" : "KO");
			res.status(200);
			return "";
		});

		//Get Dashboard info
		get("/getOverview", (Request req, Response res) -> {
			log(req);
			JSONObject cfg = progressManager.getOverview();
			if(cfg == null) {
				res.status(204);
				return new JSONObject();
			}

			res.status(200);

			System.out.println("[Server] Sent config: " + cfg.toJSONString());


			res.type("application/json");
			return cfg.toString();
		});


		System.out.println("Server is running...");
	}
}
