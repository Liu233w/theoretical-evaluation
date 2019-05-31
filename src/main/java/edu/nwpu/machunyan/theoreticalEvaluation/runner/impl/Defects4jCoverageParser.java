package edu.nwpu.machunyan.theoreticalEvaluation.runner.impl;

import edu.nwpu.machunyan.theoreticalEvaluation.runner.CoverageRunnerException;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.Coverage;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementInfo;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementMap;
import edu.nwpu.machunyan.theoreticalEvaluation.runner.data.StatementMapType;
import edu.nwpu.machunyan.theoreticalEvaluation.utils.XmlUtils;
import one.util.streamex.StreamEx;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * 读取 Defects4j 的覆盖文件，获取覆盖信息。
 */
public class Defects4jCoverageParser {

    /**
     * 从字符串形式的覆盖率信息中生成对象形式的覆盖率信息。将复用并更新 statementMap 中的信息。
     *
     * @param document
     * @param statementMap
     * @return
     * @throws CoverageRunnerException
     */
    public static Coverage generateCoverageFromString(
        String document,
        Defects4jStatementMap statementMap)
        throws CoverageRunnerException {

        final Document root;
        try {
            root = XmlUtils.resolveDocumentFromString(document);
        } catch (SAXException e) {
            throw new CoverageRunnerException("Error when parsing coverage xml", e);
        }

        final Coverage coverage = new Coverage();

        final NodeList classes = root.getElementsByTagName("class");
        for (int i = 0, classesLength = classes.getLength(); i < classesLength; i++) {
            final Element clazz = (Element) classes.item(i);

            final String filename = clazz.getAttribute("filename");

            final NodeList lines = clazz.getElementsByTagName("line");
            for (int j = 0, length = lines.getLength(); j < length; j++) {
                final Element line = (Element) lines.item(j);

                final int lineNumber = Integer.parseInt(line.getAttribute("number"));
                final int hits = Integer.parseInt(line.getAttribute("hits"));

                final int statementIndex = statementMap.addOrGetStatement(filename, lineNumber).getStatementIndex();
                coverage.setCoverageForStatement(statementIndex, hits);
            }
        }

        return coverage;
    }

    /**
     * 处理 Defects4j 的语句编号
     */
    public static class Defects4jStatementMap {

        /**
         * 上一条添加的语句的编号
         */
        private int lastStatmentIndex = 0;

        /**
         * 文件名 -> 行号 -> 语句
         */
        private HashMap<String, HashMap<Integer, StatementInfo>> statementLocation
            = new HashMap<>();

        /**
         * 用来输出的语句。使用这个成员来优化时间复杂度。
         */
        // 第一个元素是 null
        private LinkedList<StatementInfo> achievedStatements = new LinkedList<>(Collections.singletonList(null));

        /**
         * 获取指定的语句对象，如果不存在，则创建
         *
         * @param filePath
         * @param lineNumber
         * @return
         */
        public StatementInfo addOrGetStatement(String filePath, int lineNumber) {

            final HashMap<Integer, StatementInfo> mp = statementLocation
                .computeIfAbsent(filePath, a -> new HashMap<>());
            if (mp.containsKey(lineNumber)) {
                return mp.get(lineNumber);
            } else {
                final StatementInfo s = new StatementInfo(++lastStatmentIndex, filePath, lineNumber, lineNumber);
                mp.put(lineNumber, s);
                achievedStatements.add(s);
                return s;
            }
        }

        /**
         * 生成一个标准格式的 statementMap
         *
         * @return
         */
        public StatementMap resolveStatementMap() {

            final ArrayList<StatementInfo> collect = StreamEx
                .of(achievedStatements)
                .collect(Collectors.toCollection(ArrayList::new));

            return new StatementMap(StatementMapType.LINE_BASED, collect);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Defects4jStatementMap)) {
                return false;
            }

            return this.achievedStatements.equals(((Defects4jStatementMap) obj).achievedStatements);
        }
    }
}
