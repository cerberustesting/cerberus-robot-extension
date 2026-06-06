ARG NAMESPACE=selenium
ARG VERSION=151.0-geckodriver-0.36-grid-4.44.0-20260505
ARG BASE=node-firefox
FROM ${NAMESPACE}/${BASE}:${VERSION}
LABEL authors=${AUTHORS}

USER root

#RUN apt-get update -qqy && apt-get install --no-install-recommends -qqy libx11-6 libxext6 libxi6 libxtst6 libxrender1 libxrandr2 libxfixes3 libxcursor1 libxinerama1 libxt6 libopencv-java
# Dépendances pour Robot/AWT
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    libx11-6 \
    libxext6 \
    libxi6 \
    libxtst6 \
    libxrender1 \
    libxrandr2 \
    libxfixes3 \
    libxcursor1 \
    libxinerama1 \
    libxt6 \
    libfontconfig1 \
    libfreetype6 \
    libnss3 \
    openjdk-21-jdk \
    ffmpeg \
    wmctrl \
 && rm -rf /var/lib/apt/lists/*
RUN ln -s /usr/lib/jni/libopencv_java454d.so /usr/lib/libopencv_java.so

#==============================
# Cerberus Robot Extension
#==============================
ARG CERBERUS_ROBOT_EXTENSION_VERSION=1.7.2
ENV CERBERUS_ROBOT_EXTENSION_VERSION=${CERBERUS_ROBOT_EXTENSION_VERSION}
#COPY cerberus-robot-extension-${CERBERUS_ROBOT_EXTENSION_VERSION}-full.jar /opt/selenium/cerberus-robot-extension.jar
RUN  wget --no-verbose http://vm.cerberus-testing.org/delivery/cerberus-robot-extension-$CERBERUS_ROBOT_EXTENSION_VERSION.jar -O /opt/selenium/cerberus-robot-extension.jar

COPY start-cerberus-robot-extension.sh /opt/start-cerberus-robot-extension.sh
RUN chmod 0755 /opt/start-cerberus-robot-extension.sh

# Supervisor configuration file
COPY cerberus-robot-extension.conf /etc/supervisor/conf.d/

USER ${SEL_UID}
