/**
 * ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cn.mw.monitor.weixin.huaxingwebservice;

public class ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator extends org.apache.axis.client.Service implements ESB_ITSM_ITSM_RequestIncidentInfoServiceagent {

    public ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator() {
    }


    public ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ESB_ITSM_ITSM_RequestIncidentInfoServiceagentLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for RequestInfoService
    private String RequestInfoService_address = "http://10.109.18.33:9028/Services/ESB_ITSM_ITSM_RequestIncidentInfo/v1.0.0";

    public String getRequestInfoServiceAddress() {
        return RequestInfoService_address;
    }

    // The WSDD service name defaults to the port name.
    private String RequestInfoServiceWSDDServiceName = "RequestInfoService";

    public String getRequestInfoServiceWSDDServiceName() {
        return RequestInfoServiceWSDDServiceName;
    }

    public void setRequestInfoServiceWSDDServiceName(String name) {
        RequestInfoServiceWSDDServiceName = name;
    }

    public RequestInfoService getRequestInfoService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(RequestInfoService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getRequestInfoService(endpoint);
    }

    public RequestInfoService getRequestInfoService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            RequestInfoServiceBindingStub _stub = new RequestInfoServiceBindingStub(portAddress, this);
            _stub.setPortName(getRequestInfoServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setRequestInfoServiceEndpointAddress(String address) {
        RequestInfoService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (RequestInfoService.class.isAssignableFrom(serviceEndpointInterface)) {
                RequestInfoServiceBindingStub _stub = new RequestInfoServiceBindingStub(new java.net.URL(RequestInfoService_address), this);
                _stub.setPortName(getRequestInfoServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("RequestInfoService".equals(inputPortName)) {
            return getRequestInfoService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://10.109.18.33:9028/Services/ESB_ITSM_ITSM_RequestIncidentInfo/v1.0.0", "ESB_ITSM_ITSM_RequestIncidentInfo.serviceagent");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://10.109.18.33:9028/Services/ESB_ITSM_ITSM_RequestIncidentInfo/v1.0.0", "RequestInfoService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {

if ("RequestInfoService".equals(portName)) {
            setRequestInfoServiceEndpointAddress(address);
        }
        else
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
