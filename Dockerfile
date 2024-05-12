FROM java

COPY monitor-aggregator-1.0-SNAPSHOT.jar /opt/app/monitor-system/
COPY startup.sh /root
COPY mwc.so /opt/app/monitor-system/lib/
COPY config.properties /opt/app/monitor-system/

RUN chmod +x /root/startup.sh

CMD ["sh","-c","/root/startup.sh"]

