FROM maven:3-jdk-8
COPY pom.xml /usr/src/myapp/pom.xml
RUN mvn -B -f /usr/src/myapp/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN mvn compile
