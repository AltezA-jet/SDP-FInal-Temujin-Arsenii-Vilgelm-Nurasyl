package com.gamelab.ui.audio;

import java.util.List;

public enum SoundCue {
    SWING(.50,"WHSH_Whoosh_HoveAud_SwordCombat_07.wav","WHSH_Whoosh_HoveAud_SwordCombat_26.wav"),
    FLESH(.75,"GOREStab_SwordStabGore_HoveAud_SwordCombat_01.wav","GOREStab_SwordStabGore_HoveAud_SwordCombat_11.wav","GOREStab_SwordStabGore_HoveAud_SwordCombat_17.wav"),
    HARD(.55,"WEAPSwrd_BaseMetal_HoveAud_SwordCombat_03.wav","WEAPSwrd_BaseMetal_HoveAud_SwordCombat_06.wav"),
    DEMON(.65,"demon_0.wav","demon_1.wav"),
    SLIME(.60,"slime_0.wav","slime_1.wav"),
    MYSTIC(.50,"mystic_0.wav","mystic_1.wav"),
    ETHEREAL(.45,"ghost_0.wav","ghost_1.wav"),
    FIRE(.40,"fire_0.wav","fire_1.wav"),
    POISON(.45,"poison_0.wav","poison_1.wav");

    private final double gain;
    private final List<String> files;
    SoundCue(double gain,String... files) { this.gain=gain; this.files=List.of(files); }
    public double gain() { return gain; }
    public List<String> files() { return files; }
    public static final String ROOT="/assets/audio/combat/";
}
