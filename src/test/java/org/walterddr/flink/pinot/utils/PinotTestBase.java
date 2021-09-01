package org.walterddr.flink.pinot.utils;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.types.Row;
import org.apache.pinot.integration.tests.BaseClusterIntegrationTest;
import org.apache.pinot.spi.data.FieldSpec;
import org.apache.pinot.spi.data.Schema;

import java.util.ArrayList;
import java.util.List;

public class PinotTestBase extends BaseClusterIntegrationTest {

    public static final List<Row> testData = new ArrayList<>();
    public static final RowTypeInfo testTypeInfo =
            new RowTypeInfo(
                    new TypeInformation[] {Types.INT, Types.LONG, Types.STRING},
                    new String[] {"a", "b", "c"});
    public static final Schema schema = new Schema.SchemaBuilder()
            .addSingleValueDimension("a", FieldSpec.DataType.INT)
            .addSingleValueDimension("b", FieldSpec.DataType.LONG)
            .addSingleValueDimension("c", FieldSpec.DataType.STRING)
            .build();

    static {
        testData.add(Row.of(1, 1L, "Hi"));
        testData.add(Row.of(2, 2L, "Hello"));
        testData.add(Row.of(3, 2L, "Hello world"));
        testData.add(Row.of(3, 3L, "Hello world!"));
    }

}
