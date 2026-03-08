# Multipass Docker Setup Guide

This guide helps you run Docker commands from your macOS host on a Multipass Ubuntu VM.

## Prerequisites
- Multipass installed on your macOS
- SSH access configured

## Step 1: Create and Configure Ubuntu VM

### Create the VM
```bash
# Create Ubuntu VM with sufficient resources
multipass launch --name docker-vm --cpus 2 --memory 4G --disk 20G
```

### Get VM IP
```bash
multipass info docker-vm
```

## Step 2: Install Docker on Ubuntu VM

### SSH into the VM
```bash
multipass shell docker-vm
```

### Install Docker
```bash
# Update package index
sudo apt update

# Install required packages
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Add Docker's official GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Add Docker repository
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Update package index again
sudo apt update

# Install Docker
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker $USER

# Start and enable Docker
sudo systemctl start docker
sudo systemctl enable docker
```

### Install Docker Compose (standalone)
```bash
# Download Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Make it executable
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker-compose --version
```

### Configure Docker for remote access
```bash
# Create docker daemon configuration
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
sudo systemctl daemon-reload
sudo systemctl restart docker

# Verify Docker is running
sudo systemctl status docker
```

### Exit the VM
```bash
exit
```

## Step 3: Configure Host Machine

### Get VM IP Address
```bash
VM_IP=$(multipass info docker-vm | grep IPv4 | awk '{print $2}')
echo "VM IP: $VM_IP"
```

### Test Docker connection from host
```bash
# Test connection (replace VM_IP with actual IP)
docker -H tcp://VM_IP:2376 version
```

## Step 4: Set up SSH key authentication (Optional but recommended)

### Generate SSH key on host (if not already done)
```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/multipass_docker
```

### Copy SSH key to VM
```bash
# Get VM IP
VM_IP=$(multipass info docker-vm | grep IPv4 | awk '{print $2}')

# Copy SSH key
ssh-copy-id -i ~/.ssh/multipass_docker.pub ubuntu@$VM_IP
```

## Troubleshooting

### If Docker daemon fails to start
```bash
# Check Docker logs
multipass exec docker-vm -- sudo journalctl -u docker.service -f

# Restart Docker service
multipass exec docker-vm -- sudo systemctl restart docker
```

### If connection is refused
```bash
# Check if Docker is listening on port 2376
multipass exec docker-vm -- sudo netstat -tlnp | grep 2376

# Check firewall (Ubuntu usually doesn't block by default)
multipass exec docker-vm -- sudo ufw status
```

### Reset Docker configuration
```bash
# Remove custom configuration and restart
multipass exec docker-vm -- sudo rm /etc/docker/daemon.json
multipass exec docker-vm -- sudo rm -rf /etc/systemd/system/docker.service.d
multipass exec docker-vm -- sudo systemctl daemon-reload
multipass exec docker-vm -- sudo systemctl restart docker
```