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

/** CloudSQL PostgreSQL constants. */
public final class CloudsqlPostgresConstants {

  public static final String PLUGIN_NAME = "CloudsqlPostgres";
  public static final String INSTANCE_NAME = "instanceName";
  public static final String CONNECTION_TIMEOUT = "connectionTimeout";
  public static final String CLOUDSQL_POSTGRES_CONNECTION_STRING_FORMAT =
      "jdbc:postgresql:///%s?cloudSqlInstance=%s&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
          + "&user=%s&password=%s";

  private CloudsqlPostgresConstants() {}
}
