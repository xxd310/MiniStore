CREATE TABLE tp_in_storage (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,num varchar(50) NOT NULL,is_delete tinyint(1) NOT NULL DEFAULT '0',create_time int(8) NOT NULL,create_user varchar(20) DEFAULT NULL,operate_type int(5) DEFAULT NULL,local_num varchar(50) NOT NULL,remark varchar(255) DEFAULT NULL,u_id int(8) NOT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_in_storitem (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,u_id int(8) NOT NULL,in_id int(8) NOT NULL,category_id int(8) NOT NULL,product_num varchar(100) DEFAULT NULL,product_name varchar(100) DEFAULT NULL,local_num int(8) NOT NULL,amount double(8,2) NOT NULL DEFAULT '0.00',in_price double(2,12) NOT NULL DEFAULT '0.00',token varchar(100) NOT NULL,number double(2,8) DEFAULT NULL,create_time int(8) DEFAULT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_local_product (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,local_num varchar(50) NOT NULL,category_id int(8) DEFAULT NULL,product_num varchar(50) NOT NULL,product_name varchar(200) NOT NULL,number double(8,2) NOT NULL,u_id int(8) NOT NULL,change_time int(8) NOT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_location (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,num varchar(32) NOT NULL,name varchar(64) NOT NULL,is_delete int(1) NOT NULL DEFAULT '0',create_time int(8) NOT NULL,u_id int(8) NOT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_out_storage (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,num varchar(100) DEFAULT NULL,is_delete int(1) NOT NULL DEFAULT '0',create_time int(8) NOT NULL DEFAULT '0',create_user varchar(50) DEFAULT NULL,remark varchar(400) DEFAULT NULL,operate_type int(1) NOT NULL,u_id int(8) NOT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_out_storitem (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,u_id int(8) NOT NULL,out_id int(8) NOT NULL,category_id int(8) DEFAULT '0',product_num varchar(100) NOT NULL,product_name varchar(100) NOT NULL,local_num varchar(50) NOT NULL,number double(2,8) DEFAULT NULL,out_price double(2,12) DEFAULT NULL,amount double(2,12) DEFAULT NULL,create_time int(8) DEFAULT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_product_category (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,name varchar(32) NOT NULL,is_delete int(1) NOT NULL DEFAULT '0',create_time int(8) NOT NULL,create_user varchar(30) NOT NULL DEFAULT '0',remark varchar(255) DEFAULT NULL,u_id int(8) NOT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_storage_user (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,token varchar(100) NOT NULL,name varchar(100) DEFAULT NULL,user_name varchar(100) NOT NULL);
CREATE TABLE tp_check_stock (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,local_num int(8) NOT NULL,status int(3) DEFAULT NULL,create_time int(8) DEFAULT NULL,create_user varchar(20) DEFAULT NULL,u_id int(8) NOT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_checkitem (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,check_id int(8) NOT NULL,u_id int(8) NOT NULL,product_num varchar(50) DEFAULT NULL,product_name varchar(50) DEFAULT NULL,local_number int(8) NOT NULL,actual_number int(8) NOT NULL,create_time int(8) DEFAULT NULL,status tinyint(1) NOT NULL,token varchar(100) NOT NULL,token_id varchar(150) DEFAULT NULL);
CREATE TABLE tp_authorize_record (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,authorize_time varchar(50) DEFAULT NULL,end_time varchar(50) DEFAULT NULL,validity_time varchar(50) DEFAULT NULL);