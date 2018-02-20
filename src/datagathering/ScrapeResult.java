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

<<<<<<< HEAD
    public Integer getVolume(int index) {
        return tradeVolume[index];
    }
=======
>>>>>>> 0a17c9612f8ac823a5f8aa64f63647bbf7de7af6

    public int getSize() {
        return names.length;
    }

    public Boolean equals(ScrapeResult sr){
        Boolean areEqual = true;
        if(!this.codes.equals(sr.codes)){
            areEqual = false;
        }
        if(!this.names.equals(sr.names)){
            areEqual = false;
        }
        if(!this.groups.equals(sr.groups)){
            areEqual = false;
        }
        if(!this.prices.equals(sr.prices)){
            areEqual = false;
        }
        if(!this.absChange.equals(sr.absChange)){
            areEqual = false;
        }
        if(!this.percChange.equals(sr.percChange)){
            areEqual = false;
        }
        return areEqual;

    }

}
