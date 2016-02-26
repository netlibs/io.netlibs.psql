package io.netlibs.psql;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jive.oss.junit.docker.DockerContainerRule;
import com.jive.oss.junit.docker.ExtRunner;
import com.jive.oss.junit.docker.RunIf;

@RunIf(DockerContainerRule.Available.class)
@RunWith(ExtRunner.class)
public class DockerReplicationTest
{

  @Rule
  public final DockerContainerRule docker = new DockerContainerRule("zourzouvillys/postgresql:9.5")
      .expose("5432/tcp")
      .env("POSTGRES_DB", "jpgrepl-test")
      .env("POSTGRES_USER", "jpgrepl-test-user");

  /**
   * try connecting to a real PostgreSQL instance and sync replication.
   */

  @Test
  public void testClient() throws InterruptedException
  {

    // perform a connection, and sync.
    InetSocketAddress server = docker.target("5432/tcp");

    // try 10 times, waiting 5 second between attempts.
    int c = 5;

    //
    while (c-- > 0)
    {

      System.err.println("Trying to connect");

      CountDownLatch latch = new CountDownLatch(1);

      PostgresConnection conn = new PostgresConnectionBuilder()
          .username("jpgrepl-test-user")
          .database("jpgrepl-test")
          .newConnection(server.getHostName(), server.getPort());
      
      latch.await(5, TimeUnit.SECONDS);

      Thread.sleep(1000);

    }

    Assert.fail("didn't establish connection to postgresql");

  }

}
