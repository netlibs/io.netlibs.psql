package io.netlibs.psql.replication;

import io.netlibs.psql.WalPosition;

/**
 * 
 * @author theo
 *
 */

public interface ReplicationCreatedHandle
{

  /**
   * The WAL position that continues from where the snapshot leaves off.
   */

  WalPosition consistentPoint();

  /**
   * The snapshot that identifies where the consistent point takes over.
   */

  String snapshotName();

}
