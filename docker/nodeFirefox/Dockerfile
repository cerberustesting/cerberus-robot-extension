FROM selenium/node-base:4.19.0-20240328
LABEL authors=Cerberus

USER root

#=========
# Firefox
#=========
ARG FIREFOX_VERSION=latest
RUN FIREFOX_DOWNLOAD_URL=$(if [ $FIREFOX_VERSION = "latest" ] || [ $FIREFOX_VERSION = "beta-latest" ] || [ $FIREFOX_VERSION = "nightly-latest" ] || [ $FIREFOX_VERSION = "devedition-latest" ] || [ $FIREFOX_VERSION = "esr-latest" ]; then echo "https://download.mozilla.org/?product=firefox-$FIREFOX_VERSION-ssl&os=linux64&lang=en-US"; else echo "https://download-installer.cdn.mozilla.net/pub/firefox/releases/$FIREFOX_VERSION/linux-x86_64/en-US/firefox-$FIREFOX_VERSION.tar.xz"; fi) \
  && apt-get update -qqy \
  && apt-get -qqy --no-install-recommends install libavcodec-extra \
     libgtk-3-dev libdbus-glib-1-dev xz-utils\
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
  && wget -O /tmp/firefox.tar.xz $FIREFOX_DOWNLOAD_URL \
  && rm -rf /opt/firefox \
  && tar -C /opt -xf /tmp/firefox.tar.xz\
  && rm /tmp/firefox.tar.xz \
  && mv /opt/firefox /opt/firefox-$FIREFOX_VERSION \
  && ln -fs /opt/firefox-$FIREFOX_VERSION/firefox /usr/bin/firefox

#============
# GeckoDriver
#============
ARG GECKODRIVER_VERSION=latest
RUN GK_VERSION=$(if [ ${GECKODRIVER_VERSION:-latest} = "latest" ]; then echo "0.34.0"; else echo $GECKODRIVER_VERSION; fi) \
  && echo "Using GeckoDriver version: "$GK_VERSION \
  && wget --no-verbose -O /tmp/geckodriver.tar.gz https://github.com/mozilla/geckodriver/releases/download/v$GK_VERSION/geckodriver-v$GK_VERSION-linux64.tar.gz \
  && rm -rf /opt/geckodriver \
  && tar -C /opt -zxf /tmp/geckodriver.tar.gz \
  && rm /tmp/geckodriver.tar.gz \
  && mv /opt/geckodriver /opt/geckodriver-$GK_VERSION \
  && chmod 755 /opt/geckodriver-$GK_VERSION \
  && ln -fs /opt/geckodriver-$GK_VERSION /usr/bin/geckodriver

#============================================
# Firefox cleanup script and supervisord file
#============================================
COPY firefox-cleanup.sh /opt/bin/firefox-cleanup.sh
COPY supervisor.conf /etc/supervisor/conf.d/supervisor.conf

USER ${SEL_UID}

#============================================
# Dumping Browser information for config
#============================================
RUN echo "firefox" > /opt/selenium/browser_name
RUN firefox --version | awk '{print $3}' > /opt/selenium/browser_version
RUN echo "\"moz:firefoxOptions\": {\"binary\": \"/usr/bin/firefox\"}" > /opt/selenium/browser_binary_location

ENV SE_OTEL_SERVICE_NAME "selenium-node-firefox"

#Cerberus Robot Extension

USER root

ARG CERBERUS_ROBOT_EXTENSION_VERSION="1.7.2"

RUN  wget --no-verbose http://vm.cerberus-testing.org/delivery/cerberus-robot-extension-$CERBERUS_ROBOT_EXTENSION_VERSION.jar -O /opt/selenium/cerberus-robot-extension.jar
RUN apt-get update -qqy && apt-get install -qqy libopencv4.5-java
RUN ln -s /usr/lib/jni/libopencv_java454d.so /usr/lib/libopencv_java.so

COPY start-cerberus-robot-extension.sh /opt/start-cerberus-robot-extension.sh

RUN chmod 0755 /opt/start-cerberus-robot-extension.sh

USER ${SEL_UID}
