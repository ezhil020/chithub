CREATE DATABASE chit_fund;

USE chit_fund;

-- Table for chit groups
CREATE TABLE chit_groups (
    id VARCHAR(6) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    scheme_amount DOUBLE NOT NULL,
    duration INT NOT NULL,
    monthly_due DOUBLE NOT NULL,
    total_members INT NOT NULL,
    available_slots INT NOT NULL,
    is_auction_live BOOLEAN DEFAULT FALSE,
    auctions_conducted int default 0
);

-- Table for customers
CREATE TABLE customers (
    customer_id VARCHAR(255) PRIMARY KEY,
    customer_name VARCHAR(255),
    aadhar_no BIGINT,
    pan_no VARCHAR(10),
    address VARCHAR(255),
    phone_num VARCHAR(10),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    bank_account_no varchar(11),
    registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for customer_chit_groups (junction table)
CREATE TABLE customer_chit_groups (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(255),
    chit_group_id varchar(255),
    is_winner BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (chit_group_id) references chit_groups(id)
);

-- Table for storing auction information
CREATE TABLE auction (
    auction_no INT,
    chit_group_id VARCHAR(6),
    winner_id VARCHAR(6),
    discount_amount DECIMAL(10, 2),
    monthly_due double default 0.00,
    PRIMARY KEY (auction_no, chit_group_id),
    FOREIGN KEY (chit_group_id) REFERENCES chit_groups(id)
);

-- Table for storing bid information
CREATE TABLE bid (
    auction_no INT,
    chit_group_id VARCHAR(6),
    customer_id VARCHAR(6),
    bid_amount DECIMAL(10, 2),
    FOREIGN KEY (auction_no, chit_group_id) REFERENCES auction(auction_no, chit_group_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT PK_bid PRIMARY KEY (auction_no, chit_group_id, customer_id)
);

create table admin(
	admin_name varchar(255) primary key,
    password varchar(255),
    account_no varchar(11)
);

create table customer_account(
	account_no varchar(11),
    balance double default 50000,
    foreign key (account_no) references customers(bank_account_no)
);

create table admin_account(
	account_no varchar(11),
    balance double default 1000000,
    foreign key (account_no) references admin(account_no)
);

CREATE TABLE payment (
    customer_id VARCHAR(255),
    chit_group_id VARCHAR(255),
    auction_no INT,
    due_amount DOUBLE,
    is_due_paid BOOLEAN,
    PRIMARY KEY (customer_id, chit_group_id, auction_no),
    FOREIGN KEY (customer_id) REFERENCES customer_chit_groups(customer_id),
    FOREIGN KEY (chit_group_id) REFERENCES customer_chit_groups(chit_group_id),
    FOREIGN KEY (auction_no) REFERENCES auction(auction_no)
);