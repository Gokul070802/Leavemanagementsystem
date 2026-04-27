# Leave Management System - Visual Flow Diagrams

## 1. Complete Application Lifecycle Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                    LOCAL DEVELOPMENT (Developer)                     │
│                                                                       │
│  Laptop/PC                                                            │
│  ├─ Java IDE (VS Code, IntelliJ)                                    │
│  ├─ Maven (build tool)                                              │
│  ├─ Git (version control)                                           │
│  └─ Docker Desktop (testing containers)                             │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                    ┌────────▼─────────┐
                    │  1. CODE & TEST  │
                    │  ✓ Write code    │
                    │  ✓ Run unit tests│
                    │  ✓ Test locally  │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────────────┐
                    │  2. GIT PUSH TO GITHUB   │
                    │  (Developer pushes code) │
                    └────────┬─────────────────┘
                             │
        ┌────────────────────▼──────────────────────┐
        │   GITHUB ACTIONS CI/CD PIPELINE TRIGGERED │ (Automated)
        │                                            │
        │  Stage 1: Code Checkout                   │
        │  Stage 2: Build & Test (mvn install)      │
        │  Stage 3: Docker Build                    │
        │  Stage 4: Push to Registry                │
        │  Stage 5: Deploy to Cloud                 │
        │                                            │
        │  ✓ All pass? → Deploy                     │
        │  ✗ Any fail? → Stop & Alert               │
        └────────┬──────────────────────────────────┘
                 │
        ┌────────▼────────────────────┐
        │  PRODUCTION DEPLOYMENT       │
        │  Cloud Platform (AWS/Azure)  │
        │                              │
        │  ├─ Docker container running │
        │  ├─ Exposed on port 8081     │
        │  ├─ Connected to database    │
        │  └─ Behind load balancer     │
        └────────┬────────────────────┘
                 │
        ┌────────▼──────────────────────┐
        │  USERS ACCESSING APPLICATION  │
        │  via domain (lms.company.com) │
        │                               │
        │  ├─ Employees apply leave     │
        │  ├─ Managers approve requests │
        │  └─ Admin manages system      │
        └────────┬──────────────────────┘
                 │
        ┌────────▼────────────────────────┐
        │  MONITORING & ALERTING          │
        │  (Continuous - 24/7)            │
        │                                 │
        │  ├─ Check uptime                │
        │  ├─ Monitor CPU/Memory          │
        │  ├─ Track error rates           │
        │  ├─ Alert on issues             │
        │  └─ Auto-rollback on failure    │
        └─────────────────────────────────┘
```

---

## 2. Development to Production Timeline

```
┌──────────────────────────────────────────────────────────────────────┐
│  TIMELINE: Feature from code to production                           │
└──────────────────────────────────────────────────────────────────────┘

MON 9:00 AM          📝 Developer writes code
                     └─ Creates new branch in Git
                     └─ Writes feature + tests locally

MON 5:00 PM          🔄 Code review requested
                     └─ Push to GitHub
                     └─ Create Pull Request
                     └─ Team lead reviews

TUE 10:00 AM         ✓ Pull Request approved
                     └─ Feedback incorporated

TUE 10:30 AM         ⚡ MERGE to main branch
                     └─ GitHub Actions automatically triggered

TUE 10:31 AM         🔧 BUILD STAGE
                     ├─ Maven compiles code      (30 sec)
                     ├─ Tests run               (2 min)
                     └─ JAR package created     (1 min)

TUE 10:34 AM         🐳 DOCKER STAGE
                     ├─ Docker image built      (2 min)
                     └─ Pushed to registry      (1 min)

TUE 10:37 AM         ☁️ DEPLOYMENT STAGE
                     ├─ New container starts    (1 min)
                     ├─ Health checks pass      (30 sec)
                     └─ Traffic redirected      (30 sec)

