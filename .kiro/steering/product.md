# Product Overview

FinTrack is a personal financial tracking application with a client-server architecture.

## Components

- **Frontend (FinTrack-JavaFE)**: Java Swing desktop application providing a modern UI for managing personal finances
- **Backend (fintrack-backend2)**: Java socket server handling business logic, data persistence, and transaction management

## Core Features

- User authentication (register, login, password management)
- Transaction management (add, update, delete income/expense records)
- Financial summaries and reporting (balance, income, expense calculations)
- Category-based transaction organization
- Account/wallet management
- CSV report generation and export

## Architecture

Client-server model with socket-based communication:
- Frontend sends text commands to backend via socket connection
- Backend processes commands and returns structured responses
- Backend handles all data persistence to flat files
- Real-time updates broadcast to all connected clients
