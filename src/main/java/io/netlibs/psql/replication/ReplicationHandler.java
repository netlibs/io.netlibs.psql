package io.netlibs.psql.replication;

/**
 * The handler implemented by a consumer who wants to follow a logical replication stream.
 * 
 * @author theo
 *
 */

public interface ReplicationHandler
{

  /**
   * Called to initialise the state before playing events.
   */

  void init(ReplicationCreatedHandle event);

}
