# SkyLink - Airline Reservation System

A full-stack Java web application for airline flight booking with real-time seat selection and booking management. Built with Java 17, Spark framework, and modern web technologies.

## Features

- **Flight Management**: Search and filter flights by origin and destination
- **Real-time Seat Booking**: Interactive seat map with visual seat selection
- **Booking Management**: View bookings, cancel reservations, track passenger history
- **Thread-Safe Operations**: ConcurrentHashMap and AtomicBoolean for safe concurrent bookings
- **RESTful API**: Complete REST API with JSON responses
- **Modern UI**: Responsive, intuitive web interface

## Tech Stack

- **Backend**: Java 17, Spark Java Framework, Gson
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Build Tool**: Maven
- **Testing**: JUnit 5

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Local Development

1. **Clone the repository**

```bash
git clone <your-repo-url>
cd SkyLink
```

2. **Build the project**

```bash
mvn clean package
```

3. **Run the server**

```bash
java -jar target/sky-link-1.0-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn exec:java -Dexec.mainClass="com.skylink.SkyLinkServer"
```

4. **Access the application**

Open your browser: http://localhost:4567

API Health Check: http://localhost:4567/api/health

### Test API Endpoints

```bash
# Health check
curl http://localhost:4567/api/health

# List all flights
curl http://localhost:4567/api/flights

# Search flights
curl "http://localhost:4567/api/flights?origin=NYC&destination=LAX"

# Get specific flight
curl http://localhost:4567/api/flight/SKY123
```

## Deployment to Render (Free Tier)

### Step-by-Step Guide

1. **Push code to GitHub**

Create a GitHub repository

Push your code:

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin <your-github-repo-url>
git push -u origin main
```

2. **Deploy to Render**

Go to render.com and sign up (free)

Click "New +" → "Web Service"

Click "Connect account" and authorize GitHub

Select your repository

3. **Configure Service**

- Name: skylink (or your preferred name)
- Environment: Java
- Build Command: mvn clean package -DskipTests
- Start Command: java -jar target/sky-link-1.0-SNAPSHOT.jar
- Plan: Select Free

4. **Add Environment Variable**

Click "Environment" tab

Add variable:

- Key: PORT
- Value: 4567

5. **Deploy**

Click "Create Web Service"

Wait 5-10 minutes for the first build

Your app will be live at: https://your-app-name.onrender.com

### Render Free Tier Notes

- ✅ Free forever - No credit card required
- ⏰ Auto-sleep: App sleeps after 15 minutes of inactivity
- 🔄 Auto-wake: First request wakes the app (30-60 seconds)
- 🌐 Custom domain: Supported on free tier
- 📊 512MB RAM: Sufficient for this application

### Post-Deployment Checklist

- Test health endpoint: curl https://your-app.onrender.com/api/health
- Visit frontend: https://your-app.onrender.com
- Test flight search
- Test seat booking
- Test booking cancellation
- Share your live URL!

### Troubleshooting

**Build fails:**

Ensure pom.xml has correct main class configuration

Check Java version (should be 17+)

**App won't start:**

Verify PORT environment variable is set

Check Render logs in dashboard

**Slow first request:**

Normal on free tier - app is waking from sleep

Subsequent requests are fast

**CORS errors:**

Backend has CORS enabled by default

No additional configuration needed

## Project Structure

```
SkyLink/
├── pom.xml                 # Maven configuration
├── README.md              # This file
└── src/
    ├── main/
    │   ├── java/com/skylink/
    │   │   ├── Booking.java         # Booking entity
    │   │   ├── BookingManager.java  # Booking business logic
    │   │   ├── Flight.java          # Flight entity
    │   │   ├── FlightDTO.java       # Data transfer object
    │   │   ├── Seat.java            # Seat with atomic operations
    │   │   ├── SeatDTO.java         # Seat DTO
    │   │   └── SkyLinkServer.java   # REST API server
    │   └── resources/
    │       └── public/
    │           └── index.html       # Web UI
    └── test/
        └── java/com/skylink/
            └── *Test.java           # Unit tests
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/flights | List all flights (optional: ?origin=NYC&destination=LAX) |
| GET | /api/flight/:flightNumber | Get flight details |
| POST | /api/book | Book a seat |
| GET | /api/bookings?passengerName=John | Get bookings by passenger |
| GET | /api/booking/:bookingId | Get booking details |
| DELETE | /api/booking/:bookingId | Cancel booking |
| GET | /api/health | Health check |

## Running Tests

```bash
mvn test
```

## Architecture Highlights

- **ConcurrentHashMap**: Thread-safe flight storage
- **AtomicBoolean**: Lock-free seat booking state
- **Synchronized blocks**: Prevents race conditions
- **DTO Pattern**: Clean JSON serialization
- **RESTful Design**: Standard HTTP methods and status codes

## License

MIT License

## Contributing

This is a portfolio project. Feel free to fork, star, or use as a reference.

Built with ❤️ for showcasing full-stack Java development skills

---

**📚 Final Year Project** - Submitted as a comprehensive demonstration of full-stack Java development capabilities.
