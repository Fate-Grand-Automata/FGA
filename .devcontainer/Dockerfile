FROM mcr.microsoft.com/devcontainers/java:25-bookworm

# Always start as root for provisioning
USER root
ENV DEBIAN_FRONTEND=noninteractive

# ---- System dependencies ----
# Remove yarn repository to avoid GPG key errors
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
        curl \
        unzip \
        zip \
        libglu1-mesa \
        git-lfs \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && git lfs install --system

# ---- Github CLI ----
# This is Debian
RUN (type -p wget >/dev/null || (sudo apt update && sudo apt install wget -y)) \
	&& sudo mkdir -p -m 755 /etc/apt/keyrings \
	&& out=$(mktemp) && wget -nv -O$out https://cli.github.com/packages/githubcli-archive-keyring.gpg \
	&& cat $out | sudo tee /etc/apt/keyrings/githubcli-archive-keyring.gpg > /dev/null \
	&& sudo chmod go+r /etc/apt/keyrings/githubcli-archive-keyring.gpg \
	&& sudo mkdir -p -m 755 /etc/apt/sources.list.d \
	&& echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
	&& sudo apt update \
	&& sudo apt install gh -y

# ---- Github Copilot ----
RUN curl -fsSL https://gh.io/copilot-install | bash

# ---- Android CLI ----
RUN sudo curl -fsSL https://dl.google.com/android/cli/latest/linux_x86_64/install_root.sh | bash

# ---- Android SDK env ----
ENV ANDROID_SDK_ROOT=/usr/local/android-sdk
ENV ANDROID_HOME=${ANDROID_SDK_ROOT}
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# ---- Android command line tools ----
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-14742923_latest.zip -o /tmp/cmdline-tools.zip \
    && unzip /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

# ---- SDK packages ----
RUN yes | sdkmanager --licenses \
    && sdkmanager --update \
    && sdkmanager \
        "platform-tools" \
        "platforms;android-37.0" \
        "build-tools;37.0.0"

# ---- Update Android command line tools using CLI ----
RUN android sdk install cmdline-tools/latest

# ---- Permissions (CRITICAL) ----
RUN chown -R vscode:vscode ${ANDROID_SDK_ROOT}

# Drop privileges
USER vscode
