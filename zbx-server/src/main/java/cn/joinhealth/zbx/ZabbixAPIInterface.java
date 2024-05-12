package cn.joinhealth.zbx;

import com.fasterxml.jackson.databind.JsonNode;

public interface ZabbixAPIInterface {
  void init();

  void destroy();

  JsonNode call(RequestAbstract request);

  boolean login(String user, String password);
}

