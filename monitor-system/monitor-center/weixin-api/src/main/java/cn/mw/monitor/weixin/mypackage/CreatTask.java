
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>anonymous complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="App_ID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="App_PWD" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Task_Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Class_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Type_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Biz_Sub_Type_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StartTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Stop_Time" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Priority" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Is_Need_Report" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Task_Content" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Time_Scheme_ID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Auto_Mode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Preorder_Mode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Schedule_Mode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Periodic_Mode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "appID",
    "appPWD",
    "taskName",
    "bizClassID",
    "bizTypeID",
    "bizSubTypeID",
    "startTime",
    "stopTime",
    "priority",
    "isNeedReport",
    "taskContent",
    "timeSchemeID",
    "autoMode",
    "preorderMode",
    "scheduleMode",
    "periodicMode"
})
@XmlRootElement(name = "CreatTask")
public class CreatTask {

    @XmlElement(name = "App_ID")
    protected int appID;
    @XmlElement(name = "App_PWD")
    protected String appPWD;
    @XmlElement(name = "Task_Name")
    protected String taskName;
    @XmlElement(name = "Biz_Class_ID")
    protected String bizClassID;
    @XmlElement(name = "Biz_Type_ID")
    protected String bizTypeID;
    @XmlElement(name = "Biz_Sub_Type_ID")
    protected String bizSubTypeID;
    @XmlElement(name = "StartTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startTime;
    @XmlElement(name = "Stop_Time", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar stopTime;
    @XmlElement(name = "Priority")
    protected int priority;
    @XmlElement(name = "Is_Need_Report")
    protected int isNeedReport;
    @XmlElement(name = "Task_Content")
    protected String taskContent;
    @XmlElement(name = "Time_Scheme_ID")
    protected int timeSchemeID;
    @XmlElement(name = "Auto_Mode")
    protected int autoMode;
    @XmlElement(name = "Preorder_Mode")
    protected int preorderMode;
    @XmlElement(name = "Schedule_Mode")
    protected int scheduleMode;
    @XmlElement(name = "Periodic_Mode")
    protected int periodicMode;

    /**
     * ��ȡappID���Ե�ֵ��
     * 
     */
    public int getAppID() {
        return appID;
    }

    /**
     * ����appID���Ե�ֵ��
     * 
     */
    public void setAppID(int value) {
        this.appID = value;
    }

    /**
     * ��ȡappPWD���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppPWD() {
        return appPWD;
    }

    /**
     * ����appPWD���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppPWD(String value) {
        this.appPWD = value;
    }

    /**
     * ��ȡtaskName���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * ����taskName���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskName(String value) {
        this.taskName = value;
    }

    /**
     * ��ȡbizClassID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizClassID() {
        return bizClassID;
    }

    /**
     * ����bizClassID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizClassID(String value) {
        this.bizClassID = value;
    }

    /**
     * ��ȡbizTypeID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizTypeID() {
        return bizTypeID;
    }

    /**
     * ����bizTypeID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizTypeID(String value) {
        this.bizTypeID = value;
    }

    /**
     * ��ȡbizSubTypeID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizSubTypeID() {
        return bizSubTypeID;
    }

    /**
     * ����bizSubTypeID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizSubTypeID(String value) {
        this.bizSubTypeID = value;
    }

    /**
     * ��ȡstartTime���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * ����startTime���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartTime(XMLGregorianCalendar value) {
        this.startTime = value;
    }

    /**
     * ��ȡstopTime���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStopTime() {
        return stopTime;
    }

    /**
     * ����stopTime���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStopTime(XMLGregorianCalendar value) {
        this.stopTime = value;
    }

    /**
     * ��ȡpriority���Ե�ֵ��
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * ����priority���Ե�ֵ��
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
    }

    /**
     * ��ȡisNeedReport���Ե�ֵ��
     * 
     */
    public int getIsNeedReport() {
        return isNeedReport;
    }

    /**
     * ����isNeedReport���Ե�ֵ��
     * 
     */
    public void setIsNeedReport(int value) {
        this.isNeedReport = value;
    }

    /**
     * ��ȡtaskContent���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskContent() {
        return taskContent;
    }

    /**
     * ����taskContent���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskContent(String value) {
        this.taskContent = value;
    }

    /**
     * ��ȡtimeSchemeID���Ե�ֵ��
     * 
     */
    public int getTimeSchemeID() {
        return timeSchemeID;
    }

    /**
     * ����timeSchemeID���Ե�ֵ��
     * 
     */
    public void setTimeSchemeID(int value) {
        this.timeSchemeID = value;
    }

    /**
     * ��ȡautoMode���Ե�ֵ��
     * 
     */
    public int getAutoMode() {
        return autoMode;
    }

    /**
     * ����autoMode���Ե�ֵ��
     * 
     */
    public void setAutoMode(int value) {
        this.autoMode = value;
    }

    /**
     * ��ȡpreorderMode���Ե�ֵ��
     * 
     */
    public int getPreorderMode() {
        return preorderMode;
    }

    /**
     * ����preorderMode���Ե�ֵ��
     * 
     */
    public void setPreorderMode(int value) {
        this.preorderMode = value;
    }

    /**
     * ��ȡscheduleMode���Ե�ֵ��
     * 
     */
    public int getScheduleMode() {
        return scheduleMode;
    }

    /**
     * ����scheduleMode���Ե�ֵ��
     * 
     */
    public void setScheduleMode(int value) {
        this.scheduleMode = value;
    }

    /**
     * ��ȡperiodicMode���Ե�ֵ��
     * 
     */
    public int getPeriodicMode() {
        return periodicMode;
    }

    /**
     * ����periodicMode���Ե�ֵ��
     * 
     */
    public void setPeriodicMode(int value) {
        this.periodicMode = value;
    }

}
