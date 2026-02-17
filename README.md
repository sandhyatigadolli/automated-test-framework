## Automated Test Execution Framework

## A full-stack automated testing platform built using Spring Boot and React.

## Architecture Overview

Backend: Spring Boot  
Frontend: React + Vite  
Messaging: RabbitMQ  
Authentication: JWT  
Testing: Selenium + TestNG  

## Features

- Role-based authentication (Admin/User)
- UI test execution (Selenium)
- API testing support
- Real-time analytics dashboard
- Asynchronous execution via RabbitMQ
- Data-driven testing (CSV, JSON, Excel)

##  Tech Stack

| Backend | Frontend | Messaging | Testing |
|----------|----------|------------|----------|
| Spring Boot | React | RabbitMQ | Selenium |

##  Project Structure
root
├── src/ (Spring Boot backend)
├── Frontend/ (React app)
├── pom.xml

## How to run
- backend: mvn spring-boot:run -Dskips
- frontend: npm install
            npm run dev

## Future Improvements

- Docker containerization
- CI/CD pipeline
- Cloud deployment


