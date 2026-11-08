import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;

/** Reproducible event sounds: supplied Hove Audio grunts/air layers plus original synthesized cues. */
class PrepareEventSounds {
    static final int RATE=44100;
    static final Path ROOT=Path.of("src/main/resources/assets/audio/combat");
    public static void main(String[] args) throws Exception {
        try(var zip=new ZipFile("../Sword Combat Sound Effects Pack FREE VERSION.zip")) {
            for(int i=0;i<2;i++) {
                String number=i==0?"01":"13";
                var entry=zip.getEntry("Sword Combat Sound Effects Pack FREE VERSION/Voicelines/Damage Grunts/VOXScrm_DamageGrunt_HoveAudio_SwordCombat_"+number+".wav");
                try(var input=zip.getInputStream(entry)) { write("player_hurt_"+i,trim(read(input),1)); }
            }
            var entry=zip.getEntry("Sword Combat Sound Effects Pack FREE VERSION/Voicelines/Damage Grunts/VOXScrm_DamageGrunt_HoveAudio_SwordCombat_27.wav");
            try(var input=zip.getInputStream(entry)) { write("player_death",pitch(trim(read(input),1.3),.86)); }
        }
        double[] air;
        try(var input=Files.newInputStream(ROOT.resolve("WHSH_Whoosh_HoveAud_SwordCombat_07.wav"))) { air=read(input); }
        write("jump",mix(pitch(trim(air,.14),1.5),.3,notes(.16,180,260),.14));
        write("dodge",pitch(trim(air,.23),1.2));
        write("land",thud(.12,95));
        write("enemy_down",thud(.22,62));
        write("boss_down",notes(.22,146.83,110,73.42));
        write("wave_start",notes(.12,220,293.66));
        write("wave_clear",notes(.12,392,523.25));
        write("stage_clear",notes(.15,261.63,329.63,392,523.25));
        write("level_up",notes(.11,523.25,659.25,783.99));
        write("potion",notes(.10,440,659.25,880));
        write("summon",mix(pitch(trim(air,.45),.65),.65,notes(.30,110,138.59),.35));
        write("phase_change",notes(.25,110,155.56,220));
        write("stone_cast",mix(thud(.22,72),.7,pitch(trim(air,.20),.8),.3));
        write("ice_cast",mix(notes(.09,880,1174.66),.6,pitch(trim(air,.16),1.4),.4));
    }
    static double[] read(InputStream source) throws Exception {
        try(var input=AudioSystem.getAudioInputStream(new BufferedInputStream(source));
            var converted=AudioSystem.getAudioInputStream(new AudioFormat(RATE,16,2,true,false),input)) {
            byte[] bytes=converted.readAllBytes();double[] out=new double[bytes.length/4];
            for(int i=0;i<out.length;i++) out[i]=((short)((bytes[i*4]&255)|(bytes[i*4+1]<<8))+(short)((bytes[i*4+2]&255)|(bytes[i*4+3]<<8)))/65536.0;
            return out;
        }
    }
    static double[] trim(double[] input,double seconds) {
        double peak=0;for(double sample:input)peak=Math.max(peak,Math.abs(sample));
        int first=0,last=input.length-1;while(first<last && Math.abs(input[first])<peak*.02)first++;
        while(last>first && Math.abs(input[last])<peak*.02)last--;
        first=Math.max(0,first-220);last=Math.min(input.length,last+1103);
        return Arrays.copyOfRange(input,first,Math.min(last,first+(int)(RATE*seconds)));
    }
    static double[] pitch(double[] input,double speed) {
        double[] out=new double[(int)(input.length/speed)];
        for(int i=0;i<out.length;i++) {double pos=i*speed;int a=(int)pos;out[i]=input[a]+(input[Math.min(input.length-1,a+1)]-input[a])*(pos-a);}
        return out;
    }
    static double[] mix(double[] a,double ga,double[] b,double gb) {
        double[] out=new double[Math.max(a.length,b.length)];
        for(int i=0;i<out.length;i++)out[i]=(i<a.length?a[i]*ga:0)+(i<b.length?b[i]*gb:0);return out;
    }
    static double[] notes(double duration,double... frequencies) {
        int step=(int)(RATE*duration);double[] out=new double[step*frequencies.length+RATE/6];
        for(int note=0;note<frequencies.length;note++) for(int i=0;i<step+RATE/6;i++) {
            double t=(double)i/RATE,attack=Math.min(1,t/.008);
            out[note*step+i]+=attack*Math.exp(-t*13)*(Math.sin(2*Math.PI*frequencies[note]*t)+.18*Math.sin(4*Math.PI*frequencies[note]*t));
        }
        return out;
    }
    static double[] thud(double duration,double frequency) {
        var random=new Random(81);double[] out=new double[(int)(RATE*duration)];double noise=0;
        for(int i=0;i<out.length;i++) {double t=(double)i/RATE;noise+=(random.nextDouble()*2-1-noise)*.12;
            out[i]=(Math.sin(2*Math.PI*frequency*t)*.65+noise*.35)*Math.exp(-t*28);}
        return out;
    }
    static void write(String name,double[] samples) throws Exception {
        double peak=0;for(double sample:samples)peak=Math.max(peak,Math.abs(sample));
        byte[] bytes=new byte[samples.length*4];
        for(int i=0;i<samples.length;i++) {
            double fade=Math.min(1,Math.min(i/220.0,(samples.length-i-1)/441.0));
            int value=(int)(samples[i]/Math.max(.0001,peak)*24000*fade);
            for(int channel=0;channel<2;channel++){int index=i*4+channel*2;bytes[index]=(byte)value;bytes[index+1]=(byte)(value>>8);}
        }
        try(var stream=new AudioInputStream(new ByteArrayInputStream(bytes),new AudioFormat(RATE,16,2,true,false),samples.length)) {
            AudioSystem.write(stream,AudioFileFormat.Type.WAVE,ROOT.resolve(name+".wav").toFile());
        }
        System.out.println(name+".wav");
    }
}
