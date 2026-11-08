package com.gamelab.application.arena;

import java.util.List;
import static com.gamelab.application.arena.PlatformPhysics.*;

/** Uses the same platform intersections for aiming and projectile collisions. */
public final class FiringPositionPlanner {
    public record Position(double x,double y) { }
    private FiringPositionPlanner() { }

    public static boolean blocked(double x1,double y1,double x2,double y2,double radius,List<Platform> platforms) {
        double dy=y2-y1;
        for (Platform platform : platforms) {
            if (Math.abs(dy)<.000001) {
                if (Math.abs(y1-platform.y())<.001 && Math.max(x1,x2)+radius>platform.x()
                        && Math.min(x1,x2)-radius<platform.x()+platform.width()) return true;
                continue;
            }
            double t=(platform.y()-y1)/dy;
            if (t>0 && t<=1 && platform.supports(x1+(x2-x1)*t,radius)) return true;
        }
        return false;
    }

    public static boolean canShoot(double x,double feetY,double targetX,double targetY,double range,List<Platform> platforms) {
        return Math.hypot(targetX-x,targetY-feetY)<=range
                && !blocked(x,feetY-55,targetX,targetY-48,12,platforms);
    }

    public static Position find(Body body,double targetX,double targetY,double range,List<Platform> platforms) {
        Position best=null;
        double bestCost=Double.POSITIVE_INFINITY;
        for (int surface=-1;surface<platforms.size();surface++) {
            double y=surface<0 ? FLOOR : platforms.get(surface).y();
            double left=surface<0 ? 45 : platforms.get(surface).x()+35;
            double right=surface<0 ? 1875 : platforms.get(surface).x()+platforms.get(surface).width()-35;
            int samples=Math.max(1,(int)Math.ceil((right-left)/45));
            for (int i=0;i<=samples;i++) {
                double x=left+(right-left)*i/samples;
                if (!canShoot(x,y,targetX,targetY,range,platforms)) continue;
                double cost=Math.abs(x-body.x)+Math.abs(y-body.y)*2
                        +Math.abs(Math.abs(x-targetX)-range*.65)*.15;
                if (cost<bestCost) { bestCost=cost; best=new Position(x,y); }
            }
        }
        return best;
    }
}
