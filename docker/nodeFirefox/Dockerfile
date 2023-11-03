# ---------------------------------------------------
# Content of https://github.com/SeleniumHQ/docker-selenium/blob/3.141.59-20210929/NodeBase/Dockerfile
# Content of https://github.com/SeleniumHQ/docker-selenium/blob/3.141.59-20210929/NodeFirefox/Dockerfile
# ---------------------------------------------------
# Changelog
# 2023-07-31 : Added creation of /home/seluser/Downloads Folder


FROM selenium/node-base:3.141.59-20201119
LABEL authors=Cerberus

# ---------------------------------------------------

USER root

#============================================
# Firefox
#============================================
# can specify versions by FIREFOX_VERSION;
#  e.g. 113.0
#       116.0.3
#============================================
# Check available versions here: https://download-installer.cdn.mozilla.net/pub/firefox/releases/
ARG FIREFOX_VERSION=latest
ARG GECKODRIVER_VERSION=latest
ARG VNC_PASSWORD=XXXXXXXX
ARG CERBERUS_ROBOT_EXTENSION_VERSION=1.7.1


#=========
# Firefox
#=========
RUN FIREFOX_DOWNLOAD_URL=$(if [ $FIREFOX_VERSION = "latest" ] || [ $FIREFOX_VERSION = "nightly-latest" ] || [ $FIREFOX_VERSION = "devedition-latest" ] || [ $FIREFOX_VERSION = "esr-latest" ]; then echo "https://download.mozilla.org/?product=firefox-$FIREFOX_VERSION-ssl&os=linux64&lang=en-US"; else echo "https://download-installer.cdn.mozilla.net/pub/firefox/releases/$FIREFOX_VERSION/linux-x86_64/en-US/firefox-$FIREFOX_VERSION.tar.bz2"; fi) \
  && apt-get update -qqy \
  && apt-get -qqy --no-install-recommends install firefox libavcodec-extra \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
  && wget --no-verbose -O /tmp/firefox.tar.bz2 $FIREFOX_DOWNLOAD_URL \
  && apt-get -y purge firefox \
  && rm -rf /opt/firefox \
  && tar -C /opt -xjf /tmp/firefox.tar.bz2 \
  && rm /tmp/firefox.tar.bz2 \
  && mv /opt/firefox /opt/firefox-$FIREFOX_VERSION \
  && ln -fs /opt/firefox-$FIREFOX_VERSION/firefox /usr/bin/firefox

RUN echo $FIREFOX_VERSION



#============
# GeckoDriver
#============
RUN GK_VERSION=$(if [ ${GECKODRIVER_VERSION:-latest} = "latest" ]; then echo "0.29.1"; else echo $GECKODRIVER_VERSION; fi) \
  && echo "Using GeckoDriver version: "$GK_VERSION \
  && wget --no-verbose -O /tmp/geckodriver.tar.gz https://github.com/mozilla/geckodriver/releases/download/v$GK_VERSION/geckodriver-v$GK_VERSION-linux64.tar.gz \
  && rm -rf /opt/geckodriver \
  && tar -C /opt -zxf /tmp/geckodriver.tar.gz \
  && rm /tmp/geckodriver.tar.gz \
  && mv /opt/geckodriver /opt/geckodriver-$GK_VERSION \
  && chmod 755 /opt/geckodriver-$GK_VERSION \
  && ln -fs /opt/geckodriver-$GK_VERSION /usr/bin/geckodriver \
  && mkdir -p /home/seluser/Downloads

# ---------------------------------------------------



COPY generate_config /opt/bin/generate_config
RUN chmod 755 /opt/bin/generate_config

USER 1200
#Generating a default config during build time
RUN /opt/bin/generate_config > /opt/selenium/config.json


USER root


#=====
# VNC
#=====
RUN apt-get update -qqy \
  && apt-get -qqy install \
  x11vnc \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

#=========
# fluxbox
# A fast, lightweight and responsive window manager
#=========
RUN apt-get update -qqy \
  && apt-get -qqy install \
    fluxbox \
  && rm -rf /var/lib/apt/lists/* /var/cache/apt/*

USER 1200

#==============================
# Generating the VNC password as seluser
# So the service can be started with seluser
#==============================

RUN mkdir -p ${HOME}/.vnc \
  && x11vnc -storepasswd secret ${HOME}/.vnc/passwd

#==========
# Relaxing permissions for OpenShift and other non-sudo environments
#==========
RUN sudo chmod -R 777 ${HOME} \
  && sudo chgrp -R 0 ${HOME} \
  && sudo chmod -R g=u ${HOME}

#==============================
# Scripts to run fluxbox and x11vnc
#==============================
COPY start-fluxbox.sh \
      start-vnc.sh \
      /opt/bin/

#==============================
# Supervisor configuration file
#==============================
COPY selenium-debug.conf /etc/supervisor/conf.d/

EXPOSE 5900

#==============================
# VNC Securisation - Don't forget to chage it ;-)
#==============================
RUN x11vnc -storepasswd $VNC_PASSWORD /home/seluser/.vnc/passwd

#==============================
# Cerberus Robot Extension
#==============================
RUN  wget --no-verbose http://vm.cerberus-testing.org/delivery/cerberus-robot-extension-$CERBERUS_ROBOT_EXTENSION_VERSION.jar -O /opt/selenium/cerberus-robot-extension.jar

USER root

RUN apt-get update -qqy && apt-get -qqy --no-install-recommends install sikuli-ide  libopencv3.2 libopencv3.2-java libopencv3.2-jni tesseract-ocr && rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/lib/jni/libopencv_java320.so /usr/lib/libopencv_java.so

#==============================
# SCript in order to kill zombie processes
#==============================
COPY kill-browser-zombieprocess.sh /opt/kill-browser-zombieprocess.sh
RUN chmod 0755 /opt/kill-browser-zombieprocess.sh

USER seluser

ENV SE_OPTS="-servlets org.cerberus.robot.extension.sikuli.ExecuteSikuliAction -servlets org.cerberus.robot.extension.filemanagement.ExecuteFilemanagementAction -servlets org.cerberus.robot.extension.management.ExecuteManagementAction"

