package io.netlibs.psql.netty.handler;

import java.util.ArrayList;
import java.util.List;

import io.netlibs.psql.netty.MessageType;
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
import io.netlibs.psql.wire.ParameterStatus;
import io.netlibs.psql.wire.PostgreSQLPacket;
import io.netlibs.psql.wire.ReadyForQuery;
import io.netlibs.psql.wire.RowDescription;
import io.netlibs.psql.wire.UnknownMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * decode PostgreSQL messages.
 */

public class PostgreSQLDecoder extends ByteToMessageDecoder
{

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception
  {

    while (in.readableBytes() >= 5)
    {

      final byte type = in.getByte(in.readerIndex());

      final int len = in.getInt(in.readerIndex() + 1);

      if (in.readableBytes() < (len + 1))
      {
        return;
      }

      in.skipBytes(5);

      final MessageType mtype = MessageType.getType(type);

      final ByteBuf payload = in.readSlice(len - 4).retain();

      out.add(parse(mtype, payload));

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

    }
    return new UnknownMessage(mtype);
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
