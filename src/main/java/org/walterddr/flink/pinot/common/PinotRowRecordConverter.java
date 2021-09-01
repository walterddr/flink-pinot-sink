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

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.apache.pinot.spi.data.readers.GenericRow;

import java.util.Map;

/**
 * Converts {@link Row} type data into {@link GenericRow} format.
 */
public class PinotRowRecordConverter implements RecordConverter<Row>  {

    private final RowTypeInfo rowTypeInfo;
    private final String[] fieldNames;
    private final TypeInformation<?>[] fieldTypes;

    public PinotRowRecordConverter(RowTypeInfo rowTypeInfo) {
        this.rowTypeInfo = rowTypeInfo;
        this.fieldNames = rowTypeInfo.getFieldNames();
        this.fieldTypes = rowTypeInfo.getFieldTypes();
    }

    @Override
    public GenericRow convertToRow(Row value) {
        GenericRow row = new GenericRow();
        for (int i=0; i<value.getArity(); i++) {
            row.putValue(fieldNames[i], value.getField(i));
        }
        return row;
    }

    @Override
    public Row convertFromRow(GenericRow row) {
        Row value = new Row(fieldNames.length);
        for (Map.Entry<String, Object> e : row.getFieldToValueMap().entrySet()) {
            value.setField(rowTypeInfo.getFieldIndex(e.getKey()), e.getValue());
        }
        return value;
    }
}
