package footsiebot.datagatheringcore;

public class ScrapeResult {
    private String[] codes;
    private String[] names;
    private String[] groups;
    private double[] prices;
    private double[] absChange;
    private double[] percChange;

    public ScrapeResult(String[] codes, String[] names, String[] groups, double[] prices, double[] absChange, double[] percChange) {
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

    public double getPrice(int index) {
        return prices[index];
    }

    public double getAbsChange(int index) {
        return absChange[index];
    }

    public double getPercChange(int index) {
        return percChange[index];
    }

}
