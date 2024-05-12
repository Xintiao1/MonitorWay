
package cn.mw.monitor.weixin.mypackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Receve_Response complex type�� Java �ࡣ
 * 
 * <p>����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 * 
 * <pre>
 * &lt;complexType name="Receve_Response">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tempuri.org/}Msg_Response">
 *       &lt;sequence>
 *         &lt;element name="ReceveList" type="{http://tempuri.org/}ArrayOfReceve_Info" minOccurs="0"/>
 *         &lt;element name="ReportList" type="{http://tempuri.org/}ArrayOfReport_Info" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Receve_Response", propOrder = {
    "receveList",
    "reportList"
})
public class ReceveResponse
    extends MsgResponse
{

    @XmlElement(name = "ReceveList")
    protected ArrayOfReceveInfo receveList;
    @XmlElement(name = "ReportList")
    protected ArrayOfReportInfo reportList;

    /**
     * ��ȡreceveList���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfReceveInfo }
     *     
     */
    public ArrayOfReceveInfo getReceveList() {
        return receveList;
    }

    /**
     * ����receveList���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfReceveInfo }
     *     
     */
    public void setReceveList(ArrayOfReceveInfo value) {
        this.receveList = value;
    }

    /**
     * ��ȡreportList���Ե�ֵ��
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfReportInfo }
     *     
     */
    public ArrayOfReportInfo getReportList() {
        return reportList;
    }

    /**
     * ����reportList���Ե�ֵ��
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfReportInfo }
     *     
     */
    public void setReportList(ArrayOfReportInfo value) {
        this.reportList = value;
    }

}
