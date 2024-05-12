../kafka/bin/kafka-topics.sh --bootstrap-server localhost:9093 --create --replication-factor 1 --partitions 1 --topic zabbix-alert
../kafka/bin/kafka-topics.sh --bootstrap-server localhost:9093 --create --replication-factor 1 --partitions 1 --topic assets-message
../kafka/bin/kafka-topics.sh --bootstrap-server localhost:9093 --create --replication-factor 1 --partitions 1 --topic scan-queue
../kafka/bin/kafka-topics.sh --bootstrap-server localhost:9093 --create --replication-factor 1 --partitions 1 --topic t5bczabbix-alert
../kafka/bin/kafka-topics.sh --bootstrap-server localhost:9093 --list