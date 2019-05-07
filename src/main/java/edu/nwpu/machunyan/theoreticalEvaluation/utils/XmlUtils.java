package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class XmlUtils {

    /**
     * 从文本格式的 xml 文档生成 w3c 的文档对象
     *
     * @param document
     * @return
     * @throws SAXException
     */
    @SneakyThrows({ParserConfigurationException.class, IOException.class}) // 不太可能会发生的异常
    public static Document resolveDocumentFromString(String document)
        throws SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        // 移除 dtd validation
        factory.setValidating(false);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new InputSource(new StringReader(document)));
    }
}
