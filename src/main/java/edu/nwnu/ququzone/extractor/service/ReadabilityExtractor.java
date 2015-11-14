package edu.nwnu.ququzone.extractor.service;

import edu.nwnu.ququzone.extractor.result.FailureResult;
import edu.nwnu.ququzone.extractor.result.Result;
import edu.nwnu.ququzone.extractor.result.SuccessResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * readability extractor.
 *
 * @author Yang XuePing
 */
@Component("readabilityExtractor")
public class ReadabilityExtractor extends AbstractExtractor {
    private static final Map<String, Pattern> REGEXPS = new HashMap<>();

    private static final Set<String> DEFAULT_TAGS_TO_SCORE = new HashSet<>();

    private static final Set<String> DIV_TO_P_ELEMS = new HashSet<>();

    static {
        REGEXPS.put("unlikelyCandidates", Pattern.compile("banner|combx|comment|community|disqus|extra|foot|header|" +
                "menu|related|remark|rss|share|shoutbox|sidebar|skyscraper|sponsor|ad-break|agegate|pagination|pager|popup|nav|navbar"));
        REGEXPS.put("okMaybeItsACandidate", Pattern.compile("and|article|body|column|main|shadow"));
        REGEXPS.put("positive", Pattern.compile("article|body|content|entry|hentry|main|page|pagination|post|text|blog|story"));
        REGEXPS.put("negative", Pattern.compile("hidden|banner|combx|comment|com-|contact|foot|footer|footnote|masthead|media|" +
                "meta|outbrain|promo|related|scroll|share|shoutbox|sidebar|skyscraper|sponsor|shopping|tags|tool|widget"));
        REGEXPS.put("extraneous", Pattern.compile("print|archive|comment|discuss|e[\\-]?mail|share|reply|all|login|sign|single|utility"));
        REGEXPS.put("byline", Pattern.compile("byline|author|dateline|writtenby"));
        REGEXPS.put("replaceFonts", Pattern.compile("<(/?)font[^>]*>"));
        REGEXPS.put("normalize", Pattern.compile("\\s{2,}"));
        REGEXPS.put("videos", Pattern.compile("//(www\\.)?(dailymotion|youtube|youtube-nocookie|player\\.vimeo)\\.com"));
        REGEXPS.put("nextLink", Pattern.compile("(next|weiter|continue|>([^\\|]|$)|»([^\\|]|$))"));
        REGEXPS.put("prevLink", Pattern.compile("(prev|earl|old|new|<|«)"));
        REGEXPS.put("whitespace", Pattern.compile("^\\s*$"));
        REGEXPS.put("hasContent", Pattern.compile("\\S$"));

        DEFAULT_TAGS_TO_SCORE.add("section");
        DEFAULT_TAGS_TO_SCORE.add("h2");
        DEFAULT_TAGS_TO_SCORE.add("h3");
        DEFAULT_TAGS_TO_SCORE.add("h4");
        DEFAULT_TAGS_TO_SCORE.add("h5");
        DEFAULT_TAGS_TO_SCORE.add("h6");
        DEFAULT_TAGS_TO_SCORE.add("p");
        DEFAULT_TAGS_TO_SCORE.add("td");
        DEFAULT_TAGS_TO_SCORE.add("pre");

        DIV_TO_P_ELEMS.add("a");
        DIV_TO_P_ELEMS.add("blockquote");
        DIV_TO_P_ELEMS.add("dl");
        DIV_TO_P_ELEMS.add("div");
        DIV_TO_P_ELEMS.add("img");
        DIV_TO_P_ELEMS.add("ol");
        DIV_TO_P_ELEMS.add("p");
        DIV_TO_P_ELEMS.add("pre");
        DIV_TO_P_ELEMS.add("table");
        DIV_TO_P_ELEMS.add("ul");
        DIV_TO_P_ELEMS.add("select");
    }

    private static void removeElementByTag(Element element, String tag) {
        element.getElementsByTag(tag).forEach(node -> node.remove());
    }

    private static Element setNodeTag(Element element, String tag) {
        Element replacement = element.ownerDocument().createElement(tag);
        replacement.html(element.html());
        element.attributes().forEach(attr -> replacement.attr(attr.getKey(), attr.getValue()));
        element.replaceWith(replacement);
        return replacement;
    }

