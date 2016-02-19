package io.netlibs.psql;

public class PostgresConnectionBuilder extends AbstractConnectionBuilder<PostgresConnectionBuilder>
{

  PostgresConnectionListener listener;

  public PostgresConnectionBuilder listener(PostgresConnectionListener listener)
  {
    this.listener = listener;
    return this;
  }

  public DefaultPostgresConnection newConnection(String host, int port)
  {
    DefaultPostgresConnection conn = new DefaultPostgresConnection(this);
    conn.connect(host, port);
    return conn;
  }

}
