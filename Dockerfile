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

# Variables de entorno
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=render

# Puerto de la aplicación
EXPOSE 8080

# Comando para iniciar la aplicación
CMD ["java", "-Xmx300m", "-Xms100m", "-jar", "app.jar"]