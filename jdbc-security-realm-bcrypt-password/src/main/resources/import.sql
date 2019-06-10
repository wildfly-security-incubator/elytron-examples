--
-- JBoss, Home of Professional Open Source
-- Copyright 2019, Red Hat, Inc. and/or its affiliates, and individual
-- contributors by the @authors tag. See the copyright.txt in the
-- distribution for a full listing of individual contributors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- http://www.apache.org/licenses/LICENSE-2.0
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

--Comment out the following line when using modular crypt
CREATE TABLE USERS (ID INT, USERNAME VARCHAR(20), PASSWORD VARCHAR(60), SALT VARCHAR(60), ITERATION_COUNT INT);

--Uncomment the following line when using modular crypt
--CREATE TABLE USERS (ID INT, USERNAME VARCHAR(20), PASSWORD VARCHAR(60));

CREATE TABLE ROLES (ID INT, NAME VARCHAR(20));
CREATE TABLE USERS_ROLES (USER_ID INT, ROLE_ID INT);

--Comment out the following two lines when using hex encoding or modular crypt
INSERT INTO USERS (ID, USERNAME, PASSWORD, SALT, ITERATION_COUNT) VALUES (1, 'quickstartUser', 'NxHRwFg/YkkRgGUq/D6ARnD+caxlmIg=', 'DMakCMy9DYKBVW/HNrCrPw==',10);
INSERT INTO USERS (ID, USERNAME, PASSWORD, SALT, ITERATION_COUNT) VALUES (2, 'guest', 'sspDe1PYqDdAJ5EeHoRJcXZPuM+vOi8=', 'VO0V0x+j/oZ5r7VvxdzDzw==', 10);

--Uncomment following two lines when using hex encoding
--INSERT INTO USERS (ID, USERNAME, PASSWORD, SALT, ITERATION_COUNT) VALUES (1, 'quickstartUser', '1b16d96be4b63b73d6ec7008f656effefa94ddfa5daa14', 'd75ef1b0445dde97315936f8cb62d960',10);
--INSERT INTO USERS (ID, USERNAME, PASSWORD, SALT, ITERATION_COUNT) VALUES (2, 'guest', 'c4537f43dd62c6c854a7e39378a972bcca8df71810954d', 'a1f8cff8469a6d8203905c38345fc5a3', 10);


--Uncomment following two lines to use modular crypt
--INSERT INTO USERS (ID, USERNAME, PASSWORD) VALUES (1, 'quickstartUser', '$2a$10$5P6AM77To04c0MQuW0eRnOVfBBHQz5kdefCGdErgjVCfCgrF4CO/.');
--INSERT INTO USERS (ID, USERNAME, PASSWORD) VALUES (2, 'guest', '$2a$10$ZBubpz1cCVvNGxkA/hNRz.9MHT5NmR1oh3ToLw8a9xEm8OcOwUV6C');

INSERT INTO ROLES (ID, NAME) VALUES (1, 'quickstarts');
INSERT INTO ROLES (ID, NAME) VALUES (2, 'guest');

INSERT INTO USERS_ROLES (USER_ID, ROLE_ID) VALUES (1,1);
INSERT INTO USERS_ROLES (USER_ID, ROLE_ID) VALUES (2,2);
