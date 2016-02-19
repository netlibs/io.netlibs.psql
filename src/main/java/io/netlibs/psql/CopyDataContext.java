package io.netlibs.psql;

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
