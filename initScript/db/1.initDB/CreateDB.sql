#root用户登录并授权
CREATE DATABASE IF NOT EXISTS monitor_log DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON monitor_log.* TO '@monitor_log_user@'@'localhost';

CREATE DATABASE IF NOT EXISTS monitor DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE USER '@monitor_user@'@'localhost' IDENTIFIED BY 'Dev20$uiyD7';
GRANT ALL PRIVILEGES ON monitor.* TO '@monitor_user@'@'localhost';
flush privileges;

