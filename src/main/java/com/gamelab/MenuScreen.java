package com.gamelab;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Shadowfall main menu on a fixed 1920x1080 canvas (Main scales it to the window).
 *
 * Layers, bottom to top:
 *   menu_bg.png      clean artwork (no logo, no buttons, no twinkling stars, no loose embers)
 *   effects canvas   stars, castle windows, lantern glow, lake shimmer, embers, lantern sparks
 *   logo.png         separate logo + a moving shine
 *   MenuButton x4    identical buttons with mouse / keyboard focus
 */
public final class MenuScreen extends Pane {

    public static final double W = 1920;
    public static final double H = 1080;

    // ---- layout measured from the reference (1920x1080 pixels)
    private static final double LOGO_X = 533.97;
    private static final double LOGO_Y = 180.29;
    private static final double FRAME_X = 756.75;
    private static final double[] BTN_Y = {434.07, 535.12, 636.17, 737.22};
    private static final double[][] LABEL_OFF = {{115.98, 26.41}, {121.72, 26.41}, {122.87, 27.56}, {166.51, 28.71}};
    private static final double GLOW_DX = -10.33;
    private static final double GLOW_DY = -8.04;
    private static final String[] LABELS = {"new", "continue", "settings", "exit"};

    private static final double LANTERN_X = 174.3;
    private static final double LANTERN_Y = 761.8;
    private static final double MOON_X = 1653;
    private static final double MOON_Y = 172;
    private static final double SHIMMER_X = 1275;

    /** Lit castle windows: x, y, w, h. */
    private static final int[][] WINDOWS = {
            {1709, 385, 2, 11}, {1673, 387, 2, 9}, {1700, 386, 3, 10}, {1700, 423, 5, 8},
            {1706, 423, 5, 8}, {1700, 434, 5, 11}, {1708, 434, 5, 11}, {1656, 439, 2, 7},
            {1656, 447, 2, 5}, {1766, 454, 1, 7}, {1781, 495, 3, 20}, {1768, 496, 3, 18},
            {1794, 497, 3, 17}, {1586, 522, 3, 5}, {1577, 529, 3, 14}, {1587, 528, 2, 14},
            {1611, 539, 2, 11}};

    private final Random rnd = new Random();
    private final Canvas fx = new Canvas(W, H);
    private final MenuButton[] buttons = new MenuButton[4];
    private int focus = 0;

    private Image atlas;
    private final List<Sprite> stars = new ArrayList<>();
    private final List<Sprite> embers = new ArrayList<>();
    private final List<Spark> sparks = new ArrayList<>();

    private ImageView shine;
    private final Polygon shineClip = new Polygon();
    private double logoW;
    private double logoH;

