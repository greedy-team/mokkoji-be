FROM amd64/amazoncorretto:21
WORKDIR /app
COPY ./build/libs/mokkoji-0.0.1-SNAPSHOT.jar /app/mokkoji.jar
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=prod", "mokkoji.jar"]
