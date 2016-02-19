package io.netlibs.psql.wire;

import lombok.Value;

@Value
public class NotificationResponse implements PostgreSQLPacket
{
  
  private int senderProcessId;
  private String name;
  private String payload;

  @Override
  public <T> T apply(PostgreSQLPacketVisitor<T> visitor)
  {
    return visitor.visitNotificationResponse(this);
  }

}
