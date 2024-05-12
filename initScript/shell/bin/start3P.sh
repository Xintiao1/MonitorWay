Log="startup_"`date +%Y%m%d`".log"

echo "start zookeeper"
../kafka/bin/zookeeper-server-start.sh  -daemon ../kafka/config/zookeeper.properties
sleep 20
echo "zookeeper started"

echo "start kafka"
../kafka/bin/kafka-server-start.sh  -daemon ../kafka/config/server.properties
sleep 20
echo "kafka started"