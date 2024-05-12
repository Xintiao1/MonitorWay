echo "stop redis"
REDIS_PASSWD=jhkj520
redis-cli -a $REDIS_PASSWD shutdown
sleep 20
echo "redis stopped"

echo "stop zookeeper"
../kafka/bin/zookeeper-server-stop.sh
sleep 20
echo "zookeeper stopped"
echo "stop kafka"
../kafka/bin/kafka-server-stop.sh
sleep 20
echo "kafka stopped"