# Step-by-Step Guide: Deploying LOS Backend on Render.com

This guide walks you through deploying the **Loan Origination System (LOS)** Spring Boot backend on **[Render.com](https://render.com)**.

---

## 🎯 Overview of Steps

1. [Create PostgreSQL Database on Render](#1-create-postgresql-database-on-render)
2. [Deploy Web Service (Spring Boot) on Render](#2-deploy-web-service-on-render)
3. [Set Environment Variables on Render](#3-set-environment-variables-on-render)
4. [Verify and Test Live Deployment](#4-verify-and-test-live-deployment)

---

## 1. Create PostgreSQL Database on Render

1. Log in to [dashboard.render.com](https://dashboard.render.com).
2. Click **New +** ➔ **PostgreSQL**.
3. Fill in the database details:
   - **Name**: `los-master-db`
   - **Database**: `los_master_db`
   - **User**: `postgres` (or default)
   - **Region**: Select closest to you (e.g. *Singapore*, *Frankfurt*, *Oregon*)
   - **Plan**: **Free**
4. Click **Create Database**.
5. Once created, note down the following connection details from the Render dashboard:
   - **Hostname**: (e.g. `dpg-xxxxxx.singapore-postgres.render.com`)
   - **Port**: `5432`
   - **Database**: `los_master_db`
   - **Username**: (e.g. `postgres` or `los_user`)
   - **Password**: (copy the generated password)
   - **Internal Database URL** (used if services are on Render in the same region).

---

## 2. Deploy Web Service on Render

1. On the Render Dashboard, click **New +** ➔ **Web Service**.
2. Connect your GitHub repository:
   - Repository URL: `https://github.com/pranavjangam500-eng/LOS-Backend`
3. Configure the Web Service:

| Setting | Value |
| :--- | :--- |
| **Name** | `los-backend` |
| **Region** | *Same region as your database* |
| **Branch** | `main` |
| **Root Directory** | *(leave empty)* |
| **Runtime** | **Docker** *(Recommended)* OR **Java** |
| **Plan Type** | **Free** |

### If using Docker (Recommended):
- Render will automatically detect the [`Dockerfile`](file:///Users/khushmeet/Project/LOS/Dockerfile) and build the Java 21 container with multi-stage compilation.

### If using Java Native Runtime:
- **Build Command**: `mvn clean package -DskipTests`
- **Start Command**: `java -jar target/los-1.0.0-SNAPSHOT.jar`

---

## 3. Set Environment Variables on Render

Scroll down to the **Environment Variables** section on Render and add the following keys:

| Key | Value / Example | Description |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Activates PostgreSQL mode (`local` for in-memory) |
| `MASTER_DB_URL` | `jdbc:postgresql://<RENDER_DB_HOST>:5432/los_master_db` | Master PostgreSQL JDBC connection |
| `MASTER_DB_USERNAME` | `<RENDER_DB_USER>` | Master Database Username |
| `MASTER_DB_PASSWORD` | `<RENDER_DB_PASSWORD>` | Master Database Password |
| `TENANT_DB_HOST` | `<RENDER_DB_HOST>` | Host for dynamic tenant databases |
| `TENANT_DB_PORT` | `5432` | PostgreSQL port |
| `TENANT_DB_USERNAME` | `<RENDER_DB_USER>` | Tenant Database Username |
| `TENANT_DB_PASSWORD` | `<RENDER_DB_PASSWORD>` | Tenant Database Password |

> [!TIP]
> **Zero-Config Fast Test**:
> If you want to deploy the web service first without setting up PostgreSQL, set `SPRING_PROFILES_ACTIVE = local`. The app will run immediately with in-memory multi-tenant databases.

---

## 4. Verify and Test Live Deployment

1. Click **Create Web Service**.
2. Render will build and deploy the container.
3. Once the deployment logs show:
   ```
   Started LosApplication in ... seconds
   ```
4. Copy your live Render URL (e.g. `https://los-backend-xxxx.onrender.com`).

### Test Endpoints:

- **Swagger UI**:  
  `https://<YOUR-RENDER-URL>.onrender.com/swagger-ui.html`

- **Health Check**:  
  `https://<YOUR-RENDER-URL>.onrender.com/api/v1/auth/health`

- **Live Login Test (cURL)**:
  ```bash
  curl -X POST https://<YOUR-RENDER-URL>.onrender.com/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "email": "superadmin@hdfcbank.com",
      "password": "Admin@123"
    }'
  ```
