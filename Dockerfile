# Base Image
FROM python:3.8.10
LABEL maintainer="Phong Bui"

# Arguments that can be set with docker build
ARG FLINK_HOME=/opt/flink

# Export the environment variable FLINK_HOME where flink will be installed
ENV FLINK_HOME=${FLINK_HOME}

# Install dependencies and tools
RUN apt-get update -yqq && \
    apt-get upgrade -yqq && \
    apt-get install -yqq --no-install-recommends \
    wget \
    libczmq-dev \
    curl \
    libssl-dev \
    git \
    inetutils-telnet \
    bind9utils freetds-dev \
    libkrb5-dev \
    libsasl2-dev \
    libffi-dev libpq-dev \
    freetds-bin build-essential \
    default-libmysqlclient-dev \
    rsync \
    zip \
    unzip \
    gcc \
    vim \
    locales \
    default-jre-headless \
    && apt-get clean

COPY ./requirements.txt /requirements.txt

# Upgrade pip
# Create flink user 
# Install subpackages
RUN pip install --upgrade pip && \
    useradd -ms /bin/bash -d ${FLINK_HOME} flink && \
    pip install -r requirements.txt

# Copy the eventProducer.py from host to container (at path FLINK_HOME)
COPY eventProducer.py /eventProducer.py

# Set the eventProducer.py file to be executable
RUN chmod +x ./eventProducer.py

# Set the owner of the files in FLINK_HOME to the user flink
RUN chown -R flink: ${FLINK_HOME}

# Set the username to use
USER flink

# Set workdir (it's like a cd inside the container)
WORKDIR ${FLINK_HOME}