#!/bin/bash

mongod --replSet  my-mongo-set &
sleep 30
#mongo --eval 'config = {"_id" : "my-mongo-set","members" : [{"_id" : 0,"host" : "mongo1:27017"},{"_id" : 1,"host" : "mongo2:27017"},{"_id" : 2,"host" : "mongo3:27017"}]};rs.initiate(config)' >> /tmp/rr1
mongo --eval 'config = {"_id" : "my-mongo-set","members" : [{"_id" : 0,"host" : "mongo1:27017"}]};rs.initiate(config)' >> /tmp/rr1

curl -i -u guest:guest -H "content-type:application/json" -XPUT -d'{"type":"topic","durable":true}' http://mb:15672/api/exchanges/%2f/eiffel.demo

tail -f /dev/null
