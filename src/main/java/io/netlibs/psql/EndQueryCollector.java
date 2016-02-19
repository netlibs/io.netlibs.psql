package io.netlibs.psql;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.netlibs.psql.wire.CommandComplete;
import io.netlibs.psql.wire.DataRow;
import io.netlibs.psql.wire.EmptyQueryResponse;
import io.netlibs.psql.wire.ErrorResponse;
import io.netlibs.psql.wire.NoticeResponse;
import io.netlibs.psql.wire.ReadyForQuery;
import io.netlibs.psql.wire.RowDescription;
import io.netlibs.psql.wire.RowDescription.Entry;

/**
 * Collector which provides a completable future which completes once we get a ReadyForQuery.
 * 
 * @author theo
 *
 */
public class EndQueryCollector implements QueryListener
{

  private final CompletableFuture<Map<String, String>> future = new CompletableFuture<>();

  public Map<String, String> get() throws QueryException
  {
    try
    {
      return future.get();
    }
    catch (InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }
    catch (ExecutionException ex)
    {
      try
      {
        throw ex.getCause();
      }
      catch (QueryException qex)
      {
        throw qex;
      }
      catch (Throwable t)
      {
        throw new RuntimeException(ex.getCause());
      }
    }
  }

  @Override
  public void description(RowDescription e)
  {
  }

  @Override
  public void row(DataRow e)
  {
  }

  @Override
  public void emptyResponse(EmptyQueryResponse e)
  {
    // future.complete(null);
  }

  @Override
  public void error(ErrorResponse e)
  {
    // future.completeExceptionally(new QueryException(e));
  }

  @Override
  public void notice(NoticeResponse e)
  {
  }

  @Override
  public void commandComplete(CommandComplete e)
  {
  }

  @Override
  public void readyForQuery(ReadyForQuery e)
  {
    future.complete(null);
  }

}
