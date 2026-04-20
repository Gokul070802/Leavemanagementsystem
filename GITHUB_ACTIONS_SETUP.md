# GitHub Actions CI/CD Configuration

## Service Account Setup ✅
- **Service Account**: `github-actions-sa@project-0168023a-2379-46dc-989.iam.gserviceaccount.com`
- **Workload Identity Pool**: `projects/460701269805/locations/global/workloadIdentityPools/github-pool`
- **OIDC Provider**: `projects/460701269805/locations/global/workloadIdentityPools/github-pool/providers/github-provider`
- **Repository Binding**: `Gokul070802/Leavemanagementsystem`

## GitHub Actions Secrets Required

Set these in GitHub repository → **Settings → Secrets and variables → Actions**:

### GCP Authentication
```
GCP_WORKLOAD_IDENTITY_PROVIDER=projects/460701269805/locations/global/workloadIdentityPools/github-pool/providers/github-provider
GCP_SERVICE_ACCOUNT=github-actions-sa@project-0168023a-2379-46dc-989.iam.gserviceaccount.com
GCP_PROJECT_ID=project-0168023a-2379-46dc-989
```

### Database Configuration
```
DB_URL=jdbc:postgresql:///leavepal?cloudSqlInstance=project-0168023a-2379-46dc-989:asia-south1:leavepal-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory&ipTypes=PUBLIC
DB_USERNAME=leavepaldb
```

### GCP Resources
```
GCP_REGION=asia-south1
GCP_ARTIFACT_REPO=leavepal-repo
GCP_CLOUD_RUN_SERVICE=leave-management-app
```

## IAM Roles Configured

The GitHub Actions service account has been granted:

| Role | Purpose | Status |
|------|---------|--------|
| `roles/artifactregistry.writer` | Push Docker images to Artifact Registry | ✅ Configured |
| `roles/run.admin` | Deploy to Cloud Run | ✅ Configured |
| `roles/iam.serviceAccountUser` | Use service account for authentication | ✅ Configured |
| `roles/cloudsql.client` | Access Cloud SQL instances | ✅ Configured |
| `roles/iam.workloadIdentityUser` | Use Workload Identity Federation | ✅ Configured |

## How to Re-enable CI/CD

1. **Update GitHub Actions Secrets** (if not already set):
   - Go to: Repository → Settings → Secrets and variables → Actions
   - Add all secrets from the list above

2. **Re-enable the workflow**:
   - Edit `.github/workflows/ci-cd-gcp-cloud-run.yml`
   - Uncomment the `push:` trigger:
     ```yaml
     on:
       push:
         branches:
           - main
         paths:
           - Leavemanagementsystem/**
           - .github/workflows/ci-cd-gcp-cloud-run.yml
       workflow_dispatch:
     ```

3. **Commit and push**:
   - The next commit to `main` that touches `Leavemanagementsystem/` will trigger the CI/CD
   - GitHub Actions will build, test, push Docker image, and deploy to Cloud Run

## Verification

To verify the setup works:
```bash
# Check service account exists
gcloud iam service-accounts describe \
  github-actions-sa@project-0168023a-2379-46dc-989.iam.gserviceaccount.com \
  --project=project-0168023a-2379-46dc-989

# Check Workload Identity Pool
gcloud iam workload-identity-pools describe github-pool \
  --project=project-0168023a-2379-46dc-989 \
  --location=global
```

## Troubleshooting

### If deployments still fail:
1. Check GitHub Actions workflow logs in the "Actions" tab
2. Look for errors in Cloud Run revision logs
3. Verify all GitHub Actions secrets are set correctly (no typos, values match exactly)
4. Ensure `GCP_PROJECT_ID` is the project ID (not the project number)

### If images aren't being pushed:
1. Verify `GCP_ARTIFACT_REPO` exists: `leavepal-repo`
2. Verify the service account has `roles/artifactregistry.writer`
3. Check that Docker build succeeds locally

### If Cloud Run deployment fails:
1. Verify `DB_URL` and `DB_USERNAME` are correct
2. Check that Cloud SQL secrets exist: `DB_PASSWORD` and `JWT_SECRET`
3. Verify `GCP_CLOUD_RUN_SERVICE` is `leave-management-app`
