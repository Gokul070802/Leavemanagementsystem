# Leave Management System - Project Demo Guide
**Complete Development to Deployment Journey**

---

## 📋 Executive Overview

This guide explains the **entire lifecycle** of the Leave Management System from development to production, including all tools, steps, and automation flows.

### What is This Application?
- **Purpose**: A web-based system for employees to apply for leave and managers to approve/reject requests
- **Tech Stack**: Spring Boot 3.2 + Java 21 (Backend) | HTML/JS (Frontend) | PostgreSQL/GCP (Database)
- **Deployment**: Docker containers on cloud infrastructure
- **Current Status**: 95% complete, production-ready

---

## 🏗️ PHASE 1: DEVELOPMENT ENVIRONMENT

### What Developers Need

| Tool | Purpose | Installation |
|------|---------|--------------|
| **Java 21** | Backend runtime & compilation | Download from Oracle/Eclipse Temurin |
| **Maven 3.9+** | Build & dependency management | Automatic with IDE or manual install |
| **PostgreSQL** | Database (we use Supabase cloud) | Already hosted on Supabase (no local install needed) |
| **IDE (VS Code/IntelliJ)** | Code editor | Download from vendor |
| **Git** | Version control | Git installation + GitHub account |
| **Docker Desktop** | Container runtime (for local testing) | Download from Docker |
| **Postman** | API testing | Optional but recommended |

### Development Workflow (What Developers Do Daily)

```
1. Clone repository from GitHub
   ↓
2. Set up environment variables (.env file)
   ↓
3. Run: mvn clean install  (downloads dependencies)
   ↓
4. Start backend: mvn spring-boot:run
   ↓
5. Open frontend in browser: localhost:8081
   ↓
6. Code → Test → Commit → Push to GitHub
```

### Environment Setup Files
- **`.env` file**: Contains database credentials, JWT secrets
  - Stored locally (NOT in git for security)
  - Example variables:
    ```
    DB_URL=postgresql://...
    DB_PASSWORD=***
    JWT_SECRET=your-secret-key
    ```

---

## 🧪 PHASE 2: LOCAL TESTING & DEVELOPMENT

### Developer Testing (Before Pushing to Git)

```
What to test:
├── Unit Tests (test individual Java methods)
├── Integration Tests (test database interactions)
├── API Testing (Postman - test 24+ REST endpoints)
│   ├── User Registration/Login
│   ├── Apply Leave
│   ├── Approve/Reject Leave
│   ├── View Leave Balance
│   └── ... (16/17 user stories)
└── UI Testing (manual browser testing)
```

### How to Run Tests

| Test Type | Command | Purpose |
|-----------|---------|---------|
| All tests | `mvn test` | Run unit + integration tests |
| Specific test | `mvn test -Dtest=ClassName` | Test one class |
| Skip tests | `mvn install -DskipTests` | Fast build without testing |

### Testing Checklist for Demo
- ✅ Login with admin credentials
- ✅ Create new employee
- ✅ Apply for leave (as employee)
- ✅ Approve/Reject (as manager)
- ✅ View leave balance
- ✅ Check audit logs

---

## 🏗️ PHASE 3: BUILD PROCESS

### What is "Building"?
Building = Converting source code (`.java` files) into executable code (`.class` files in JAR)

### Build Steps (Automated by Maven)

```
1. Compile Java code
   └─ Checks syntax, generates bytecode
   
2. Run tests
   └─ Executes test cases
   
3. Package application
   └─ Creates JAR file (Java Archive)
   └─ Includes all dependencies
   
4. Output
   └─ target/Leavemanagementsystem-0.0.1-SNAPSHOT.jar
   └─ Size: ~50-70 MB (with all libraries)
```

### Build Command
```bash
mvn clean install
# OR (skip tests for faster build)
mvn clean package -DskipTests
```

