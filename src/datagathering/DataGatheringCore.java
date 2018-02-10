package footsiebot.datagatheringcore;

public class DataGatheringCore implements IDataGathering {
    private WebScraper webScraper;
    private NewsScraper newsScraper;

    public ScrapeResult getData() {
        return webScraper.scrape();
    }

    public Article[] getNews(String company) {
        return newsScraper.scrapeNews(company);
    }

    public Article[] getNews(String[] companies) {
        return newsScraper.scrapeNews(companies);
    }

}
