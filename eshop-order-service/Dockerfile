FROM amazoncorretto:21-alpine-jdk as build

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew build

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]