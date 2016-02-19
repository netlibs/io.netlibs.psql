package io.netlibs.psql;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

class AbstractConnectionBuilder<T>
{

  private static final EventLoopGroup DEFAULT_EVENT_LOOP_GROUP = new NioEventLoopGroup();

  EventLoopGroup group = DEFAULT_EVENT_LOOP_GROUP;
  String username;
  String database;
  Map<String, String> params = new HashMap<>();

  public T group(EventLoopGroup group)
  {
    this.group = group;
    return (T) this;
  }

  public T username(String username)
  {
    this.username = username;
    return (T) this;
  }

  public T param(String key, String value)
  {
    this.params.put(key, value);
    return (T) this;
  }

  public T database(String database)
  {
    this.database = database;
    return (T) this;
  }

}
