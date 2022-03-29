FROM openjdk:11
COPY pom.xml /usr/src/myapp/pom.xml
RUN mvn -B -f /tmp/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