### Build Artifacts
```
target/
├── Leavemanagementsystem-0.0.1-SNAPSHOT.jar  ← Main application
├── classes/                                   ← Compiled code
├── test-classes/                             ← Compiled tests
└── generated-sources/                        ← Auto-generated files
```

### Build Duration
- **First build**: ~3-5 minutes (downloads dependencies)
- **Subsequent builds**: ~1-2 minutes (dependencies cached)

---

## 🐳 PHASE 4: CONTAINERIZATION (Docker)

### Why Docker?
- **Problem**: App works on dev's laptop but fails on server ("works on my machine" issue)
- **Solution**: Package app + dependencies + OS into a container = guaranteed to work anywhere

### What is Docker?
Think of Docker as a **lightweight virtual machine** that contains:
- Operating System (Linux)
- Java Runtime
- Application JAR file
- All dependencies
- Configuration files

### How We Build Docker Image

#### Step 1: Create Dockerfile (Recipe for Docker)
```dockerfile
FROM openjdk:21-slim              # Start with Java 21 base image
WORKDIR /app                      # Set working directory
COPY target/*.jar app.jar         # Copy JAR file into container
EXPOSE 8081                       # Expose port 8081
ENTRYPOINT ["java", "-jar", "app.jar"]  # Run the app
```

#### Step 2: Build Docker Image
```bash
# From project root directory
docker build -t lms-app:latest .

# What happens:
# 1. Downloads Java 21 base image (~500 MB)
# 2. Copies your JAR into the image
# 3. Creates final image (~600-700 MB)
```

#### Step 3: Run Container Locally
```bash
docker run -d \
  -p 8081:8081 \
  -e DB_URL=postgresql://... \
  -e DB_PASSWORD=*** \
  lms-app:latest

# Result: Application running inside container
# Access: http://localhost:8081
```

### Docker Benefits for This Project
| Benefit | Details |
|---------|---------|
| **Consistency** | Works same on laptop, test server, production |
| **Easy deployment** | Just run `docker run` command |
| **Scalability** | Can run multiple containers for load balancing |
| **Isolation** | Container issues don't affect host system |

---

## 🚀 PHASE 5: DEPLOYMENT OPTIONS

### Option A: Traditional Cloud Server (AWS EC2, Azure VM, etc.)

```
Steps:
1. Create virtual machine (Linux server)
2. Install Docker on server
3. Upload Docker image (or pull from registry)
4. Run: docker run -p 8081:8081 lms-app:latest
5. Expose port 8081 to internet via firewall rules
6. Setup domain name (e.g., lms.company.com)
7. Add SSL certificate (HTTPS)
```

**Pros**: Full control, lower cost
**Cons**: Need server management expertise

---

### Option B: Container Orchestration (Kubernetes)

```
For production with multiple servers:

1. Deploy Docker image to Kubernetes cluster
   ├── Multiple replicas running simultaneously
   ├── Auto-scaling based on traffic
   └── Load balancing between instances
   
2. Kubernetes handles:
   ├── Restarting failed containers
   ├── Distributing traffic
   ├── Rolling updates (zero downtime)
   └── Resource management
```

**Pros**: Highly scalable, professional-grade
**Cons**: Complex to set up

---

### Option C: Cloud Container Services (Recommended)

#### AWS Container Services
- **ECS (Elastic Container Service)**: Managed containers
  ```
  Steps:
  1. Upload Docker image to ECR (registry)
  2. Create ECS task definition
  3. Deploy task to ECS cluster
  4. ECS auto-scales, monitors health
  ```

#### Azure Container Instances
- Simplest option: just upload image
  ```bash
  az container create \
    --image lms-app:latest \
    --port 8081 \
    --cpu 1 --memory 2
  ```

#### AWS App Runner / Heroku
- **Easiest**: Just push code/image, service handles everything

---

## 🔄 PHASE 6: CI/CD PIPELINE (GitHub Actions)

### What is CI/CD?

