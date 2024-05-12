
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
 *         &lt;element name="GetReportResponseResult" type="{http://tempuri.org/}Report_Response" minOccurs="0"/>
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
    "getReportResponseResult"
})
@XmlRootElement(name = "GetReportResponseResponse")
public class GetReportResponseResponse {

    @XmlElement(name = "GetReportResponseResult")
    protected ReportResponse getReportResponseResult;

    /**
     * ��ȡgetReportResponseResult���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ReportResponse }
     *     
     */
    public ReportResponse getGetReportResponseResult() {
        return getReportResponseResult;
    }

    /**
     * ����getReportResponseResult���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ReportResponse }
     *     
     */
    public void setGetReportResponseResult(ReportResponse value) {
        this.getReportResponseResult = value;
    }

}
