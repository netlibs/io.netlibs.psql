package io.netlibs.psql.replication;

import java.util.concurrent.TimeUnit;

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
final class ReplicationPacketHandler extends SimpleChannelInboundHandler<ReplicationPacket>
{

  private long outputWrittenLsn;
  private ScheduledFuture<?> future;

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ReplicationPacket msg) throws Exception
  {

    if (msg instanceof XLogData)
    {

    }
    else if (msg instanceof XKeepAlive)
    {

      XKeepAlive keepalive = (XKeepAlive) msg;

      this.outputWrittenLsn = keepalive.getCurrentEnd();

      if (keepalive.isReply())
      {
        this.sendKeepalive(ctx);
      }

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
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {
    this.future = ctx.executor().scheduleWithFixedDelay(() -> this.sendKeepalive(ctx), 5, 5, TimeUnit.SECONDS);
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    this.future.cancel(true);
    super.channelInactive(ctx);
  }

}
