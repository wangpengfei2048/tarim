package org.deepexi.source;

import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.CombinedScanTask;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

public class TarimCombinedScanTask implements CombinedScanTask {

    private String partitionID;
    private long scanHandle;
    private final List<FileScanTask> tasks;
    private String host;
    private int port;
    private RowType rowType;
    private String schemaJson;
    private int tableID;

    public TarimCombinedScanTask(int tableID, RowType rowType, String schemaJson, String partitionID, long scanHandle, String host, int port, List<FileScanTask>  tasks) {
        Preconditions.checkNotNull(tasks, "tasks cannot be null");
        this.tableID = tableID;
        this.partitionID = partitionID;
        this.scanHandle = scanHandle;

        this.port = port;
        this.host = host;
        this.tasks = tasks;
        this.rowType = rowType;
        this.schemaJson = schemaJson;

    }
    @Override
    public Collection<FileScanTask> files() {
        return ImmutableList.copyOf(this.tasks);
    }

    public void addTask(FileScanTask task){
        tasks.add(task);
    }

    public void setTrunk(String partitionID){
        this.partitionID = partitionID;
    }
    public void setHost(String host){
        this.host = host;
    }
    public void setPort(int port){
        this.port = port;
    }
    public void setScanHandle(long scanHandle){
        this.scanHandle = scanHandle;
    }

    public String getPartitionID(){
        return this.partitionID;
    }

    public long getScanHandle(){
        return this.scanHandle;
    }

    public String getHost(){
        return this.host;
    }

    public int getPort(){
        return this.port;
    }

    public RowType getType(){return this.rowType;}

    public String getSchemaJson(){return this.schemaJson;}

    public int getTableID(){
        return this.tableID;
    }
}
