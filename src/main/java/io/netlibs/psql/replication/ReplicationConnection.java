package io.netlibs.psql.replication;

import io.netlibs.psql.AbstractConnection;
import io.netlibs.psql.WalPosition;
import io.netlibs.psql.netty.handler.PostgreSQLClientNegotiation;
import io.netlibs.psql.netty.handler.PostgreSQLDecoder;
import io.netlibs.psql.netty.handler.PostgreSQLEncoder;
import io.netlibs.psql.netty.handler.PostgreSQLHandshakeCompleteEvent;
import io.netlibs.psql.netty.handler.PostgreSQLReplicationCopyDataCodec;
import io.netlibs.psql.wire.CommandComplete;
import io.netlibs.psql.wire.PostgreSQLPacket;
import io.netlibs.psql.wire.Query;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * A postgresql replication protocol connection.
 * 
 * If an error occurs, the connection is terminated.
 * 
 * @author theo
 *
 */

@Slf4j
public class ReplicationConnection extends AbstractConnection
{

  private final String slotId;
  private WalPosition position;
  private String create;

  private ReplicationHandler handler;

  ReplicationConnection(ReplicationConnectionBuilder b)
  {

    super(b);

    this.slotId = b.slotId;
    this.position = b.position;
    this.create = b.create;
    this.handler = b.handler;

    if (this.slotId == null)
    {
      throw new IllegalArgumentException("slotId");
    }

    if (this.handler == null)
    {
      throw new IllegalArgumentException("handler");
    }

    params.put("replication", "database");

  }

  private static enum HandlerState
  {
    Waiting, Identifying, Creating, Starting
  }

  private HandlerState state = HandlerState.Waiting;

  /**
   * handler which dispatches events from the netty thread to the user thread.
   */

  private final class Handler extends SimpleChannelInboundHandler<PostgreSQLPacket>
  {

    private ChannelHandlerContext ctx;

    // note that any packets which contain byte buffers are NOT retained after we return, so need to copy if needed.

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PostgreSQLPacket msg) throws Exception
    {

      if (msg instanceof CommandComplete)
      {
        this.nextStage();
      }
      else
      {
        System.err.println(msg);
      }

    }

    /**
     * The previous command completed, do the next thing.
     */

    private void nextStage()
    {

      log.debug("Executing next stage in {}", state);

      switch (state)
      {
        case Waiting:
          state = HandlerState.Identifying;
          sendIdentify();
          break;
        case Identifying:
          if (create != null)
          {
            state = HandlerState.Creating;
            sendCreate();
            break;
          }
        case Creating:
          state = HandlerState.Starting;
          sendStart();
          break;
        case Starting:
          // hmmp, that's odd...
          throw new RuntimeException("Unexpected next stage for state");

      }

    }

    void sendIdentify()
    {
      ctx.writeAndFlush(new Query("IDENTIFY_SYSTEM"));
    }

    void sendCreate()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("CREATE_REPLICATION_SLOT ").append(slotId).append(" LOGICAL ").append(create);
      ctx.writeAndFlush(new Query(sb.toString()));
    }

    void sendStart()
    {

      StringBuilder sb = new StringBuilder();

      sb.append("START_REPLICATION SLOT ").append(slotId).append(" LOGICAL ");

      if (position != null)
      {
        sb.append(position);
      }
      else
      {
        sb.append("0/0");
      }

      ctx.writeAndFlush(new Query(sb.toString()));

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {

      if (evt instanceof PostgreSQLHandshakeCompleteEvent)
      {
        this.ctx = ctx;
        // no need to leave it laying around.
        ctx.channel().pipeline().remove(PostgreSQLClientNegotiation.class);
        nextStage();
      }

    }

  }

  void connect(String host, int port)
  {

    final Bootstrap b = new Bootstrap();

    b.group(group)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(final SocketChannel ch) throws Exception
          {
            final ChannelPipeline p = ch.pipeline();
            p.addLast(new PostgreSQLDecoder());
            p.addLast(new PostgreSQLEncoder());
            p.addLast(new PostgreSQLClientNegotiation(ReplicationConnection.this.params));
            p.addLast(new PostgreSQLReplicationCopyDataCodec());
            p.addLast(new Handler());
            p.addLast(new ReplicationPacketHandler());
          }
        });

    // attempt to connect.
    this.connectFuture = b.connect(host, port);

  }

}
