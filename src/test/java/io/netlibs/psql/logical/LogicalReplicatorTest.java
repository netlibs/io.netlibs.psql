package io.netlibs.psql.logical;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 */

public class LogicalReplicatorTest
{

  static
  {
    // jpgrepl uses JUL, but testing uses logback-classic.
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private static final Map<String, String> getPluginParams()
  {

    // parameters for the logical replication plugin
    final Map<String, String> params = new HashMap<>();

    params.put("pretty-print", "0");
    params.put("write-in-chunks", "0");
    params.put("include-timestamp", "0");
    params.put("include-schemas", "0");
    params.put("include-types", "0");
    params.put("include-xids", "1");
    params.put("include-lsn", "1");

    return params;

  }

  /**
   * In this test, we use the wal2json logical replication plugin. localhost install must have it installed too.
   */

  @Test
  public void test() throws Exception
  {

  }

}
