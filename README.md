# Netty PostgreSQL SQL/replication client

A netty PostgreSQL protocol implementation for both the replication and SQL interfaces.


PostgreSQL 9.4's introduction of logical replication is awesome - it can allow consistent snapshots followed by a stream of changes.  However, actually 
getting the data out of postgres isn't so simple.  So this library provides a simple API for directly talking postgres' replication protocol.

You can then use this client to do things such as create a replication slot, receive the snapshot name, and start following from the consistent point to. The output
of the logical events depends on the WAL plugin used (wal2json does a good job).

## Use Cases

### In-memory Graph Model / Query Seperation

The initial driver for this code was to build a neo4j graph representation of an existing (relational) database, based on the domain model.  Upon slot creation, the model is built from the snapshot (by fetching in bulk), then events are received using wal2json and the graph mutated.  The synchronizer uses neo4j's transactional support to keep the graph in sync.  If the process fails, it results from where it left off if the neo4j database is intact - otherwise it rebuilds it.   
 
### Search Indexing

Rather than trying to keep applications in sync with full-text search indexes, a snapshot is used to build the initial index in bulk, then updates applied as micro batches.
 

## Usage

    ReplicationConnection conn = new ReplicationConnectionBuilder()
        .username("theo")
        .slotId("test_slot")
        .create("wal2json")
        .database("theo")
        .newConnection("192.168.182.130", 32820);


## Logical WAL replication logic

Misc notes ...


  CREATE_REPLICATION_SLOT {slotId} LOGICAL {plugin}

      final String pointId = info.get("consistent_point");
      final String snapshotName = info.get("snapshot_name");

  BEGIN;
  SET TRANSACTION ISOLATION LEVEL REPEATABLE READ READ ONLY;
  SET TRANSACTION SNAPSHOT '{snapshot_name}'
  // do queries
  
  
  
  // once done ...
  
  START_REPLICATION consistent_point
  
  
  // verify
  plugin, database, slot_type must match.
  
  // SQL version:
  SELECT plugin, slot_type, database, active, xmin, catalog_xmin, restart_lsn FROM pg_catalog.pg_replication_slots WHERE slot_name = " + ProtoUtils.value(this.slotId)