TUE 10:40 AM         ✅ LIVE IN PRODUCTION!
                     └─ ~10 minutes total
                     └─ ALL AUTOMATED (0 manual intervention)

[Total time: 25 hours, but only ~10 minutes of automation]
```

---

## 3. Build & Deployment Architecture

```
                        ┌─────────────────┐
                        │   DEVELOPER     │
                        │   (Writes code) │
                        └────────┬────────┘
                                 │
                         (git push)
                                 │
                    ┌────────────▼──────────────┐
                    │  GitHub Repository        │
                    │  - Main branch            │
                    │  - Feature branches       │
                    │  - Pull requests          │
                    └────────────┬──────────────┘
                                 │
                   (Webhook triggers on push)
                                 │
                    ┌────────────▼──────────────┐
                    │  GitHub Actions (CI/CD)   │
                    │                           │
                    ├─ Checkout code            │
                    ├─ Setup Java 21            │
                    ├─ Maven clean install      │ ◄─ Compile & test
                    ├─ Docker build             │ ◄─ Create image
                    ├─ Push to registry         │ ◄─ Store image
                    └────────────┬──────────────┘
                                 │
              ┌──────────────────┴──────────────────┐
              │                                     │
        ┌─────▼──────────┐            ┌────────────▼──────┐
        │  Docker Hub    │            │  AWS ECR Registry  │
        │  (Public)      │            │  (Private)         │
        └────────┬───────┘            └────────────┬───────┘
                 │                                 │
    ┌────────────┴─────────────────────────┬──────┘
    │                                      │
    │  ┌──────────────────────────────────▼─┐
    │  │   AWS / Azure / Cloud Platform     │
    │  │                                    │
    │  ├─ Deploy container                 │
    │  ├─ Auto-scale replicas              │
    │  ├─ Load balance traffic             │
    │  ├─ Health monitoring                │
    │  └─ Rollback on failure              │
    │                                      │
    │  ┌──────────────────────────────────┐│
    │  │ Container Instance 1              ││
    │  │ lms-app v1.2.3                    ││
    │  │ :8081                             ││
    │  └──────────────────────────────────┘│
    │                                      │
    │  ┌──────────────────────────────────┐│
    │  │ Container Instance 2              ││
    │  │ lms-app v1.2.3                    ││
    │  │ :8081                             ││
    │  └──────────────────────────────────┘│
    │                                      │
    │  ┌──────────────────────────────────┐│
    │  │ Container Instance 3              ││
    │  │ lms-app v1.2.3                    ││
    │  │ :8081                             ││
    │  └──────────────────────────────────┘│
    │                                      │
    └──────────────────────────────────────┘
                    │
         (Distributed across 3 regions)
                    │
              ┌─────▼──────┐
              │   Database │
              │  Supabase  │
              │ PostgreSQL │
              └────────────┘
```

---

## 4. GitHub Actions Pipeline (Detailed)

```
GITHUB PUSH EVENT
       │
       ▼
┌──────────────────────┐
│ Workflow Triggered   │
│ (deploy.yml)         │
└──────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│ STAGE 1: Checkout & Setup                │
├──────────────────────────────────────────┤
│ ✓ actions/checkout@v3                    │
│   └─ Clone repository code               │
│                                          │
│ ✓ actions/setup-java@v3                  │
│   └─ Setup Java 21 JDK                   │
│                                          │
│ ✓ actions/cache@v3                       │
│   └─ Cache Maven dependencies             │
└──────────────────────┬───────────────────┘
                       │
                       ▼
         ┌──────────────────────────────────┐
         │ Compilation Check                │
         │ FAILED? ────► STOP & ALERT       │
         │ SUCCESS? ──► Continue            │
         └──────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────┐
