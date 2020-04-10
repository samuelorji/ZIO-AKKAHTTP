#!/bin/bash
echo ":::::: Dumping todo Data into mysql"

export MYSQLCONTAINER=$(docker ps  | grep mysql | cut -d" " -f 1)
cat ./scripts/mysql/schema.sql | docker exec -i $MYSQLCONTAINER mysql -u root --password=password
cat ./scripts/mysql/patch.sql | docker exec -i $MYSQLCONTAINER mysql -u root --password=password test

echo ":::::: Done  I Hope ::::::"
