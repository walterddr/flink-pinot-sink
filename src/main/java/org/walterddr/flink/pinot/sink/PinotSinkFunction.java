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

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.pinot.plugin.segmentuploader.SegmentUploaderDefault;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.data.Schema;
import org.apache.pinot.spi.ingestion.segment.uploader.SegmentUploader;
import org.apache.pinot.spi.ingestion.segment.writer.SegmentWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walterddr.flink.pinot.common.RecordConverter;
import org.walterddr.flink.pinot.common.PinotTableUtils;

import java.net.URI;
import java.util.Properties;


/**
 * The sink function for Pinot.
 *
 * This version of the sink function doesn't leverage {@link SegmentWriter} API's
 * ability buffer data and also share that data with checkpoint state.
 * Instead it uses an internal buffer within PinotSinkFunction for checkpoint.
 *
 * This should change once we introduce FlinkPinotSegmentWriter
 *
 * @param <T> type of record supported
 */
public class PinotSinkFunction<T> extends RichSinkFunction<T>
    implements CheckpointedFunction {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(PinotSinkFunction.class);

    private final RecordConverter<T> recordConverter;

    private TableConfig tableConfig;
    private Schema schema;

    private transient SegmentWriter _segmentWriter;
    private transient SegmentUploader _segmentUploader;

    public PinotSinkFunction(
            RecordConverter<T> recordConverter,
            TableConfig tableConfig,
            Schema schema) {
        this.recordConverter = recordConverter;
        this.tableConfig = tableConfig;
        this.schema = schema;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        _segmentWriter = new FlinkSegmentWriter(this.getRuntimeContext().getIndexOfThisSubtask());
        _segmentWriter.init(tableConfig, schema);
        _segmentUploader = new SegmentUploaderDefault();
        _segmentUploader.init(tableConfig);
    }

    @Override
    public void close() throws Exception {
        flush();
        _segmentWriter.close();
    }

    @Override
    public void invoke(T value, Context context) throws Exception {
        _segmentWriter.collect(recordConverter.convertToRow(value));
    }

    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
        // clear and flush.
        flush();
        // snapshot state
        // ...
    }

    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {
        // no initialization needed
        // ...
    }

    private void flush() throws Exception {
        URI segmentURI = _segmentWriter.flush();
        _segmentUploader.uploadSegment(segmentURI, null);
    }
}
