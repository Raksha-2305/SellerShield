FROM eclipse-temurin:11-jdk

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/SellerShield-0.0.1-SNAPSHOT.jar"]