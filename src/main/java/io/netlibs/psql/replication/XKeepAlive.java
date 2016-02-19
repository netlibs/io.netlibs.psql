package io.netlibs.psql.replication;

import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class XKeepAlive implements ReplicationPacket
{
  private final long currentEnd;
  // micros since 2000-01-01
  private final long serverTimeMicros;
  private final boolean reply;
}
