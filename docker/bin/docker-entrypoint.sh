#!/bin/bash

set -e

echo "I: launching entrypoint: $@"

if [ "$1" = 'postgres' ]; then

	echo "I: starting PostgreSQL in ${PGDATA}"


	mkdir -p "$PGDATA"
	chmod 700 "$PGDATA"
	chown -R postgres "$PGDATA"

	chmod g+s /run/postgresql
	chown -R postgres /run/postgresql

	if [ ! -s "$PGDATA/PG_VERSION" ]; then

		echo "I: first run, initiaising DB"

		#POSTGRESQL_CONF_SAMPLE=/srv/postgresql.conf
		#PG_HBA_SAMPLE=/srv/pg_hba.conf
		#PG_IDENT_SAMPLE=/srv/pg_ident.conf


		gosu postgres initdb -A trust -E UTF-8 --no-locale "${PGDATA}"
		cp /srv/postgresql.conf ${PGDATA}
		cp /srv/pg_hba.conf ${PGDATA}

		echo "I: perfoming initial DB startup (UNIX socket only)"
		gosu postgres pg_ctl -D "$PGDATA" -o "-c listen_addresses=''" -w start


		: ${POSTGRES_USER:=postgres}
		: ${POSTGRES_DB:=$POSTGRES_USER}

		export POSTGRES_USER POSTGRES_DB

		if [ "$POSTGRES_DB" != 'postgres' ]; then
			psql --username postgres <<-EOSQL
				CREATE DATABASE "$POSTGRES_DB" ;
			EOSQL
			echo
		fi

		if [ "$POSTGRES_USER" = 'postgres' ]; then
			op='ALTER'
		else
			op='CREATE'
		fi

		psql --username postgres <<-EOSQL
			$op USER "$POSTGRES_USER" WITH SUPERUSER $pass ;
		EOSQL
		echo


		echo
		for f in /docker-entrypoint-initdb.d/*; do
			case "$f" in
				*.sh)  echo "$0: running $f"; . "$f" ;;
				*.sql) 
					echo "$0: running $f"; 
					psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" < "$f"
					echo 
					;;
				*)     echo "$0: ignoring $f" ;;
			esac
			echo
		done

		gosu postgres pg_ctl -D "$PGDATA" -m fast -w stop
		# set_listen_addresses '*'

		echo
		echo 'I: PostgreSQL init process complete; ready for start up.'
		echo

	fi

	exec /usr/bin/start-postgres "$@"

fi


exec "$@"

