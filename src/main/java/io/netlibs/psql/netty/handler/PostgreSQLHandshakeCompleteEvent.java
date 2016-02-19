package io.netlibs.psql.netty.handler;

import java.util.Map;

import io.netlibs.psql.wire.BackendKeyData;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class PostgreSQLHandshakeCompleteEvent
{

  @Singular
  private Map<String, String> parameters;
  private final BackendKeyData backendKey;

}
