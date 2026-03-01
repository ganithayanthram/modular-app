#!/bin/bash

# Docker Remote Helper Script for Multipass
# This script allows you to run Docker commands on your Multipass Ubuntu VM from macOS

set -e

# Configuration
VM_NAME="docker-vm"
DOCKER_PORT="2376"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to get VM IP
get_vm_ip() {
    local ip=$(multipass info $VM_NAME 2>/dev/null | grep IPv4 | awk '{print $2}')
    if [ -z "$ip" ]; then
        print_error "Could not get IP for VM: $VM_NAME"
        print_error "Make sure the VM is running: multipass start $VM_NAME"
        exit 1
    fi
    echo "$ip"
}

# Function to check if VM is running
check_vm_status() {
    local status=$(multipass info $VM_NAME 2>/dev/null | grep State | awk '{print $2}')
    if [ "$status" != "Running" ]; then
        print_warning "VM $VM_NAME is not running. Starting it..."
        multipass start $VM_NAME
        sleep 5
    fi
}

# Function to test Docker connection
test_docker_connection() {
    local vm_ip=$1
    print_status "Testing Docker connection to $vm_ip:$DOCKER_PORT..."
    
    if docker -H tcp://$vm_ip:$DOCKER_PORT version >/dev/null 2>&1; then
        print_status "Docker connection successful!"
        return 0
    else
        print_error "Docker connection failed!"
        return 1
    fi
}

# Function to setup Docker daemon on VM
setup_docker_daemon() {
    local vm_ip=$1
    print_status "Setting up Docker daemon on VM..."
    
    # Check if Docker is installed
    if ! multipass exec $VM_NAME -- which docker >/dev/null 2>&1; then
        print_error "Docker is not installed on the VM. Please run the setup first."
        exit 1
    fi
    
    # Configure Docker daemon
    multipass exec $VM_NAME -- sudo tee /etc/docker/daemon.json > /dev/null <<EOF
{
  "hosts": ["unix:///var/run/docker.sock", "tcp://0.0.0.0:2376"],
  "tls": false
}
EOF

    # Create systemd override
    multipass exec $VM_NAME -- sudo mkdir -p /etc/systemd/system/docker.service.d
    multipass exec $VM_NAME -- sudo tee /etc/systemd/system/docker.service.d/override.conf > /dev/null <<EOF
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd
EOF

    # Restart Docker
    multipass exec $VM_NAME -- sudo systemctl daemon-reload
    multipass exec $VM_NAME -- sudo systemctl restart docker
    
    sleep 3
    print_status "Docker daemon configured successfully!"
}

# Main function
main() {
    case "${1:-help}" in
        "setup")
            print_status "Setting up Docker on Multipass VM..."
            check_vm_status
            VM_IP=$(get_vm_ip)
            setup_docker_daemon $VM_IP
            test_docker_connection $VM_IP
            ;;
        "ip")
            check_vm_status
            VM_IP=$(get_vm_ip)
            echo "$VM_IP"
            ;;
        "test")
            check_vm_status
            VM_IP=$(get_vm_ip)
            test_docker_connection $VM_IP
            ;;
        "docker")
            shift
            check_vm_status
            VM_IP=$(get_vm_ip)
            print_status "Running Docker command on VM ($VM_IP)..."
            docker -H tcp://$VM_IP:$DOCKER_PORT "$@"
            ;;
        "compose")
            shift
            check_vm_status
            VM_IP=$(get_vm_ip)
            print_status "Running Docker Compose command on VM ($VM_IP)..."
            DOCKER_HOST=tcp://$VM_IP:$DOCKER_PORT docker-compose "$@"
            ;;
        "ssh")
            check_vm_status
            multipass shell $VM_NAME
            ;;
        "start")
            multipass start $VM_NAME
            ;;
        "stop")
            multipass stop $VM_NAME
            ;;
        "restart")
            multipass restart $VM_NAME
            ;;
        "help"|*)
            echo "Docker Remote Helper for Multipass"
            echo ""
            echo "Usage: $0 <command> [options]"
            echo ""
            echo "Commands:"
            echo "  setup          - Configure Docker daemon on VM for remote access"
            echo "  ip             - Get VM IP address"
            echo "  test           - Test Docker connection to VM"
            echo "  docker <cmd>   - Run docker command on VM"
            echo "  compose <cmd>  - Run docker-compose command on VM"
            echo "  ssh            - SSH into the VM"
            echo "  start          - Start the VM"
            echo "  stop           - Stop the VM"
            echo "  restart        - Restart the VM"
            echo "  help           - Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 setup                           # Initial setup"
            echo "  $0 docker ps                       # List containers"
            echo "  $0 compose up -d                   # Start services"
            echo "  $0 compose down                    # Stop services"
            echo ""
            ;;
    esac
}

main "$@"