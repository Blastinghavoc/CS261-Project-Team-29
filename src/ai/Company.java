package footsiebot.ai;

import java.lang.Comparable;
import java.util.*;

public class Company implements Comparable<Company> {

  private String code;
  private ArrayList<IntentData> intents;
  private HashMap<AIIntent,Float[]> mapping;
  private Float intentScale;
  private Float newsScale;

  private Float newsCount;
  private Float newsAdj;

  public Company(String code, ArrayList<IntentData> intents, HashMap<AIIntent,Float[]> mapping, Float intentScale, Float newsScale, Float newsCount, Float newsAdj) {
    this.code = code;
    this.intents = intents;
    this.mapping = mapping;
    this.intentScale = intentScale;
    this.newsScale = newsScale;
    this.newsCount = newsCount;
    this.newsAdj = newsAdj;
  }
  //TOTEST
  public void decrementPriorityOfIntent(AIIntent i) {
    for(IntentData id: intents) {
      if(id.getIntent().equals(i)) {
        // TODO
        id.decrementPriority(1+ (0.5f * (id.getAdjustment())) );
        mapping.put(i,new Float[]{id.getCount(),id.getAdjustment()});
      }
    }
  }

  public String getCode() {
	return code;
  }

  public AIIntent getTopIntent() {
    // get most requested intent
    Set<Map.Entry<AIIntent,Float[]>> entrySet = mapping.entrySet();

    AIIntent currMax = AIIntent.SPOT_PRICE;
    float startMaxValue = mapping.get(AIIntent.SPOT_PRICE)[0] - mapping.get(AIIntent.SPOT_PRICE)[1];

    for(Map.Entry<AIIntent,Float[]> e: entrySet) {
      float value = e.getValue()[0] - e.getValue()[1];
      if(value > startMaxValue) currMax = e.getKey();
    }
    return currMax;
  }

  public Float getPriority() {
    float spotPriority = mapping.get(AIIntent.SPOT_PRICE)[0] - mapping.get(AIIntent.SPOT_PRICE)[1] ;
    float openingPriority = mapping.get(AIIntent.OPENING_PRICE)[0] - mapping.get(AIIntent.OPENING_PRICE)[1] ;
    float closingPriority = mapping.get(AIIntent.CLOSING_PRICE)[0] - mapping.get(AIIntent.CLOSING_PRICE)[1] ;
    float absoluteChangePriority = mapping.get(AIIntent.ABSOLUTE_CHANGE)[0] - mapping.get(AIIntent.ABSOLUTE_CHANGE)[1] ;
    float percentageChangePriority = mapping.get(AIIntent.PERCENT_CHANGE)[0] - mapping.get(AIIntent.PERCENT_CHANGE)[1];

    float newsPriority = newsCount - newsAdj;

    return intentScale * (spotPriority + openingPriority + closingPriority + absoluteChangePriority + percentageChangePriority) + newsScale * (newsPriority);
  }

  @Override
  public int compareTo(Company c) {
    Float r = c.getPriority() - this.getPriority();
    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }

}
