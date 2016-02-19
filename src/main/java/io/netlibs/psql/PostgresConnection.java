package io.netlibs.psql;

/**
 * The primary interface for interacting with a PostgreSQL connection asynchronously.
 * 
 * @author Theo Zourzouvillys
 *
 */

public interface PostgresConnection extends AutoCloseable
{

  /**
   * Send a query to the server, and provide the response messages directly to the given listener.
   * 
   * @param query
   *          The query to send to the server.
   * 
   * @param listener
   *          The listener to receive the events.
   * 
   * @return
   */

  <T extends QueryListener> T query(String query, T listener);

  /**
   * Send a query to the server, and provide the response messages directly to the given listener.
   * 
   * COPY data is handed to the CopyDataHandle.
   * 
   * @param query
   * @param listener
   * @param handle
   * @return
   */

  <T extends QueryListener> T query(String query, T listener, CopyDataHandle handle);

  /**
   * blocks the calling thread until the database connection is established or failed.
   * 
   * note: this defeats the point of async i/o. only use for tests/debugging.
   * 
   */

  void sync();

  /**
   * terminates the underlying connection to the database. any active outstanding work will be silently aborted, and any attempt to use this
   * connection will fail.
   */

  void close();

  /**
   * Provide a builder instance.
   */

  static PostgresConnectionBuilder newBuilder()
  {
    return new PostgresConnectionBuilder();
  }

}
