package io.netlibs.psql.sql;

import io.netlibs.psql.wire.NotificationResponse;

/**
 * Interface used for connection level async notifications.
 * 
 * @author Theo Zourzouvillys
 *
 */

public interface SqlConnectionListener
{

  /**
   * The connection is ready to be queried.
   */

  void ready();

  /**
   * A notification was received.
   */

  void notification(NotificationResponse e);

  /**
   * The server informed us of a parameter changing.
   */

  void param(String key, String value);

  /**
   * The connection was closed.  The connection is no longer usable.
   */

  void closed();

}
