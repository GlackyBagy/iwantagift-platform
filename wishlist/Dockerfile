FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package


FROM eclipse-temurin:21-jre AS extractor

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=extractor /app/extracted/dependencies/ ./
COPY --from=extractor /app/extracted/spring-boot-loader/ ./
COPY --from=extractor /app/extracted/snapshot-dependencies/ ./
COPY --from=extractor /app/extracted/application/ ./

EXPOSE 8001

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]