    private double time;
    private long lastNanos;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (lastNanos == 0) {
                lastNanos = now;
            }
            double dt = Math.min(0.1, (now - lastNanos) / 1_000_000_000.0);
            lastNanos = now;
            time += dt;
            updateSparks(dt);
            drawEffects();
            updateShine();
            for (MenuButton b : buttons) {
                b.update(dt, time);
            }
        }
    };

    // ------------------------------------------------------------- setup

    public MenuScreen() {
        setPrefSize(W, H);
        setMinSize(W, H);
        setMaxSize(W, H);
        setClip(new Rectangle(W, H));
        setStyle("-fx-background-color: black;");

        ImageView bg = new ImageView(require("/assets/menu_bg.png"));
        bg.setMouseTransparent(true);

        atlas = require("/assets/fx_atlas.png");
        loadSprites();
        fx.setBlendMode(BlendMode.ADD);
        fx.setMouseTransparent(true);

        Image logoImg = require("/assets/logo.png");
        ImageView logo = new ImageView(logoImg);
        logo.setLayoutX(LOGO_X);
        logo.setLayoutY(LOGO_Y);
        logo.setMouseTransparent(true);
        logoW = logoImg.getWidth();
        logoH = logoImg.getHeight();

        shine = new ImageView(require("/assets/logo_shine.png"));
        shine.setLayoutX(LOGO_X);
        shine.setLayoutY(LOGO_Y);
        shine.setBlendMode(BlendMode.ADD);
        shine.setClip(shineClip);
        shine.setVisible(false);
        shine.setMouseTransparent(true);

        getChildren().addAll(bg, fx, logo, shine);

        Image frame = require("/assets/button_frame.png");
        Image glow = require("/assets/button_glow.png");
        for (int i = 0; i < 4; i++) {
            final int index = i;
            MenuButton b = new MenuButton(frame, require("/assets/label_" + LABELS[i] + ".png"), glow,
                    LABEL_OFF[i][0], LABEL_OFF[i][1], GLOW_DX, GLOW_DY);
            b.setLayoutX(FRAME_X);
            b.setLayoutY(BTN_Y[i]);
            b.setOnHover(() -> setFocus(index));
            buttons[i] = b;
            getChildren().add(b);
        }
        setFocus(0);
        timer.start();
    }

    // --------------------------------------------------------- public API

    public void setOnNewGame(Runnable r) {
        buttons[0].setOnAction(r);
    }

    public void setOnContinue(Runnable r) {
        buttons[1].setOnAction(r);
    }

    public void setOnSettings(Runnable r) {
        buttons[2].setOnAction(r);
    }

    public void setOnExit(Runnable r) {
        buttons[3].setOnAction(r);
    }

    /** When false, CONTINUE is dimmed and cannot be selected (for example: no save yet). */
    public void setContinueEnabled(boolean value) {
        buttons[1].setEnabled(value);
        if (!value && focus == 1) {
            setFocus(0);
        }
    }

    public void stop() {
        timer.stop();
    }

    public void handleKey(KeyCode code) {
        switch (code) {
            case UP:
            case W:
                moveFocus(-1);
                break;
            case DOWN:
            case S:
                moveFocus(1);
                break;
            case ENTER:
            case SPACE:
                buttons[focus].fire();
                break;
            default:
                break;
        }
    }

    private void setFocus(int index) {
        focus = index;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setButtonFocused(i == index);
        }
    }

    private void moveFocus(int dir) {
        int i = focus;
        for (int k = 0; k < buttons.length; k++) {
            i = (i + dir + buttons.length) % buttons.length;
            if (buttons[i].isButtonEnabled()) {
                setFocus(i);
                return;
            }
        }
    }

    // ----------------------------------------------------------- sprites

    private static final class Sprite {
        final int sx;
        final int sy;
        final int w;
        final int h;
        double phase;
        double rate;
        double period;
        double travel;
        double t;

        Sprite(int sx, int sy, int w, int h) {
            this.sx = sx;
            this.sy = sy;
            this.w = w;
            this.h = h;
        }
    }

    private static final class Spark {
        double x;
        double y;
        double vx;
        double vy;
        double phase;
        double life;
        double max;
    }

    private void loadSprites() {
        try (InputStream in = MenuScreen.class.getResourceAsStream("/assets/fx_sprites.txt")) {
            if (in == null) {
                System.err.println("[Shadowfall] fx_sprites.txt not found, stars and embers are disabled");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.trim().split("\\s+");
                if (p.length != 5) {
                    continue;
                }
                Sprite s = new Sprite(Integer.parseInt(p[1]), Integer.parseInt(p[2]),
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]));
                s.phase = rnd.nextDouble() * Math.PI * 2;
                if (p[0].equals("star")) {
                    s.rate = 0.8 + rnd.nextDouble() * 1.6;
                    stars.add(s);
                } else {
                    s.period = 6 + rnd.nextDouble() * 7;
                    s.travel = 90 + rnd.nextDouble() * 150;
                    s.t = rnd.nextDouble() * s.period;
                    embers.add(s);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[Shadowfall] could not read fx_sprites.txt: " + e);
        }
    }

    private static Image require(String path) {
        URL url = MenuScreen.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Missing resource " + path
                    + " (copy the files from the assets folder to src/main/resources/assets/)");
        }
        return new Image(url.toExternalForm());
    }

    // ------------------------------------------------------------ update

    private void updateSparks(double dt) {
        // lantern sparks: ~22 per second
        if (sparks.size() < 90 && rnd.nextDouble() < dt * 22) {
            Spark s = new Spark();
            s.x = LANTERN_X + (rnd.nextDouble() - 0.5) * 14;
            s.y = LANTERN_Y - 26 + rnd.nextDouble() * 8;
            s.vx = (rnd.nextDouble() - 0.35) * 20;
            s.vy = -(35 + rnd.nextDouble() * 60);
            s.phase = rnd.nextDouble() * 6.28;
            s.max = 1.3 + rnd.nextDouble() * 2.4;
            s.life = s.max;
            sparks.add(s);
        }
        for (int i = sparks.size() - 1; i >= 0; i--) {
            Spark s = sparks.get(i);
            s.x += (s.vx + Math.sin(time * 3 + s.phase) * 16) * dt;
            s.y += s.vy * dt;
            s.life -= dt;
            if (s.life <= 0) {
                sparks.remove(i);
            }
        }
        for (Sprite e : embers) {
            e.t += dt;
            if (e.t > e.period) {
                e.t -= e.period;
            }
        }
    }

    private void updateShine() {
        double cycle = time % 5.0;
        if (cycle < 1.0) {
            double x = -120 + cycle * (logoW + 240);
            shineClip.getPoints().setAll(x, 0.0, x + 70, 0.0, x + 30, logoH, x - 40, logoH);
            shine.setOpacity(0.55);
            shine.setVisible(true);
        } else {
            shine.setVisible(false);
        }
    }

    // -------------------------------------------------------------- draw

    private void drawEffects() {
        GraphicsContext g = fx.getGraphicsContext2D();
        g.setGlobalAlpha(1);
        g.setGlobalBlendMode(BlendMode.SRC_OVER);
        g.clearRect(0, 0, W, H);
        g.setGlobalBlendMode(BlendMode.ADD);

        // moon breathing
        double mr = 190;
        double ma = 0.07 + 0.03 * Math.sin(time * 0.7);
        g.setFill(new RadialGradient(0, 0, MOON_X, MOON_Y, mr, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(170, 120, 255, ma)), new Stop(1, Color.rgb(170, 120, 255, 0))));
        g.fillOval(MOON_X - mr, MOON_Y - mr, 2 * mr, 2 * mr);

        // twinkling stars
        for (Sprite s : stars) {
            double v = 0.5 + 0.5 * Math.sin(time * s.rate + s.phase);
            g.setGlobalAlpha(0.2 + 0.8 * v * v);
            g.drawImage(atlas, s.sx, s.sy, s.w, s.h, s.sx, s.sy, s.w, s.h);
        }
        g.setGlobalAlpha(1);

        // shimmering moon path on the lake
        for (int row = 0; row < 34; row++) {
            double k = row / 34.0;
            double half = 16 + k * 70 + Math.sin(time * 1.6 + row * 0.9) * 8;
            double cx = SHIMMER_X + Math.sin(time * 0.9 + row * 0.55) * (4 + k * 10);
            double a = (0.10 + 0.10 * Math.sin(time * 2.1 + row * 1.7)) * (1 - k * 0.5);
            if (a > 0.01) {
                g.setFill(Color.rgb(190, 150, 255, a));
                g.fillRect(Math.floor((cx - half) / 4) * 4, 884 + row * 6, Math.floor(half * 2 / 4) * 4, 3);
            }
        }

        // castle windows
        for (int i = 0; i < WINDOWS.length; i++) {
            int[] w = WINDOWS[i];
            double f = 0.5 + 0.5 * Math.sin(time * (3.5 + (i % 4)) + i * 1.7);
            double dip = Math.sin(time * 0.9 + i * 2.3) > 0.93 ? 0.2 : 1.0; // rare "candle gutter"
            double a = (0.10 + 0.30 * f) * dip;
            g.setFill(Color.rgb(255, 190, 80, a));
            g.fillRect(w[0], w[1], w[2], w[3]);
            double gr = 18;
            double cx = w[0] + w[2] / 2.0;
            double cy = w[1] + w[3] / 2.0;
            g.setFill(new RadialGradient(0, 0, cx, cy, gr, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.rgb(255, 170, 60, 0.10 * f * dip)), new Stop(1, Color.rgb(255, 170, 60, 0))));
            g.fillOval(cx - gr, cy - gr, 2 * gr, 2 * gr);
        }

        // lantern glow
        double flick = 0.85 + 0.15 * Math.sin(time * 9) + 0.05 * Math.sin(time * 23);
        double lr = 140 * flick;
        g.setFill(new RadialGradient(0, 0, LANTERN_X, LANTERN_Y - 10, lr, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 150, 50, 0.30 * flick)), new Stop(1, Color.rgb(255, 150, 50, 0))));
        g.fillOval(LANTERN_X - lr, LANTERN_Y - 10 - lr, 2 * lr, 2 * lr);

        // loose embers from the artwork: they rise, shrink and dissolve
        for (Sprite e : embers) {
            double u = e.t / e.period;
            double fadeIn = Math.min(1, u / 0.08);
            double fadeOut = u > 0.65 ? 1 - (u - 0.65) / 0.35 : 1;
            double a = Math.max(0, Math.min(fadeIn, fadeOut));
            if (a <= 0.02 || (u > 0.7 && rnd.nextDouble() < (u - 0.7) * 1.2)) {
                continue;
            }
            double scale = u < 0.6 ? 1 : 1 - (u - 0.6) / 0.4 * 0.55;
            double cx = e.sx + e.w / 2.0 + Math.sin(time * 0.8 + e.phase) * 14 * u;
            double cy = e.sy + e.h / 2.0 - e.travel * u;
            g.setGlobalAlpha(a);
            g.drawImage(atlas, e.sx, e.sy, e.w, e.h, cx - e.w * scale / 2, cy - e.h * scale / 2,
                    e.w * scale, e.h * scale);
        }
        g.setGlobalAlpha(1);

        // sparks flying up from the lantern: yellow -> orange -> red, shrinking and crumbling away
        for (Spark s : sparks) {
            double t = 1 - s.life / s.max;
            if (t > 0.55 && rnd.nextDouble() < (t - 0.55) * 1.3) {
                continue;
            }
            int size = t < 0.35 ? 6 : t < 0.7 ? 4 : 2;
            double a = Math.pow(1 - t, 0.7);
            Color c = t < 0.35 ? Color.rgb(255, 225, 130, a)
                    : t < 0.7 ? Color.rgb(255, 150, 50, a) : Color.rgb(210, 60, 30, a);
            g.setFill(c);
            g.fillRect(Math.floor(s.x / 2) * 2, Math.floor(s.y / 2) * 2, size, size);
        }
    }
}
