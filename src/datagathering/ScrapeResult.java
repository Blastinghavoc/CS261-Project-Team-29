package footsiebot.datagathering;


public class ScrapeResult {
    private String[] codes;
    private String[] names;
    private String[] groups;
    private Float[] prices;
    private Float[] absChange;
    private Float[] percChange;
    private Integer[] tradeVolume;

    public ScrapeResult(String[] codes, String[] names, String[] groups, Float[] prices, Float[] absChange, Float[] percChange, Integer[] tradeVolume) {
        this.codes = codes;
        this.names = names;
        this.groups = groups;
        this.prices = prices;
        this.absChange = absChange;
        this.percChange = percChange;
        this.tradeVolume = tradeVolume;
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

    public Float getPrice(int index) {
        return prices[index];
    }

    public Float getAbsChange(int index) {
        return absChange[index];
    }

    public Float getPercChange(int index) {
        return percChange[index];
    }

    public Integer getVolume(int index) {
        return tradeVolume[index];
    }

    public int getSize() {
        return names.length;
    }

}
