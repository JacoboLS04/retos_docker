FROM jenkins/jenkins:lts-jdk21

# Install git and handy utilities inside the Jenkins controller container
USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends git curl ca-certificates dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Switch back to the jenkins user
USER jenkins
