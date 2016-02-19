FROM postgres:9.5

MAINTAINER Theo Zourzouvillys <theo@jive.com>

RUN apt-get update && apt-get install -y postgresql-server-dev-9.5 curl make gcc unzip


ADD https://github.com/eulerto/wal2json/archive/master.zip /usr/local/src/wal2json.zip
WORKDIR /usr/local/src/
RUN unzip wal2json.zip

RUN USE_PGXS=1 make -C /usr/local/src/wal2json-master install

RUN apt-get --purge remove -y postgresql-server-dev-9.5 curl make gcc unzip && apt-get autoclean -y
RUN rm -rf /usr/local/src/wal2json

# Post Install Configuration.
ADD bin/start-postgres /usr/bin/start-postgres
RUN chmod +x /usr/bin/start-postgres


ADD bin/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh


ADD ./data /srv

#ADD bin/heartbeat.template /usr/share/postgresql/9.3/heartbeat.template
#ADD conf/postgresql.conf.template /usr/share/postgresql/9.3/postgresql.conf.template
#ADD conf/pg_hba.conf.template /usr/share/postgresql/9.3/pg_hba.conf.template
#ADD conf/recovery.conf.template /usr/share/postgresql/9.3/recovery.conf.template

# Open the container up to the world.
EXPOSE 5432/tcp

RUN mkdir -p /var/run/postgresql && chown -R postgres /var/run/postgresql

ENV PGDATA /var/lib/postgresql/data
VOLUME /var/lib/postgresql/data


ENTRYPOINT ["/docker-entrypoint.sh"]

# Start PostgreSQL.
CMD ["postgres"]
