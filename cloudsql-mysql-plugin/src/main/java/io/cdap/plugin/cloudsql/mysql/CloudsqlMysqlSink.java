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

import com.google.common.collect.ImmutableMap;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.etl.api.batch.BatchSink;
import io.cdap.plugin.db.batch.sink.AbstractDBSink;

import java.util.Map;
import javax.annotation.Nullable;

/** Sink support for a CloudSQL MySQL database. */
@Plugin(type = BatchSink.PLUGIN_TYPE)
@Name(CloudsqlMysqlConstants.PLUGIN_NAME)
@Description(
    "Writes records to a CloudSQL MySQL table. Each record will be written in a row in the table.")
public class CloudsqlMysqlSink extends AbstractDBSink {

  private final CloudsqlMysqlSinkConfig config;

  public CloudsqlMysqlSink(CloudsqlMysqlSinkConfig config) {
    super(config);
    this.config = config;
  }

  /** CloudSQL MySQL sink configuration. */
  public static class CloudsqlMysqlSinkConfig extends AbstractDBSink.DBSinkConfig {

    @Name(CloudsqlMysqlConstants.INSTANCE_NAME)
    @Description(
        "The CloudSQL instance to connect to in the format <PROJECT_ID>:<REGION>:<INSTANCE_NAME>."
            + "Can be found in the instance overview page.")
    public String instanceName;

    @Name(DATABASE)
    @Description("Database name to connect to")
    public String database;

    @Name(CloudsqlMysqlConstants.CONNECTION_TIMEOUT)
    @Description(
        "The timeout value used for socket connect operations. If connecting to the server takes longer"
            + " than this value, the connection is broken. "
            + "The timeout is specified in seconds and a value of zero means that it is disabled")
    @Nullable
    public Integer connectionTimeout;
    
    @Name(TRANSACTION_ISOLATION_LEVEL)
    @Description("Transaction isolation level for queries run by this sink.")
    @Nullable
    public String transactionIsolationLevel;

    @Override
    public String getConnectionString() {
      return String.format(
          CloudsqlMysqlConstants.CLOUDSQL_MYSQL_CONNECTION_STRING_FORMAT,
          database,
          instanceName,
          user,
          password);
    }
    
    @Override
    public String getTransactionIsolationLevel() {
      return transactionIsolationLevel;
    }

    @Override
    public Map<String, String> getDBSpecificArguments() {
      return ImmutableMap.of(
          CloudsqlMysqlConstants.CONNECTION_TIMEOUT, String.valueOf(connectionTimeout));
    }
  }
}
