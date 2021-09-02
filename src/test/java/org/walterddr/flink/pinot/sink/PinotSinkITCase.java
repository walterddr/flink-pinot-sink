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

package org.walterddr.flink.pinot.sink;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.config.table.TableType;
import org.apache.pinot.spi.config.table.ingestion.BatchIngestionConfig;
import org.apache.pinot.spi.config.table.ingestion.IngestionConfig;
import org.apache.pinot.spi.ingestion.batch.BatchConfigProperties;
import org.apache.pinot.spi.utils.JsonUtils;
import org.apache.pinot.spi.utils.builder.TableNameBuilder;
import org.apache.pinot.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.walterddr.flink.pinot.common.PinotRowRecordConverter;
import org.walterddr.flink.pinot.utils.PinotTestBase;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PinotSinkITCase extends PinotTestBase {

    @BeforeClass
    public void setUp()
            throws Exception {
        TestUtils.ensureDirectoriesExistAndEmpty(_tempDir, _segmentDir, _tarDir);

        // Start the Pinot cluster
        startZk();
        startController();
        startBroker();
        startServer();
    }

    @AfterClass
    public void tearDown()
            throws Exception {
        stopServer();
        stopBroker();
        stopController();
        stopZk();

        FileUtils.deleteDirectory(_tempDir);
    }

    @Override
    @Nullable
    protected IngestionConfig getIngestionConfig() {
        Map<String, String> batchConfigMap = new HashMap<>();
        batchConfigMap.put(BatchConfigProperties.OUTPUT_DIR_URI, _tarDir.getAbsolutePath());
        batchConfigMap.put(BatchConfigProperties.OVERWRITE_OUTPUT, "false");
        batchConfigMap.put(BatchConfigProperties.PUSH_CONTROLLER_URI, _controllerBaseApiUrl);
        return new IngestionConfig(new BatchIngestionConfig(Lists.newArrayList(batchConfigMap), "APPEND", "HOURLY"), null,
                null, null, null);
    }

    @Test
    public void testPinotSinkWrite() throws Exception {
        StreamExecutionEnvironment execEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        execEnv.setParallelism(1);
        DataStream<Row> srcDs = execEnv.fromCollection(testData).returns(testTypeInfo);

        TableConfig tableConfig = createOfflineTableConfig();
        addTableConfig(tableConfig);
        srcDs.addSink(new PinotSinkFunction<>(
                new PinotRowRecordConverter(testTypeInfo),
                tableConfig,
                schema
        ));
        execEnv.execute();
        Assert.assertEquals(getNumSegments(), 1);
        Assert.assertEquals(getNumDocsInLatestSegment(), 4);
    }

    private int getNumSegments()
            throws IOException {
        String tableNameWithType = TableNameBuilder.forType(TableType.OFFLINE).tableNameWithType(getTableName());
        String jsonOutputStr = sendGetRequest(_controllerRequestURLBuilder.
                forSegmentListAPIWithTableType(tableNameWithType, TableType.OFFLINE.toString()));
        JsonNode array = JsonUtils.stringToJsonNode(jsonOutputStr);
        return array.get(0).get("OFFLINE").size();
    }

    private int getNumDocsInLatestSegment()
            throws IOException {
        String tableNameWithType = TableNameBuilder.forType(TableType.OFFLINE).tableNameWithType(getTableName());
        String jsonOutputStr = sendGetRequest(_controllerRequestURLBuilder.
                forSegmentListAPIWithTableType(tableNameWithType, TableType.OFFLINE.toString()));
        JsonNode array = JsonUtils.stringToJsonNode(jsonOutputStr);
        JsonNode segments = array.get(0).get("OFFLINE");
        String segmentName = segments.get(segments.size() - 1).asText();

        jsonOutputStr = sendGetRequest(_controllerRequestURLBuilder.
                forSegmentMetadata(tableNameWithType, segmentName));
        JsonNode metadata = JsonUtils.stringToJsonNode(jsonOutputStr);
        return metadata.get("segment.total.docs").asInt();
    }
}