│ STAGE 2: Build & Test                    │
├──────────────────────────────────────────┤
│ $ mvn clean install                      │
│                                          │
│ ✓ Compile source code                    │
│ ✓ Run unit tests                         │
│ ✓ Run integration tests                  │
│ ✓ Check code coverage                    │
│ ✓ Package JAR file                       │
└──────────────────────┬───────────────────┘
                       │
                       ▼
         ┌──────────────────────────────────┐
         │ Test Results Check               │
         │ FAILED? ────► STOP & ALERT       │
         │ SUCCESS? ──► Continue            │
         └──────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────┐
│ STAGE 3: Docker Build                    │
├──────────────────────────────────────────┤
│ $ docker build -t lms:v1.0.0 .           │
│                                          │
│ ✓ Pull base image (Java 21)              │
│ ✓ Copy JAR into image                    │
│ ✓ Set environment variables              │
│ ✓ Expose port 8081                       │
│ ✓ Create final image                     │
└──────────────────────┬───────────────────┘
                       │
                       ▼
         ┌──────────────────────────────────┐
         │ Image Build Check                │
         │ FAILED? ────► STOP & ALERT       │
         │ SUCCESS? ──► Continue            │
         └──────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────┐
│ STAGE 4: Push to Registry                │
├──────────────────────────────────────────┤
│ $ docker push docker.io/company/lms:...  │
│                                          │
│ ✓ Login to Docker Hub                    │
│ ✓ Upload image layers                    │
│ ✓ Tag with version                       │
│ ✓ Update latest tag                      │
└──────────────────────┬───────────────────┘
                       │
                       ▼
         ┌──────────────────────────────────┐
         │ Registry Push Check              │
         │ FAILED? ────► STOP & ALERT       │
         │ SUCCESS? ──► Continue            │
         └──────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────┐
│ STAGE 5: Deploy to Production            │
├──────────────────────────────────────────┤
│ ✓ Connect to AWS/Azure                   │
│ ✓ Deploy new container version           │
│ ✓ Run health checks                      │
│ ✓ Wait for readiness probe               │
│ ✓ Update load balancer                   │
│ ✓ Drain old containers                   │
└──────────────────────┬───────────────────┘
                       │
                       ▼
         ┌──────────────────────────────────┐
         │ Deployment Health Check          │
         │ FAILED? ────► ROLLBACK           │
         │ SUCCESS? ──► COMPLETE            │
         └──────────────────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────┐
│ ✅ DEPLOYMENT COMPLETE                  │
│                                          │
│ Application live at:                     │
│ https://lms.company.com                  │
│                                          │
│ Duration: ~10 minutes (fully automated)  │
└──────────────────────────────────────────┘
```

---

## 5. Local vs Production Comparison

```
┌─────────────────────────────────────────────────────────────────┐
│                    LOCAL (Developer Machine)                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Source Code                                                │
│     └─ .java files on disk                                    │
│                                                                │
│  2. Compilation                                               │
│     └─ javac converts .java → .class files                   │
│                                                                │
│  3. Database                                                  │
│     └─ Connects to Supabase PostgreSQL (cloud)              │
│                                                                │
│  4. Execution                                                 │
│     └─ java -jar app.jar on laptop                          │
│                                                                │
│  5. Access                                                    │
│     └─ http://localhost:8081                                │
│                                                                │
│  6. Issues                                                    │
│     └─ Works on my machine! (but not on server)            │
│                                                                │
└─────────────────────────────────────────────────────────────────┘

                          DOCKER BRIDGE

┌─────────────────────────────────────────────────────────────────┐
│                   PRODUCTION (Cloud Server)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Docker Image                                              │
│     └─ Contains: Java + compiled code + dependencies        │
│                                                                │
│  2. Container Runtime                                         │
│     └─ Executes inside isolated container                   │
│                                                                │
│  3. Database                                                  │
│     └─ Same Supabase PostgreSQL (cloud)                     │
│                                                                │
│  4. Execution                                                 │
│     └─ docker run -p 8081:8081 lms-app:latest              │
│                                                                │
│  5. Access                                                    │
│     └─ https://lms.company.com (via domain)                │
│                                                                │
│  6. Reliability                                               │
│     └─ Guaranteed to work (exact same environment)         │
│                                                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. Monitoring & Alerting Flow

