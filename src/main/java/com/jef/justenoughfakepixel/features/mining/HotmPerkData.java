package com.jef.justenoughfakepixel.features.mining;


public enum HotmPerkData {


    MINING_SPEED        ("Mining Speed",           50,  PowderType.MITHRIL,  3.0),
    MINING_FORTUNE      ("Mining Fortune",          50,  PowderType.MITHRIL,  3.05),
    TITANIUM_INSANIUM   ("Titanium Insanium",       50,  PowderType.MITHRIL,  3.1),
    LUCK_OF_THE_CAVE    ("Luck of the Cave",        45,  PowderType.MITHRIL,  3.07),
    EFFICIENT_MINER     ("Efficient Miner",         100, PowderType.MITHRIL,  2.6),
    QUICK_FORGE         ("Quick Forge",             20,  PowderType.MITHRIL,  3.2),


    OLD_SCHOOL          ("Old-School",              20,  PowderType.GEMSTONE, 4.0),
    PROFESSIONAL        ("Professional",            140, PowderType.GEMSTONE, 2.3),
    MOLE                ("Mole",                    200, PowderType.GEMSTONE, 2.17883),
    GEM_LOVER           ("Gem Lover",               20,  PowderType.GEMSTONE, 4.0),
    SEASONED_MINEMAN    ("Seasoned Mineman",         100, PowderType.GEMSTONE, 2.3),
    FORTUNATE_MINEMAN   ("Fortunate Mineman",        50,  PowderType.GEMSTONE, 3.2),
    BLOCKHEAD           ("Blockhead",               20,  PowderType.GEMSTONE, 4.0),
    KEEP_IT_COOL        ("Keep It Cool",            50,  PowderType.GEMSTONE, 3.07),
    LONESOME_MINER      ("Lonesome Miner",          45,  PowderType.GEMSTONE, 3.07),
    GREAT_EXPLORER      ("Great Explorer",          20,  PowderType.GEMSTONE, 4.0),
    POWDER_BUFF         ("Powder Buff",             50,  PowderType.GEMSTONE, 3.2),
    SPEEDY_MINEMAN      ("Speedy Mineman",          50,  PowderType.GEMSTONE, 3.2),
    SUBTERRANEAN_FISHER ("Subterranean Fisher",     40,  PowderType.GEMSTONE, 3.07),

    NO_STONE_UNTURNED     ("No Stone Unturned",       50,  PowderType.GLACITE, 3.05),
    STRONG_ARM            ("Strong Arm",              100, PowderType.GLACITE, 2.3),
    STEADY_HAND           ("Steady Hand",             100, PowderType.GLACITE, 2.6),
    WARM_HEART            ("Warm Heart",              50,  PowderType.GLACITE, 3.1),
    SURVEYOR              ("Surveyor",                20,  PowderType.GLACITE, 4.0),
    METAL_HEAD            ("Metal Head",              20,  PowderType.GLACITE, 4.0),
    RAGS_TO_RICHES        ("Rags to Riches",          50,  PowderType.GLACITE, 3.05),
    EAGER_ADVENTURER      ("Eager Adventurer",        100, PowderType.GLACITE, 2.3),
    CRYSTALLINE           ("Crystalline",             50,  PowderType.GLACITE, 3.3),
    GIFTS_FROM_THE_DEPARTED("Gifts from the Departed",100, PowderType.GLACITE, 2.45),
    MINING_MASTER         ("Mining Master",           10,  PowderType.GLACITE, -1), // special base: (level+7)^5
    DEAD_MANS_CHEST       ("Dead Man's Chest",        50,  PowderType.GLACITE, 3.2),
    VANGUARD_SEEKER       ("Vanguard Seeker",         50,  PowderType.GLACITE, 3.1);

    public final String guiName;
    public final int maxLevel;
    public final PowderType powderType;

    public final long totalCostMaxLevel;

    private final double exponent;

    HotmPerkData(String guiName, int maxLevel, PowderType powderType, double exponent) {
        this.guiName    = guiName;
        this.maxLevel   = maxLevel;
        this.powderType = powderType;
        this.exponent   = exponent;
        this.totalCostMaxLevel = calculateTotalCost(maxLevel);
    }

    public long cost(int level) {
        if (this == MINING_MASTER) {
            return (long) Math.pow(level + 7.0, 5.0);
        }
        return (long) Math.pow(level + 1.0, exponent);
    }

    public long calculateTotalCost(int desiredLevel) {
        long total = 0;
        for (int level = 2; level <= desiredLevel; level++) {
            total += cost(level);
        }
        return total;
    }

    public static HotmPerkData findByGuiName(String name) {
        for (HotmPerkData perk : values()) {
            if (perk.guiName.equals(name)) return perk;
        }
        return null;
    }

    public enum PowderType {
        MITHRIL ("Mithril Powder",  "§2"),
        GEMSTONE("Gemstone Powder", "§d"),
        GLACITE ("Glacite Powder",  "§b");

        public final String displayName;
        public final String color;

        PowderType(String displayName, String color) {
            this.displayName = displayName;
            this.color       = color;
        }
    }
}