package com.gamelab.domain.progression;

import java.util.List;

/** Campaign stage targets and clear rewards from the GDD. */
public final class CampaignBalance {
    public record Stage(int number, int enemyCount, int experience, int expectedExitLevel,
                        int statPointReward, int skillPointReward, boolean boss) { }
    private static final List<Stage> STAGES=List.of(
        new Stage(1,20,414,3,3,1,false), new Stage(2,24,674,5,3,1,false),
        new Stage(3,28,1015,7,3,1,false), new Stage(4,30,1344,8,2,1,false),
        new Stage(5,32,1704,10,3,1,false), new Stage(6,35,3502,13,4,1,true));
    private CampaignBalance(){}
    public static List<Stage> stages(){return STAGES;}
    public static Stage stage(int number){if(number<1||number>STAGES.size())throw new IllegalArgumentException("Stage must be 1–6");return STAGES.get(number-1);}
    public static int clearStatReward(Difficulty difficulty){return difficulty==Difficulty.HARD?4:3;}
    public static int clearSkillReward(Difficulty difficulty){return difficulty==Difficulty.HARD?2:1;}
    public static int respecGold(Difficulty difficulty, boolean firstRespec){return firstRespec?0:switch(difficulty){case EASY->50;case NORMAL->80;case HARD->150;};}
}
