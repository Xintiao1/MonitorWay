
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
 *         &lt;element name="CreatTaskResult" type="{http://tempuri.org/}SendSMSRet" minOccurs="0"/>
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
    "creatTaskResult"
})
@XmlRootElement(name = "CreatTaskResponse")
public class CreatTaskResponse {

    @XmlElement(name = "CreatTaskResult")
    protected SendSMSRet creatTaskResult;

    /**
     * ��ȡcreatTaskResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link SendSMSRet }
     *     
     */
    public SendSMSRet getCreatTaskResult() {
        return creatTaskResult;
    }

    /**
     * ����creatTaskResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link SendSMSRet }
     *     
     */
    public void setCreatTaskResult(SendSMSRet value) {
        this.creatTaskResult = value;
    }

}
