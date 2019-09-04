FROM centos:7

## ================================================================================
# Docker image metadata define inside the Docker image.
LABEL team="TransitionManager Development Team"
LABEL author="TM Developers"
LABEL environment="Development" env="Development"
LABEL image="tm-application"

## ================================================================================
# Switching to user to perform privelige user actions
USER root

## ================================================================================
# Environmental variable use to define parameter to build the docker image.
ENV GRAILS_VERSION="2.5.4"
ENV JAVA_HOME="/usr/java/latest"

## ================================================================================
# The following statement will setup all the required tools that is consider core
# base of the TransitionManager build and Development environment.
RUN echo "[google-chrome]" >> /etc/yum.repos.d/google-chrome.repo && \
    echo "name=google-chrome" >> /etc/yum.repos.d/google-chrome.repo && \
    echo "baseurl=http://dl.google.com/linux/chrome/rpm/stable/x86_64" >> /etc/yum.repos.d/google-chrome.repo && \
    echo "enabled=1" >> /etc/yum.repos.d/google-chrome.repo && \
    echo "gpgcheck=1" >> /etc/yum.repos.d/google-chrome.repo && \
    echo "gpgkey=https://dl-ssl.google.com/linux/linux_signing_key.pub" >> /etc/yum.repos.d/google-chrome.repo

RUN echo ">>> Installing packages" && \
        yum install -y epel-release sudo
RUN yum install -y curl vim wget mysql-client bash unzip zip tar \
        git openssh shadow less file tcpdump \
        wget cronie which gcc-c++ make \
        bzip2 gzip mysql mysql-utilities postfix google-chrome-stable-76.0.3809.132-1
RUN echo ">>> Installing Java JDK" && \
        mkdir -p /usr/java
RUN yum install -y java-1.8.0-openjdk-devel && \
        ln -s /usr/lib/jvm/java-1.8.0/ ${JAVA_HOME} && \
        echo "JAVA_HOME=${JAVA_HOME}" > /etc/profile.d/java.sh && \
        echo "PATH=$PATH:${JAVA_HOME}/bin" >> /etc/profile.d/java.sh && \
    echo ">>> Setting up and switch to tmuser" && \
        groupadd tmusers && \
        useradd -m tmuser -g tmusers -s /bin/bash && \
        chown tmuser:tmusers /opt && \
    echo ">>> Housecleaning" && \
        rm -rf /var/cache/yum/* && \
    echo ">>> that's all folks"

## ================================================================================
# Installing basic packages, tools, Jenkins NJPL jar file, NodeJS among others
# tools required to to build and deploy a TM application build.
RUN echo ">>> Setting up Postfix Settings" && \
        sed -i 's/^inet_protocols.*\=.*/inet_protocols = ipv4/' /etc/postfix/main.cf && \
    echo ">>> Setting up sudo credentials" && \
        sed -i 's/^Defaults.*secure_path.*/Defaults    secure_path = \/sbin:\/bin:\/usr\/sbin:\/usr\/bin:\/usr\/local\/bin/g' /etc/sudoers && \
        echo "tmuser ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers.d/tmuser && \
    echo ">>> Installing NodeJS" && \
        curl --silent --location https://rpm.nodesource.com/setup_10.x | bash - && \
        yum -y install nodejs && \
    echo ">>> Creating TM User workspace" && \
        mkdir -p /opt /data /home/tmuser /home/workspace && \
        chown -R tmuser: /home/tmuser /home/workspace /opt /data && \
        chmod 777 /home/workspace && \
    echo ">>> Housecleaning" && \
        rm -rf /var/cache/yum/* && \
    echo ">>> That's all folks"
RUN chown root:root /opt/google/chrome/chrome-sandbox && \
    chmod 4755 /opt/google/chrome/chrome-sandbox

## ================================================================================
#  Switching to tmuser
USER tmuser

## ================================================================================
# Installing SDKMAN, Grails and configure GIT for the tmuser
RUN echo ">>> Installing sdkman and grails on tmuser" && \
        curl -s "https://get.sdkman.io" | bash - && \
        source /home/tmuser/.sdkman/bin/sdkman-init.sh && \
        sdk install grails ${GRAILS_VERSION}
RUN echo ">>> Setting up git config" && \
        mkdir -p /home/tmuser/.ssh && \
        ssh-keyscan -p 7999 git.tdsops.net >> /home/tmuser/.ssh/known_hosts && \
    echo ">>> that's all folks"

## ================================================================================
# Define the user working directory

COPY . /opt/tdstm/
