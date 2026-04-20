# Deployment Notes

## Current Status
- **Live Service**: Revision 00039-twk (asia-south1)
- **URL**: https://leave-management-app-h46z2zfjuq-el.a.run.app
- **Status**: ✅ Working & Stable
- **Auto-Deploy**: ❌ Disabled (GitHub Actions CI/CD paused)

## Issues Encountered

### Issue 1: Half-day leave not working
- **Root Cause**: `Integer` columns in database couldn't store 0.5
- **Solution**: Changed `duration` and balance fields to `NUMERIC(4,1)` type
- **Status**: ✅ Fixed & Deployed in revision 00039-twk

### Issue 2: Cloud Run deployment failures (revisions 00040+)
- **Root Cause**: GitHub Actions artifact registry or credentials issue
  - Images push to artifact registry successfully in CI/CD
  - But Cloud Run can't pull the images or image references are lost
  - Error: "Image 'asia-south1-docker.pkg.dev/.../leave-management-app:manual-39' not found"
- **Status**: ⚠️ Workaround applied - auto-deploy disabled

## How to Deploy Going Forward

### Option 1: Manual Cloud Run Deploy (Recommended)
```bash
# 1. Build locally
cd Leavemanagementsystem
mvn clean package

# 2. Create image (requires Docker installed)
docker build -t leave-management-app:latest .

# 3. Tag for Artifact Registry
docker tag leave-management-app:latest \
  asia-south1-docker.pkg.dev/project-0168023a-2379-46dc-989/leavepal-repo/leave-management-app:$(date +%s)

# 4. Push to registry
docker push asia-south1-docker.pkg.dev/project-0168023a-2379-46dc-989/leavepal-repo/leave-management-app:[TAG]

# 5. Deploy to Cloud Run
gcloud run deploy leave-management-app \
  --region=asia-south1 \
  --project=project-0168023a-2379-46dc-989 \
  --image=asia-south1-docker.pkg.dev/project-0168023a-2379-46dc-989/leavepal-repo/leave-management-app:[TAG] \
  --set-env-vars "DB_URL=jdbc:postgresql:///leavepal?cloudSqlInstance=project-0168023a-2379-46dc-989:asia-south1:leavepal-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory&ipTypes=PUBLIC" \
  --set-env-vars "DB_USERNAME=leavepaldb" \
  --set-secrets="DB_PASSWORD=DB_PASSWORD:latest" \
  --set-secrets="JWT_SECRET=JWT_SECRET:latest" \
  --allow-unauthenticated
```

### Option 2: Fix GitHub Actions CI/CD
To re-enable auto-deploy, verify these secrets exist in GitHub Actions:
1. `DB_URL` - correct Cloud SQL connection string
2. `DB_USERNAME` - `leavepaldb`
3. `GCP_WORKLOAD_IDENTITY_PROVIDER` - valid workload identity
4. `GCP_SERVICE_ACCOUNT` - valid service account with Artifact Registry push permissions
5. `GCP_PROJECT_ID` - `project-0168023a-2379-46dc-989`
6. `GCP_REGION` - `asia-south1`
7. `GCP_ARTIFACT_REPO` - `leavepal-repo`
8. `GCP_CLOUD_RUN_SERVICE` - `leave-management-app`

Then uncomment the `push:` trigger in `.github/workflows/ci-cd-gcp-cloud-run.yml` and commit.

## Database
- **Type**: PostgreSQL 15 (Cloud SQL)
- **Instance**: `project-0168023a-2379-46dc-989:asia-south1:leavepal-db`
- **Database**: `leavepal`
- **User**: `leavepaldb`
- **Columns**: ✅ All balance/duration fields are `NUMERIC(4,1)` (supports 0.5 values)

## Half-Day Leave
- ✅ Feature is **FULLY WORKING** on production
- ✅ Database supports 0.5 day values
- ✅ Backend accepts and processes 0.5 day requests
- ✅ Frontend can send half-day leave requests

## Key Commits
- `4b1956c` - Half-day leave fix (duration & balance to NUMERIC(4,1))
- `91f0573` - Database migration (ALTER TABLE commands executed)
- `5f6c0c9` - Fixed Cloud SQL instance in CI/CD
- `ab15b0f` - Disabled auto-deploy due to artifact registry issues
