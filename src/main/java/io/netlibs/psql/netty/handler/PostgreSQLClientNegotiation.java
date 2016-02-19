package io.netlibs.psql.netty.handler;

import java.util.HashMap;
import java.util.Map;

import io.netlibs.psql.wire.AuthenticationOk;
import io.netlibs.psql.wire.AuthenticationPacket;
import io.netlibs.psql.wire.BackendKeyData;
import io.netlibs.psql.wire.ParameterStatus;
import io.netlibs.psql.wire.PostgreSQLPacket;
import io.netlibs.psql.wire.ReadyForQuery;
import io.netlibs.psql.wire.StartupMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends the initial payload when the channel is opened.
 */

@Slf4j
public class PostgreSQLClientNegotiation extends SimpleChannelInboundHandler<PostgreSQLPacket>
{

  private BackendKeyData backend = null;
  private Map<String, String> params = new HashMap<>();
  private Map<String, String> properties;
  private boolean established = false;

  public PostgreSQLClientNegotiation(Map<String, String> properties)
  {
    this.properties = properties;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, PostgreSQLPacket msg) throws Exception
  {

    if (established)
    {
      ctx.fireChannelRead(msg);
      return;
    }

    if (msg instanceof ReadyForQuery)
    {
      this.established = true;
      ctx.fireUserEventTriggered(new PostgreSQLHandshakeCompleteEvent(params, backend));
      return;
    }
    else if (msg instanceof BackendKeyData)
    {
      this.backend = (BackendKeyData) msg;
    }
    else if (msg instanceof ParameterStatus)
    {
      this.params.put(((ParameterStatus) msg).getKey(), ((ParameterStatus) msg).getValue());
    }
    else if (msg instanceof AuthenticationOk)
    {
      // nothing to do ...
    }
    else if (msg instanceof AuthenticationPacket)
    {
      // err, crap.
      log.warn("authentication type {} not supported", msg);
    }
    else
    {
      log.warn("client negotiation got unexpected message {}", msg);
    }

  }

  private void initialize(ChannelHandlerContext ctx)
  {
    ctx.writeAndFlush(new StartupMessage(3, 0, this.properties));
  }

  private void destroy()
  {
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception
  {
    if (ctx.channel().isActive() && ctx.channel().isRegistered())
    {
      // channelActvie() event has been fired already, which means this.channelActive() will
      // not be invoked. We have to initialize here instead.
      initialize(ctx);
    }
    else
    {
      // channelActive() event has not been fired yet. this.channelActive() will be invoked
      // and initialization will occur there.
    }
    super.handlerAdded(ctx);
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
  {
    destroy();
    super.handlerRemoved(ctx);
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception
  {
    // Initialize early if channel is active already.
    if (ctx.channel().isActive())
    {
      initialize(ctx);
    }
    super.channelRegistered(ctx);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {
    initialize(ctx);
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    destroy();
    super.channelInactive(ctx);
  }

}
