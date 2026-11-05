FROM maven:3.9.6-eclipse-temurin-21
WORKDIR /app

# GUI libraries needed by JavaFX (the base image is headless) + fonts for Japanese/Arabic
RUN apt-get update && apt-get install -y --no-install-recommends \
    libx11-6 libxext6 libxrender1 libxtst6 libxi6 libgtk-3-0 mesa-utils wget unzip \
    fontconfig fonts-takao-gothic fonts-noto-core \
    && fc-cache -f \
    && rm -rf /var/lib/apt/lists/*

ENV LANG=C.UTF-8

# JavaFX SDK (modules are loaded from --module-path at run time)
RUN mkdir -p /javafx-sdk \
    && wget -O javafx.zip https://download2.gluonhq.com/openjfx/21/openjfx-21_linux-x64_bin-sdk.zip \
    && unzip javafx.zip -d /javafx-sdk \
    && mv /javafx-sdk/javafx-sdk-21/lib /javafx-sdk/lib \
    && rm -rf /javafx-sdk/javafx-sdk-21 javafx.zip

# Build the fat JAR (maven-shade-plugin in pom.xml)
COPY pom.xml .
COPY . /app
RUN mvn package

# Send the GUI to the X server on Windows (Xming)
ENV DISPLAY=host.docker.internal:0.0

# MariaDB runs on the Windows host, not inside the container
ENV DB_HOST=host.docker.internal

CMD ["java", \
     "--module-path", "/javafx-sdk/lib", \
     "--add-modules", "javafx.controls,javafx.fxml", \
     "-Dprism.order=sw", \
     "-jar", "target/temperature_converter.jar"]

# How to run (Windows):
#   1. Start Xming (XLaunch: Multiple windows, display 0, "No Access Control" ticked)
#   2. docker build -t marakusa/temperature-converter .
#   3. docker run --rm marakusa/temperature-converter