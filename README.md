# Bus-Booking-Application
A scalable, non-blocking microservices-based application that enables users to book and cancel long-distance bus reservations. The system handles real-time seat availability and booking confirmation, integrating various services such as user registration, scheduling, booking, and notification processing.

**System Design**

![image](https://github.com/user-attachments/assets/2e45d194-9df1-40e7-8cf2-26f05be52e69)

**Architecture Overview**

This application follows a microservices architecture, broken down into several services:

•	User Service: Manages user registration, authentication, and profile data.

•	Schedule Service: Handles schedules, buses and seats.

•	Booking Service: Manages seat bookings and cancellations.

•	Notification Service: Sends out booking confirmations using Kafka.


**Technology Stack**

•	Backend – Java, Spring Boot, Webflux

•	Database – PostgreSQL

•	Messaging – Apache Kafka(for notifications)

**High-Level Workflow**

1.	A user browses the buses by providing the source and destination and selects a seat.

2.	The booking service checks the seat availability with the schedule service and books a seat.

3.	Upon successful seat booking a Kafka message is sent to the notification service

4.	The user receives a confirmation email

**Database Schema**

![image](https://github.com/user-attachments/assets/6a4f1146-e27b-4069-86f2-65fafcf38d42)


**Future Improvements**

•	Implementing payment service

•	Implementing GUI
