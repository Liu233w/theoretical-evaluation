package edu.nwpu.machunyan.theoreticalEvaluation.utils;

import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        factory.setValidating(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)));
    }
}
