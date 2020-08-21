# Paillasse

Paillaisse is dead simple minimalistic scientific experiment manager (or said more humbly a bunch of bash scripts, docker images, packed with a small Java server to distribute tasks and keep track of progresses).
It enables an user to distribute modular experments over several workers.

### Experiment design

An experiment is a series of steps described in json configuration file that looks like the following example:

```json
{
  "steps": [
    {
      "step": "clone_repo",
      "parameters": [
        "commit","repo","url"
      ],
      "output":["commit"]
    },
    {
      "step": "cd",
      "parameters": [
        "repo"
      ],
      "output":[]
    },
    {
      "step": "mvn_compile",
      "parameters": [],
      "output":[]
    },
    {
      "step": "mvn_test",
      "parameters": [],
      "output":["nb_test"]
    }
  ]
}
```

Each step corresponds to a bash script of the same name (See `./scripts`). It has a list of parameter and a list of ouputs. (Outputs can be reused as parameter of posterior steps.)

The list of tasks to be distributed is describe in an input dataset. (This dataset needs to contains values for all parameter that are not produced as an output of a previous step.)

Example:

```csv
repo,url,commit
fofa-java,https://github.com/0nise/fofa-java.git,54e7842111e1d1df137b08a4158c400e23d922e3
yibai-java-sdk,https://github.com/100sms/yibai-java-sdk.git,7c2b960cfb4e9424ddcb279f4a62f58d19fc7ade
httpselftest,https://github.com/1and1/httpselftest.git,c3d54a2222b22e44fef0b0e88b298d8bcb5d2f0b
snmpman,https://github.com/1and1/snmpman.git,ad514fbac17aaa0b627fd0370cb7179a42cd9a14
```

### Adding new steps

To add a step, you need to add a script of the same name in `scripts/tasks`.
The script can be hust a wrapper for some other program. The parameter are all in a json file named TASK_NAME.in. Result need to be written in a file TASK_NAME.out in json format. If the task fail, it must exit with a non 0 return code. Otherwise, the file TASK_NAME.out must be non empty (default would be `{}`).

Note that if your step needs some file, the directory lib is added to the worker's docker image.

### Deploying the experiment

Building the server image:

```bash
cd dispatcher
mvn clean install
docker build . -t paillasse-server
```

Running a server instance:

```bash
docker run -v /path/to/your/configuration/dir:/data -p 8090:8090 paillasse-server
```
Your configuration directory must include a `config.json` file as well as a `data.csv` file.


Building the worker image:

```bash
docker build . -t paillasse-worker
```

Running a worker instance:

```bash
docker run -v /path/to/your/workdir:/workdir --network="host" paillasse-worker
```



Supervising the experiment

Open `http://server-ip:server-port/` in your browser.


