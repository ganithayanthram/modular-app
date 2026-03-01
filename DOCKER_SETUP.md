# Docker Setup for Testcontainers

This document explains how to configure Docker for running integration tests with Testcontainers in this project.

## Overview

Integration tests (`*ITest.java`) use [Testcontainers](https://www.testcontainers.org/) to spin up PostgreSQL containers. Testcontainers needs access to a Docker daemon, which can be:

1. **Host Docker** — Docker Desktop or Colima running directly on your machine
2. **Remote Docker** — Docker running inside a Multipass VM or remote server

The project supports both setups without requiring code changes.

---

## Configuration Precedence

Docker configuration is loaded in the following order (highest to lowest priority):

1. **Environment variables** (e.g., `export TESTCONTAINERS_DOCKER_HOST=tcp://192.168.64.8:2376`)
2. **Gradle properties** (e.g., `./gradlew test -Ptestcontainers.docker.host=tcp://192.168.64.8:2376`)
3. **`testcontainers.properties` file** in the project root (gitignored, developer-specific)
4. **`application-test.properties` defaults** (assumes local Docker)

---

## Setup Instructions

### Option 1: Host Docker (Docker Desktop or Colima)

If you have Docker Desktop or Colima installed locally, **no configuration is needed**. The project defaults to auto-detecting your local Docker socket.

**Verify Docker is running:**
```bash
docker ps
```

**Run tests:**
```bash
./gradlew test
./gradlew integrationTest
gradle clean build
```

Testcontainers will automatically find your Docker socket at:
- macOS/Linux: `unix:///var/run/docker.sock`
- Colima: `unix:///Users/<user>/.colima/default/docker.sock`

---

### Option 2: Multipass VM Docker

If you run Docker inside a Multipass VM (e.g., `docker-vm`), you need to configure the remote Docker host.

#### Step 1: Verify Multipass VM is running

```bash
multipass list
# Should show 'docker-vm' with state 'Running'
```

#### Step 2: Get the VM's IP address

```bash
multipass info docker-vm | grep IPv4
# Example output: IPv4: 192.168.64.8
```

#### Step 3: Configure Testcontainers

Choose **one** of the following methods:

**Method A: Create `testcontainers.properties` (Recommended)**

Create a file named `testcontainers.properties` in the project root:

```properties
# Multipass Docker Configuration
testcontainers.docker.remote=true
testcontainers.docker.host=tcp://192.168.64.8:2376
testcontainers.ryuk.disabled=true
testcontainers.checks.disabled=true
testcontainers.docker.api-version=1.44
```

Replace `192.168.64.8` with your VM's actual IP address.

**Method B: Set environment variables**

Add to your `~/.bashrc`, `~/.zshrc`, or `~/.bash_profile`:

```bash
export TESTCONTAINERS_DOCKER_HOST=tcp://192.168.64.8:2376
export TESTCONTAINERS_RYUK_DISABLED=true
export TESTCONTAINERS_CHECKS_DISABLE=true
export DOCKER_API_VERSION=1.44
```

Then reload your shell:
```bash
source ~/.zshrc  # or ~/.bashrc
```

**Method C: Pass Gradle properties**

```bash
./gradlew test \
  -Ptestcontainers.docker.remote=true \
  -Ptestcontainers.docker.host=tcp://192.168.64.8:2376 \
  -Ptestcontainers.ryuk.disabled=true \
  -Ptestcontainers.checks.disabled=true
```

#### Step 4: Run tests

```bash
./gradlew test
./gradlew integrationTest
gradle clean build
```

You should see log output like:
```
🐳 Using remote Docker: tcp://192.168.64.8:2376
```

---

## CI/CD Configuration

### GitHub Actions / GitLab CI

In your CI pipeline, Docker is typically available via the host socket. No special configuration is needed:

```yaml
# .github/workflows/test.yml
- name: Run tests
  run: ./gradlew test
```

If your CI uses a remote Docker daemon, set environment variables:

```yaml
env:
  TESTCONTAINERS_DOCKER_HOST: tcp://docker-daemon:2376
  TESTCONTAINERS_RYUK_DISABLED: true
```

---

## Troubleshooting

### Error: "Could not find a valid Docker environment"

**Cause:** Testcontainers cannot find the Docker daemon.

**Solution:**
1. Verify Docker is running: `docker ps`
2. Check your configuration (see setup instructions above)
3. For Multipass, verify the VM is running: `multipass list`
4. For Multipass, verify the Docker daemon is listening on port 2376:
   ```bash
   multipass exec docker-vm -- sudo netstat -tlnp | grep 2376
   ```

### Error: "Connection refused" (Multipass)

**Cause:** The Docker daemon inside the Multipass VM is not listening on the expected port.

**Solution:**
1. SSH into the VM: `multipass shell docker-vm`
2. Check Docker daemon configuration:
   ```bash
   sudo cat /etc/docker/daemon.json
   # Should contain: "hosts": ["tcp://0.0.0.0:2376", "unix:///var/run/docker.sock"]
   ```
3. Restart Docker: `sudo systemctl restart docker`

### Tests are slow (Multipass)

**Cause:** Ryuk (Testcontainers' cleanup container) is enabled.

**Solution:** Set `testcontainers.ryuk.disabled=true` in your configuration.

---

## Property Reference

| Property | Default | Description |
|---|---|---|
| `testcontainers.docker.remote` | `false` | Set to `true` for remote Docker (Multipass/VM) |
| `testcontainers.docker.host` | (none) | Docker host URI (e.g., `tcp://192.168.64.8:2376`) |
| `testcontainers.ryuk.disabled` | `false` | Disable Ryuk cleanup container (recommended for remote Docker) |
| `testcontainers.checks.disabled` | `false` | Disable Testcontainers startup checks |
| `testcontainers.docker.api-version` | (auto) | Docker API version (e.g., `1.44`) |

---

## Questions?

- Testcontainers documentation: https://www.testcontainers.org/
- Multipass documentation: https://multipass.run/docs

