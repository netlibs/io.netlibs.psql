package io.netlibs.psql.logical.decoders.wal2json;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.JsonNode;

import io.netlibs.psql.jpgrepl.wal2json.ChangeSet;
import io.netlibs.psql.jpgrepl.wal2json.ChangeSet.Change;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTaoProcessor
{

  private final DataSource ds;

  public BaseTaoProcessor(final DataSource ds)
  {
    this.ds = ds;
  }

  public void init(final String snapshotId)
  {

    this.init();

    try (Connection conn = this.ds.getConnection())
    {
      conn.createStatement().execute("BEGIN TRANSACTION ISOLATION LEVEL REPEATABLE READ READ ONLY");
      conn.createStatement().execute(String.format("SET TRANSACTION SNAPSHOT '%s'", snapshotId));
      this.fetch(conn);
    }
    catch (final Exception ex)
    {
      ex.printStackTrace();
    }
  }


  protected abstract void init();

  protected abstract void fetch(Connection conn) throws Exception;

  /**
   * Processes a changeset (e.g, txn).
   */

  public void process(final ChangeSet set)
  {

    for (Change change : set.change)
    {
      switch (change.kind)
      {
        case "insert":
          insert(change.schema, change.table, map(change.columnnames, change.columnvalues));
          break;
        case "update":
          update(change.schema, change.table, map(change.columnnames, change.columnvalues), map(change.oldkeys.keynames, change.oldkeys.keyvalues));
          break;
        case "delete":
          delete(change.schema, change.table, map(change.oldkeys.keynames, change.oldkeys.keyvalues));
          break;
        default:
          log.warn("Unknown change type: {}", change.kind);
          break;
      }
    }

  }

  private Map<String, JsonNode> map(String[] names, JsonNode[] values)
  {
    Map<String, JsonNode> vals = new HashMap<>();
    for (int i = 0; i < names.length; ++i)
    {
      vals.put(names[i], values[i]);
    }
    return vals;
  }

  private void insert(String schema, String table, Map<String, JsonNode> values)
  {
    System.err.println("Inserted");
    switch (table)
    {
      case "objects":
        log.debug("New Object: {}, {}", values.get("id").asText(null), values.get("type"));
        break;
      case "assocs":
        log.debug("New Assoc: {} -> {} [{}] @ {}", values.get("id1"), values.get("id2"), values.get("atype").asText(null), values.get("timestamp"));
        break;
    }
  }

  private void update(String schema, String table, Map<String, JsonNode> values, Map<String, JsonNode> keys)
  {
    System.err.println("Update");
  }

  private void delete(String schema, String table, Map<String, JsonNode> key)
  {
    System.err.println("Delete");
  }

}
