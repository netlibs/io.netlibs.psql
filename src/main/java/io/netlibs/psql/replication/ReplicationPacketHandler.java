package io.netlibs.psql.replication;

import java.util.concurrent.TimeUnit;

import io.netlibs.psql.CopyDataContext;
import io.netlibs.psql.CopyDataHandle;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * This netty channel handler is added into the pipeline by the {@link ReplicationConnection} while we're in START_REPLICATION mode.
 * 
 * @author theo
 *
 */

@Slf4j
public final class ReplicationPacketHandler extends SimpleChannelInboundHandler<ReplicationPacket> implements CopyDataContext
{

  private long outputWrittenLsn;
  private ScheduledFuture<?> future;
  private CopyDataHandle handle;
  private long head = 0;
  private long acked = 0;

  public ReplicationPacketHandler(CopyDataHandle handle)
  {
    this.handle = handle;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ReplicationPacket msg) throws Exception
  {

    if (msg instanceof XLogData)
    {
      XLogData xlog = (XLogData) msg;

      if (acked == 0)
      {
        acked = xlog.getStartingPoint();
      }

      head = xlog.getStartingPoint();
      handle.data(xlog);
      log.debug("{} bytes buffered", head - acked);
    }
    else if (msg instanceof XKeepAlive)
    {

      XKeepAlive keepalive = (XKeepAlive) msg;

      this.outputWrittenLsn = keepalive.getCurrentEnd();

      if (keepalive.isReply())
      {
        this.sendKeepalive(ctx);
      }

      handle.keepalive((XKeepAlive) msg);

    }
    else
    {
      System.err.println(msg);
    }

  }

  /**
   * While in CopyBoth mode, we need to send periodic keepalives.
   */

  private void sendKeepalive(ChannelHandlerContext ctx)
  {
    final long position = this.outputWrittenLsn;
    log.debug("Sending Keepalive position={}", Long.toHexString(position));
    ctx.writeAndFlush(new XKeepAlive(this.outputWrittenLsn, System.currentTimeMillis() * 1000, false));
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception
  {
    if (ctx.channel().isActive() && ctx.channel().isRegistered())
    {
      // channelActvie() event has been fired already, which means this.channelActive() will
      // not be invoked. We have to initialize here instead.
      this.handle.init(this);
    }
    else
    {
      // channelActive() event has not been fired yet. this.channelActive() will be invoked
      // and initialization will occur there.
    }
    super.handlerAdded(ctx);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {
    this.handle.init(this);
    this.future = ctx.executor().scheduleWithFixedDelay(() -> this.sendKeepalive(ctx), 5, 5, TimeUnit.SECONDS);
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    this.future.cancel(true);
    super.channelInactive(ctx);
  }

  @Override
  public void ack(long startingPosition)
  {
    this.acked = startingPosition;
    log.debug("ACKing {} ({} buffered)", startingPosition, head - acked);
  }

  @Override
  public void close()
  {
    log.debug("client requested closing of COPY session");
  }

}
