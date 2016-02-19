package io.netlibs.psql;

import io.netlibs.psql.wire.ErrorResponse;
import lombok.Getter;

public class QueryException extends RuntimeException
{

  @Getter
  private ErrorResponse error;

  public QueryException(ErrorResponse e)
  {
    super(e.toString());
    this.error = e;    
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

}
