package org.walterddr.flink.pinot.common;

import org.apache.flink.types.Row;
import org.apache.pinot.spi.data.readers.GenericRow;

/**
 * Converts {@link Row} type data into {@link GenericRow} format.
 */
public class PinotRowRecordConverter implements RecordConverter<Row>  {

    @Override
    public GenericRow convertToRow(Row value) {
        return null;
    }

    @Override
    public Row convertFromRow(GenericRow row) {
        return null;
    }
}
