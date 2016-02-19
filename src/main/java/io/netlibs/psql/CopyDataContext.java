package io.netlibs.psql;

/**
 * library side API passed to consumers for interacting with a COPY context.
 * 
 * @author theo
 *
 */

public interface CopyDataContext
{

  /**
   * acks processing of the log up to - and including - the log block which started at the given position.
   */

  void ack(long startingPosition);

  /**
   * request this copy context is closed.
   */

  void close();

}
