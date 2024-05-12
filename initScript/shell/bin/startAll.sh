Log="startup_"`date +%Y%m%d`".log"

echo "start zookeeper"
nohup ../kafka/bin/zookeeper-server-start.sh  -daemon ../kafka/config/zookeeper.properties >../zookeeper/logs/$Log &
sleep 20

echo "start kafka"
nohup ../kafka/bin/kafka-server-start.sh  -daemon ../kafka/config/server.properties > /dev/null 2>&1 1>../kafka/logs/$Log &
sleep 20

echo "start monitor-timertask"
../monitor-timer/startup.sh

echo "start monitor-system"
../monitor-system/startup.sh
