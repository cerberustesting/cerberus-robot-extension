FROM openjdk:11

EXPOSE 8093
ARG VERSION
ENV PACKAGE_NAME=cerberus-robot-extension-$VERSION.jar

#wget https://vm.cerberus-testing.org/delivery/cerberus-robot-extension-$VERSION.jar
RUN echo "Download Cerberus Executor Application" && \
    wget https://github.com/cerberustesting/cerberus-robot-extension/releases/download/$VERSION/cerberus-robot-extension-$VERSION-full.jar

ENTRYPOINT sh -c 'java -jar "$PACKAGE_NAME" -p 8093'
