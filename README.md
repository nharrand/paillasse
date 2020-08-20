# Paillasse

Paillaisse is dead simple minimalistic scientific experiment manager.
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

### Deploying the experiment

Building the serverimage:

```bash

```

Running a server instance:

```bash

```

Building the worker image:

```bash

```

Running a worker instance:

```bash

```

Supervising the experiment

Open `http://server-ip:server-port/` in your browser.


