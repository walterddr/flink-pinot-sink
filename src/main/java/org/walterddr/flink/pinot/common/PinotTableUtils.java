/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.walterddr.flink.pinot.common;

import org.apache.flink.configuration.Configuration;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.config.table.TableType;
import org.apache.pinot.spi.config.table.ingestion.IngestionConfig;
import org.apache.pinot.spi.data.Schema;
import org.apache.pinot.spi.utils.builder.TableConfigBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.apache.pinot.spi.config.table.TableConfig.*;

public final class PinotTableUtils {

    public static final String SCHEMA_FILE_KEY = "schemaFile";

    /**
     * construct table config based on properties and configuration parameters of
     * the {@link org.walterddr.flink.pinot.sink.PinotSinkFunction}.
     *
     * @param props
     * @param parameters
     * @return
     */
    public static TableConfig constructTableConfig(Properties props, Configuration parameters) {
        return new TableConfigBuilder(TableType.valueOf(props.getProperty(TABLE_TYPE_KEY)))
                .setTableName(props.getProperty(TABLE_NAME_KEY))
                .setIngestionConfig((IngestionConfig)props.get(INGESTION_CONFIG_KEY))
                .build();
    }

    /**
     * Construct table schema based on properties and configuration parameters of
     * the {@link org.walterddr.flink.pinot.sink.PinotSinkFunction}.
     *
     * @param props
     * @param parameters
     * @return
     */
    public static Schema constructSchema(Properties props, Configuration parameters) throws IOException {
        if (props.containsKey(SCHEMA_FILE_KEY)) {
            File schemaFile = new File(props.getProperty(SCHEMA_FILE_KEY));
            return Schema.fromFile(schemaFile);
        } else {
            Schema.SchemaBuilder schemaBuilder = new Schema.SchemaBuilder();
            return schemaBuilder.build();
        }
    }
}
