
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
 *         &lt;element name="GetReceveInfoResult" type="{http://tempuri.org/}Receve_Response" minOccurs="0"/>
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
    "getReceveInfoResult"
})
@XmlRootElement(name = "GetReceveInfoResponse")
public class GetReceveInfoResponse {

    @XmlElement(name = "GetReceveInfoResult")
    protected ReceveResponse getReceveInfoResult;

    /**
     * ��ȡgetReceveInfoResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ReceveResponse }
     *     
     */
    public ReceveResponse getGetReceveInfoResult() {
        return getReceveInfoResult;
    }

    /**
     * ����getReceveInfoResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ReceveResponse }
     *     
     */
    public void setGetReceveInfoResult(ReceveResponse value) {
        this.getReceveInfoResult = value;
    }

}
