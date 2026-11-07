package com.gamelab.application.arena;

import java.util.List;

/** One-way platforms: bodies pass upward through them and land on their tops. Coordinates use feet. */
public final class PlatformPhysics {
    public static final double FLOOR = 900, GRAVITY = 1500, JUMP_SPEED = 720;
    public record Platform(double x, double y, double width) {
        public Platform {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(width) || width <= 0)
                throw new IllegalArgumentException("Invalid platform");
        }
        public boolean supports(double center, double radius) { return center + radius > x && center - radius < x + width; }
    }
    public static final class Body {
        public double x, y, velocityY;
        public boolean grounded;
        public Body(double x, double y) { this.x = x; this.y = y; grounded = y == FLOOR; }
    }
    public void jump(Body body) {
        if (body.grounded) { body.velocityY = -JUMP_SPEED; body.grounded = false; }
    }
    public void step(Body body, double horizontalSpeed, double radius, double dt, List<Platform> platforms) {
        if (!Double.isFinite(dt) || dt < 0 || dt > .05) throw new IllegalArgumentException("Physics step must be 0–0.05 seconds");
        double previousY = body.y;
        body.x = Math.max(radius, Math.min(1920 - radius, body.x + horizontalSpeed * dt));
        body.velocityY += GRAVITY * dt;
        body.y += body.velocityY * dt;
        body.grounded = false;
        double landingY = FLOOR;
        if (body.velocityY >= 0) {
            for (Platform platform : platforms) {
                if (platform.supports(body.x, radius) && previousY <= platform.y() + .01 && body.y >= platform.y())
                    landingY = Math.min(landingY, platform.y());
            }
            if (body.y >= landingY) { body.y = landingY; body.velocityY = 0; body.grounded = true; }
        }
    }

    /** Finds a route through reachable surfaces instead of repeatedly selecting the nearest ledge. */
    public Platform nextPlatform(Body body, double targetX, double targetY, List<Platform> platforms) {
        int count=platforms.size(), floor=count, start=floor, goal=-1;
        for (int i=0;i<count;i++) {
            Platform surface=platforms.get(i);
            if (Math.abs(surface.y()-body.y)<1 && surface.supports(body.x,30)) start=i;
            if (Math.abs(surface.y()-targetY)<15 && surface.supports(targetX,0)) goal=i;
        }
        // Do not chase a transient height while the hero is in midair.
        if (goal<0 || start==goal) return null;
        double[] cost=new double[count+1];
        java.util.Arrays.fill(cost,Double.POSITIVE_INFINITY);
        int[] previous=new int[count+1]; java.util.Arrays.fill(previous,-1);
        boolean[] visited=new boolean[count+1]; cost[start]=0;
        for (int step=0;step<=count;step++) {
            int from=-1;
            for (int i=0;i<=count;i++) if (!visited[i] && (from<0 || cost[i]<cost[from])) from=i;
            if (from<0 || !Double.isFinite(cost[from]) || from==goal) break;
            visited[from]=true;
            for (int to=0;to<=count;to++) {
                if (to==from) continue;
                double distance;
                if (to==floor) distance=250; // Walk off a ledge, then reapproach on the ground.
                else {
                    Platform destination=platforms.get(to);
                    double fromY=from==floor ? FLOOR : platforms.get(from).y();
                    double rise=fromY-destination.y();
                    double discriminant=JUMP_SPEED*JUMP_SPEED-2*GRAVITY*rise;
                    if (discriminant<0) continue;
                    if (from!=floor) {
                        Platform origin=platforms.get(from);
                        double gap=Math.max(destination.x()-(origin.x()+origin.width()),
                                origin.x()-(destination.x()+destination.width()));
                        double flight=(JUMP_SPEED+Math.sqrt(discriminant))/GRAVITY;
                        if (gap>280*flight-15) continue;
                    }
                    double fromX=from==floor ? body.x : platforms.get(from).x()+platforms.get(from).width()/2;
                    distance=Math.abs(destination.x()+destination.width()/2-fromX)+180;
                }
                if (cost[from]+distance<cost[to]) { cost[to]=cost[from]+distance; previous[to]=from; }
            }
        }
        if (!Double.isFinite(cost[goal])) return null;
        int next=goal;
        while (previous[next]!=start && previous[next]>=0) next=previous[next];
        return next==floor ? null : platforms.get(next);
    }
}
