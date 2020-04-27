# PostgreSQL Action


Description
-----------
Action that runs a PostgreSQL command on a CloudSQL PostgreSQL instance.


Use Case
--------
The action can be used whenever you want to run a PostgreSQL command before or after a data pipeline.
For example, you may want to run a SQL update command on a database before the pipeline source pulls data from tables.


Properties
----------
**Driver Name:** Name of the JDBC driver to use.

**Database Command:** Database command to execute.

**Database:** PostgreSQL database name.

**Instance Name:** The CloudSQL instance to connect to in the format <PROJECT_ID>:\<REGION>:<INSTANCE_NAME>. 
Can be found in the instance overview page.

**Username:** User identity for connecting to the specified database.

**Password:** Password to use to connect to the specified database.

**Connection Arguments:** A list of arbitrary string key/value pairs as connection arguments. These arguments
will be passed to the JDBC driver as connection arguments for JDBC drivers that may need additional configurations.

**Connection Timeout** The timeout value used for socket connect operations. If connecting to the server takes longer
than this value, the connection is broken.The timeout is specified in seconds and a value of zero means that it is 
disabled.

Example
-------
Suppose you want to execute a query against a CloudSQL PostgreSQL database named "prod", as "postgres" user with "postgres" 
password (Get the latest version of the CloudSQL socket factory jar with driver and dependencies 
[here](https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory/releases) and add the driver for PostgreSQL. 
You can also provide driver name for some specific driver, otherwise "cloudsql-postgresql" will be used), 
then configure plugin with:


```
Driver Name: "cloudsql-postgresql"
Database Command: "UPDATE table_name SET price = 20 WHERE ID = 6"
Instance Name: [PROJECT_ID]:[REGION]:[INSTANCE_NAME]
Database: "prod"
Username: "postgres"
Password: "postgres"
```
