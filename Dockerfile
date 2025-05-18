# Etapa de compilación
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias Maven
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Compilar la aplicación
RUN ./mvnw package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Scripts de inicio
COPY --from=build /app/src/main/resources/application.properties /app/application.properties
COPY --from=build /app/src/main/resources/application-render.properties /app/application-render.properties

# Variables de entorno
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=render

# Puerto de la aplicación
EXPOSE 8080

# Comando para iniciar la aplicación con debugging habilitado
CMD ["java", "-Xmx300m", "-Xms100m", "-Dspring.config.additional-location=file:/app/", "-jar", "app.jar"]