| Term | Meaning | What It Does |
|------|---------|--------------|
| **CI** (Continuous Integration) | Developers commit code frequently | Automatically test & build |
| **CD** (Continuous Deployment) | Code automatically deployed to production | Zero manual intervention after approval |

### Our GitHub Actions Pipeline

```
Developer pushes code to GitHub
    ↓
[GitHub Actions Triggered]
    ↓
├─ STEP 1: Checkout code
├─ STEP 2: Set up Java 21
├─ STEP 3: Cache Maven dependencies
├─ STEP 4: Run mvn clean install (compiles + tests)
├─ STEP 5: Build Docker image
├─ STEP 6: Push image to registry (Docker Hub / ECR)
└─ STEP 7: Deploy to cloud (ECS, App Runner, etc.)
    ↓
[Application available on production URL]
```

### Pipeline File Location
```
.github/
└── workflows/
    └── deploy.yml  ← GitHub Actions configuration
```

### Pipeline Workflow Breakdown

#### Trigger Events
```yaml
on:
  push:
    branches: [main]           # Run when pushing to main branch
  pull_request:
    branches: [main]           # Run when creating pull request
  manual:                      # Can manually trigger from GitHub UI
```

#### Stage 1: Code Checkout & Setup
```
- Clone latest code from GitHub
- Install Java 21
- Setup Maven cache (speeds up builds)
```

#### Stage 2: Build & Test
```
mvn clean install
├── Compile source code
├── Run all tests (unit + integration)
├── Generate JAR file
└── If FAILED → Stop pipeline, notify developer
```

#### Stage 3: Docker Build
```
docker build -t lms-app:v1.0.0 .
├── Create Docker image
├── Tag with version number
└── If FAILED → Stop pipeline
```

#### Stage 4: Push to Registry
```
docker push docker.io/company/lms-app:v1.0.0
├── Upload to Docker Hub (or private registry)
├── Image available for deployment
└── If FAILED → Stop pipeline
```

#### Stage 5: Deploy to Production
```
Deploy Docker image to cloud service
├── New container starts with latest code
├── Old container stops (zero downtime with rolling update)
├── Health checks verify app is working
└── If health check FAILS → Rollback to previous version
```

---

## 📊 Pipeline Success Criteria

Pipeline only proceeds to next stage if current stage succeeds:

```
✅ Tests Pass
    ↓
✅ Build Succeeds
    ↓
✅ Docker Image Created
    ↓
✅ Image Pushed to Registry
    ↓
✅ Deployed to Production
    ↓
✅ Health Checks Pass
    ↓
✅ Accessible at production URL
```

If ANY stage fails:
- ❌ Pipeline STOPS
- 📧 Email notification sent to team
- 🔧 Developer fixes issue
- 🔄 Commits fix → Pipeline runs again

---

## 🔐 PHASE 7: SECURITY IN CI/CD

### Secrets Management

```
Sensitive data (passwords, API keys) MUST NOT be in code:

❌ WRONG:
  DB_PASSWORD = "mypassword123"   ← In code (visible to everyone)

✅ RIGHT:
  DB_PASSWORD = process.env.DB_PASSWORD   ← Read from environment
  
  # GitHub: Settings → Secrets
  # Set DB_PASSWORD = mypassword123 (only in GitHub, not in code)
```

### Security Checks in Pipeline

```
Pipeline automatically:
├─ Scans code for vulnerabilities (SonarQube)
├─ Checks dependencies for known CVEs
├─ Validates Docker image security
├─ Runs SAST (Static Application Security Testing)
└─ Blocks deployment if issues found
```

---

## 📈 PHASE 8: MONITORING & MAINTENANCE

### Post-Deployment Monitoring

