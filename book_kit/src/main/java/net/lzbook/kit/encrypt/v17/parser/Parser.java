package net.lzbook.kit.encrypt.v17.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by Administrator on 15-5-27. 解析器接口
 */
public interface Parser {
    /**
     * 对Document进行元素降噪
     * 
     * @param document
     * @return
     */
    public Document denoiseElementForDoc(Document document);

    /**
     * 定位到文章正文部分
     * 
     * @param document
     * @return
     */
    public Element excavateContent(Document document);

    /**
     * 对正文元素进行无用元素降噪
     * 
     * @param contentElement
     */
    public void denioseElementForContentElement(Element contentElement);

    /**
     * 对正文元素进行无用内容和敏感词降噪
     * 
     * @param contentStr 正文内容
     * @param yellowWords 敏感词过滤规则
     * @return
     */
    public String denioseContentForContentElement(Element contentElement, String[] yellowWords);

    /**
     * 获取正文Element
     * 
     * @param document
     * @return
     */
    public String getContent(Document document, String[] yellowWords);

    /**
     * 获取正文文本（不包含Html标签）
     * 
     * @param document
     * @return
     */
    public String convertContent(Element contentElement);

}