```
APPLICATION RUNNING IN PRODUCTION
        │
        ├─────────────────────────────┬──────────────────────────┐
        │                             │                          │
        ▼                             ▼                          ▼
   ┌─────────┐                 ┌─────────────┐          ┌──────────────┐
   │ Metrics │                 │   Logs      │          │  Errors      │
   ├─────────┤                 ├─────────────┤          ├──────────────┤
   │ CPU%    │                 │ User login  │          │ Exception X  │
   │ Memory% │                 │ Query time  │          │ Stack trace  │
   │ Latency │                 │ API calls   │          │ Timestamp    │
   │ Requests│                 │ DB queries  │          │ User affected│
   └────┬────┘                 └────┬────────┘          └───────┬──────┘
        │                           │                          │
        └───────────────────────────┼──────────────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │   Monitoring Platform          │
                    │  (CloudWatch/DataDog/Splunk)  │
                    └───────────────┬────────────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                  │
                 ▼                  ▼                  ▼
        ┌─────────────────┐ ┌─────────────┐ ┌──────────────┐
        │   Dashboard     │ │   Alerts    │ │  Thresholds  │
        ├─────────────────┤ ├─────────────┤ ├──────────────┤
        │ Visual graphs   │ │ SMS to oncal│ │ CPU > 80%    │
        │ Real-time data  │ │ Email alert │ │ Error > 10/m │
        │ Trend analysis  │ │ PagerDuty   │ │ Latency > 5s │
        └─────────────────┘ └─────────────┘ └──────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │   ON-CALL ENGINEER NOTIFIED    │
                    │   (Phone call + SMS + Email)   │
                    └───────────────┬────────────────┘
                                    │
                        ┌───────────┴───────────┐
                        │                       │
                        ▼                       ▼
              ┌──────────────────┐    ┌─────────────────┐
              │   INVESTIGATE    │    │   EXECUTE FIX   │
              ├──────────────────┤    ├─────────────────┤
              │ Check logs       │    │ Git commit      │
              │ View dashboard   │    │ Push fix        │
              │ SSH into server  │    │ Pipeline runs   │
              │ Run diagnostics  │    │ Auto-deployed   │
              └──────────────────┘    └─────────────────┘
                        │                       │
                        └───────────┬───────────┘
                                    │
                        ┌───────────▼────────────────┐
                        │   ISSUE RESOLVED          │
                        │   Alert cleared           │
                        │   Post-incident review    │
                        └───────────────────────────┘
```

---

## 7. Rollback Procedure (if needed)

```
PRODUCTION INCIDENT DETECTED
        │
        ▼
┌──────────────────────────┐
│  Alerts triggered:       │
│  - High error rate       │
│  - Slow response times   │
│  - Service unavailable   │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────────────────────┐
│  Option 1: QUICK ROLLBACK (< 1 minute)   │
│                                          │
│  Previous version: v1.2.2                │
│  Current version:  v1.2.3 (broken)       │
│                                          │
│  $ aws ecs update-service \               │
│      --service lms-prod \                 │
│      --force-new-deployment \             │
│      --desired-count 0                    │
│                                          │
│  ✓ Stop broken version                   │
│  ✓ Start previous version from registry   │
│  ✓ Health checks pass                    │
│  ✓ Traffic redirected                    │
│  ✓ Service restored!                     │
└──────────────────────────────────────────┘
           │
           ▼
    ┌─────────────────┐
    │  INCIDENT OVER  │
    │  Service up!    │
    └─────────────────┘
           │
           ▼
┌──────────────────────────────────────────┐
│  Option 2: FIX & REDEPLOY (5-10 min)     │
│                                          │
│  1. Identify root cause in logs          │
│  2. Developer fixes issue locally        │
│  3. Commits to GitHub                    │
│  4. Pipeline runs automatically          │
│  5. Tests all pass                       │
│  6. New version deployed                 │
│  7. Health checks confirm                │
│                                          │
└──────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────┐
│  POST-INCIDENT:                          │
│                                          │
│  ✓ Review what happened                  │
│  ✓ Add test case to prevent recurrence   │
│  ✓ Update monitoring thresholds          │
│  ✓ Document in runbook                   │
│  ✓ Team learns from incident             │
└──────────────────────────────────────────┘
```

