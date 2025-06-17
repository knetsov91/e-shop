FROM amazoncorretto:21-alpine-jdk as build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew build

RUN ./gradlew

FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]