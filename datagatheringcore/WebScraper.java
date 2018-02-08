package footsiebot.datagatheringcore;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScraper {

    public WebScraper() {}

    public ScrapeResult scrape() {
        org.w3c.dom.Document page;
        
        try {
            page = Jsoup.connect("https://arcane-citadel-48781.herokuapp.com/").get();
        } catch (IOException e) {
            return null;
        }

        Element content = page.getElementById("feedContent");
        Elements entries = content.getElementsByClass("feedEntryContent");

        for (Element entry : entries) {
            
        }
        
	    return null;
    }
}
