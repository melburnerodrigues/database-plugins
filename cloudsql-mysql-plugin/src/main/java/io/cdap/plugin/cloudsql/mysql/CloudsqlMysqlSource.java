/*
 * Copyright © 2019 Cask Data, Inc.
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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.plugin.db.batch.source.AbstractDBSource;

import javax.annotation.Nullable;

/** Batch source to read from CloudSQL MySQL. */
@Plugin(type = "batchsource")
@Name(CloudsqlMysqlConstants.PLUGIN_NAME)
@Description(
    "Reads from a CloudSQL database table using a configurable SQL query."
        + " Outputs one record for each row returned by the query.")
public class CloudsqlMysqlSource extends AbstractDBSource {

  private final CloudsqlMysqlSourceConfig config;

  public CloudsqlMysqlSource(CloudsqlMysqlSourceConfig config) {
    super(config);
    this.config = config;
  }

  @Override
  protected String createConnectionString() {
    return String.format(
        CloudsqlMysqlConstants.CLOUDSQL_MYSQL_CONNECTION_STRING_FORMAT,
        config.database,
        config.instanceName,
        config.user,
        config.password);
  }

  /** CloudSQL MySQL source config. */
  public static class CloudsqlMysqlSourceConfig extends AbstractDBSource.DBSourceConfig {

    //    @Name(CloudsqlMysqlConstants.PROJECT)
    //    @Description("Google Cloud Project ID, which uniquely identifies a project. "
    //            + "It can be found on the Dashboard in the Google Cloud Platform Console.")
    //    @Macro
    //    public String project;

    @Name(CloudsqlMysqlConstants.SERVICE_ACCOUNT_FILE_PATH)
    @Description(
        "Path on the local file system of the service account key used "
            + "for authorization. Can be set to 'auto-detect' when running on a Dataproc cluster. "
            + "When running on other clusters, the file must be present on every node in the cluster.")
    @Macro
    @Nullable
    protected String serviceFilePath;

    @Name(CloudsqlMysqlConstants.INSTANCE_NAME)
    @Description("The CloudSQL instance to connect to.")
    public String instanceName;

    @Name(DATABASE)
    @Description("Database name to connect to")
    public String database;

    @Override
    public String getConnectionString() {
      return String.format(
          CloudsqlMysqlConstants.CLOUDSQL_MYSQL_CONNECTION_STRING_FORMAT,
          database,
          instanceName,
          user,
          password);
    }
  }
}
