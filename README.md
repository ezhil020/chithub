# Online Chit Fund Management (Java)

## Overview

**Online Chit Fund Management** is a Java-based application designed to digitize and automate the management of chit funds—a popular rotating savings and credit association. This system provides a platform for both organizers and members to efficiently manage, participate in, and track chit fund operations online.

## What is a Chit Fund?

A chit fund is a type of group savings scheme where members contribute a fixed amount regularly, and the collected sum is given to one member of the group in each cycle (often via an auction or lottery). It is widely used in countries like India as an informal financial tool for savings and borrowing.

## Project Objective

This project aims to:
- **Automate all chit fund processes**: from member registration and monthly collection to winner selection and payment disbursal.
- **Increase transparency and trust** among members by providing clear transaction records, automated winner selection, and robust reporting.
- **Reduce paperwork and manual errors** by digitizing all records and procedures.
- **Enable remote participation** so members and organizers can access the system from anywhere.

## Key Features

- **User Management**: Registration and authentication for organizers and members.
- **Chit Fund Creation & Participation**: Organizers can create new chit funds; members can join and participate.
- **Bidding/Auction System**: Monthly or periodic auction/bidding to determine the winner for each cycle.
- **Payment Tracking**: Automated tracking of member contributions, disbursals, and outstanding payments.
- **Reporting & Transparency**: Clear, accessible records of all transactions and fund statuses for both organizers and participants.
- **Graphical User Interface (GUI)**: Built with Java Swing for an interactive desktop experience.
- **Database Integration**: Uses a relational database (such as MySQL) for persistent, secure data storage.

## Architecture

- **Database Layer**: Handles connectivity and data storage/retrieval.
- **Domain Layer**: Contains business logic and chit fund operations.
- **GUI Layer**: Manages user interaction and visual presentation.

## Who Is This For?

- Chit fund companies or informal groups seeking to digitize their fund management.
- Individual members who want transparency, trust, and convenience in participating in chit funds.

## Benefits

- Reduces manual work and errors.
- Enhances trust and transparency.
- Enables easy access and participation for all users.
- Maintains secure, organized, and easily retrievable records.

## Getting Started

1. **Clone the Repository**  
   `git clone https://github.com/ezhil020/chithub.git`

2. **Set Up the Database**  
   - Create a new database (e.g., `chitfund_db`) in MySQL or your preferred RDBMS.
   - Update database credentials in the code (likely in `DatabaseConnection.java`).

3. **Compile and Run**  
   - Compile the Java code using your IDE or command line.
   - Run the main GUI class (e.g., `MainFrame.java`).

4. **Login and Use**  
   - Register as an organizer or member.
   - Create or join chit funds, participate in auctions, and manage payments.

## Documentation

For detailed technical and functional documentation, see [`Online chit fund management.pdf`](https://github.com/ezhil020/chithub/blob/main/Online%20chit%20fund%20management.pdf).

---
