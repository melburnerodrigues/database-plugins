# CloudSQL MySQL Batch Source


Description
-----------
Reads from a CloudSQL MySQL instance using a configurable SQL query.
Outputs one record for each row returned by the query.


Use Case
--------
The source is used whenever you need to read from a CloudSQL MySQL instance. For example, you may want
to create daily snapshots of a database table by using this source and writing to
a TimePartitionedFileSet.


Properties
----------
**Reference Name:** Name used to uniquely identify this source for lineage, annotating metadata, etc.

**Driver Name:** Name of the JDBC driver to use.

**Database:** MySQL database name.

**Instance Name:** The CloudSQL instance to connect to in the format <PROJECT_ID>:\<REGION>:<INSTANCE_NAME>.
Can be found in the instance overview page.

**Import Query:** The SELECT query to use to import data from the specified table.
You can specify an arbitrary number of columns to import, or import all columns using \*. The Query should
contain the '$CONDITIONS' string. For example, 'SELECT * FROM table WHERE $CONDITIONS'.
The '$CONDITIONS' string will be replaced by 'splitBy' field limits specified by the bounding query.
The '$CONDITIONS' string is not required if numSplits is set to one.

**Bounding Query:** Bounding Query should return the min and max of the values of the 'splitBy' field.
For example, 'SELECT MIN(id),MAX(id) FROM table'. Not required if numSplits is set to one.

**Split-By Field Name:** Field Name which will be used to generate splits. Not required if numSplits is set to one.

**Number of Splits to Generate:** Number of splits to generate.

**Username:** User identity for connecting to the specified database.

**Password:** Password to use to connect to the specified database.

**Connection Arguments:** A list of arbitrary string key/value pairs as connection arguments. These arguments
will be passed to the JDBC driver as connection arguments for JDBC drivers that may need additional configurations.

**Schema:** The schema of records output by the source. This will be used in place of whatever schema comes
back from the query. However, it must match the schema that comes back from the query,
except it can mark fields as nullable and can contain a subset of the fields.


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
------
Suppose you want to read data from CloudSQL MySQL database named "prod", as "root" user with "root" password (Get the 
latest version of the CloudSQL socket factory jar with driver and dependencies 
[here](https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory/releases) and add the driver for MySQL. 
You can also provide driver name for some specific driver, otherwise "cloudsql-mysql" will be used), then configure plugin with: 


```
Reference Name: "src1"
Driver Name: "cloudsql-mysql"
Database: "prod"
Instance Name: [PROJECT_ID]:[REGION]:[INSTANCE_NAME]
Import Query: "select id, name, email, phone from users;"
Number of Splits to Generate: 1
Username: "root"
Password: "root"
```  

For example, if the 'id' column is a primary key of type int and the other columns are
non-nullable varchars, output records will have this schema:

    | field name     | type                |
    | -------------- | ------------------- |
    | id             | int                 |
    | name           | string              |
    | email          | string              |
    | phone          | string              |
