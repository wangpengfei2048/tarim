package org.deepexi.source;

import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.DataStreamScanProvider;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.abilities.SupportsFilterPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsLimitPushDown;
import org.apache.flink.table.connector.source.abilities.SupportsProjectionPushDown;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.expressions.ResolvedExpression;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;

public class TarimTableSource implements ScanTableSource, SupportsProjectionPushDown, SupportsFilterPushDown, SupportsLimitPushDown {
    private final int[] projectedFields;
    private final long limit;
    private final List<Expression> filters;

    private final TableLoader tableLoader;
    private final TableSchema schema;
    private final Map<String, String> properties;
    private final boolean isLimitPushDown;
    private final ReadableConfig readableConfig;

    private TarimTableSource(TarimTableSource toCopy) {
        this.tableLoader = toCopy.tableLoader;
        this.schema = toCopy.schema;
        this.properties = toCopy.properties;
        this.projectedFields = toCopy.projectedFields;
        this.isLimitPushDown = toCopy.isLimitPushDown;
        this.limit = toCopy.limit;
        this.filters = toCopy.filters;
        this.readableConfig = toCopy.readableConfig;
    }

    public TarimTableSource(TableLoader tableLoader, TableSchema schema, Map<String, String> properties,
                            ReadableConfig readableConfig) {
        this(tableLoader, schema, properties, null, false, -1, ImmutableList.of(), readableConfig);
    }

    private TarimTableSource(TableLoader tableLoader, TableSchema schema, Map<String, String> properties,
                               int[] projectedFields, boolean isLimitPushDown,
                               long limit, List<Expression> filters, ReadableConfig readableConfig) {
        this.tableLoader = tableLoader;
        this.schema = schema;
        this.properties = properties;
        this.projectedFields = projectedFields;
        this.isLimitPushDown = isLimitPushDown;
        this.limit = limit;
        this.filters = filters;
        this.readableConfig = readableConfig;
    }

    private DataStream<RowData> createDataStream(StreamExecutionEnvironment execEnv) {
        return TarimSource.forRowData()
                .env(execEnv)
                .tableLoader(tableLoader)
                .properties(properties)
                //.project(getProjectedSchema())
               //.limit(limit)
               // .filters(filters)
               // .flinkConf(readableConfig)
                .build();
    }

    @Override
    public ChangelogMode getChangelogMode() {
        return ChangelogMode.all();
    }

    @Override
    public ScanTableSource.ScanRuntimeProvider getScanRuntimeProvider(ScanTableSource.ScanContext runtimeProviderContext) {
        return new DataStreamScanProvider() {
            @Override
            public DataStream<RowData> produceDataStream(StreamExecutionEnvironment execEnv) {
                return createDataStream(execEnv);
            }

            @Override
            public boolean isBounded() {
                return TarimSource.isBounded(properties);
            }
        };
    }

    @Override
    public DynamicTableSource copy() {
        return null;
    }

    @Override
    public String asSummaryString() {
        return null;
    }

    @Override
    public Result applyFilters(List<ResolvedExpression> list) {
        return null;
    }

    @Override
    public void applyLimit(long l) {

    }

    @Override
    public boolean supportsNestedProjection() {
        return false;
    }

    @Override
    public void applyProjection(int[][] ints) {

    }

}
