FROM maven:3.6-jdk-11

RUN apt-get -qq update && apt-get install --no-install-recommends -qqy \
        git httpie jq bc


ARG USER_HOME_DIR="/home/maven"
RUN adduser maven --disabled-login --disabled-password
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"
COPY settings.xml $MAVEN_CONFIG/settings.xml
RUN mkdir $MAVEN_CONFIG/repository
RUN chown maven:maven $USER_HOME_DIR -R

USER maven

COPY scripts /app/scripts
COPY lib /app/lib

WORKDIR /workdir

CMD ["/app/scripts/modular-worker-run.sh", "127.0.0.1:8090"]
