
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="GetTaskInfoResult" type="{http://tempuri.org/}TaskInfo_Response" minOccurs="0"/>
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
    "getTaskInfoResult"
})
@XmlRootElement(name = "GetTaskInfoResponse")
public class GetTaskInfoResponse {

    @XmlElement(name = "GetTaskInfoResult")
    protected TaskInfoResponse getTaskInfoResult;

    /**
     * ��ȡgetTaskInfoResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link TaskInfoResponse }
     *     
     */
    public TaskInfoResponse getGetTaskInfoResult() {
        return getTaskInfoResult;
    }

    /**
     * ����getTaskInfoResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link TaskInfoResponse }
     *     
     */
    public void setGetTaskInfoResult(TaskInfoResponse value) {
        this.getTaskInfoResult = value;
    }

}
