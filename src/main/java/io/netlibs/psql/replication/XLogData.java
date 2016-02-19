package io.netlibs.psql.replication;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class XLogData extends DefaultByteBufHolder implements ReplicationPacket
{

  private final long startingPoint;
  private final long currentEnd;
  // as microseconds since midnight on 2000-01-01.
  private final long txtime;

  public XLogData(long startingPoint, long currentEnd, long txtime, ByteBuf binaryData)
  {
    super(binaryData);
    this.startingPoint = startingPoint;
    this.currentEnd = currentEnd;
    this.txtime = txtime;
  }
  
}
