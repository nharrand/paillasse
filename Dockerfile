FROM maven:3.6-jdk-11

RUN apt-get -qq update && apt-get install --no-install-recommends -qqy \
        git httpie jq

COPY lib /app/lib
COPY scripts /app/scripts

WORKDIR /workdir

CMD ["/app/scripts/modular-worker-run.sh", "127.0.0.1:8090"]
