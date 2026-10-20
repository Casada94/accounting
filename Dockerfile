# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN ./gradlew bootJar --no-daemon \
    && find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' \
        -exec cp '{}' /workspace/app.jar \;

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update \
    && apt-get install --no-install-recommends --yes \
        texlive-latex-base \
        texlive-latex-recommended \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system accounting \
    && useradd --system --gid accounting --home-dir /app accounting

WORKDIR /app

COPY --from=builder --chown=accounting:accounting /workspace/app.jar app.jar

RUN mkdir -p /app/generated/pdf \
    && chown accounting:accounting /app /app/generated /app/generated/pdf

USER accounting

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
