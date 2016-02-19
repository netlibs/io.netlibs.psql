package io.netlibs.psql.jpgrepl.wal2json;

/**
 * A listener which follows the WAL log.
 *
 * @author theo
 *
 */

public interface WalChangeSetProcessor
{

  void process(ChangeSet set);

}
