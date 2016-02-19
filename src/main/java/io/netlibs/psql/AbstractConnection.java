package io.netlibs.psql;

import java.util.HashMap;

import io.netlibs.psql.wire.Query;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class shared between the replication and SQL connections.
 * 
 * @author theo
 *
 */

@Slf4j
public abstract class AbstractConnection implements AutoCloseable
{
  
  private static final String DEFAULT_USERNAME = System.getProperty("user.name", "postgres");

  // the handshake promise. dispatched once the session is established.
  protected Promise<Channel> handshakePromise = GlobalEventExecutor.INSTANCE.newPromise();

  // any current query listener.
  protected QueryListener listener;

  protected EventLoopGroup group;
  protected ChannelFuture connectFuture;
  protected HashMap<String, String> params = new HashMap<>();

  public AbstractConnection(AbstractConnectionBuilder<?> b)
  {

    this.group = b.group;

    this.params.putAll(b.params);
    
    if (b.username == null)
    {
      params.put("user", DEFAULT_USERNAME);
    }
    else
    {
      params.put("user", b.username);
    }

    if (b.database == null)
    {
      params.put("database", params.get("user"));
    }
    else
    {
      params.put("database", b.database);
    }

    params.put("client_encoding", "UTF-8");
    
  }

  
  /**
   * Sends an SQL query on this connection, and streams the result to the given handler.
   */

  public <T extends QueryListener> T query(String query, T listener)
  {

    if (!this.handshakePromise.isSuccess())
    {
      throw new IllegalStateException();
    }
    if (this.listener != null)
    {
      throw new IllegalStateException("listener already active");
    }

    this.listener = listener;

    handshakePromise.getNow().writeAndFlush(new Query(query));

    return listener;
    
  }

  /**
   * blocks the caller until the connection is ready (or failed).
   */

  public void sync()
  {
    try
    {
      this.handshakePromise.sync();
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void close()
  {

    log.debug("Closing connection");

    if (this.connectFuture != null)
    {
      this.connectFuture.cancel(true);
      this.connectFuture = null;
    }

    if (this.handshakePromise.isSuccess())
    {
      this.handshakePromise.getNow().close();
      this.handshakePromise = null;
    }
  }

}
