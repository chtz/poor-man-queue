FROM java:8-jre-alpine
VOLUME /tmp
ADD poor-man-queue-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
RUN sh -c 'mkdir /stor'
ENV JAVA_OPTS="-DstoragePath=/stor"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
