ARG NAMESPACE=selenium
ARG VERSION=4.29.0-20250222
ARG BASE=node-chrome
FROM ${NAMESPACE}/${BASE}:${VERSION}
LABEL authors=${AUTHORS}

USER root

#==============================
# Cerberus Robot Extension
#==============================
ARG CERBERUS_ROBOT_EXTENSION_VERSION=1.7.2
RUN  wget --no-verbose http://vm.cerberus-testing.org/delivery/cerberus-robot-extension-$CERBERUS_ROBOT_EXTENSION_VERSION.jar -O /opt/selenium/cerberus-robot-extension.jar


RUN apt-get update -qqy && apt-get install --no-install-recommends -qqy libopencv406-jni
RUN apt-get update -qqy && apt-get install --no-install-recommends -qqy libopencv-java
RUN ln -s /usr/lib/jni/libopencv_java454d.so /usr/lib/libopencv_java.so

COPY start-cerberus-robot-extension.sh /opt/start-cerberus-robot-extension.sh
RUN chmod 0755 /opt/start-cerberus-robot-extension.sh

# Supervisor configuration file
COPY cerberus-robot-extension.conf /etc/supervisor/conf.d/

USER ${SEL_UID}
