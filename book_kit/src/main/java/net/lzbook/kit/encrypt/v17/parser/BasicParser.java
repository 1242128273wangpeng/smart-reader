package net.lzbook.kit.encrypt.v17.parser;

import net.lzbook.kit.encrypt.v17.rating.Rating;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by Administrator on 15-5-27. 默认的通用解析器
 */
public class BasicParser implements Parser {

	@Override
	public Document denoiseElementForDoc(Document document) {
		document.getElementsByTag("script").remove();
		document.getElementsByTag("style").remove();
		document.getElementsByTag("select").remove();
		document.getElementsByTag("link").remove();
		document.getElementsByTag("input").remove();
		document.getElementsByTag("object").remove();
		document.getElementsByTag("textarea").remove();
		//69书吧，内容在table里
		//document.getElementsByTag("table").remove();
		document.getElementsByTag("ul").remove();
		document.getElementsByTag("img").remove();
		document.getElementsByTag("a").attr("href", "javascript:void(0)").remove();
		document.getElementsByAttributeValue("display", "none").remove();
		//宜搜
		document.getElementsByAttributeValueStarting("class", "foot").remove();
		document.getElementsByAttributeValue("class", "settings").remove();
		document.getElementsByAttributeValueContaining("style", "display:none").remove();
		document.getElementsByAttributeValueContaining("style", "overflow: hidden").remove();
		return document;
	}

	@Override
	public Element excavateContent(Document document) {
		Element body = document.body();
		doScoreToElement(body);
		Element maxScoreElement = getMaxScoreChild(body);
		StringBuffer buffer = new StringBuffer();
		StringBuffer pathBuffer = checkPath(maxScoreElement, buffer, document);
		String path = pathBuffer.toString();
		if (path.contains(">p>")) {
			path = path.split(">p>")[0];
		}
		if (path.endsWith(">")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path.endsWith(">p")) {
			path = path.substring(0, path.length() - 2);
		}
		Elements contentElements = body.select(path);
		Element resElement = null;
		for (Element element : contentElements) {
			if(resElement != null){
				String str1 = element.text();
				String str2 = resElement.text();
				if(str1.length() > str2.length()){
					resElement = element;
				}
			}else{
				resElement = element;
			}
		}
		return resElement;
	}

	@Override
	public void denioseElementForContentElement(Element contentElement) {
		contentElement.getElementsByTag("div").remove();
		contentElement.getElementsByTag("span").remove();
	}

	@Override
	public String denioseContentForContentElement(Element contentElement, String[] yellowWords) {
		String contentStr = contentElement.html();
		String stockCodes[] = new String[] { "(正文)*", "((第[\\W]+章){1}([\\s])+([^\\x00-\\xff]*[\\w]*)+){1}",
				"([`~!@#$%^*()+=|{}'',\\[\\]\"])" };
		for (String string : stockCodes) {
			contentStr = contentStr.replaceAll(string, "");
		}
		contentElement.html(removeYellowWords(contentStr, yellowWords));
		return contentStr;
	}

	@Override
	public String getContent(Document document, String[] yellowWords) {
		denoiseElementForDoc(document);
		Element element = excavateContent(document);
		if (element == null) {
			return null;
		}
		denioseElementForContentElement(element);
		denioseContentForContentElement(element, yellowWords);
		return convertContent(element);
	}

	@Override
	public String convertContent(Element contentElement) {
		String html = contentElement.html();
		// html转义字符转换为html元素
		if (html.indexOf("&lt;") > 0 || html.indexOf("&gt;") > 0) {
			html = html.replaceAll("(&lt;)", "<");
			html = html.replaceAll("(&gt;)", ">");
			contentElement = contentElement.html(html);
		}
		// br替换
		Elements brElements = contentElement.getElementsByTag("br").after("\\n");
		if (brElements != null && brElements.size() > 0) {
			brElements.remove();
		}
		// p替换
		Elements pElements = contentElement.getElementsByTag("p");
		if (pElements != null && pElements.size() > 0) {
			html = html.replaceAll("(<p>|</p>)", "\\n");
		}
		// 保持空格长度
		html = contentElement.text().replace("&nbsp;", " ");
		return html;
	}

	protected String removeYellowWords(String contentStr, String[] yellowWords) {
		if (yellowWords != null && yellowWords.length > 0) {
			for (String yellowWord : yellowWords) {
				contentStr = contentStr.replaceAll(yellowWord, "*");
			}
		}
		return contentStr;
	}

	private int doScoreToElement(Element element) {
		Elements children = element.children();
		if (children.size() == 0) {// 不含有子节点
			return Rating.doRate(element);
		} else {// 含有子节点
			int accum = Rating.doOwnTextRate(element);
			for (Element child : children) {
				accum += doScoreToElement(child);
			}
			element.attr("score", String.valueOf(accum));
			return accum;
		}
	}

	private StringBuffer checkPath(Element element, StringBuffer accum, Document document) {
		if (element == null || element.parent() == null) {
			return accum;
		}
		if ("div".equals(element.tagName())) {
			if (element.hasAttr("id")) {
				accum.insert(0, element.tagName() + "#" + element.attr("id") + ">");
				return accum;
			}
			if (element.hasAttr("class")) {
				String classStr = element.attr("class").trim().replace(" ", ".");
				if (document.getElementsByClass(classStr).size() <= 1) {
					accum.insert(0, element.tagName() + "." + element.attr("class") + ">");
					return accum;
				}
			}
		}
		if (element.parent() != null) {
			Element parentElement = element.parent();

			String tagStr = parentElement.tagName();
			// 如果能够找到带有ID属性的父节点就停止查找
			if (parentElement.hasAttr("id")) {
				accum.insert(0, tagStr + "#" + parentElement.attr("id") + ">");
			} else if (parentElement.hasAttr("class")) {
				String classStr = parentElement.attr("class").trim().replace(" ", ".");
				/**
				 * 不考虑P标签的class属性
				 */
				if ("p".equals(tagStr)) {
					classStr = "";
				}
				if (!"".equals(classStr)) {
					accum.insert(0, tagStr + "." + classStr + ">");
				} else {
					accum.insert(0, tagStr + ">");
				}
				/**
				 * 判断class是否唯一 如果是P标签，则往上查找
				 */
				if ("p".equals(tagStr) || document.getElementsByClass(classStr).size() > 1) {
					accum = checkPath(element.parent(), accum, document);
				} else {
					return accum;
				}
			} else {
				accum.insert(0, tagStr + ">");
				if (!"body".equals(tagStr)) {
					accum = checkPath(element.parent(), accum, document);
				}
			}
		}
		return accum;
	}

	public Element getMaxScoreChild(Element element) {
		if (element.childNodeSize() == 0) {
			return element;
		}
		Elements children = element.children();
		if (children == null || children.size() == 0) {
			return element;
		}
		Element maxScoreElement = children.first();
		int score = 0;
		for (Element e : children) {
			String strScore = e.attr("score");
			if (strScore == null) {
				continue;
			}
			if (Integer.valueOf(strScore) > score) {
				maxScoreElement = e;
				score = Integer.valueOf(strScore);
			}
		}
		return getMaxScoreChild(maxScoreElement);
	}

}
