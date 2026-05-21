# MySawit
Short startup guide for a fresh clone.

## Prerequisites
- Docker Desktop with Docker Compose
- Java 21
- Node.js and npm

## 1. Clone the repository
```bash
git clone <repo-url>
cd MySawit
```

## 2. Create the backend env file
Create a `.env` file in the repository root with the database and storage values used by the backend:
```properties
DB_URL=jdbc:postgresql://localhost:5432/mysawit
DB_USERNAME=mysawit
DB_PASSWORD=penyakitsawitgila
SUPABASE_URL=...
SUPABASE_KEY=...
SUPABASE_BUCKET=...
```

## 3. Start the database and monitoring stack
```bash
docker compose up -d
```

This starts PostgreSQL, Prometheus, and Grafana.

- PostgreSQL: `localhost:5432`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001` (`admin` / `admin123`)

If Prometheus does not show backend metrics, check `prometheus/prometheus.yml`. The scrape target should point to `host.docker.internal:8080` so the container can reach the backend on your machine.

## 4. Start the backend
Open a new terminal in the `backend` directory and run:
```powershell
cd backend
.\gradlew.bat bootRun
```

The backend runs on `http://localhost:8080`.

## 5. Start the frontend
Open another terminal in the `frontend` directory and run:
```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:3000`.

## 6. Check monitoring
After the backend is running, open:
- `http://localhost:8080/actuator/prometheus`
- `http://localhost:9090`
- `http://localhost:3001`

To make Grafana show the metrics, add Prometheus as a data source:
1. Open Grafana and sign in with `admin` / `admin123`.
2. Go to `Connections` > `Data sources` > `Add data source`.
3. Select `Prometheus`.
4. Set the URL to `http://prometheus:9090`.
5. Click `Save & test`.

If you want to see a report, create or import a dashboard after the data source is saved. If it still does not show data, the backend may still be incomplete or not exposing the expected endpoint yet.

To stop everything:
```bash
docker compose down
```
