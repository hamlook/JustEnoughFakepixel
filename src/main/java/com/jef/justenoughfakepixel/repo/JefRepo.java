// Credit: NotEnoughFakepixel (https://github.com/davidbelesp/NotEnoughFakepixel)

package com.jef.justenoughfakepixel.repo;

public class JefRepo {

    private static final String BASE =
            "https://raw.githubusercontent.com/hamlook/JustEnoughFakepixel/main/data/";

    public static final String KEY_UPDATE      = "update";
    public static final String KEY_PLAYERSIZES = "playersizes";
    public static final String KEY_ENCHANTS    = "enchants";

    private JefRepo() {}

    public static void init() {
        RepoHandler.register(KEY_UPDATE,      BASE + "update.json");
        RepoHandler.register(KEY_PLAYERSIZES, BASE + "playersizes.json");
        RepoHandler.register(KEY_ENCHANTS,    BASE + "enchants.json");
        RepoHandler.warmupAll();
    }
}