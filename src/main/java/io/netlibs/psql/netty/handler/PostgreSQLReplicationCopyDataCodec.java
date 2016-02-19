package io.netlibs.psql.netty.handler;

import java.util.List;

import io.netlibs.psql.replication.XKeepAlive;
import io.netlibs.psql.replication.XLogData;
import io.netlibs.psql.wire.CopyData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

/**
 * a codec which converts CopyData to/from {@link XLogData} messages, when running in replication mode with START_REPLICATION.
 */

@Slf4j
public class PostgreSQLReplicationCopyDataCodec extends MessageToMessageCodec<CopyData, XKeepAlive>
{

  @Override
  protected void decode(ChannelHandlerContext ctx, CopyData msg, List<Object> out) throws Exception
  {
    
    final ByteBuf copydata = Unpooled.wrappedBuffer(msg.getData());
    final byte type = copydata.readByte();
    
    switch (type)
    {
      case 'w': // data
      {
        final long startingPoint = copydata.readLong();
        final long currentEnd = copydata.readLong();
        final long txtime = copydata.readLong();
        // A single WAL record is never split across two XLogData messages. When a WAL record crosses a WAL page boundary, and
        // is therefore already split using continuation records, it can be split at the page boundary. In other words, the first
        // main WAL record and its continuation records can be sent in different XLogData messages.
        out.add(new XLogData(startingPoint, currentEnd, txtime, copydata.slice()));
        break;
      }
      case 'k': // keepalive
      {
        final long currentEnd = copydata.readLong();
        final long serverTimeMicros = copydata.readLong();
        boolean reply = copydata.readBoolean();
        out.add(new XKeepAlive(currentEnd, serverTimeMicros, reply));
        break;
      }
      default:
      {
        // shoud we send upstream anyway?
        log.warn("Unknown type: '{}'", type);
        break;
      }
    }

  }

  /**
   * 
   * @param ctx
   * @param msg
   * @param out
   * @throws Exception
   */

  @Override
  protected void encode(ChannelHandlerContext ctx, XKeepAlive msg, List<Object> out) throws Exception
  {
    ByteBuf xkp = ctx.alloc().buffer();
    xkp.writeByte('r');
    xkp.writeLong(msg.getCurrentEnd());
    xkp.writeLong(msg.getCurrentEnd());
    xkp.writeLong(0);
    xkp.writeLong(msg.getServerTimeMicros());
    xkp.writeByte(0);
    out.add(new CopyData(xkp));
  }

}
