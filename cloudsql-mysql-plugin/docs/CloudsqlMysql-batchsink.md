# CloudSQL MySQL Batch Sink


Description
-----------
Writes records to a CloudSQL MySQL table. Each record will be written to a row in the table.


Use Case
--------
This sink is used whenever you need to write to a MySQL table.
Suppose you periodically build a recommendation model for products on your online store.
The model is stored in a FileSet and you want to export the contents
of the FileSet to a MySQL table where it can be served to your users.

Column names would be autodetected from input schema.

Properties
----------
**Reference Name:** Name used to uniquely identify this sink for lineage, annotating metadata, etc.

**Driver Name:** Name of the JDBC driver to use.

**Database:** MySQL database name.

**Instance Name:** The CloudSQL instance to connect to in the format <PROJECT_ID>:\<REGION>:<INSTANCE_NAME>. 
Can be found in the instance overview page.

**Table Name:** Name of the table to export to. Table must exist prior to running the pipeline.

**Username:** User identity for connecting to the specified database.

**Password:** Password to use to connect to the specified database.

**Transaction Isolation Level:** Transaction isolation level for queries run by this sink. 

**Connection Timeout:** The timeout value (in seconds) used for socket connect operations. If connecting to the server 
takes longer than this value, the connection is broken. A value of 0 means that it is disabled.

**Connection Arguments:** A list of arbitrary string key/value pairs as connection arguments. These arguments
will be passed to the JDBC driver as connection arguments for JDBC drivers that may need additional configurations.


Data Types Mapping
----------

    | MySQL Data Type                | CDAP Schema Data Type | Comment                                            |
    | ------------------------------ | --------------------- | -------------------------------------------------- |
    | BIT                            | boolean               |                                                    |
    | TINYINT                        | int                   |                                                    |
    | BOOL, BOOLEAN                  | boolean               |                                                    |
    | SMALLINT                       | int                   |                                                    |
    | MEDIUMINT                      | double                |                                                    |
    | INT,INTEGER                    | int                   |                                                    |
    | BIGINT                         | long                  |                                                    |
    | FLOAT                          | float                 |                                                    |
    | DOUBLE                         | double                |                                                    |
    | DECIMAL                        | decimal               |                                                    |
    | DATE                           | date                  |                                                    |
    | DATETIME                       | timestamp             |                                                    |
    | TIMESTAMP                      | timestamp             |                                                    |
    | TIME                           | time                  |                                                    |
    | YEAR                           | date                  |                                                    |
    | CHAR                           | string                |                                                    |
    | VARCHAR                        | string                |                                                    |
    | BINARY                         | bytes                 |                                                    |
    | VARBINARY                      | bytes                 |                                                    |
    | TINYBLOB                       | bytes                 |                                                    |
    | TINYTEXT                       | string                |                                                    |
    | BLOB                           | bytes                 |                                                    |
    | TEXT                           | string                |                                                    |
    | MEDIUMBLOB                     | bytes                 |                                                    |
    | MEDIUMTEXT                     | string                |                                                    |
    | LONGBLOB                       | bytes                 |                                                    |
    | LONGTEXT                       | string                |                                                    |
    | ENUM                           | string                |                                                    |
    | SET                            | string                |                                                    |


Example
-------
Suppose you want to write output records to "users" table of CloudSQL MySQL database named "prod", as "root" user with 
"root" password (Get the latest version of the CloudSQL socket factory jar with driver and dependencies 
[here](https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory/releases) and add the driver for MySQL. 
You can also provide driver name for some specific driver, otherwise "cloudsql-mysql" will be used), then configure plugin with: 

```
Reference Name: "sink1"
Driver Name: "cloudsql-mysql"
Instance Name: [PROJECT_ID]:[REGION_ID]:[INSTANCE_NAME]
Database: "prod"
Table Name: "users"
Username: "root"
Password: "root"
```
