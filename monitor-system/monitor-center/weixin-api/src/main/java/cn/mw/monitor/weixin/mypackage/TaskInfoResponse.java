
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>TaskInfo_Response complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="TaskInfo_Response">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tempuri.org/}Msg_Response">
 *       &lt;sequence>
 *         &lt;element name="Task_ID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Task_Status_Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Priority" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Total_Count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Succ_Count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Fail_Count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Error_Count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Recev_Count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskInfo_Response", propOrder = {
    "taskID",
    "taskStatusName",
    "priority",
    "totalCount",
    "succCount",
    "failCount",
    "errorCount",
    "recevCount"
})
public class TaskInfoResponse
    extends MsgResponse
{

    @XmlElement(name = "Task_ID")
    protected String taskID;
    @XmlElement(name = "Task_Status_Name")
    protected String taskStatusName;
    @XmlElement(name = "Priority")
    protected int priority;
    @XmlElement(name = "Total_Count")
    protected int totalCount;
    @XmlElement(name = "Succ_Count")
    protected int succCount;
    @XmlElement(name = "Fail_Count")
    protected int failCount;
    @XmlElement(name = "Error_Count")
    protected int errorCount;
    @XmlElement(name = "Recev_Count")
    protected int recevCount;

    /**
     * ��ȡtaskID���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskID() {
        return taskID;
    }

    /**
     * ����taskID���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskID(String value) {
        this.taskID = value;
    }

    /**
     * ��ȡtaskStatusName���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskStatusName() {
        return taskStatusName;
    }

    /**
     * ����taskStatusName���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskStatusName(String value) {
        this.taskStatusName = value;
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
     * ��ȡtotalCount���Ե�ֵ��
     * 
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * ����totalCount���Ե�ֵ��
     * 
     */
    public void setTotalCount(int value) {
        this.totalCount = value;
    }

    /**
     * ��ȡsuccCount���Ե�ֵ��
     * 
     */
    public int getSuccCount() {
        return succCount;
    }

    /**
     * ����succCount���Ե�ֵ��
     * 
     */
    public void setSuccCount(int value) {
        this.succCount = value;
    }

    /**
     * ��ȡfailCount���Ե�ֵ��
     * 
     */
    public int getFailCount() {
        return failCount;
    }

    /**
     * ����failCount���Ե�ֵ��
     * 
     */
    public void setFailCount(int value) {
        this.failCount = value;
    }

    /**
     * ��ȡerrorCount���Ե�ֵ��
     * 
     */
    public int getErrorCount() {
        return errorCount;
    }

    /**
     * ����errorCount���Ե�ֵ��
     * 
     */
    public void setErrorCount(int value) {
        this.errorCount = value;
    }

    /**
     * ��ȡrecevCount���Ե�ֵ��
     * 
     */
    public int getRecevCount() {
        return recevCount;
    }

    /**
     * ����recevCount���Ե�ֵ��
     * 
     */
    public void setRecevCount(int value) {
        this.recevCount = value;
    }

}
