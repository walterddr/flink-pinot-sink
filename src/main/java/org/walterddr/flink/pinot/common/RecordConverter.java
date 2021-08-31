package org.walterddr.flink.pinot.common;

import org.apache.pinot.spi.data.readers.GenericRow;

/**
 * Record converter that converts sink input format into
 * Pinot {@link GenericRow}.
 *
 * @param <T> supported sink input format.
 */
public interface RecordConverter<T> {

    /**
     *
     * @param value
     * @return
     */
    GenericRow convertToRow(T value);

    /**
     *
     * @param row
     * @return
     */
    T convertFromRow(GenericRow row);
}