---

## 8. Infrastructure Scaling

```
TRAFFIC INCREASES → AUTO-SCALING ACTIVATES

Time: 9:00 AM (Normal traffic)
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  Container 1  │  │  Container 2  │  │  Container 3  │
│  CPU: 30%     │  │  CPU: 25%     │  │  CPU: 28%     │
│  Memory: 45%  │  │  Memory: 40%  │  │  Memory: 42%  │
└───────────────┘  └───────────────┘  └───────────────┘

Time: 10:00 AM (Traffic spike - CEO posts about app)
┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐
│  Container 1  │  │  Container 2  │  │  Container 3  │  │ Container 4  │
│  CPU: 85%     │  │  CPU: 82%     │  │  CPU: 88%     │  │ CPU: 35%     │
│  Memory: 90%  │  │  Memory: 88%  │  │  Memory: 92%  │  │ Memory: 45%  │
└───────────────┘  └───────────────┘  └───────────────┘  └──────────────┘
                            ↓
           [Scaling policy triggered]
           [CPU > 80% for 2 minutes]
           [Create 2 more containers]
                            ↓
Time: 10:02 AM (Scaling complete)
┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│Container 1  │  │Container 2  │  │Container 3  │  │Container 4  │  │Container 5  │
│CPU: 52%     │  │CPU: 48%     │  │CPU: 55%     │  │CPU: 50%     │  │CPU: 45%     │
└─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
                                       ↓
                          Load balanced equally

Time: 11:00 AM (Traffic reduces - scaling down)
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│Container 1  │  │Container 2  │  │Container 3  │
│CPU: 32%     │  │CPU: 28%     │  │CPU: 30%     │
└─────────────┘  └─────────────┘  └─────────────┘
                            ↓
           [Scaling policy triggered]
           [CPU < 40% for 5 minutes]
           [Remove 2 containers]
                            ↓
                    (Back to normal)
```

---

## 9. Security Layers

```
APPLICATION SECURITY STACK
        │
        ├─ LAYER 1: Network Security
        │  ├─ Firewall rules (whitelist IPs)
        │  ├─ SSL/TLS encryption (HTTPS)
        │  ├─ DDoS protection
        │  └─ WAF (Web Application Firewall)
        │
        ├─ LAYER 2: Application Authentication
        │  ├─ JWT tokens (login required)
        │  ├─ Token expiration (24 hours)
        │  ├─ Refresh tokens (7 days)
        │  └─ Logout invalidates tokens
        │
        ├─ LAYER 3: Authorization (Access Control)
        │  ├─ Role-based access (RBAC)
        │  ├─ @PreAuthorize on endpoints
        │  ├─ Admin can delete users
        │  ├─ Manager can approve leave
        │  └─ Employee can apply leave
        │
        ├─ LAYER 4: Data Protection
        │  ├─ Passwords hashed (BCrypt)
        │  ├─ Database encryption at rest
        │  ├─ Database encryption in transit
        │  ├─ Secrets in environment (.env)
        │  └─ Never log sensitive data
        │
        └─ LAYER 5: Monitoring & Audit
           ├─ Audit logs (who did what)
           ├─ Intrusion detection
           ├─ Vulnerability scanning
           ├─ Log analysis (SIEM)
           └─ Incident response plan
```

---

