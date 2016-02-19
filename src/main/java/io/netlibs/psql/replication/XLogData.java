package io.netlibs.psql.replication;

import io.netty.buffer.ByteBuf;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class XLogData implements ReplicationPacket
{
  private final long startingPoint;
  private final long currentEnd;
  // as microseconds since midnight on 2000-01-01.
  private final long txtime;  
  private final ByteBuf byteBuf;
}
