package io.netlibs.psql.netty.handler;

import java.nio.charset.StandardCharsets;

import io.netlibs.psql.netty.ProtoUtils;
import io.netlibs.psql.wire.AuthenticationOk;
import io.netlibs.psql.wire.AuthenticationUnknown;
import io.netlibs.psql.wire.BackendKeyData;
import io.netlibs.psql.wire.CommandComplete;
import io.netlibs.psql.wire.CopyBothResponse;
import io.netlibs.psql.wire.CopyData;
import io.netlibs.psql.wire.CopyDone;
import io.netlibs.psql.wire.DataRow;
import io.netlibs.psql.wire.EmptyQueryResponse;
import io.netlibs.psql.wire.ErrorResponse;
import io.netlibs.psql.wire.Execute;
import io.netlibs.psql.wire.NoticeResponse;
import io.netlibs.psql.wire.ParameterStatus;
import io.netlibs.psql.wire.PostgreSQLPacket;
import io.netlibs.psql.wire.PostgreSQLPacketVisitor;
import io.netlibs.psql.wire.Query;
import io.netlibs.psql.wire.ReadyForQuery;
import io.netlibs.psql.wire.RowDescription;
import io.netlibs.psql.wire.StartupMessage;
import io.netlibs.psql.wire.UnknownMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * encode PostgreSQL messages.
 */

@Slf4j
public class PostgreSQLEncoder extends MessageToByteEncoder<PostgreSQLPacket>
{

  @Override
  protected void encode(ChannelHandlerContext ctx, PostgreSQLPacket msg, ByteBuf out) throws Exception
  {
    
    log.debug(" <<< {}", msg);

    msg.apply(new PostgreSQLPacketVisitor<Void>() {

      @Override
      public Void visitAuthenticationOk(AuthenticationOk pkt)
      {
        return null;
      }

      @Override
      public Void visitAuthenticationUnknown(AuthenticationUnknown pkt)
      {
        return null;
      }

      @Override
      public Void visitBackendKeyData(BackendKeyData data)
      {
        return null;
      }

      @Override
      public Void visitCommandComplete(CommandComplete cmd)
      {
        return null;
      }

      @Override
      public Void visitUnknownMessage(UnknownMessage unknownMessage)
      {
        return null;
      }

      @Override
      public Void visitCopyBothResponse(CopyBothResponse cmd)
      {
        return null;
      }

      @Override
      public Void visitCopyData(CopyData copyData)
      {
        out.writeByte('d');
        out.writeInt(copyData.getData().readableBytes() + 4);
        out.writeBytes(copyData.getData());
        return null;
      }

      @Override
      public Void visitCopyDone(CopyDone copyDone)
      {
        return null;
      }

      @Override
      public Void visitDataRow(DataRow dataRow)
      {
        return null;
      }

      @Override
      public Void visitErrorResponse(ErrorResponse errorResponse)
      {
        return null;
      }

      @Override
      public Void visitNoticeResponse(NoticeResponse noticeResponse)
      {
        return null;
      }

      @Override
      public Void visitParameterStatus(ParameterStatus parameterStatus)
      {
        return null;
      }

      @Override
      public Void visitReadyForQuery(ReadyForQuery readyForQuery)
      {
        return null;
      }

      @Override
      public Void visitRowDescription(RowDescription rowDescription)
      {
        return null;
      }

      @Override
      public Void visitStartupMessage(StartupMessage msg)
      {
        int pos = out.writerIndex();
        out.writeInt(0);
        out.writeInt(ProtoUtils.PROTO_VERSION);
        msg.getParameters().forEach((key, value) -> ProtoUtils.addParam(out, key, value));
        out.writeByte(0);
        out.setInt(pos, out.writerIndex() - pos);
        return null;
      }

      @Override
      public Void visitQuery(Query query)
      {
        out.writeByte('Q');
        int pos = out.writerIndex();
        out.writeInt(0); // update in a bit
        out.writeBytes(query.getQuery().getBytes(StandardCharsets.UTF_8));
        out.writeByte(0);
        out.setInt(pos, out.writerIndex() - pos);
        return null;
      }

      @Override
      public Void visitExecute(Execute execute)
      {
        out.writeByte('E');
        int pos = out.writerIndex();
        out.writeInt(0); // update in a bit
        out.writeBytes(execute.getCommand().getBytes(StandardCharsets.UTF_8));
        out.writeByte(execute.getMaxRows());
        out.setInt(pos, out.writerIndex() - pos);
        return null;
      }

      @Override
      public Void visitEmptyQueryResponse(EmptyQueryResponse emptyQueryResponse)
      {
        return null;
      }

    });

  }

}
