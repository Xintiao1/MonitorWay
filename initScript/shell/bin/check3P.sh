#!/bin/bash
KAFKA_HOME=/opt/app/kafka

conf_path=`redis-cli -a jhkj520 config get dir | tail -1`
conf_file=${conf_path%/}/redis.conf
# Get maxmemory value from Redis configuration file
maxmemory=`grep "^maxmemory" $conf_file | awk '{print $2}'`
if [[ -n "$maxmemory" ]]; then
  echo "maxmemory = $maxmemory"
  echo "Redis is ok"
else
  echo "Redis is down"
  exit 1
fi

ZK_SERVER="localhost"
# Check if ZooKeeper is running by checking Kafka's connection to it
STATUS=$(echo ruok | nc $ZK_SERVER 2181 | grep -i imok)
if [ "$STATUS" = "imok" ]; then
    echo "ZooKeeper is running."
else
    echo "ZooKeeper is not running."
fi

$KAFKA_HOME/bin/kafka-consumer-groups.sh --list --bootstrap-server localhost:9093
if [ $? -eq 0 ]; then
    echo "Kafka is running."
else
    echo "Kafka is not running."
fi