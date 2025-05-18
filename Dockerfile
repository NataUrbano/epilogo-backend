# Etapa de compilación
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias Maven para aprovechar la caché de Docker
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Crear application-render.properties con contenido seguro
RUN echo "# App general" > src/main/resources/application-render.properties && \
    echo "spring.application.name=epilogo" >> src/main/resources/application-render.properties && \
    echo "server.port=\${PORT:8080}" >> src/main/resources/application-render.properties && \
    echo "" >> src/main/resources/application-render.properties && \
    echo "# Database" >> src/main/resources/application-render.properties && \
    echo "spring.datasource.url=\${JDBC_DATABASE_URL}" >> src/main/resources/application-render.properties && \
    echo "spring.datasource.username=\${JDBC_DATABASE_USERNAME:postgres}" >> src/main/resources/application-render.properties && \
    echo "spring.datasource.password=\${JDBC_DATABASE_PASSWORD}" >> src/main/resources/application-render.properties && \
    echo "spring.datasource.driver-class-name=org.postgresql.Driver" >> src/main/resources/application-render.properties && \
    echo "" >> src/main/resources/application-render.properties && \
    echo "# JPA/Hibernate" >> src/main/resources/application-render.properties && \
    echo "spring.jpa.hibernate.ddl-auto=update" >> src/main/resources/application-render.properties && \
    echo "spring.jpa.show-sql=false" >> src/main/resources/application-render.properties && \
    echo "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect" >> src/main/resources/application-render.properties && \
    echo "" >> src/main/resources/application-render.properties && \
    echo "# JWT" >> src/main/resources/application-render.properties && \
    echo "jwt.secret=\${JWT_SECRET:defaultsecretkey123456789012345678901234567890}" >> src/main/resources/application-render.properties && \
    echo "jwt.expiration=\${JWT_EXPIRATION:86400000}" >> src/main/resources/application-render.properties && \
    echo "jwt.refresh-expiration=\${JWT_REFRESH_EXPIRATION:604800000}" >> src/main/resources/application-render.properties && \
    echo "jwt.issuer=epilogo" >> src/main/resources/application-render.properties

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