package com.deepexi.tarimdb.tarimkv;

import java.util.List;
import java.util.Set;
//import java.util.String;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.deepexi.rpc.TarimKVProto.*;
import com.deepexi.rpc.TarimKVProto;
import com.deepexi.rpc.TarimKVMetaGrpc;
import com.deepexi.rpc.TarimKVProto.DataDistributionRequest;
import com.deepexi.rpc.TarimKVProto.DataDistributionResponse;
import com.deepexi.rpc.TarimKVProto.DistributionInfo;
//import com.deepexi.rpc.TarimKVProto.StatusResponse;
import com.deepexi.tarimdb.util.TarimKVException;
import com.deepexi.tarimdb.util.Status;

import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

/**
 * TarimKVLocal
 *  
 *
 */
public class TarimKVLocal {

    public final static Logger logger = LogManager.getLogger(TarimKVLocal.class);

    private KVLocalMetadata lMetadata;
    private SlotManager slotManager;
    private TarimKVMetaClient metaClient;

    public TarimKVLocal(TarimKVMetaClient metaClient, KVLocalMetadata lMetadata) {
        this.metaClient = metaClient;
        this.lMetadata = lMetadata;
        logger.debug("TarimKVLocal constructor, local metadata: " + lMetadata.toString());
    }

    public void init(){
        slotManager.init(lMetadata.slots);
    }

    private void getSlot(int chunkID, Slot slot) throws TarimKVException {
        boolean refreshed = false;
        String slotID;
        do{
            slotID = metaClient.getMasterReplicaSlot(chunkID);
            KVLocalMetadata.Node node = metaClient.getReplicaNode(slotID);
            if(node.host != lMetadata.address || node.port != lMetadata.port){
                if(refreshed == true){
                    throw new TarimKVException(Status.DISTRIBUTION_ERROR);
                }
                metaClient.refreshDistribution();
                refreshed = true;
            }else{
                break;
            }
        }while(true);

        slot = slotManager.getSlot(slotID);
        if(slot == null){
            throw new TarimKVException(Status.MASTER_SLOT_NOT_FOUND);
        }
    } 

    private void validPutParam(PutRequest request) throws TarimKVException{
        if(request.getTableID() > 0
           && request.getChunkID() > 0 
           && request.getValuesCount() > 0) {
           return;
        }
        throw new TarimKVException(Status.PARAM_ERROR);
    }
    /**
     */
    public void put(PutRequest request) throws RocksDBException, TarimKVException {
        validPutParam(request);
        Slot slot = null;
        getSlot(request.getChunkID(), slot);

        String cfName = Integer.toString(request.getTableID());
        slot.createColumnFamilyIfNotExist(cfName);

        // writing key-values
        WriteOptions writeOpt = new WriteOptions();
        WriteBatch batch = new WriteBatch();
        for(TarimKVProto.KeyValue kv : request.getValuesList()){
            batch.put(KeyValueCodec.KeyEncode( new KeyValueCodec(request.getChunkID(), kv) ).getBytes()
                      , kv.getValue().getBytes());
        }
        slot.batchWrite(writeOpt, batch); // TODO: need lock
    }

    /**
     */
    public List<KeyValue> get(GetRequest request) {
        return null;
    }

    /**
     */
    public List<KeyValue> prefixSeek(PrefixSeekRequest request) {
        return null;
    }

    /**
     */
    public void delete(DeleteRequest request) {
    }

    /*------ chunk scan (only local) ------*/
     
    public KVSchema.PrepareScanInfo prepareChunkScan(String tableID, long[] chunks){
        // Note: need keeping the snapshot before complete scan (snapshot counter?).
        return null;
    }

    // ifComplete is output parameter, scan not complete until ifComplete == true.
    public List<KeyValue> deltaChunkScan(KVSchema.DeltaScanParam param, boolean ifComplete){ 
        return null;
    }

    // stop scan even ifComplete == false
    public void closeChunkScan(int snapshotID){
    }
}
