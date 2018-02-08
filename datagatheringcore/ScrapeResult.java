package footsiebot.datagatheringcore;

public class ScrapeResult {
    private String[] codes;
    private String[] names;
    private String[] groups;
    private Double[] prices;
    private Double[] absChange;
    private Double[] percChange;

    public ScrapeResult(String[] codes, String[] names, String[] groups, Double[] prices, Double[] absChange, Double[] percChange) {
        this.codes = codes;
        this.names = names;
        this.groups = groups;
        this.prices = prices;
        this.absChange = absChange;
        this.percChange = percChange;
    }

    public String getCode(int index) {
        return codes[index];
    }

    public String getName(int index) {
        return names[index];
    }

    public String getGroup(int index) {
        return groups[index];
    }

    public Double getPrice(int index) {
        return prices[index];
    }

    public Double getAbsChange(int index) {
        return absChange[index];
    }

    public Double getPercChange(int index) {
        return percChange[index];
    }

}
