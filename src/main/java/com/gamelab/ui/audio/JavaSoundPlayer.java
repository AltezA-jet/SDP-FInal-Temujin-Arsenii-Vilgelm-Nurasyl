package com.gamelab.ui.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.util.*;
import java.util.concurrent.*;

/** Preloaded, overlapping WAV clips on a daemon worker; audio device failures never stop gameplay. */
public final class JavaSoundPlayer implements SoundPlayer {
    private final ExecutorService worker=Executors.newSingleThreadExecutor(task -> {
        Thread thread=new Thread(task,"shadowfall-audio"); thread.setDaemon(true); return thread;
    });
    private final Map<SoundCue,List<Clip>> clips=new EnumMap<>(SoundCue.class);
    private final Map<SoundCue,Integer> previous=new EnumMap<>(SoundCue.class);
    private double volume=.65;
    private boolean closed, warned;

    public JavaSoundPlayer() { submit(this::preload); }
    private void preload() {
        for (SoundCue cue:SoundCue.values()) {
            List<Clip> voices=new ArrayList<>();
            for (String file:cue.files()) {
                Clip clip=null;
                try {
                    var resource=getClass().getResourceAsStream(SoundCue.ROOT+file);
                    if (resource==null) throw new IllegalStateException("Missing audio: "+file);
                    try (var input=AudioSystem.getAudioInputStream(new BufferedInputStream(resource))) {
                        clip=AudioSystem.getClip(); clip.open(input); voices.add(clip);
                    }
                } catch (Exception exception) {
                    if (clip!=null) clip.close();
                    if (!warned) { warned=true; System.err.println("[Shadowfall audio] "+exception.getMessage()); }
                }
            }
            clips.put(cue,voices);
        }
    }
    private synchronized void submit(Runnable action) { if (!closed) worker.execute(action); }
    @Override public void play(SoundCue cue) {
        Objects.requireNonNull(cue);
        submit(() -> {
            List<Clip> voices=clips.get(cue);
            if (volume<=0 || voices==null || voices.isEmpty()) return;
            int last=previous.getOrDefault(cue,-1);
            List<Integer> available=new ArrayList<>();
            for (int i=0;i<voices.size();i++) if (!voices.get(i).isRunning() && i!=last) available.add(i);
            int index=available.isEmpty() ? (last+1)%voices.size() : available.get(ThreadLocalRandom.current().nextInt(available.size()));
            previous.put(cue,index);
            Clip clip=voices.get(index);
            clip.stop(); clip.setFramePosition(0); applyVolume(clip,cue); clip.start();
        });
    }
    private void applyVolume(Clip clip,SoundCue cue) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            double decibels=volume<=0 ? gain.getMinimum() : 20*Math.log10(volume*cue.gain());
            gain.setValue((float)Math.max(gain.getMinimum(),Math.min(gain.getMaximum(),decibels)));
        }
    }
    @Override public void setVolume(double value) {
        if (!Double.isFinite(value) || value<0 || value>1) throw new IllegalArgumentException("Volume must be 0–1");
        submit(() -> {
            volume=value;
            clips.forEach((cue,voices) -> voices.forEach(clip -> {
                if (volume==0) clip.stop(); else applyVolume(clip,cue);
            }));
        });
    }
    @Override public void stop() { submit(() -> clips.values().forEach(voices -> voices.forEach(Clip::stop))); }
    @Override public synchronized void close() {
        if (closed) return;
        closed=true;
        worker.execute(() -> { clips.values().forEach(voices -> voices.forEach(Clip::close)); clips.clear(); });
        worker.shutdown();
    }
}
