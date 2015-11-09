package edu.nwnu.ququzone.extractor.service;

import edu.nwnu.ququzone.extractor.result.Result;
import edu.nwnu.ququzone.extractor.result.SuccessResult;
import edu.nwnu.ququzone.extractor.utlis.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * row block extractor implement.
 *
 * @author Yang XuePing
 */
@Component("rowBlockExtractor")
public class RowBlockExtractor extends AbstractExtractor {
    @Override
    protected Result parse(Document doc) {
        return new SuccessResult(getArticleTitle(doc), getContentElement(doc).html());
    }

    public Element getContentElement(Document doc) {
        Map<Element, CountInfo> infoMap = new HashMap<>();
        clean(doc);
        computeInfo(doc.body(), infoMap);
        double maxScore = 0;
        Element content = null;
        for (Map.Entry<Element, CountInfo> entry : infoMap.entrySet()) {
            Element tag = entry.getKey();
            if (tag.tagName().equals("a") || tag == doc.body()) {
                continue;
            }
            double score = computeScore(tag, infoMap);
            if (score > maxScore) {
                maxScore = score;
                content = tag;
            }
        }
        if (content == null) {
            throw new RuntimeException("extraction failed");
        }
        return content;
    }

    private String getArticleTitle(Document doc) {
        String title = doc.title();
        if (!StringUtils.isEmpty(title)) {
            return title;
        }
        title = doc.select("head title").text().trim();
        if (!title.isEmpty()) {
            return title;
        }
        title = doc.select("head meta[name=title]").attr("content").trim();
        if (!title.isEmpty()) {
            return title;
        }
        title = doc.select("head meta[property=og:title]").attr("content").trim();
        if (!title.isEmpty()) {
            return title;
        }
        return doc.getElementsByTag("h1").text().trim();
    }

    protected void clean(Document doc) {
        doc.select("script,noscript,style,iframe,br").remove();
    }

    protected CountInfo computeInfo(Node node, Map<Element, CountInfo> infoMap) {
        if (node instanceof Element) {
            Element tag = (Element) node;

            CountInfo countInfo = new CountInfo();
            for (Node childNode : tag.childNodes()) {
                CountInfo childCountInfo = computeInfo(childNode, infoMap);
                countInfo.textCount += childCountInfo.textCount;
                countInfo.linkTextCount += childCountInfo.linkTextCount;
                countInfo.tagCount += childCountInfo.tagCount;
                countInfo.linkTagCount += childCountInfo.linkTagCount;
                countInfo.leafList.addAll(childCountInfo.leafList);
                countInfo.densitySum += childCountInfo.density;
                countInfo.pCount += childCountInfo.pCount;
            }
            countInfo.tagCount++;
            String tagName = tag.tagName();
            if ("a".equals(tagName)) {
                countInfo.linkTextCount = countInfo.textCount;
                countInfo.linkTagCount++;
            }
            if ("p".equals(tagName)) {
                countInfo.pCount++;
            }

            int pureLen = countInfo.textCount - countInfo.linkTextCount;
            int len = countInfo.tagCount - countInfo.linkTagCount;
            if (pureLen == 0 || len == 0) {
                countInfo.density = 0;
            } else {
                countInfo.density = (pureLen + 0.0) / len;
            }
            infoMap.put(tag, countInfo);
            return countInfo;
        }
        if (node instanceof TextNode) {
            TextNode tn = (TextNode) node;
            CountInfo countInfo = new CountInfo();
            int len = tn.text().length();
            countInfo.textCount = len;
            countInfo.leafList.add(len);
            return countInfo;
        }
        return new CountInfo();
    }

    protected double computeScore(Element tag, Map<Element, CountInfo> infoMap) {
        CountInfo countInfo = infoMap.get(tag);
        double var = Math.sqrt(computeVar(countInfo.leafList) + 1);
        double score = Math.log(var) * countInfo.densitySum * Math.log(countInfo.textCount - countInfo.linkTextCount + 1) * Math.log10(countInfo.pCount + 2);
        return score;
    }

    private double computeVar(List<Integer> data) {
        if (data.size() == 0) return 0;

        if (data.size() == 1) {
            return data.get(0) / 2;
        }

        double sum = 0;
        for (Integer i : data) {
            sum += i;
        }
        double ave = sum / data.size();
        sum = 0;
        for (Integer i : data) {
            sum += (i - ave) * (i - ave);
        }
        sum = sum / data.size();
        return sum;
    }

    private static class CountInfo {
        int textCount = 0;
        int linkTextCount = 0;
        int tagCount = 0;
        int linkTagCount = 0;
        double density = 0;
        double densitySum = 0;
        double score = 0;
        int pCount = 0;
        List<Integer> leafList = new LinkedList<>();
    }
}
