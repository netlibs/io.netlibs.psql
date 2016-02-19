package io.netlibs.psql.wire;

import io.netlibs.psql.netty.MessageType;
import lombok.Value;

@Value
public class UnknownMessage implements PostgreSQLPacket
{
  
  private final MessageType type;

  @Override
  public <T> T apply(PostgreSQLPacketVisitor<T> visitor)
  {
    return visitor.visitUnknownMessage(this);
  }

}
