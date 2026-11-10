package com.gamelab.application.arena;

import java.util.List;
import static com.gamelab.application.arena.PlatformPhysics.Platform;

/** Shared stage geometry and encounter data. */
public final class ArenaLayouts {
    public record Layout(String name, String folder, String background, String ground, String medium, String small,
                         List<Platform> platforms, double platformCollisionInset) {
        public Layout(String name, String folder, String background, String ground, String medium, String small, List<Platform> platforms) { this(name, folder, background, ground, medium, small, platforms, 0); }
        public Layout { platforms = List.copyOf(platforms); }
        public String asset(String file) { return folder.startsWith("/") ? folder + "/" + file : "Background+platform/" + folder + "/" + file; }
        public double platformTextureY(Platform platform) { return platform.y() - platformCollisionInset; }
    }
    private static Platform p(double x, double y, double width) { return new Platform(x, y, width); }
    private static final List<Layout> ALL = List.of(
        new Layout("Forgotten Ruins","1_background_platform","stage1_ruins_background_1920x1080.png","Ruin_ground.png","stage1_ruins_platform_medium.png","stage1_ruins_platform_small.png",List.of(p(210,755,320),p(560,615,400),p(1120,755,310),p(1460,615,330))),
        new Layout("Ice Caves","2_background_platform","stage2_ice_background_1920x1080.png","Ruin_ice.png","stage2_ice_platform_medium.png","stage2_ice_platform_small.png",List.of(p(180,760,280),p(490,630,300),p(950,760,330),p(1350,630,340),p(1710,770,150))),
        new Layout("Burning Mines","3_background_platform","Stage3_lava_backgtound.png","stage3_lava_ground.png","stage3_lava_platform_medium.png","stage3_lava_platform_small.png",List.of(p(250,760,340),p(760,760,380),p(1330,760,340))),
        new Layout("Poison Swamps","4_background_platform","stage4_background_1920x1080.png","Stage4_ground.png","stage4_platform_medium.png","stage4_platform_small.png",List.of(p(150,795,300),p(480,665,330),p(900,785,350),p(1310,665,340),p(1680,805,170)),15),
        new Layout("Dark Castle","5_background_platform","fd0940a9-b040-47b9-8380-1019d21084a4.png","44c1c0cb-7d39-46f6-9092-ee9d8a89a899.png","030ce0fe-4eda-4449-ac58-ca41b4f941c2.png","683454fa-d241-4f01-9a5f-5f8f5388a0b8.png",List.of(p(140,755,360),p(530,615,350),p(990,755,370),p(1380,615,380))),
        new Layout("Dark Lord's Domain","/assets/All/Background+platform/Final_background_platform","5447206429547764760.jpg","wall.png","platform_1.png","platform_2.png",List.of(p(220,760,420),p(620,630,680),p(1270,760,400)))
    );
    public static Layout stage(int stage) { if(stage<1||stage>6) throw new IllegalArgumentException("Stage must be 1-6"); return ALL.get(stage-1); }
    public static List<String> wave(int stage,int wave) {
        stage(stage); if(wave<1||wave>3) throw new IllegalArgumentException("Wave must be 1-3"); if(stage==6) return List.of("shadow-lord");
        String melee=switch(stage){case 1->"soldier";case 2->"ice-beast";case 3->"demon";case 4->"slime";default->"ghost";};
        String ranged=switch(stage){case 1->"archer";case 2->"elemental";case 3->"mage";case 4->"doctor";default->"ghost-ranged";};
        if(wave==1)return List.of(melee,melee,melee); if(wave==2)return List.of(melee,ranged,melee,ranged);
        String boss=ArenaCatalog.stage(stage,true).get(0).id(); return stage==4?List.of(boss):List.of(boss,melee,melee);
    }
    private ArenaLayouts() { }
}
