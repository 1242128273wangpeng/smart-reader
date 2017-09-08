package net.lzbook.kit.encrypt.v17;

import net.lzbook.kit.encrypt.MainExtractorInterface;
import net.lzbook.kit.encrypt.v17.parser.BasicParser;
import net.lzbook.kit.encrypt.v17.parser.JsonParser;
import net.lzbook.kit.encrypt.v17.util.NovelException;
import net.lzbook.kit.encrypt.v17.util.NovelHttpConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainExtractor implements MainExtractorInterface {
	
	@Override
	public String extract(String url) throws NovelException {
		BasicParser parser = null;
		Document doc = NovelHttpConnection.get(url);
		if(doc ==null || doc.body() ==null){
			 throw new NovelException("novel extract failed!");
		}
		String body = doc.body().html();
		if (body != null) {
			if((body.startsWith("{") && body.endsWith("}")) || (body.startsWith("[") && body.endsWith("]")) ){
				parser = new JsonParser();
			}else{
				parser = new BasicParser();
			}
			String content = parser.getContent(Jsoup.parse(body), null);
		    return content;
		} else {
		    throw new NovelException("novel extract failed!");
		}
	}
	

}
  
