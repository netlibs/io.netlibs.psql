package io.netlibs.psql.wire;

import lombok.Value;

@Value
public class EmptyQueryResponse implements PostgreSQLPacket
{
  
  @Override
  public <T> T apply(PostgreSQLPacketVisitor<T> visitor)
  {
    return visitor.visitEmptyQueryResponse(this);
  }

}
