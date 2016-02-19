package io.netlibs.psql;

import io.netlibs.psql.replication.XKeepAlive;
import io.netlibs.psql.replication.XLogData;

public interface CopyDataHandle
{
  
  void init(CopyDataContext ctx);

  void data(XLogData msg);

  void keepalive(XKeepAlive msg);
  
}
