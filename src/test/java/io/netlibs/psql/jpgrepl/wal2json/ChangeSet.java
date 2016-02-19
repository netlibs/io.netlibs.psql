package io.netlibs.psql.jpgrepl.wal2json;

import com.fasterxml.jackson.databind.JsonNode;

public class ChangeSet
{

  public long xid;
  public Change change[];

  public static class Change
  {
    public String kind;
    public String schema;
    public String table;
    public String[] columnnames;
    public JsonNode[] columnvalues;
    public Keys oldkeys;

    public static class Keys
    {
      public String[] keynames;
      public JsonNode[] keyvalues;
    }

  }

}
