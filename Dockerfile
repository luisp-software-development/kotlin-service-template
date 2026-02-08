# First Stage: build the jar
FROM eclipse-temurin:21-jdk AS maven-compiler
WORKDIR /usr/app
COPY . .
RUN ./gradlew clean build -x test

# Second stage: build the custom JRE
FROM eclipse-temurin:21-jdk-alpine AS jre-builder

# Install binutils, required by jlink
RUN apk update &&  \
    apk add binutils

# Build small JRE image
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /optimized-jdk-21

# Third stag: use jar and custom jre to build the app image
FROM alpine:latest
ENV JAVA_HOME=/opt/jdk/jdk-21
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy JRE from the builder image
COPY --from=jre-builder /optimized-jdk-21 $JAVA_HOME

# Add app user
ARG APPLICATION_USER=spring

# Create a user to run the application, don't run as root
RUN addgroup --system $APPLICATION_USER &&  adduser --system $APPLICATION_USER --ingroup $APPLICATION_USER

# Create the application directory
RUN mkdir /app && chown -R $APPLICATION_USER /app

# Copy JAR from the compiler image
COPY --from=maven-compiler --chown=$APPLICATION_USER:$APPLICATION_USER /usr/app/build/libs/*.jar /app/app.jar

# Set application user as active
USER $APPLICATION_USER

# Run application
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]