# 🚀 Multipass Docker Quick Start Guide

This guide will get you up and running with Docker on Multipass in just a few minutes.

## 📋 Prerequisites

- Multipass installed on your macOS
- Terminal access

## 🎯 Quick Setup (5 minutes)

### Step 1: Create the VM
```bash
# Create Ubuntu VM with sufficient resources for Docker
multipass launch --name docker-vm --cpus 2 --memory 4G --disk 20G

# Wait for VM to be ready (about 30 seconds)
multipass info docker-vm
```

### Step 2: Install Docker on the VM
```bash
# Copy the installation script to the VM
multipass transfer scripts/install-docker-vm.sh docker-vm:

# SSH into the VM and run the installation
multipass shell docker-vm

# Inside the VM, run:
chmod +x install-docker-vm.sh
./install-docker-vm.sh

# Apply docker group changes
newgrp docker

# Exit the VM
exit
```

### Step 3: Setup Remote Docker Access
```bash
# From your host machine, setup the Docker daemon for remote access
./scripts/docker-remote.sh setup

# Test the connection
./scripts/docker-remote.sh test
```

### Step 4: Load Convenient Aliases
```bash
# Add to your shell profile (choose one):
echo "source $(pwd)/scripts/docker-aliases.sh" >> ~/.zshrc
# OR
echo "source $(pwd)/scripts/docker-aliases.sh" >> ~/.bashrc

# Reload your shell or source the file
source ~/.zshrc
# OR
source scripts/docker-aliases.sh
```

## 🎉 You're Ready!

Now you can use Docker commands from your host machine:

```bash
# Start your modular-app services
modular-up

# Check running containers
modular-ps

# View logs
modular-logs

# Connect to database
modular-db-connect

# Stop services
modular-down
```

## 🔧 Available Commands

### VM Management
```bash
vm-start          # Start the VM
vm-stop           # Stop the VM
vm-restart        # Restart the VM
vm-ssh            # SSH into the VM
vm-ip             # Get VM IP address
vm-test           # Test Docker connection
```

### Docker Commands
```bash
vm-docker ps                    # List containers
vm-docker images               # List images
vm-docker pull postgres:17     # Pull an image
vm-exec container-name bash    # Execute command in container
```

### Docker Compose Commands
```bash
vm-compose up -d              # Start services in background
vm-compose down               # Stop and remove containers
vm-compose ps                 # List compose services
vm-compose logs -f service    # Follow logs for a service
```

### Project Specific (Modular App)
```bash
modular-up                    # Start all services (PostgreSQL + MinIO)
modular-down                  # Stop all services
modular-restart               # Restart all services
modular-logs                  # View all service logs
modular-ps                    # List running services
modular-db-connect           # Connect to PostgreSQL database
modular-db-logs              # View database logs
modular-minio-logs           # View MinIO logs
```

## 🐛 Troubleshooting

### VM won't start
```bash
# Check VM status
multipass info docker-vm

# If stopped, start it
multipass start docker-vm
```

### Docker connection fails
```bash
# Test connection
vm-test

# If it fails, reconfigure Docker daemon
vm-setup

# Check if Docker is running in VM
multipass exec docker-vm -- sudo systemctl status docker
```

### Port conflicts
If you get port conflicts, check what's running:
```bash
# Check what's using port 5432 (PostgreSQL)
lsof -i :5432

# Check what's using port 9000 (MinIO)
lsof -i :9000
```

### Reset everything
```bash
# Stop and delete VM
multipass stop docker-vm
multipass delete docker-vm
multipass purge

# Start over with Step 1
```

## 📊 Resource Usage

The VM uses:
- **CPU**: 2 cores
- **Memory**: 4GB RAM
- **Disk**: 20GB storage

You can adjust these when creating the VM:
```bash
multipass launch --name docker-vm --cpus 4 --memory 8G --disk 40G
```

## 🔗 Accessing Services

Once your services are running:

- **PostgreSQL**: `localhost:5432`
  - Username: `modularapp`
  - Password: `modularapp`
  - Database: `modularapp`

- **MinIO Console**: `http://localhost:9001`
  - Username: `minio`
  - Password: `minio123`

- **MinIO API**: `http://localhost:9000`

## 💡 Pro Tips

1. **Auto-start VM**: Add `multipass start docker-vm` to your shell profile for automatic startup
2. **Resource monitoring**: Use `vm-docker stats` to monitor container resource usage
3. **Cleanup**: Regularly run `vm-docker system prune` to clean up unused images and containers
4. **Backup**: Use `multipass snapshot docker-vm` to create VM snapshots before major changes

## 🆘 Need Help?

If you encounter issues:

1. Check the VM is running: `multipass info docker-vm`
2. Test Docker connection: `vm-test`
3. Check Docker logs: `multipass exec docker-vm -- sudo journalctl -u docker.service -f`
4. Restart Docker: `multipass exec docker-vm -- sudo systemctl restart docker`

Happy coding! 🎉