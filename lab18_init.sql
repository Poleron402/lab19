DROP DATABASE IF EXISTS prescription;
CREATE DATABASE prescription;
USE prescription;
create table doctor(
                       id int primary key auto_increment,
                       ssn varchar(9) not null unique,
                       last_name varchar(30) not null,
                       first_name varchar(30) not null,
                       specialty varchar(30),
                       practice_since int );
INSERT INTO doctor (ssn, last_name, first_name, specialty, practice_since) VALUES
                                                                               ('123456789', 'Smith', 'John', 'Cardiology', 2005),
                                                                               ('987654321', 'Johnson', 'Jane', 'Pediatrics', 2010);
CREATE TABLE patient (
                         id INT PRIMARY KEY auto_increment,
                         ssn INT NOT NULL UNIQUE,
                         first_name VARCHAR(50) NOT NULL,
                         last_name VARCHAR(50) NOT NULL,
                         street VARCHAR(100) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         state VARCHAR(2) NOT NULL,
                         zip INT NOT NULL,
                         doctor_id INT,
                         FOREIGN KEY (doctor_id) REFERENCES doctor(id));
INSERT INTO patient (ssn, first_name, last_name, street, city, state, zip, doctor_id) VALUES
                                                                                          (123456789, 'John', 'Doe', '456 Oak Street', 'Cityville', 'CA', 12345, 1),
                                                                                          (987654321, 'Jane', 'Smith', '789 Pine Avenue', 'Townsville', 'NY', 67890, 2);


CREATE TABLE drug (
                      drugID INT PRIMARY KEY auto_increment,
                      name VARCHAR(50) NOT NULL);

INSERT INTO drug (name) VALUES ('Aspirin');
INSERT INTO drug (name) VALUES ('Acetaminophen');
INSERT INTO drug (name) VALUES ('Ibuprofen');
INSERT INTO drug (name) VALUES ('Lisinopril');
INSERT INTO drug (name) VALUES ('Metformin');

SELECT * FROM drug;

CREATE TABLE IF NOT EXISTS pharmacy (
                                        pharmID INT PRIMARY KEY auto_increment,
                                        name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL);

INSERT INTO pharmacy (name, address, phone) VALUES
                                                ('ABC Pharmacy', '123 Main Street, Cityville', '555-1234'),
                                                ('XYZ Pharmacy', '456 Oak Avenue, Townsville', '555-5678'),
                                                ('Eagle Pharmacy', '789 Pine Road, Villagetown', '555-9012'),
                                                ('Sunrise Pharmacy', '101 Maple Lane, Hamletville', '555-3456');


CREATE TABLE IF NOT EXISTS cost (
                                    pharmId INT NOT NULL,
                                    drugId INT NOT NULL,
                                    unit VARCHAR(100), -- like tablets, bottles, etc
    cost DECIMAL(6,2) NOT NULL,
    amount INT NOT NULL,
    PRIMARY KEY (pharmId, drugId),
    FOREIGN KEY (pharmId) REFERENCES pharmacy (pharmID),
    FOREIGN KEY (drugId) REFERENCES drug (drugID));


INSERT INTO cost (pharmId, drugId, unit, cost, amount) VALUES
                                                           (1, 1, 'tablets', 5.99, 100),
                                                           (1, 2, 'bottles', 10.50, 50),
                                                           (1, 3, 'tablets', 7.25, 75),
                                                           (1, 4, 'bottles', 15.75, 30),
                                                           (1, 5, 'tablets', 8.50, 60);

-- XYZ Pharmacy
INSERT INTO cost (pharmId, drugId, unit, cost, amount) VALUES
                                                           (2, 1, 'tablets', 6.25, 120),
                                                           (2, 2, 'bottles', 11.75, 40),
                                                           (2, 3, 'tablets', 8.00, 90),
                                                           (2, 4, 'bottles', 14.20, 25),
                                                           (2, 5, 'tablets', 9.75, 70);

-- Eagle Pharmacy
INSERT INTO cost (pharmId, drugId, unit, cost, amount) VALUES
                                                           (3, 1, 'tablets', 5.50, 80),
                                                           (3, 2, 'bottles', 10.00, 60),
                                                           (3, 3, 'tablets', 6.75, 100),
                                                           (3, 4, 'bottles', 14.50, 35),
                                                           (3, 5, 'tablets', 7.90, 50);

-- Sunrise Pharmacy
INSERT INTO cost (pharmId, drugId, unit, cost, amount) VALUES
                                                           (4, 1, 'tablets', 6.75, 150),
                                                           (4, 2, 'bottles', 12.50, 45),
                                                           (4, 3, 'tablets', 9.00, 80),
                                                           (4, 4, 'bottles', 16.00, 20),
                                                           (4, 5, 'tablets', 8.25, 65);

CREATE TABLE prescription (
                              RXID INT PRIMARY KEY auto_increment,
                              quantity INT NOT NULL,
                              number_refills INT NOT NULL,
                              doctor_id INT,
                              patient_id INT,
                              pharmID INT,
                              drugID INT,
                              FOREIGN KEY (doctor_id) REFERENCES doctor(id),
                              FOREIGN KEY (patient_id) REFERENCES patient(id),
                              FOREIGN KEY (drugID) REFERENCES drug(drugID),
                              FOREIGN KEY (pharmID) REFERENCES pharmacy(pharmID));

CREATE TABLE prescription_fill(
                                  fillId INT AUTO_INCREMENT,
                                  date DATETIME NOT NULL,
                                  price DECIMAL(6,2) NOT NULL,
                                  pharmId INT NOT NULL,
                                  RXID INT NOT NULL,
                                  PRIMARY KEY (fillId),
                                  FOREIGN KEY (pharmID) REFERENCES pharmacy (pharmID),
                                  FOREIGN KEY (RXID) REFERENCES prescription (RXID)

);
select * from doctor;
select * from prescription;