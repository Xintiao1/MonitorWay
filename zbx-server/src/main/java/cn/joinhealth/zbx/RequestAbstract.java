package cn.joinhealth.zbx;

import lombok.Data;

@Data
public abstract class RequestAbstract {
  String jsonrpc = "2.0";

  Object params;

  String method;

  String auth;

  Integer id;

  abstract void setParams(Object params);
}
