package io.netlibs.psql.wire;

/**
 * The interface used by all packets.
 */

public interface PostgreSQLPacket
{

  <T> T apply(PostgreSQLPacketVisitor<T> visitor);
  
}
