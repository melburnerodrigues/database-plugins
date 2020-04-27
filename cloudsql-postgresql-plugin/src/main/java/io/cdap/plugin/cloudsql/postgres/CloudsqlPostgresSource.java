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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.etl.api.batch.BatchSource;
import io.cdap.plugin.db.SchemaReader;
import io.cdap.plugin.db.batch.source.AbstractDBSource;
import io.cdap.plugin.postgres.PostgresDBRecord;
import io.cdap.plugin.postgres.PostgresSchemaReader;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;

/** Batch source to read from a CloudSQL PostgreSQL instance database. */
@Plugin(type = BatchSource.PLUGIN_TYPE)
@Name(CloudsqlPostgresConstants.PLUGIN_NAME)
@Description(
    "Reads from a CloudSQL PostgreSQL database table(s) using a configurable SQL query."
        + " Outputs one record for each row returned by the query.")
public class CloudsqlPostgresSource extends AbstractDBSource {

  private final CloudsqlPostgresSourceConfig cloudsqlPostgresSourceConfig;

  public CloudsqlPostgresSource(CloudsqlPostgresSourceConfig cloudsqlPostgresSourceConfig) {
    super(cloudsqlPostgresSourceConfig);
    this.cloudsqlPostgresSourceConfig = cloudsqlPostgresSourceConfig;
  }

  @Override
  protected SchemaReader getSchemaReader() {
    return new PostgresSchemaReader();
  }

  @Override
  protected Class<? extends DBWritable> getDBRecordType() {
    return PostgresDBRecord.class;
  }

  @Override
  protected String createConnectionString() {
    return String.format(
        CloudsqlPostgresConstants.CLOUDSQL_POSTGRES_CONNECTION_STRING_FORMAT,
        cloudsqlPostgresSourceConfig.database,
        cloudsqlPostgresSourceConfig.instanceName,
        cloudsqlPostgresSourceConfig.user,
        cloudsqlPostgresSourceConfig.password);
  }

  /** CloudSQL PostgreSQL source config. */
  public static class CloudsqlPostgresSourceConfig extends AbstractDBSource.DBSourceConfig {

    @Name(CloudsqlPostgresConstants.INSTANCE_NAME)
    @Description(
        "The CloudSQL instance to connect to, in the format <PROJECT_ID>:<REGION>:<INSTANCE_NAME>."
            + " Can be found in the instance overview page.")
    public String instanceName;

    @Name(DATABASE)
    @Description("Database name to connect to")
    public String database;

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
