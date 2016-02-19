package io.netlibs.psql.netty.handler;

import java.util.ArrayList;
import java.util.List;

import io.netlibs.psql.netty.ProtoUtils;
import io.netlibs.psql.wire.AuthenticationOk;
import io.netlibs.psql.wire.AuthenticationUnknown;
import io.netlibs.psql.wire.BackendKeyData;
import io.netlibs.psql.wire.CommandComplete;
import io.netlibs.psql.wire.CopyBothResponse;
import io.netlibs.psql.wire.CopyBothResponse.Format;
import io.netlibs.psql.wire.CopyData;
import io.netlibs.psql.wire.CopyDone;
import io.netlibs.psql.wire.DataRow;
import io.netlibs.psql.wire.NotificationResponse;
import io.netlibs.psql.wire.ParameterStatus;
import io.netlibs.psql.wire.PostgreSQLPacket;
import io.netlibs.psql.wire.ReadyForQuery;
import io.netlibs.psql.wire.RowDescription;
import io.netlibs.psql.wire.UnknownMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * decode PostgreSQL messages.
 */

@Slf4j
public class PostgreSQLDecoder extends ByteToMessageDecoder
{

  // number of packets we allow before flushing.
  private static final int BATCH_SIZE_MAX = 10000;

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception
  {

    while (in.readableBytes() >= 5 && out.size() < BATCH_SIZE_MAX)
    {

      final byte type = in.getByte(in.readerIndex());

      final int len = in.getInt(in.readerIndex() + 1);

      if (in.readableBytes() < (len + 1))
      {
        return;
      }

      in.skipBytes(5);

      final MessageType mtype = MessageType.getType(type);

      final ByteBuf payload = in.readSlice(len - 4);

      PostgreSQLPacket packet = parse(mtype, payload);

      log.trace("[{}] >>> {}", out.size(), packet);

      out.add(packet);

    }

  }

  /**
   * Parse the specified packet.
   * 
   * @param mtype
   * @param payload
   * @return
   */

  private static final PostgreSQLPacket parse(MessageType mtype, ByteBuf payload)
  {

    switch (mtype)
    {

      case AuthRequest:
      {

        int authType = payload.readInt();

        switch (authType)
        {
          case 0:
            return new AuthenticationOk();
          default:
            return new AuthenticationUnknown();
        }

      }

      case BackendKeyData:
      {
        int processId = payload.readInt();
        int secret = payload.readInt();
        return new BackendKeyData(processId, secret);
      }

      case CommandComplete:
      {
        return new CommandComplete(ProtoUtils.parseString(payload));
      }

      case CopyBothResponse:
      {
        return parseCopyBothResponse(payload);
      }

      case CopyData:
      {
        return parseCopyData(payload);
      }

      case CopyDone:
      {
        return new CopyDone();
      }

      case DataRow:
      {
        return new DataRow(ProtoUtils.parseDataRow(payload));
      }

      case ErrorResponse:
      {
        return ProtoUtils.parseError(payload);
      }

      case NoticeResponse:
      {
        return ProtoUtils.parseNotice(payload);
      }

      case ParameterStatus:
      {
        String key = ProtoUtils.parseString(payload);
        String value = ProtoUtils.parseString(payload);
        return new ParameterStatus(key, value);
      }

      case ReadyForQuery:
      {
        return new ReadyForQuery(payload.readByte());
      }

      case RowDescription:
      {
        return new RowDescription(ProtoUtils.parseRowDescription(payload));
      }
      case NotificationResponse:
      {
        int processId = payload.readInt();
        String name = ProtoUtils.parseString(payload);
        String data = ProtoUtils.parseString(payload);
        return new NotificationResponse(processId, name, data);
      }

    }
    return new UnknownMessage(mtype.getType());
  }

  private static final CopyData parseCopyData(final ByteBuf buffer)
  {
    return new CopyData(buffer);
  }

  private static final CopyBothResponse parseCopyBothResponse(ByteBuf cbp)
  {

    final int type = cbp.readByte();

    switch (type)
    {

      case 0:
        // rtype = ReplicationHandle.Format.Text;
        break;

      case 1:
        // rtype = ReplicationHandle.Format.Binary;
        break;

      default:
        // it's an unknown type. erp.
        // rtype = ReplicationHandle.Format.Unknown;
        break;

    }

    final int len = cbp.readShort();

    List<Integer> formats = new ArrayList<>(len);

    for (int i = 0; i < len; ++i)
    {
      formats.add((int) cbp.readShort());
    }

    return new CopyBothResponse((type) == 1 ? Format.Text : Format.Binary, formats);

  }

}
