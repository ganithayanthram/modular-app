# Docker Configuration Refactoring — Summary of Changes

## Overview

Refactored the Docker/Testcontainers configuration to support both **host Docker** (Docker Desktop/Colima) and **remote Docker** (Multipass VM) setups without requiring code changes or modifications to tracked files.

---

## Changes Made

### 1. Profile Structure Reorganization

**Moved profiles to correct locations:**

| File | Old Location | New Location | Purpose |
|---|---|---|---|
| `application-dev.properties` | `src/test/resources/` | `src/main/resources/` | Developer-local application config |
| `application-k8s.properties` | N/A (was `application-prod.properties`) | `src/main/resources/` | Kubernetes/production config |
| `application-test.properties` | `src/test/resources/` | `src/test/resources/` | Test-specific config (unchanged location) |

**Rationale:**
- `dev` and `k8s` profiles are for running the **main application** → belong in `src/main/resources/`
- `test` profile is for running **tests** → belongs in `src/test/resources/`

---

### 2. Docker Configuration Strategy

**Old approach (hardcoded per profile):**
- `application-test.properties` hardcoded Multipass IP: `tcp://192.168.64.8:2376`
- Developers using host Docker had to modify tracked files or use a different profile

**New approach (flexible, developer-specific):**
- `application-test.properties` defaults to **local Docker** (auto-detect socket)
- Developers using Multipass override via **gitignored `testcontainers.properties` file**
- Supports multiple override mechanisms (see precedence below)

---

### 3. Configuration Precedence

Docker configuration is loaded in this order (highest to lowest priority):

1. **Environment variables**
   ```bash
   export TESTCONTAINERS_DOCKER_HOST=tcp://192.168.64.8:2376
   ```

2. **Gradle properties**
   ```bash
   ./gradlew test -Ptestcontainers.docker.host=tcp://192.168.64.8:2376
   ```

3. **`testcontainers.properties` file** (gitignored, developer-specific)
   ```properties
   testcontainers.docker.remote=true
   testcontainers.docker.host=tcp://192.168.64.8:2376
   ```

4. **`application-test.properties` defaults**
   ```properties
   testcontainers.docker.remote=false  # Local Docker
   ```

---

### 4. Files Created

| File | Purpose |
|---|---|
| `DOCKER_SETUP.md` | Comprehensive setup guide for both Docker configurations |
| `testcontainers.properties.example` | Example configuration for Multipass users |
| `testcontainers.properties` | Active configuration (gitignored, Multipass setup) |
| `src/main/resources/application-dev.properties` | Developer-local application config |
| `src/main/resources/application-k8s.properties` | Kubernetes/production config with placeholders |

---

### 5. Files Modified

| File | Changes |
|---|---|
| `src/test/resources/application-test.properties` | Removed hardcoded Multipass IP, added override documentation |
| `build.gradle` | Added `loadTestcontainersConfig()` function with override support |
| `.gitignore` | Added `testcontainers.properties` to exclude developer-specific config |

---

### 6. Files Deleted

- `src/test/resources/application-dev.properties` (moved to `src/main/resources/`)
- `src/test/resources/application-prod.properties` (renamed to `application-k8s.properties` and moved)

---

## How Developers Use This

### Developer with Host Docker (Docker Desktop / Colima)

**No configuration needed!** Just run tests:

```bash
./gradlew test
gradle clean build
```

Output:
```
🐳 Using local Docker (auto-detect socket)
```

---

### Developer with Multipass VM Docker

**One-time setup:**

1. Copy the example file:
   ```bash
   cp testcontainers.properties.example testcontainers.properties
   ```

2. Edit `testcontainers.properties` with your VM's IP:
   ```properties
   testcontainers.docker.remote=true
   testcontainers.docker.host=tcp://192.168.64.8:2376
   testcontainers.ryuk.disabled=true
   testcontainers.checks.disabled=true
   testcontainers.docker.api-version=1.44
   ```

3. Run tests:
   ```bash
   ./gradlew test
   gradle clean build
   ```

Output:
```
🐳 Using remote Docker: tcp://192.168.64.8:2376
```

---

## CI/CD Configuration

No changes needed for CI/CD pipelines. The defaults in `application-test.properties` assume local Docker, which is the standard CI setup.

If your CI uses remote Docker, set environment variables:

```yaml
# .github/workflows/test.yml
env:
  TESTCONTAINERS_DOCKER_HOST: tcp://docker-daemon:2376
  TESTCONTAINERS_RYUK_DISABLED: true
```

---

## Verification

All tests pass with both configurations:

```bash
# With testcontainers.properties (Multipass)
gradle clean build
# ✅ BUILD SUCCESSFUL — 6 tests passed
# 🐳 Using remote Docker: tcp://192.168.64.8:2376

# Without testcontainers.properties (host Docker)
mv testcontainers.properties testcontainers.properties.backup
./gradlew test
# 🐳 Using local Docker (auto-detect socket)
# (Would pass if Docker Desktop/Colima were installed)
```

---

## Benefits

1. **No code changes required** to switch between Docker setups
2. **No tracked files modified** — developers use gitignored `testcontainers.properties`
3. **Flexible override mechanisms** — environment variables, Gradle properties, or config file
4. **Clear separation of concerns** — main app profiles vs test profiles
5. **CI/CD friendly** — defaults work out-of-the-box for standard CI environments
6. **Well-documented** — `DOCKER_SETUP.md` provides step-by-step instructions

---

## Documentation

See `DOCKER_SETUP.md` for:
- Detailed setup instructions for both Docker configurations
- Troubleshooting guide
- Property reference
- CI/CD configuration examples

