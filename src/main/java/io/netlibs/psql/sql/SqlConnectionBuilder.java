package io.netlibs.psql.sql;

import io.netlibs.psql.AbstractConnectionBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class SqlConnectionBuilder extends AbstractConnectionBuilder<SqlConnectionBuilder>
{

  public SqlConnection newConnection(String host, int port)
  {
    SqlConnection conn = new SqlConnection(this);
    conn.connect(host, port);
    return conn;
  }

}
