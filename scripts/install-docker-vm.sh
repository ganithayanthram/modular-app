#!/bin/bash

# Script to install Docker on Multipass Ubuntu VM
# Run this script INSIDE the Multipass VM

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_status "Starting Docker installation on Ubuntu VM..."

# Update package index
print_status "Updating package index..."
sudo apt update

# Install required packages
print_status "Installing required packages..."
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Add Docker's official GPG key
print_status "Adding Docker GPG key..."
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Add Docker repository
print_status "Adding Docker repository..."
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Update package index again
print_status "Updating package index with Docker repository..."
sudo apt update

# Install Docker
print_status "Installing Docker..."
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Add user to docker group
print_status "Adding user to docker group..."
sudo usermod -aG docker $USER

# Start and enable Docker
print_status "Starting Docker service..."
sudo systemctl start docker
sudo systemctl enable docker

# Install Docker Compose (standalone)
print_status "Installing Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Configure Docker for remote access
print_status "Configuring Docker for remote access..."
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "hosts": ["unix:///var/run/docker.sock", "tcp://0.0.0.0:2376"],
  "tls": false
}
EOF

# Create systemd override directory
sudo mkdir -p /etc/systemd/system/docker.service.d

# Create override file to remove -H flag conflict
sudo tee /etc/systemd/system/docker.service.d/override.conf > /dev/null <<EOF
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd
EOF

# Reload systemd and restart Docker
print_status "Restarting Docker with new configuration..."
sudo systemctl daemon-reload
sudo systemctl restart docker

# Wait a moment for Docker to start
sleep 3

# Verify installation
print_status "Verifying Docker installation..."
if docker --version && docker-compose --version; then
    print_status "✅ Docker installation completed successfully!"
    print_status "Docker version: $(docker --version)"
    print_status "Docker Compose version: $(docker-compose --version)"
    print_status ""
    print_warning "⚠️  You need to log out and log back in for docker group changes to take effect."
    print_warning "Or run: newgrp docker"
    print_status ""
    print_status "VM IP address: $(hostname -I | awk '{print $1}')"
    print_status "Docker is now accessible remotely on port 2376"
else
    print_error "❌ Docker installation failed!"
    exit 1
fi