    private static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    private static String getArticleTitle(Document doc) {
        String title = doc.title();
        if (isEmpty(title)) {
            title = doc.select("head title").text().trim();
            if (title.isEmpty()) {
                title = doc.select("head meta[name=title]").attr("content").trim();
                if (title.isEmpty()) {
                    title = doc.select("head meta[property=og:title]").attr("content").trim();
                    if (title.isEmpty()) {
                        title = doc.getElementsByTag("h1").text();
                    }
                }
            }
        }
        return title;
    }

    private static String grabArticle(Element body) {
        List<Element> elementsToScore = new LinkedList<>();
        Element node = body;
        while (node != null) {
            String matchString = node.className() + " " + node.id();
            if (REGEXPS.get("unlikelyCandidates").matcher(matchString).find() &&
                    !REGEXPS.get("okMaybeItsACandidate").matcher(matchString).find() &&
                    !"body".equalsIgnoreCase(node.tagName()) &&
                    !"a".equalsIgnoreCase(node.tagName())) {
                node = removeAndGetNext(node);
                continue;
            }

            if (DEFAULT_TAGS_TO_SCORE.contains(node.tagName().toLowerCase())) {
                elementsToScore.add(node);
            }

            if ("div".equalsIgnoreCase(node.tagName())) {
                if (node.children().size() == 1 && "p".equalsIgnoreCase(node.child(0).tagName())) {
                    node.replaceWith(node.child(0));
                } else if (!hasChildBlockElement(node)) {
                    node = setNodeTag(node, "p");
                    elementsToScore.add(node);
                } else {
                    node.childNodes().forEach(childNode -> {
                        if (childNode instanceof TextNode) {
                            if (!"".equals(childNode.outerHtml().trim())) {
                                Element p = childNode.ownerDocument().createElement("p");
                                p.html(childNode.outerHtml());
                                childNode.replaceWith(p);
                            }
                        }
                    });
                }
            }
            node = getNextNode(node, false);
        }

        final List<Element> candidates = new LinkedList<>();
        elementsToScore.forEach(elementToScore -> {
            if (elementToScore.parent() == null || isEmpty(elementToScore.parent().tagName())) {
                return;
            }
            String innerText = getInnerText(elementToScore, true);
            if (innerText.length() < 10) {
                return;
            }
            List<Element> ancestors = getNodeAncestors(elementToScore, 3);
            if (ancestors.size() == 0) {
                return;
            }
            int contentScore = 0;
            contentScore += 1;
            contentScore += innerText.split(",").length;
            contentScore += Math.min(innerText.length() / 100, 3);

            // TODO
        });

        return "";
    }

    private static List<Element> getNodeAncestors(Element node, int maxDepth) {
        int i = 0;
        List<Element> ancestors = new LinkedList<>();
        while (node.parent() != null) {
            ancestors.add(node.parent());
            if (++i == maxDepth)
                break;
            node = node.parent();
        }
        return ancestors;
    }

    private static String getInnerText(Element node, boolean normalizeSpaces) {
        String textContent = node.text().trim();
        return normalizeSpaces ? textContent.replaceAll(REGEXPS.get("normalize").pattern(), " ") : textContent;
    }

    private static boolean hasChildBlockElement(Element node) {
        return node.children().stream().anyMatch(child ->
                DIV_TO_P_ELEMS.contains(child.tagName().toLowerCase()) || hasChildBlockElement(child));
    }

    private static Element removeAndGetNext(Element node) {
        Element nextNode = getNextNode(node, true);
        node.remove();
        return nextNode;
    }

    private static Element getNextNode(Element node, boolean ignoreSelfAndKids) {
        if (!ignoreSelfAndKids && node.children().size() > 0) {
            return node.child(0);
        }
        if (node.nextElementSibling() != null) {
            return node.nextElementSibling();
        }
        do {
            node = node.parent();
        } while (node != null && node.nextElementSibling() == null);
        return node == null ? null : node.nextElementSibling();
    }

    @Override
    protected Result parse(Document doc) {
        removeElementByTag(doc, "script");
        removeElementByTag(doc, "noscript");

        removeElementByTag(doc, "style");
        doc.body().getElementsByTag("font").forEach(font -> setNodeTag(font, "span"));

        String articleTitle = getArticleTitle(doc);
        String articleContent = grabArticle(doc.body());

        if (articleContent != null) {
            return new SuccessResult(articleTitle, articleContent);
        }

        return new FailureResult("Can't extract main content");
    }
}
