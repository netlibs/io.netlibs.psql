package io.netlibs.psql;

public interface PostgresConnection extends AutoCloseable
{

  void sync();

  <T extends QueryListener> T query(String query, T listener);

  <T extends QueryListener> T query(String query, T listener, CopyDataHandle handle);

  void close();

}