```
Application running in production:

├─ Uptime Monitoring
│  └─ Alert if server down (SMS, email)
│
├─ Performance Monitoring
│  ├─ Response time
│  ├─ CPU/Memory usage
│  ├─ Database query performance
│  └─ Alert if degraded
│
├─ Error Tracking
│  ├─ Capture application errors
│  ├─ Stack traces logged
│  ├─ Dashboard for error trending
│  └─ Alert on new error patterns
│
└─ User Activity Logging
   ├─ Who accessed what
   ├─ Audit trail for compliance
   └─ Security event detection
```

### Monitoring Tools

| Tool | Purpose |
|------|---------|
| **CloudWatch** (AWS) / **Azure Monitor** | CPU, Memory, Network metrics |
| **DataDog** / **New Relic** | APM (Application Performance) |
| **ELK Stack** / **Splunk** | Log aggregation & analysis |
| **Sentry** | Error tracking |
| **PagerDuty** | Alert management & on-call scheduling |

---

## 🔄 Typical Production Incident Response

```
1. Alert triggered (high error rate detected)
   ↓
2. On-call engineer notified (SMS + phone)
   ↓
3. Check monitoring dashboard → identify issue
   ↓
4. Option A: Rollback to previous version
   └─ docker run lms-app:previous-version
   
   OR
   
   Option B: Fix issue
   ├─ Commit fix to GitHub
   ├─ Pipeline runs automatically
   ├─ New version deployed (same steps as normal)
   └─ Takes ~5-10 minutes
   ↓
5. Verify fix → Alert resolved
   ↓
6. Post-incident review → prevent next time
```

---

## 🎯 COMPLETE PROJECT TIMELINE

### Example: New Feature Deployment (End-to-End)

```
Monday 9:00 AM
  ├─ Developer: Creates feature branch
  ├─ Developer: Writes code + tests
  ├─ Local: Runs mvn test (all green ✓)
  └─ Local: Tests in browser
      ↓
Monday 5:00 PM
  ├─ Developer: Commits code + pushes to GitHub
  ├─ GitHub: Creates Pull Request (asks for review)
  └─ Team Lead: Reviews code (finds issues)
      ↓
Tuesday 10:00 AM
  ├─ Developer: Fixes feedback
  ├─ Developer: Commits changes
  └─ Team Lead: Approves PR ✓
      ↓
Tuesday 10:30 AM
  ├─ Developer: Clicks "Merge to main" button
  ├─ GitHub Actions: Automatically triggered
  │  ├─ Compile code ✓
  │  ├─ Run tests ✓
  │  ├─ Build Docker image ✓
  │  ├─ Push to registry ✓
  │  └─ Deploy to production ✓ (takes ~5-10 min)
  ↓
Tuesday 10:40 AM
  ├─ Feature LIVE on production
  ├─ Monitoring: Health checks passing ✓
  └─ Team: Feature available to all users
```

**Total time**: ~25 hours (mostly waiting for review, not blocked)

---

## 🛠️ TOOLS SUMMARY TABLE

### Development Tools

| Tool | Used By | Purpose | Cost |
|------|---------|---------|------|
| VS Code/IntelliJ | Developers | Code editor | Free / Paid |
| Java 21 | Backend | Runtime | Free |
| Maven | Build | Dependency & build management | Free |
| PostgreSQL/Supabase | Database | Data storage | Free-Paid |
| Git/GitHub | Version control | Code repository | Free-Paid |
| Postman | Testing | API testing | Free-Paid |

### DevOps Tools

| Tool | Used By | Purpose | Cost |
|------|---------|---------|------|
| Docker | Containerization | Package application | Free |
| GitHub Actions | CI/CD | Automation | Free (2000 min/month) |
| Docker Hub/ECR | Registry | Store images | Free-Paid |
| AWS/Azure | Cloud | Server infrastructure | Pay-as-you-go |
| DataDog/Sentry | Monitoring | Alerts & logs | Paid (varies) |

---

## 📋 DEMO CHECKLIST FOR MANAGER

### Part 1: Development & Build (5 min)
- [ ] Show source code structure
- [ ] Show test coverage report
- [ ] Show build log → compiled successfully
- [ ] Show generated JAR file size

