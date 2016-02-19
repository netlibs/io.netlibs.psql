package io.netlibs.psql.wire;

public class AuthenticationUnknown implements AuthenticationPacket
{

  @Override
  public <T> T apply(PostgreSQLPacketVisitor<T> visitor)
  {
    return visitor.visitAuthenticationUnknown(this);
  }

}
