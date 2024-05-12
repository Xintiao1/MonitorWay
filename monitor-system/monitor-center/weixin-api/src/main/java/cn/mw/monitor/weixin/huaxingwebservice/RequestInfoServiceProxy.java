package cn.mw.monitor.weixin.huaxingwebservice;

public class RequestInfoServiceProxy implements RequestInfoService {
  private String _endpoint = null;
  private RequestInfoService requestInfoService = null;

  public RequestInfoServiceProxy() {
    _initRequestInfoServiceProxy();
  }

  public RequestInfoServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initRequestInfoServiceProxy();
  }

  private void _initRequestInfoServiceProxy() {
    try {
      requestInfoService = (new ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator()).getRequestInfoService();
      if (requestInfoService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)requestInfoService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)requestInfoService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }

    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }

  public String getEndpoint() {
    return _endpoint;
  }

  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (requestInfoService != null)
      ((javax.xml.rpc.Stub)requestInfoService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

  }

  public RequestInfoService getRequestInfoService() {
    if (requestInfoService == null)
      _initRequestInfoServiceProxy();
    return requestInfoService;
  }

  public java.util.HashMap addIncidentInfo(java.util.HashMap params) throws java.rmi.RemoteException{
    if (requestInfoService == null)
      _initRequestInfoServiceProxy();
    return requestInfoService.addIncidentInfo(params);
  }


}