### Part 2: Local Testing (5 min)
- [ ] Login as admin
- [ ] Create employee
- [ ] Apply leave (as employee)
- [ ] Approve leave (as manager)
- [ ] Show audit logs

### Part 3: Docker & Containerization (3 min)
- [ ] Show Dockerfile
- [ ] Show Docker image layers
- [ ] Show running container: `docker ps`

### Part 4: CI/CD Pipeline (5 min)
- [ ] Show GitHub Actions workflow file
- [ ] Show recent pipeline run (with all stages)
- [ ] Show deployment logs
- [ ] Show notification on failure example

### Part 5: Production Monitoring (3 min)
- [ ] Show CloudWatch/DataDog dashboard
- [ ] Show uptime percentage
- [ ] Show response time metrics
- [ ] Show error logs & alerts

### Part 6: Rollback Demo (3 min)
- [ ] Simulate issue → show alert
- [ ] Rollback to previous version command
- [ ] Show immediate recovery

---

## 💡 KEY TALKING POINTS FOR MANAGER

### 1. **Automation Benefits**
   - "We don't manually deploy → GitHub Actions does it automatically"
   - "99% fewer human errors in deployment"
   - "Deployment takes ~5-10 minutes consistently"

### 2. **Risk Reduction**
   - "Tests run BEFORE production deployment"
   - "Automated rollback if something breaks"
   - "Zero downtime deployments with health checks"

### 3. **Scalability**
   - "Can go from 1 to 100 users without code changes"
   - "Docker + Kubernetes = auto-scaling"
   - "Geographic distribution for global users"

### 4. **Security**
   - "Secrets managed in GitHub (not in code)"
   - "Automated vulnerability scanning"
   - "Full audit trail of who changed what"

### 5. **Cost Efficiency**
   - "Pay only for resources used (cloud pricing)"
   - "Automated infrastructure = no DevOps hire needed"
   - "CI/CD free tier sufficient for team our size"

### 6. **Time to Market**
   - "New features go live in hours, not days"
   - "No deployment bottleneck"
   - "Team velocity increases over time"

---

## 🔗 QUICK REFERENCE COMMANDS

### Local Development
```bash
# Clone repo
git clone https://github.com/company/leave-management-system.git

# Setup environment
cp .env.example .env  # Edit with your credentials

# Build & run
mvn clean install
mvn spring-boot:run

# Run tests only
mvn test
```

### Docker Commands
```bash
# Build image
docker build -t lms-app:latest .

# Run locally
docker run -d -p 8081:8081 lms-app:latest

# Check running containers
docker ps

# View logs
docker logs <container-id>

# Stop container
docker stop <container-id>
```

### CI/CD Commands
```bash
# View GitHub Actions workflow
cat .github/workflows/deploy.yml

# Manually trigger workflow
# (Via GitHub UI → Actions → Select workflow → Run workflow)
```

---

## 📞 Support & Next Steps

### If Issues Arise

| Issue | Resolution | Time |
|-------|-----------|------|
| Build fails | Check Java version & Maven cache | 5 min |
| Tests fail | Review test logs, debug locally | 15-30 min |
| Docker build fails | Check Dockerfile syntax & base image | 10 min |
| Deployment fails | Check logs in GitHub Actions | 10 min |
| Production down | Execute rollback procedure | 5 min |

### Contacts
- **Build Issues**: DevOps Team
- **Code Issues**: Development Lead
- **Deployment**: DevOps/SRE Team
- **Monitoring Alerts**: On-call Engineer

---

## 📌 CONCLUSION

**The Leave Management System is:**
- ✅ Fully automated from code to production
- ✅ Tested automatically before each deployment
- ✅ Scalable to grow with business needs
- ✅ Monitored 24/7 for issues
- ✅ Secure with secrets management
- ✅ Easy to understand deployment process

**Next demo**: Run through entire pipeline live with manager watching real-time updates! 🚀

