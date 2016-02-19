package io.netlibs.psql.sql;

import io.netlibs.psql.AbstractConnectionBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class SqlConnectionBuilder extends AbstractConnectionBuilder<SqlConnectionBuilder>
{

  SqlConnectionListener listener;

  public SqlConnectionBuilder listener(SqlConnectionListener listener)
  {
    this.listener = listener;
    return this;
  }

  public SqlConnection newConnection(String host, int port)
  {
    SqlConnection conn = new SqlConnection(this);
    conn.connect(host, port);
    return conn;
  }

}
