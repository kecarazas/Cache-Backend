# ============================================================
#  Etapa 1: compilar el proyecto con Maven y JDK 21
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero solo el pom.xml: así Docker guarda en caché las dependencias
# y no las vuelve a descargar si solo cambias el código.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Ahora el código fuente y el empaquetado del .jar
COPY src ./src
RUN mvn -B package -DskipTests

# ============================================================
#  Etapa 2: imagen final, solo con el JRE y el .jar
# ============================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/cache-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
