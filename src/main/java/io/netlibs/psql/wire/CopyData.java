package io.netlibs.psql.wire;

import io.netty.buffer.ByteBuf;
import lombok.Value;

@Value
public class CopyData implements PostgreSQLPacket
{

  private byte[] data;

  public CopyData(ByteBuf buf)
  {
    this.data = new byte[buf.readableBytes()];
    buf.readBytes(this.data);
  }

  @Override
  public <T> T apply(PostgreSQLPacketVisitor<T> visitor)
  {
    return visitor.visitCopyData(this);
  }

  @Override
  public String toString()
  {
    return String.format("CopyData(%d bytes)", data.length);
  }

}
