package cn.mwpaas.common.utils;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;

/**
 * @author phzhou
 * @ClassName XmlUtils
 * @CreateDate 2019/3/4
 * @Description
 */
public class XmlUtils {

    /** 在XML中无效的字符 正则 */
    public final static String INVALID_REGEX = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";

    /**
     * 读取解析XML文件
     *
     * @param file XML文件
     * @return XML文档对象
     */
    public static Document readXML(File file) throws IOException, SAXException, ParserConfigurationException {
        if (file == null) {
            throw new NullPointerException("Xml file is null !");
        }

        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            // ignore
        }

        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            return readXML(in);
        } finally {
            IOUtils.close(in);
        }
    }

    /**
     * 读取解析XML文件<br>
     * 编码在XML中定义
     *
     * @param inputStream XML流
     * @return XML文档对象
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Document readXML(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        return readXML(new InputSource(inputStream));
    }

    /**
     * 读取解析XML文件<br>
     * 编码在XML中定义
     *
     * @param source {@link InputSource}
     * @return XML文档对象
     * @since 3.0.9
     */
    public static Document readXML(InputSource source) throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilder builder = createDocumentBuilder();
        return builder.parse(source);
    }

    /**
     * 创建 DocumentBuilder
     *
     * @return DocumentBuilder
     * @since 4.1.2
     */
    public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        disableXXE(dbf);
        DocumentBuilder builder;
        builder = dbf.newDocumentBuilder();
        return builder;
    }

    /**
     * 将String类型的XML转换为XML文档
     *
     * @param xmlStr XML字符串
     * @return XML文档
     */
    public static Document parseXml(String xmlStr) throws ParserConfigurationException, SAXException, IOException {
        if (StringUtils.isBlank(xmlStr)) {
            throw new IllegalArgumentException("XML content string is empty !");
        }
        xmlStr = cleanInvalid(xmlStr);
        return readXML(new InputSource(new StringReader(xmlStr)));
    }


    /**
     * 通过XPath方式读取XML的NodeList<br>
     *
     * @param expression XPath表达式
     * @param source 资源，可以是Docunent、Node节点等
     * @return NodeList
     */
    public static NodeList getNodeListByXPath(String expression, Object source) throws XPathExpressionException {
        return (NodeList) getByXPath(expression, source, XPathConstants.NODESET);
    }

    /**
     * 通过XPath方式读取XML节点等信息<br>
     *
     * @param expression XPath表达式
     * @param source 资源，可以是Docunent、Node节点等
     * @param returnType 返回类型，{@link XPathConstants}
     * @return 匹配返回类型的值
     */
    public static Object getByXPath(String expression, Object source, QName returnType) throws XPathExpressionException {
        final XPath xPath = createXPath();
        if (source instanceof InputSource) {
            return xPath.evaluate(expression, (InputSource) source, returnType);
        } else {
            return xPath.evaluate(expression, source, returnType);
        }
    }

    /**
     * 创建XPath<br>
     *
     * @return {@link XPath}
     */
    public static XPath createXPath() {
        return XPathFactory.newInstance().newXPath();
    }

    /**
     * 去除XML文本中的无效字符
     *
     * @param xmlContent XML文本
     * @return 当传入为null时返回null
     */
    public static String cleanInvalid(String xmlContent) {
        if (xmlContent == null) {
            return null;
        }
        return xmlContent.replaceAll(INVALID_REGEX, "");
    }

    /**
     * 关闭XXE，避免漏洞攻击<br>
     * see: https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
     *
     * @param dbf DocumentBuilderFactory
     * @return DocumentBuilderFactory
     */
    private static DocumentBuilderFactory disableXXE(DocumentBuilderFactory dbf) {
        String feature;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);
            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);
            // Disable external DTDs as well
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            // ignore
        }
        return dbf;
    }
}
