#!/bin/bash

# Validation script to check if Multipass Docker setup is working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Configuration
VM_NAME="docker-vm"
SCRIPT_DIR="$(dirname "$0")"
DOCKER_REMOTE="$SCRIPT_DIR/docker-remote.sh"

print_header "Multipass Docker Setup Validation"

# Check if multipass is installed
print_info "Checking Multipass installation..."
if command -v multipass >/dev/null 2>&1; then
    print_success "Multipass is installed: $(multipass version | head -n1)"
else
    print_error "Multipass is not installed or not in PATH"
    exit 1
fi

# Check if VM exists
print_info "Checking if VM '$VM_NAME' exists..."
if multipass info $VM_NAME >/dev/null 2>&1; then
    print_success "VM '$VM_NAME' exists"
    
    # Check VM status
    VM_STATUS=$(multipass info $VM_NAME | grep State | awk '{print $2}')
    if [ "$VM_STATUS" = "Running" ]; then
        print_success "VM is running"
    else
        print_warning "VM is not running (Status: $VM_STATUS)"
        print_info "Starting VM..."
        multipass start $VM_NAME
        sleep 5
    fi
else
    print_error "VM '$VM_NAME' does not exist"
    print_info "Create it with: multipass launch --name docker-vm --cpus 2 --memory 4G --disk 20G"
    exit 1
fi

# Get VM IP
print_info "Getting VM IP address..."
VM_IP=$(multipass info $VM_NAME | grep IPv4 | awk '{print $2}')
if [ -n "$VM_IP" ]; then
    print_success "VM IP: $VM_IP"
else
    print_error "Could not get VM IP address"
    exit 1
fi

# Check if docker-remote script exists
print_info "Checking docker-remote script..."
if [ -f "$DOCKER_REMOTE" ]; then
    print_success "docker-remote.sh script found"
    if [ -x "$DOCKER_REMOTE" ]; then
        print_success "docker-remote.sh is executable"
    else
        print_warning "docker-remote.sh is not executable, fixing..."
        chmod +x "$DOCKER_REMOTE"
    fi
else
    print_error "docker-remote.sh script not found at $DOCKER_REMOTE"
    exit 1
fi

# Check if Docker is installed on VM
print_info "Checking Docker installation on VM..."
if multipass exec $VM_NAME -- which docker >/dev/null 2>&1; then
    DOCKER_VERSION=$(multipass exec $VM_NAME -- docker --version)
    print_success "Docker is installed: $DOCKER_VERSION"
else
    print_error "Docker is not installed on VM"
    print_info "Install it by running: multipass shell $VM_NAME"
    print_info "Then run the install-docker-vm.sh script"
    exit 1
fi

# Check if Docker Compose is installed on VM
print_info "Checking Docker Compose installation on VM..."
if multipass exec $VM_NAME -- which docker-compose >/dev/null 2>&1; then
    COMPOSE_VERSION=$(multipass exec $VM_NAME -- docker-compose --version)
    print_success "Docker Compose is installed: $COMPOSE_VERSION"
else
    print_warning "Docker Compose is not installed on VM"
fi

# Check if Docker daemon is configured for remote access
print_info "Checking Docker daemon configuration..."
if multipass exec $VM_NAME -- sudo test -f /etc/docker/daemon.json; then
    print_success "Docker daemon configuration file exists"
    
    # Check if daemon is listening on port 2376
    if multipass exec $VM_NAME -- sudo netstat -tlnp 2>/dev/null | grep :2376 >/dev/null; then
        print_success "Docker daemon is listening on port 2376"
    else
        print_warning "Docker daemon is not listening on port 2376"
        print_info "Run: $DOCKER_REMOTE setup"
    fi
else
    print_warning "Docker daemon configuration file not found"
    print_info "Run: $DOCKER_REMOTE setup"
fi

# Test Docker connection from host
print_info "Testing Docker connection from host..."
if "$DOCKER_REMOTE" test >/dev/null 2>&1; then
    print_success "Docker connection from host is working"
else
    print_error "Docker connection from host failed"
    print_info "Run: $DOCKER_REMOTE setup"
fi

# Check if docker-compose.yml exists
print_info "Checking docker-compose.yml..."
if [ -f "docker-compose.yml" ]; then
    print_success "docker-compose.yml found"
    
    # Validate compose file
    if "$DOCKER_REMOTE" compose config >/dev/null 2>&1; then
        print_success "docker-compose.yml is valid"
    else
        print_warning "docker-compose.yml has validation errors"
    fi
else
    print_warning "docker-compose.yml not found in current directory"
fi

# Check if aliases script exists
print_info "Checking aliases script..."
ALIASES_SCRIPT="$SCRIPT_DIR/docker-aliases.sh"
if [ -f "$ALIASES_SCRIPT" ]; then
    print_success "docker-aliases.sh found"
    print_info "To load aliases, run: source $ALIASES_SCRIPT"
else
    print_warning "docker-aliases.sh not found"
fi

print_header "Validation Summary"

# Final test - try to run a simple Docker command
print_info "Running final test: docker version"
if "$DOCKER_REMOTE" docker version >/dev/null 2>&1; then
    print_success "All checks passed! Your Multipass Docker setup is working correctly."
    echo ""
    print_info "You can now use commands like:"
    echo "  $DOCKER_REMOTE compose up -d"
    echo "  $DOCKER_REMOTE docker ps"
    echo ""
    print_info "Or load aliases and use:"
    echo "  source $ALIASES_SCRIPT"
    echo "  modular-up"
    echo "  vm-docker ps"
else
    print_error "Final test failed. Please check the setup."
    exit 1
fi