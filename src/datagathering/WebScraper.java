package footsiebot.datagatheringcore;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.lang.Double;

public class WebScraper {

    public WebScraper() {}

    public ScrapeResult scrape() {
        Document page;
        
        try {
            page = Jsoup.connect("https://arcane-citadel-48781.herokuapp.com/").get();
        } catch (IOException e) {
            return null;
        }
        
        Elements entries = page.getElementsByClass("feedEntryContent");
        String[] codes = new String[100];
        String[] names = new String[100];
        String[] groups = new String[100];
        double[] prices = new double[100];
        double[] absChange = new double[100];
        double[] percChange = new double[100];
        int i = 0;
        int j = 0;         

        for (Element entry : entries) {
            String[] content = entry.ownText().split(",");
            codes[i] = content[j++];
            names[i] = content[j++];
            // group scraping here

            if (content[j].contains(".")) prices[i] = Double.parseDouble(content[j++]);
            else {
                prices[i] = Double.parseDouble(content[j] + content[j+1]);
                j+=2;
            }
            absChange[i] = Double.parseDouble(content[j++]);
            percChange[i] = Double.parseDouble(content[j++]);
            j = 0;
            i++;
        }
        
        return new ScrapeResult(codes, names, groups, prices, absChange, percChange);
    }
}
