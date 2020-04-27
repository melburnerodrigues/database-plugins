/*
 * Copyright © 2020 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.cloudsql.mysql;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import io.cdap.cdap.api.artifact.ArtifactSummary;
import io.cdap.cdap.api.plugin.PluginClass;
import io.cdap.cdap.datapipeline.DataPipelineApp;
import io.cdap.cdap.proto.id.ArtifactId;
import io.cdap.cdap.proto.id.NamespaceId;
import io.cdap.cdap.test.TestConfiguration;
import io.cdap.plugin.db.ConnectionConfig;
import io.cdap.plugin.db.DBRecord;
import io.cdap.plugin.db.batch.DatabasePluginTestBase;
import io.cdap.plugin.db.batch.sink.ETLDBOutputFormat;
import io.cdap.plugin.db.batch.source.DataDrivenETLDBInputFormat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;
import javax.sql.rowset.serial.SerialBlob;

public class CloudsqlMysqlPluginTestBase extends DatabasePluginTestBase {
  @ClassRule
  public static final TestConfiguration CONFIG = new TestConfiguration("explore.enabled", false);

  protected static final ArtifactId DATAPIPELINE_ARTIFACT_ID =
      NamespaceId.DEFAULT.artifact("data-pipeline", "3.2.0");
  protected static final ArtifactSummary DATAPIPELINE_ARTIFACT =
      new ArtifactSummary("data-pipeline", "3.2.0");
  protected static final long CURRENT_TS = System.currentTimeMillis();
  protected static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
  protected static final String JDBC_DRIVER_NAME = "mysql";
  protected static final int YEAR;
  protected static final int PRECISION = 10;
  protected static final int SCALE = 6;
  protected static final ZoneId UTC_ZONE = ZoneId.ofOffset("UTC", ZoneOffset.UTC);
  protected static final Map<String, String> BASE_PROPS =
      ImmutableMap.<String, String>builder()
          .put(
              CloudsqlMysqlConstants.INSTANCE_NAME,
              System.getProperty(
                  "cloudsqlMysql.instance", ""))
          .put(ConnectionConfig.DATABASE, System.getProperty("cloudsqlMysql.database", "mydb"))
          .put(ConnectionConfig.USER, System.getProperty("cloudsqlMysql.username", "root"))
          .put(ConnectionConfig.PASSWORD, System.getProperty("cloudsqlMysql.password", ""))
          .put(ConnectionConfig.JDBC_PLUGIN_NAME, JDBC_DRIVER_NAME)
          .build();
  protected static String connectionUrl;
  protected static boolean tearDown = true;
  private static int startCount;

  static {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date(CURRENT_TS));
    YEAR = calendar.get(Calendar.YEAR);
  }

  @BeforeClass
  public static void setupTest() throws Exception {
    if (startCount++ > 0) {
      return;
    }

    setupBatchArtifacts(DATAPIPELINE_ARTIFACT_ID, DataPipelineApp.class);

    addPluginArtifact(
        NamespaceId.DEFAULT.artifact(JDBC_DRIVER_NAME, "1.0.0"),
        DATAPIPELINE_ARTIFACT_ID,
        CloudsqlMysqlSource.class,
        CloudsqlMysqlSink.class,
        DBRecord.class,
        ETLDBOutputFormat.class,
        DataDrivenETLDBInputFormat.class,
        DBRecord.class,
        CloudsqlMysqlAction.class);

    Class<?> driverClass = Class.forName(DRIVER_CLASS);

    PluginClass mysqlDriver =
        new PluginClass(
            ConnectionConfig.JDBC_PLUGIN_TYPE,
            JDBC_DRIVER_NAME,
            "mysql driver class",
            driverClass.getName(),
            null,
            Collections.emptyMap());

    addPluginArtifact(
        NamespaceId.DEFAULT.artifact("mysql-jdbc-connector", "1.0.0"),
        DATAPIPELINE_ARTIFACT_ID,
        Sets.newHashSet(mysqlDriver),
        driverClass);

    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

    connectionUrl =
        "jdbc:mysql:///"
            + BASE_PROPS.get(ConnectionConfig.DATABASE)
            + "?cloudSqlInstance="
            + BASE_PROPS.get(CloudsqlMysqlConstants.INSTANCE_NAME)
            + "&socketFactory=com.google.cloud.sql.mysql.SocketFactory&user="
            + BASE_PROPS.get(ConnectionConfig.USER)
            + "&password="
            + BASE_PROPS.get(ConnectionConfig.PASSWORD);

    Connection conn = createConnection();
    createTestTables(conn);
    prepareTestData(conn);
  }

  protected static void createTestTables(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      // create a table that the action will truncate at the end of the run
      statement.execute("CREATE TABLE dbActionTest (x int, day varchar(10))");

      statement.execute(
          "CREATE TABLE my_table"
              + "("
              + "ID INT NOT NULL, "
              + "NAME VARCHAR(40) NOT NULL, "
              + "SCORE DOUBLE, "
              + "GRADUATED BOOLEAN, "
              + "NOT_IMPORTED VARCHAR(30), "
              + "TINY TINYINT, "
              + "SMALL SMALLINT, "
              + "MEDIUMINT_COL MEDIUMINT, "
              + "BIG BIGINT, "
              + "FLOAT_COL FLOAT, "
              + "REAL_COL REAL, "
              + "NUMERIC_COL NUMERIC("
              + PRECISION
              + ","
              + SCALE
              + "), "
              + "DECIMAL_COL DECIMAL("
              + PRECISION
              + ","
              + SCALE
              + "), "
              + "BIT_COL BIT, "
              + "DATE_COL DATE, "
              + "TIME_COL TIME, "
              + "TIMESTAMP_COL TIMESTAMP(3), "
              + "DATETIME_COL DATETIME(3), "
              + "YEAR_COL YEAR, "
              + "TEXT_COL TEXT,"
              + "TINYTEXT_COL TINYTEXT,"
              + "MEDIUMTEXT_COL MEDIUMTEXT,"
              + "LONGTEXT_COL LONGTEXT,"
              + "CHAR_COL CHAR(100),"
              + "BINARY_COL BINARY(5),"
              + "VARBINARY_COL VARBINARY(20),"
              + "TINYBLOB_COL TINYBLOB, "
              + "BLOB_COL BLOB(100), "
              + "MEDIUMBLOB_COL MEDIUMBLOB, "
              + "LONGBLOB_COL LONGBLOB, "
              + "ENUM_COL ENUM('First', 'Second', 'Third'),"
              + "SET_COL SET('a', 'b', 'c', 'd')"
              + ")");

      // 'CREATE TABLE ... SELECT' is not supported in CloudSQL
      statement.execute(
          "CREATE TABLE MY_DEST_TABLE"
              + "("
              + "ID INT NOT NULL, "
              + "NAME VARCHAR(40) NOT NULL, "
              + "SCORE DOUBLE, "
              + "GRADUATED BOOLEAN, "
              + "NOT_IMPORTED VARCHAR(30), "
              + "TINY TINYINT, "
              + "SMALL SMALLINT, "
              + "MEDIUMINT_COL MEDIUMINT, "
              + "BIG BIGINT, "
              + "FLOAT_COL FLOAT, "
              + "REAL_COL REAL, "
              + "NUMERIC_COL NUMERIC("
              + PRECISION
              + ","
              + SCALE
              + "), "
              + "DECIMAL_COL DECIMAL("
              + PRECISION
              + ","
              + SCALE
              + "), "
              + "BIT_COL BIT, "
              + "DATE_COL DATE, "
              + "TIME_COL TIME, "
              + "TIMESTAMP_COL TIMESTAMP(3), "
              + "DATETIME_COL DATETIME(3), "
              + "YEAR_COL YEAR, "
              + "TEXT_COL TEXT,"
              + "TINYTEXT_COL TINYTEXT,"
              + "MEDIUMTEXT_COL MEDIUMTEXT,"
              + "LONGTEXT_COL LONGTEXT,"
              + "CHAR_COL CHAR(100),"
              + "BINARY_COL BINARY(5),"
              + "VARBINARY_COL VARBINARY(20),"
              + "TINYBLOB_COL TINYBLOB, "
              + "BLOB_COL BLOB(100), "
              + "MEDIUMBLOB_COL MEDIUMBLOB, "
              + "LONGBLOB_COL LONGBLOB, "
              + "ENUM_COL ENUM('First', 'Second', 'Third'),"
              + "SET_COL SET('a', 'b', 'c', 'd')"
              + ")");

      statement.execute(
          "CREATE TABLE your_table"
              + "("
              + "ID INT NOT NULL, "
              + "NAME VARCHAR(40) NOT NULL, "
              + "SCORE DOUBLE, "
              + "GRADUATED BOOLEAN, "
              + "NOT_IMPORTED VARCHAR(30), "
              + "TINY TINYINT, "
              + "SMALL SMALLINT, "
              + "MEDIUMINT_COL MEDIUMINT, "
              + "BIG BIGINT, "
              + "FLOAT_COL FLOAT, "
              + "REAL_COL REAL, "
              + "NUMERIC_COL NUMERIC("
              + PRECISION
              + ","
              + SCALE
              + "), "
              + "DECIMAL_COL DECIMAL("
              + PRECISION
              + ","
              + SCALE
              + "), "
              + "BIT_COL BIT, "
              + "DATE_COL DATE, "
              + "TIME_COL TIME, "
              + "TIMESTAMP_COL TIMESTAMP(3), "
              + "DATETIME_COL DATETIME(3), "
              + "YEAR_COL YEAR, "
              + "TEXT_COL TEXT,"
              + "TINYTEXT_COL TINYTEXT,"
              + "MEDIUMTEXT_COL MEDIUMTEXT,"
              + "LONGTEXT_COL LONGTEXT,"
              + "CHAR_COL CHAR(100),"
              + "BINARY_COL BINARY(5),"
              + "VARBINARY_COL VARBINARY(20),"
              + "TINYBLOB_COL TINYBLOB, "
              + "BLOB_COL BLOB(100), "
              + "MEDIUMBLOB_COL MEDIUMBLOB, "
              + "LONGBLOB_COL LONGBLOB, "
              + "ENUM_COL ENUM('First', 'Second', 'Third'),"
              + "SET_COL SET('a', 'b', 'c', 'd')"
              + ")");
    }
  }

  protected static void prepareTestData(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement();
        PreparedStatement preparedStatement1 =
            connection.prepareStatement(
                "INSERT INTO my_table "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                    + "       ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                    + "       ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        PreparedStatement preparedStatement2 =
            connection.prepareStatement(
                "INSERT INTO your_table "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                    + "       ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                    + "       ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

      statement.execute("insert into dbActionTest values (1, '1970-01-01')");
      populateData(preparedStatement1, preparedStatement2);
    }
  }

  private static void populateData(PreparedStatement... statements) throws SQLException {
    // Insert the same data into both tables: my_table and your_table
    for (PreparedStatement preparedStatement : statements) {
      for (int i = 1; i <= 5; i++) {
        String name = "user" + i;
        preparedStatement.setInt(1, i);
        preparedStatement.setString(2, name);
        preparedStatement.setDouble(3, 123.45 + i);
        preparedStatement.setBoolean(4, (i % 2 == 0));
        preparedStatement.setString(5, "random" + i);
        preparedStatement.setShort(6, (short) i);
        preparedStatement.setShort(7, (short) i);
        preparedStatement.setInt(8, (short) i);
        preparedStatement.setLong(9, (long) i);
        preparedStatement.setFloat(10, (float) 123.45 + i);
        preparedStatement.setFloat(11, (float) 123.45 + i);
        preparedStatement.setBigDecimal(12, new BigDecimal(123.45).add(new BigDecimal(i)));
        if ((i % 2 == 0)) {
          preparedStatement.setNull(13, Types.DECIMAL);
        } else {
          preparedStatement.setBigDecimal(13, new BigDecimal(123.45).add(new BigDecimal(i)));
        }
        preparedStatement.setBoolean(14, (i % 2 == 1));
        preparedStatement.setDate(15, new Date(CURRENT_TS));
        preparedStatement.setTime(16, new Time(CURRENT_TS));
        preparedStatement.setTimestamp(17, new Timestamp(CURRENT_TS));
        preparedStatement.setTimestamp(18, new Timestamp(CURRENT_TS));
        preparedStatement.setShort(19, (short) YEAR);
        preparedStatement.setString(20, name);
        preparedStatement.setString(21, name);
        preparedStatement.setString(22, name);
        preparedStatement.setString(23, name);
        preparedStatement.setString(24, "char" + i);
        preparedStatement.setBytes(25, name.getBytes(Charsets.UTF_8));
        preparedStatement.setBytes(26, name.getBytes(Charsets.UTF_8));
        preparedStatement.setBlob(27, new SerialBlob(name.getBytes(Charsets.UTF_8)));
        preparedStatement.setBlob(28, new SerialBlob(name.getBytes(Charsets.UTF_8)));
        preparedStatement.setBlob(29, new SerialBlob(name.getBytes(Charsets.UTF_8)));
        preparedStatement.setBlob(30, new SerialBlob(name.getBytes(Charsets.UTF_8)));
        preparedStatement.setString(31, "Second");
        preparedStatement.setString(32, "a,b");
        preparedStatement.executeUpdate();
      }
    }
  }

  public static Connection createConnection() {
    try {
      Class.forName(DRIVER_CLASS);
      return DriverManager.getConnection(
          connectionUrl,
          BASE_PROPS.get(ConnectionConfig.USER),
          BASE_PROPS.get(ConnectionConfig.PASSWORD));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterClass
  public static void tearDownDB() throws SQLException {
    if (!tearDown) {
      return;
    }

    try (Connection connection = createConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("DROP TABLE my_table");
      statement.execute("DROP TABLE your_table");
      statement.execute("DROP TABLE MY_DEST_TABLE");
      statement.execute("DROP TABLE dbActionTest");
    }
  }
}
