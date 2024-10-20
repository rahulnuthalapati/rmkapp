# SheRise: Women's Finance Hub

SheRise is a full-stack web and mobile application designed to simplify the loan application process and financial management for women beneficiaries, while streamlining the workflow for bank employees and managers.  It provides a platform for secure communication, efficient loan processing, and empowerment through financial inclusion.

## Table of Contents

* [Features](#features)
* [Technologies Used](#technologies-used)
* [Architecture](#architecture)
* [Installation](#installation)
* [Usage](#usage)
* [Contributing](#contributing)
* [License](#license)

## Features

**For Beneficiaries:**

* **Easy Loan Application:**  Submit loan applications online with a user-friendly interface and upload supporting documents securely.
* **Real-time Application Tracking:** Monitor the status of loan applications and receive updates at every stage.
* **Loan Information Management:** View loan details, payment history, and upcoming dues.
* **Economic Activity Tracking:** Record business revenues, expenses, and profit margins to track performance. 
* **Income and Savings Management:** Store and update income sources, savings account details, and savings balances. 
* **Direct Communication:** Chat with bank employees and managers for quick answers and support. 
* **Feedback System:** Share feedback and suggestions to improve the platform.
* **Profile Management:**  Update personal information, change passwords, and manage account settings.

**For Bank Employees:**

* **Beneficiary Profiles Access:** View detailed profiles of beneficiaries, including financial information and loan history.
* **Loan Management Dashboard:**  Review and manage loan applications, verifying documents and updating statuses (pending, verified, approved, rejected, disbursed, completed).
* **Payment Recording:**  Easily record loan payments made by beneficiaries. 

**For Managers:**

* **Overall Loan Oversight:**  Gain a comprehensive view of all active loans and beneficiaries.
* **Loan Status Control:** Modify loan statuses, approve disbursements, and manage the loan lifecycle.
* **Feedback Monitoring:** Access and review feedback submitted by beneficiaries.

## Technologies Used

**Backend:**

* **Python (Flask):**  Provides a lightweight web framework to build RESTful APIs.
* **MySQL:** A relational database management system for secure and efficient data storage.

**Frontend:**

* **HTML, CSS, JavaScript:**  Standard web technologies used for building the user interface.
* **Jinja2:** A templating engine used with Flask for dynamic HTML generation.

**Mobile:**

* **Android Studio (Java/Kotlin):** The development environment used for building the native Android application. 

**Communication:**

* **REST API:** Enables seamless communication between the backend server, web frontend, and mobile app.

## Architecture

The application follows a client-server model, where:

* **The Flask backend** serves as the server, exposing REST APIs to handle requests from both the web and mobile clients.
* **The MySQL database** stores all the application data.
* **The web frontend** and **Android application** act as clients, interacting with the backend through API calls.

This separation of concerns allows for modularity and scalability.

## Installation

**Prerequisites:**

* Python 3.7 or higher
* MySQL server
* Android Studio

**Steps:**

1. Clone the repository: `git clone https://github.com/your-username/Empower.git`
2. Install Python dependencies: `pip install -r requirements.txt`
3. Configure the MySQL database connection in the `config.py` file.
4. Create the database tables using the SQL schema provided in `schema.sql`.
5. Run the Flask app: `flask run` 
6. Open the Android project in Android Studio and build the APK.

## Usage

**Web Application:** 

Access the web application by visiting `http://127.0.0.1:5000/` in your browser.

**Android Application:**

Install the APK on your Android device. 

## Contributing

We welcome contributions to SheRise!  Please feel free to open issues or submit pull requests.

**Guidelines:**

* Follow the project's coding conventions.
* Write clear and concise code.
* Add unit tests for new functionality.
