package io.netlibs.psql;

import io.netlibs.psql.replication.XKeepAlive;
import io.netlibs.psql.replication.XLogData;

/**
 * Handle implemented by consumers to handle a COPY context.
 * 
 * @author theo
 *
 */

public interface CopyDataHandle
{

  void init(CopyDataContext ctx);

  void data(XLogData msg);

  void keepalive(XKeepAlive msg);

}
