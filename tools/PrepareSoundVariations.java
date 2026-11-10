import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Offline, deterministic sound design using the project's supplied Hove Audio recordings. */
class PrepareSoundVariations {
    static final int RATE=44100;
    static final Path ROOT=Path.of("src/main/resources/assets/audio/combat");
    public static void main(String[] args) throws Exception {
        for (int variant=0;variant<2;variant++) {
            double[] flesh=read("GOREStab_SwordStabGore_HoveAud_SwordCombat_"+(variant==0 ? "01" : "11")+".wav");
            double[] swing=read("WHSH_Whoosh_HoveAud_SwordCombat_"+(variant==0 ? "07" : "26")+".wav");
            double[] metal=read("WEAPSwrd_BaseMetal_HoveAud_SwordCombat_"+(variant==0 ? "03" : "06")+".wav");
            write("demon_"+variant,lowpass(pitch(crop(flesh,.5),.72+variant*.07),2100),new int[]{9,17},.10);
            double[] wet=lowpass(pitch(crop(flesh,.58),.60+variant*.07),1000);
            for (int i=0;i<wet.length;i++) wet[i]*=.75+.25*Math.sin(2*Math.PI*17*i/RATE);
            write("slime_"+variant,wet,new int[]{31,57,89},.38);
            double[] light=mix(pitch(crop(flesh,.22),1.35+variant*.1),.42,pitch(crop(swing,.3),1.25),.6);
            light=mix(light,1,highpass(pitch(crop(metal,.18),1.6),2200),.08);
            write("mystic_"+variant,light,new int[]{19,37},.12);
            double[] ghost=lowpass(pitch(swing,.78+variant*.08),4200);
            double[] reverse=ghost.clone();
            for (int i=0;i<reverse.length;i++) reverse[i]=ghost[ghost.length-1-i];
            ghost=mix(ghost,.85,reverse,.23);
            write("ghost_"+variant,ghost,new int[]{43,71,113,163,229,307,401,523,659},.24);
            Random random=new Random(801+variant);
            double[] flame=new double[(int)(RATE*.48)];
            double previousNoise=0;
            for (int i=0;i<flame.length;i++) {
                double time=(double)i/flame.length;
                double noise=random.nextDouble()*2-1;
                flame[i]=(noise*.7+previousNoise*.3)*Math.sin(Math.PI*time)*Math.exp(-time*2.5);
                if (random.nextDouble()<.004) flame[i]+=(random.nextDouble()-.5)*Math.exp(-time*3);
                previousNoise=noise;
            }
            write("fire_"+variant,mix(lowpass(flame,2300),.45,pitch(swing,.9),.65),new int[]{23,47},.12);
            double[] poison=new double[(int)(RATE*.58)];
            for (int bubble=0;bubble<9;bubble++) {
                int start=(int)(RATE*(bubble*.047+random.nextDouble()*.01));
                double base=180+random.nextDouble()*700;
                for (int j=0;j<RATE*.08 && start+j<poison.length;j++) {
                    double t=(double)j/RATE;
                    poison[start+j]+=Math.sin(2*Math.PI*(base*t-900*t*t))*Math.exp(-t*55)*.2;
                }
            }
            write("poison_"+variant,mix(poison,1,pitch(crop(wet,.25),1.35),.3),new int[]{27,63},.16);
        }
    }
    static double[] read(String file) throws Exception {
        try (AudioInputStream stream=AudioSystem.getAudioInputStream(ROOT.resolve(file).toFile())) {
            if (stream.getFormat().getSampleSizeInBits()!=16 || stream.getFormat().getChannels()!=2)
                throw new IllegalArgumentException("Expected stereo PCM16: "+file);
            byte[] bytes=stream.readAllBytes(); double[] samples=new double[bytes.length/4];
            for (int i=0;i<samples.length;i++) {
                short left=(short)((bytes[i*4]&255)|(bytes[i*4+1]<<8));
                short right=(short)((bytes[i*4+2]&255)|(bytes[i*4+3]<<8));
                samples[i]=(left+right)/65536.0;
            }
            return samples;
        }
    }
    static double[] crop(double[] input,double seconds) { return Arrays.copyOf(input,Math.min(input.length,(int)(seconds*RATE))); }
    static double[] pitch(double[] input,double rate) {
        double[] output=new double[(int)(input.length/rate)];
        for (int i=0;i<output.length;i++) {
            double pos=i*rate; int a=(int)pos,b=Math.min(input.length-1,a+1);
            output[i]=input[a]+(input[b]-input[a])*(pos-a);
        }
        return output;
    }
    static double[] lowpass(double[] input,double cutoff) {
        double[] output=new double[input.length]; double value=0,alpha=1-Math.exp(-2*Math.PI*cutoff/RATE);
        for (int i=0;i<input.length;i++) { value+=alpha*(input[i]-value); output[i]=value; }
        return output;
    }
    static double[] highpass(double[] input,double cutoff) {
        double[] low=lowpass(input,cutoff),output=input.clone();
        for (int i=0;i<output.length;i++) output[i]-=low[i]; return output;
    }
    static double[] mix(double[] a,double ga,double[] b,double gb) {
        double[] out=new double[Math.max(a.length,b.length)];
        for (int i=0;i<out.length;i++) out[i]=(i<a.length ? a[i]*ga : 0)+(i<b.length ? b[i]*gb : 0);
        return out;
    }
    static void write(String name,double[] dry,int[] delays,double wet) throws Exception {
        int tail=delays[delays.length-1]*RATE/1000+RATE/12;
        double[][] stereo={new double[dry.length+tail],new double[dry.length+tail]};
        for (int channel=0;channel<2;channel++) {
            System.arraycopy(dry,0,stereo[channel],0,dry.length);
            for (int d=0;d<delays.length;d++) {
                int offset=(delays[d]+channel*(d%2==0 ? 7 : -5))*RATE/1000;
                double gain=wet*Math.pow(.8,d)*(channel==0 || d%2==0 ? 1 : .85);
                for (int i=0;i<dry.length;i++) stereo[channel][i+offset]+=dry[i]*gain;
            }
        }
        double peak=0;
        for (double[] channel:stereo) for (double value:channel) peak=Math.max(peak,Math.abs(value));
        byte[] pcm=new byte[stereo[0].length*4];
        for (int i=0;i<stereo[0].length;i++) {
            double fade=Math.min(1,Math.min(i/132.0,(stereo[0].length-i-1)/441.0));
            for (int channel=0;channel<2;channel++) {
                int value=(int)(stereo[channel][i]/Math.max(.0001,peak)*25000*fade);
                pcm[i*4+channel*2]=(byte)value; pcm[i*4+channel*2+1]=(byte)(value>>8);
            }
        }
        try (var stream=new AudioInputStream(new ByteArrayInputStream(pcm),new AudioFormat(RATE,16,2,true,false),stereo[0].length)) {
            Path file=ROOT.resolve(name+".wav"); AudioSystem.write(stream,AudioFileFormat.Type.WAVE,file.toFile());
            System.out.println(file.getFileName()+" ("+Files.size(file)+" bytes)");
        }
    }
}
