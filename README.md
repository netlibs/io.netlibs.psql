# Netty PostgreSQL SQL/replication client

A netty PostgreSQL protocol implementation for both the replication and SQL interfaces.

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