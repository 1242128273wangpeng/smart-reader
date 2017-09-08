package net.lzbook.kit.encrypt.v17.parser;

import net.lzbook.kit.encrypt.v17.rating.Rating;

import org.jsoup.nodes.Document;

/**
 * Json
 */
public class JsonParser extends BasicParser {

	public JsonParser() {
	}

	@Override
	public String getContent(Document document, String[] yellowWords) {
		String html = document.html();
		document.html(html.replace("<br>", "\\n"));
		String text = document.text();
		//去除json对象标记
		text = text.replaceAll("[\\{\\}\\[\\]]", "");
		//根据json格式分组：["key":value]
		String[] contents = text.split("(\"[\\w]+\"\\s*:)");
		int maxScore = 0;
		String contentText = "";
		for (String content : contents) {
			int score = Rating.doTextRate(content);
			if (score > maxScore) {
				maxScore = score;
				contentText = content;
			}
		}
		return removeYellowWords(contentText, yellowWords);
	}
}
