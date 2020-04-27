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

package io.cdap.plugin.cloudsql.postgres;

import com.google.common.collect.ImmutableMap;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.batch.BatchSink;
import io.cdap.plugin.db.DBRecord;
import io.cdap.plugin.db.SchemaReader;
import io.cdap.plugin.db.batch.sink.AbstractDBSink;
import io.cdap.plugin.db.batch.sink.FieldsValidator;
import io.cdap.plugin.postgres.PostgresDBRecord;
import io.cdap.plugin.postgres.PostgresFieldsValidator;
import io.cdap.plugin.postgres.PostgresSchemaReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import javax.annotation.Nullable;

/** Sink support for a CloudSQL PostgreSQL database. */
@Plugin(type = BatchSink.PLUGIN_TYPE)
@Name(CloudsqlPostgresConstants.PLUGIN_NAME)
@Description(
    "Writes records to a CloudSQL PostgreSQL table. Each record will be written in a row in the table")
public class CloudsqlPostgresSink extends AbstractDBSink {

  private static final Character ESCAPE_CHAR = '"';

  private final CloudsqlPostgresSinkConfig cloudsqlPostgresSinkConfig;

  public CloudsqlPostgresSink(CloudsqlPostgresSinkConfig cloudsqlPostgresSinkConfig) {
    super(cloudsqlPostgresSinkConfig);
    this.cloudsqlPostgresSinkConfig = cloudsqlPostgresSinkConfig;
  }

  @Override
  protected SchemaReader getSchemaReader() {
    return new PostgresSchemaReader();
  }

  @Override
  protected DBRecord getDBRecord(StructuredRecord output) {
    return new PostgresDBRecord(output, columnTypes);
  }

  @Override
  protected void setColumnsInfo(List<Schema.Field> fields) {
    List<String> columnsList = new ArrayList<>();
    StringJoiner columnsJoiner = new StringJoiner(",");
    for (Schema.Field field : fields) {
      columnsList.add(field.getName());
      columnsJoiner.add(ESCAPE_CHAR + field.getName() + ESCAPE_CHAR);
    }

    super.columns = Collections.unmodifiableList(columnsList);
    super.dbColumns = columnsJoiner.toString();
  }

  @Override
  protected FieldsValidator getFieldsValidator() {
    return new PostgresFieldsValidator();
  }

  /** CloudSQL PostgreSQL sink config. */
  public static class CloudsqlPostgresSinkConfig extends AbstractDBSink.DBSinkConfig {

    @Name(CloudsqlPostgresConstants.INSTANCE_NAME)
    @Description(
        "The CloudSQL instance to connect to, in the format <PROJECT_ID>:<REGION>:<INSTANCE_NAME>."
            + " Can be found in the instance overview page.")
    public String instanceName;
  
    @Name(CloudsqlPostgresConstants.CONNECTION_TIMEOUT)
    @Description(
            "The timeout value used for socket connect operations. If connecting to the server takes longer"
                    + " than this value, the connection is broken. "
                    + "The timeout is specified in seconds and a value of zero means that it is disabled")
    @Nullable
    public Integer connectionTimeout;

    @Name(DATABASE)
    @Description("Database name to connect to")
    public String database;

    @Name(TRANSACTION_ISOLATION_LEVEL)
    @Description("Transaction isolation level for queries run by this sink.")
    @Nullable
    public String transactionIsolationLevel;

    @Override
    public String getTransactionIsolationLevel() {
      return transactionIsolationLevel;
    }

    @Override
    protected String getEscapedTableName() {
      return ESCAPE_CHAR + tableName + ESCAPE_CHAR;
    }

    @Override
    public Map<String, String> getDBSpecificArguments() {
      return ImmutableMap.of(
          CloudsqlPostgresConstants.CONNECTION_TIMEOUT, String.valueOf(connectionTimeout));
    }

    @Override
    public String getConnectionString() {
      return String.format(
          CloudsqlPostgresConstants.CLOUDSQL_POSTGRES_CONNECTION_STRING_FORMAT,
          database,
          instanceName,
          user,
          password);
    }
  }
}