## 10. Cost Breakdown (Typical Monthly)

```
LEAVE MANAGEMENT SYSTEM - ESTIMATED MONTHLY COSTS

┌─────────────────────────────────────────┐
│ 1. CLOUD INFRASTRUCTURE (AWS/Azure)     │
│                                         │
│   Compute (3 containers)    : $45/mo    │
│   Database (Supabase)       : $25/mo    │
│   Load Balancer             : $15/mo    │
│   Data Transfer             : $10/mo    │
│   ─────────────────────────            │
│   Subtotal                  : $95/mo    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 2. MONITORING & LOGGING                 │
│                                         │
│   CloudWatch/DataDog        : $20/mo    │
│   Log aggregation           : $10/mo    │
│   ─────────────────────────            │
│   Subtotal                  : $30/mo    │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 3. CI/CD & DEVOPS                       │
│                                         │
│   GitHub Actions (free)     : $0/mo     │
│   Docker Hub storage        : $0/mo     │
│   Secrets management        : $0/mo     │
│   ─────────────────────────            │
│   Subtotal                  : $0/mo     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 4. SECURITY                             │
│                                         │
│   SSL/TLS certificates      : $0/mo     │
│   WAF rules                 : $5/mo     │
│   ─────────────────────────            │
│   Subtotal                  : $5/mo     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ 5. SUPPORT & TOOLS                      │
│                                         │
│   Postman API testing       : $10/mo    │
│   Documentation storage     : $0/mo     │
│   ─────────────────────────            │
│   Subtotal                  : $10/mo    │
└─────────────────────────────────────────┘

════════════════════════════════════════════
           TOTAL MONTHLY COST: $140/mo
════════════════════════════════════════════

Cost per user (100 employees): $1.40/user/month
Cost per user (1000 employees): $0.14/user/month

✓ FREE Tier elements:
  - GitHub Actions: 2000 min/month (we use ~200)
  - Docker Hub storage: 1 private repo
  - PostgreSQL basic: Supabase free tier up to 500MB
  - SSL certificates: Let's Encrypt (free)
```

---

## Summary: What Each Team Member Does

```
┌──────────────────────────────────────────────────────────────┐
│  DEVELOPER (writes code)                                     │
├──────────────────────────────────────────────────────────────┤
│  1. Clone repo from GitHub                                  │
│  2. Create feature branch                                   │
│  3. Write code + tests                                      │
│  4. Commit changes                                          │
│  5. Push to GitHub (Pull Request)                           │
│  6. Wait for code review                                    │
│  7. Merge after approval                                    │
│  └─ Pipeline runs automatically (developer does nothing!)   │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  DEVOPS ENGINEER (manages infrastructure)                    │
├──────────────────────────────────────────────────────────────┤
│  1. Setup GitHub Actions workflow                           │
│  2. Configure cloud infrastructure                          │
│  3. Manage Docker registry                                  │
│  4. Monitor application 24/7                                │
│  5. Handle incidents/rollbacks                              │
│  6. Optimize costs & performance                            │
│  7. Backup & disaster recovery                              │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  MANAGER (project oversight)                                │
├──────────────────────────────────────────────────────────────┤
│  1. Monitor project progress via dashboards                 │
│  2. Review uptime & performance metrics                     │
│  3. Check deployment frequency                              │
│  4. Review cost analysis monthly                            │
│  5. Approve rollback decisions if needed                    │
│  6. Coordinate security audits                              │
│  7. Plan capacity for next quarter                          │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  END USER (uses application)                                │
├──────────────────────────────────────────────────────────────┤
│  1. Access via domain: lms.company.com                      │
│  2. Login with credentials                                  │
│  3. Apply/approve/manage leave                              │
│  4. Never thinks about backend infrastructure ✓             │
└──────────────────────────────────────────────────────────────┘
```

---

**These diagrams should be printed/displayed during your manager demo for maximum clarity!**
