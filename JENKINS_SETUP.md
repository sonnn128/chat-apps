# 🚀 Jenkins CI/CD Setup Guide

This guide will help you set up Jenkins for automated CI/CD deployment of the chat-apps project.

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Jenkins Installation](#jenkins-installation)
3. [Jenkins Configuration](#jenkins-configuration)
4. [Pipeline Setup](#pipeline-setup)
5. [Testing the Pipeline](#testing-the-pipeline)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before setting up Jenkins, ensure you have:

- [ ] Docker and Docker Compose installed
- [ ] Git installed
- [ ] Access to the GitHub repository
- [ ] Docker Hub account (for image registry)
- [ ] Server with at least 4GB RAM and 2 CPU cores

---

## Jenkins Installation

### Option 1: Docker Installation (Recommended)

```bash
# Create Jenkins directories
mkdir -p ~/jenkins_home
mkdir -p ~/jenkins_backup

# Run Jenkins container
docker run -d \
  --name jenkins \
  --restart unless-stopped \
  -p 8090:8080 \
  -p 50000:50000 \
  -v ~/jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker \
  -u root \
  jenkins/jenkins:lts

# Wait for Jenkins to start (about 1-2 minutes)
sleep 60

# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Option 2: Native Installation (Ubuntu/Debian)

```bash
# Install Java
sudo apt update
sudo apt install -y openjdk-17-jdk

# Add Jenkins repository
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'

# Install Jenkins
sudo apt update
sudo apt install -y jenkins

# Start Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

---

## Jenkins Configuration

### Step 1: Initial Setup

1. **Access Jenkins**
   ```
   URL: http://localhost:8090 (or your server IP)
   ```

2. **Unlock Jenkins**
   - Paste the initial admin password obtained above
   - Click "Continue"

3. **Install Suggested Plugins**
   - Click "Install suggested plugins"
   - Wait for installation to complete

4. **Create Admin User**
   - Username: `admin`
   - Password: (set a strong password)
   - Full name: `Admin User`
   - Email: `your-email@example.com`

### Step 2: Install Required Plugins

Go to **Manage Jenkins** → **Manage Plugins** → **Available** and install:

1. **Docker Pipeline** - For Docker operations in pipeline
2. **Pipeline** - Core pipeline functionality
3. **Git plugin** - For Git integration
4. **GitHub plugin** - For GitHub integration
5. **Credentials Binding** - For managing secrets
6. **Blue Ocean** (Optional) - Modern UI for pipelines
7. **SSH Agent** (Optional) - For SSH deployment
8. **Email Extension** (Optional) - For email notifications

Click "Install without restart" and wait for completion.

### Step 3: Configure System Tools

#### Configure Maven

1. Go to **Manage Jenkins** → **Global Tool Configuration**
2. Scroll to **Maven** section
3. Click "Add Maven"
4. Name: `Maven-3.9`
5. Check "Install automatically"
6. Version: `3.9.5`
7. Click "Save"

#### Configure JDK

1. In **Global Tool Configuration** → **JDK** section
2. Click "Add JDK"
3. Name: `JDK-17`
4. Check "Install automatically"
5. Version: `jdk-17.0.8+7`
6. Click "Save"

#### Configure Docker (if using Docker)

1. In **Global Tool Configuration** → **Docker** section
2. Click "Add Docker"
3. Name: `Docker-Latest`
4. Check "Install automatically"
5. Docker version: `latest`
6. Click "Save"

### Step 4: Configure Credentials

#### Docker Hub Credentials

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Click **(global)** domain
3. Click "Add Credentials"
4. Fill in:
   - Kind: `Username with password`
   - Scope: `Global`
   - Username: (your Docker Hub username)
   - Password: (your Docker Hub password or access token)
   - ID: `dockerhub-credentials`
   - Description: `Docker Hub Credentials`
5. Click "OK"

#### GitHub Credentials

1. Click "Add Credentials" again
2. Fill in:
   - Kind: `Username with password` or `SSH Username with private key`
   - Scope: `Global`
   - Username: (your GitHub username)
   - Password/Private Key: (your GitHub password/PAT or SSH key)
   - ID: `github-credentials`
   - Description: `GitHub Credentials`
3. Click "OK"

---

## Pipeline Setup

### Step 1: Create Pipeline Job

1. Go to Jenkins Dashboard
2. Click "New Item"
3. Enter name: `chat-apps-pipeline`
4. Select "Pipeline"
5. Click "OK"

### Step 2: Configure Pipeline

#### General Settings

1. Check "GitHub project"
2. Project url: `https://github.com/sonnn128/chat-apps`

#### Build Triggers

1. Check "GitHub hook trigger for GITScm polling" (for auto-trigger on push)
2. Or check "Poll SCM" and set schedule: `H/5 * * * *` (poll every 5 minutes)

#### Pipeline Configuration

1. Definition: `Pipeline script from SCM`
2. SCM: `Git`
3. Repository URL: `https://github.com/sonnn128/chat-apps.git`
4. Credentials: Select `github-credentials`
5. Branch Specifier: `*/main` (or `*/develop` for development)
6. Script Path: `Jenkinsfile`

### Step 3: Configure Branch-based Pipelines

For multi-branch pipeline (development, staging, production):

1. Create "Multibranch Pipeline" instead of simple Pipeline
2. Branch Sources: Add `Git`
3. Repository URL: `https://github.com/sonnn128/chat-apps.git`
4. Credentials: Select `github-credentials`
5. Behaviors: Add "Discover branches"
6. Scan Multibranch Pipeline Triggers: Check and set interval
7. Click "Save"

### Step 4: Configure GitHub Webhook (Optional)

For automatic builds on push:

1. Go to GitHub repository settings
2. Navigate to **Settings** → **Webhooks** → **Add webhook**
3. Payload URL: `http://your-jenkins-url:8090/github-webhook/`
4. Content type: `application/json`
5. Events: Select "Just the push event"
6. Check "Active"
7. Click "Add webhook"

---

## Testing the Pipeline

### Step 1: Manual Build Test

1. Go to your pipeline job
2. Click "Build Now"
3. Click on the build number (e.g., #1)
4. Click "Console Output" to see logs
5. Monitor the pipeline stages

### Step 2: Verify Pipeline Stages

The pipeline should execute these stages:

1. ✓ Checkout - Clone repository
2. ✓ Environment Setup - Prepare environment
3. ✓ Build Services - Build all Java services and UI
4. ✓ Run Tests - Execute unit tests
5. ✓ Build Docker Images - Create Docker images
6. ✓ Tag and Push Images - Push to registry (main branch only)
7. ✓ Deploy - Deploy based on branch
8. ✓ Post-Deployment Verification - Health checks

### Step 3: Check Build Results

After successful build:

1. All stages should be green ✓
2. Docker images should be in registry
3. Services should be deployed (if deployment stage ran)
4. Health checks should pass

---

## Pipeline Environments

### Development (develop branch)

- Auto-deploys on commit
- No approval required
- Minimal health checks

### Staging (staging branch)

- Auto-deploys on commit
- More comprehensive testing
- Integration tests run

### Production (main branch)

- **Manual approval required**
- Full health checks
- Database backup before deployment
- Rollback capability

---

## Monitoring Pipeline

### Blue Ocean View

1. Install Blue Ocean plugin
2. Click "Open Blue Ocean" in sidebar
3. View visual pipeline progress
4. See detailed logs per stage

### Email Notifications

Configure email notifications:

1. Go to **Manage Jenkins** → **Configure System**
2. Scroll to **Extended E-mail Notification**
3. SMTP server: `smtp.gmail.com`
4. SMTP Port: `465`
5. Use SSL: Check
6. Credentials: Add Gmail credentials
7. Default recipients: `your-email@example.com`

---

## Advanced Configuration

### Docker-in-Docker Setup

If Jenkins needs to build Docker images:

```bash
# Run Jenkins with Docker socket mounted
docker run -d \
  --name jenkins \
  -p 8090:8080 \
  -p 50000:50000 \
  -v ~/jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker \
  --group-add $(getent group docker | cut -d: -f3) \
  -u root \
  jenkins/jenkins:lts

# Install Docker client inside Jenkins
docker exec -u root jenkins apt-get update
docker exec -u root jenkins apt-get install -y docker.io
```

### Parallel Execution

The Jenkinsfile already supports parallel execution for:
- Building multiple services simultaneously
- Running tests in parallel
- Faster pipeline execution

### Pipeline as Code

All pipeline configuration is in `Jenkinsfile`:
- Version controlled
- Reviewable in PRs
- Reproducible across environments

---

## Troubleshooting

### Issue: "Permission denied" when accessing Docker

**Solution:**
```bash
# Add Jenkins user to docker group
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Or for Docker container
docker exec -u root jenkins usermod -aG docker jenkins
docker restart jenkins
```

### Issue: Maven build fails with "Cannot resolve dependencies"

**Solution:**
```bash
# Configure Maven settings
docker exec jenkins mkdir -p /var/jenkins_home/.m2
docker exec jenkins bash -c 'cat > /var/jenkins_home/.m2/settings.xml << EOF
<settings>
  <mirrors>
    <mirror>
      <id>central</id>
      <url>https://repo.maven.apache.org/maven2</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF'
```

### Issue: Build fails with "Out of Memory"

**Solution:**
```bash
# Increase Jenkins heap size
docker run -d \
  --name jenkins \
  -p 8090:8080 \
  -e JAVA_OPTS="-Xmx2048m -Xms512m" \
  -v ~/jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts
```

### Issue: GitHub webhook not triggering builds

**Solution:**
1. Check webhook URL is correct
2. Verify Jenkins is accessible from internet
3. Check webhook delivery in GitHub settings
4. Ensure "GitHub hook trigger" is enabled in job

### Issue: Docker push fails with "authentication required"

**Solution:**
```bash
# Test Docker Hub login
docker login -u your-username

# Verify credentials in Jenkins
# Manage Jenkins → Manage Credentials
# Ensure dockerhub-credentials exists and is correct
```

---

## Backup and Restore

### Backup Jenkins

```bash
# Backup Jenkins home directory
tar -czf jenkins_backup_$(date +%Y%m%d).tar.gz ~/jenkins_home

# Backup specific job
tar -czf job_backup.tar.gz ~/jenkins_home/jobs/chat-apps-pipeline
```

### Restore Jenkins

```bash
# Stop Jenkins
docker stop jenkins

# Restore backup
tar -xzf jenkins_backup_YYYYMMDD.tar.gz -C ~/

# Start Jenkins
docker start jenkins
```

---

## Security Best Practices

1. **Enable CSRF Protection**: Already enabled by default
2. **Configure Authorization**: Use "Project-based Matrix Authorization Strategy"
3. **Secure Credentials**: Use Jenkins credentials store, never hardcode
4. **Update Regularly**: Keep Jenkins and plugins updated
5. **Use HTTPS**: Configure reverse proxy with SSL
6. **Audit Logs**: Enable audit trail plugin
7. **Limit Build History**: Configure "Discard old builds" (keep last 10)

---

## Next Steps

After setting up Jenkins:

1. ✓ Test manual build
2. ✓ Test automatic build on push
3. ✓ Configure notifications
4. ✓ Set up monitoring dashboards
5. ✓ Document deployment procedures
6. ✓ Train team on pipeline usage

---

## Resources

- Jenkins Documentation: https://www.jenkins.io/doc/
- Docker Pipeline Plugin: https://plugins.jenkins.io/docker-workflow/
- Blue Ocean: https://www.jenkins.io/projects/blueocean/
- Pipeline Syntax: https://www.jenkins.io/doc/book/pipeline/syntax/

---

## For Your CV/Resume

After completing this setup, you can highlight:

```
✓ Implemented Jenkins CI/CD pipeline for microservices application
✓ Configured multi-branch pipeline with automated testing and deployment
✓ Integrated Docker containerization with Jenkins
✓ Set up automated Docker image building and registry push
✓ Implemented blue-green deployment strategy
✓ Configured health checks and post-deployment verification
✓ Set up monitoring and alerting for build failures
✓ Automated database backup before production deployment
```

---

**Good luck with your Jenkins setup and internship application! 🚀**
