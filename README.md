# MANHGG room manager

Full-stack personal room listing app.

## Technology

- Frontend: React + Vite + Tailwind CSS
- Backend: Java 21 + Spring Boot
- Database: MongoDB
- Image/video storage: MongoDB GridFS
- Auth: none, for personal/internal use

## Run for development

This mode auto reloads code changes:

- React changes hot reload in the browser.
- Java changes restart Spring Boot through devtools.
- MongoDB data stays in the `mongo-data` Docker volume.

```powershell
docker compose -f docker-compose.dev.yml up
```

Open `http://localhost:5173`.

If you change `pom.xml`, stop and run the same command again.

## Run production-like Docker

```powershell
docker compose up --build
```

Open:

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api/rooms`
- MongoDB: `mongodb://localhost:27017/manhgg`

## Run manually

Start MongoDB first, then:

```powershell
cd backend
mvn spring-boot:run
```

```powershell
cd frontend
npm install
npm run dev
```

For deployment, deploy `backend` as a Java service and `frontend` as a static React app. Set `VITE_API_BASE_URL` on the frontend if the API is not served from the same domain.

## Frontend source structure

```text
frontend/src
├─ App.jsx                         Main app composition
├─ main.jsx                        React entrypoint only
├─ styles.css                      Tailwind entry and base rules
├─ components
│  ├─ layout                       App-level layout components
│  └─ ui                           Shared buttons, fields, modals, empty states
├─ features
│  └─ rooms
│     ├─ components                Room screens and room-specific UI
│     ├─ hooks                     Room state/data hooks
│     ├─ utils                     Room filtering and helpers
│     └─ constants.js              Room statuses/defaults
├─ lib                             Generic helpers
└─ services                        API clients
```

When adding a new business area later, create a new folder under `features`, for example `features/customers` or `features/appointments`.
