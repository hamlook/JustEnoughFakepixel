package com.jef.justenoughfakepixel.features.diana;

public class DianaData {

    public long activeTimeMs = 0L;

    public int totalBorrows = 0;

    public int totalMobs             = 0;
    public int mobsSinceInq          = 0;
    public int inqsSinceChimera      = 0;
    public int totalInqs             = 0;
    public int totalInqsLootshared   = 0;

    public int minotaursSinceStick = 0;
    public int totalMinotaurs      = 0;

    public int champsSinceRelic = 0;
    public int totalChamps      = 0;

    public int totalGaiaConstructs = 0;
    public int totalMinosHunters   = 0;
    public int totalSiameseLynxes  = 0;

    public int totalChimeras        = 0;
    public int totalSticks          = 0;
    public int totalRelics          = 0;
    public int dwarfTurtleShelmets  = 0;
    public int antiqueRemedies      = 0;
    public int crochetTigerPlushies = 0;

    public int  griffinFeathers = 0;
    public int  souvenirs       = 0;
    public int  crownsOfGreed   = 0;
    public long totalCoins      = 0L;

    public String getLootsharedSuffix() {
        return totalInqsLootshared > 0
                ? String.format("  \u00a77[\u00a7bLS \u00a7f%d\u00a77]", totalInqsLootshared)
                : "";
    }
}