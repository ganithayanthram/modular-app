#!/bin/bash

# Docker Aliases for Multipass VM
# Source this file in your ~/.zshrc or ~/.bashrc to use convenient aliases

# Path to the docker-remote script
DOCKER_REMOTE_SCRIPT="$(dirname "${BASH_SOURCE[0]}")/docker-remote.sh"

# Make sure the script exists
if [ ! -f "$DOCKER_REMOTE_SCRIPT" ]; then
    echo "Error: docker-remote.sh not found at $DOCKER_REMOTE_SCRIPT"
    return 1
fi

# Docker aliases
alias vm-docker="$DOCKER_REMOTE_SCRIPT docker"
alias vm-compose="$DOCKER_REMOTE_SCRIPT compose"
alias vm-setup="$DOCKER_REMOTE_SCRIPT setup"
alias vm-test="$DOCKER_REMOTE_SCRIPT test"
alias vm-ssh="$DOCKER_REMOTE_SCRIPT ssh"
alias vm-start="$DOCKER_REMOTE_SCRIPT start"
alias vm-stop="$DOCKER_REMOTE_SCRIPT stop"
alias vm-restart="$DOCKER_REMOTE_SCRIPT restart"
alias vm-ip="$DOCKER_REMOTE_SCRIPT ip"

# Convenience functions
vm-logs() {
    if [ $# -eq 0 ]; then
        echo "Usage: vm-logs <service-name>"
        echo "Example: vm-logs modularapp-db"
        return 1
    fi
    vm-compose logs -f "$@"
}

vm-exec() {
    if [ $# -lt 2 ]; then
        echo "Usage: vm-exec <container-name> <command>"
        echo "Example: vm-exec modularapp-db psql -U modularapp -d modularapp"
        return 1
    fi
    vm-docker exec -it "$@"
}

vm-ps() {
    vm-docker ps "$@"
}

vm-images() {
    vm-docker images "$@"
}

# Project specific aliases for your modular-app
alias modular-up="vm-compose up -d"
alias modular-down="vm-compose down"
alias modular-restart="vm-compose restart"
alias modular-logs="vm-compose logs -f"
alias modular-ps="vm-compose ps"

# Database specific commands
alias modular-db-connect="vm-exec modularapp-db psql -U modularapp -d modularapp"
alias modular-db-logs="vm-logs modularapp-db"

# Minio specific commands
alias modular-minio-logs="vm-logs minio"

echo "Docker VM aliases loaded successfully!"
echo ""
echo "Available aliases:"
echo "  vm-docker     - Run docker commands on VM"
echo "  vm-compose    - Run docker-compose commands on VM"
echo "  vm-setup      - Setup Docker daemon on VM"
echo "  vm-test       - Test Docker connection"
echo "  vm-ssh        - SSH into VM"
echo "  vm-start      - Start VM"
echo "  vm-stop       - Stop VM"
echo "  vm-restart    - Restart VM"
echo "  vm-ip         - Get VM IP"
echo ""
echo "Project specific aliases:"
echo "  modular-up    - Start all services"
echo "  modular-down  - Stop all services"
echo "  modular-logs  - View all logs"
echo "  modular-ps    - List running containers"
echo ""
echo "Helper functions:"
echo "  vm-logs <service>     - View logs for specific service"
echo "  vm-exec <container> <cmd> - Execute command in container"
echo "  modular-db-connect    - Connect to PostgreSQL database"