package dev.casz.aquarium;

/**
 * Stable client-preview motion for the tiny Mobitat residents.
 *
 * Only positions in the renderer are changed. No Mobitat inventory data,
 * world entity, entity AI, or collision boxes are ever modified by this class.
 * A resident advances roughly 1/10 the distance of a normal walking mob
 * each tick to match the Mobitat's 10% visual scale.
 */
public final class MobitatPreviewMovement {
    public static final float SCALE = .10f;
    private static final double TWO_PI = Math.PI * 2;

    public record Position(float x, float y, float z, float vx, float vz) {
        public float horizontalSpeed() {
            return (float)Math.hypot(vx, vz);
        }
    }

    private MobitatPreviewMovement() {}

    /**
     * Produces a smooth, looping but non-circular wander around the interior.
     * Different slots follow different phases and routes, without teleporting.
     *
     * @param time client world time in ticks, including partial ticks
     * @param slot resident index, 0..4
     * @param seed block position hash for repeatable routes after reload
     * @param flying true for airborne or swimming mobs
     * @param width real entity bounding-box width (before preview scaling)
     * @param height real entity bounding-box height (before preview scaling)
     */
    public static Position sample(double time, int slot, long seed,
                                  boolean flying, float width, float height) {
        double phase = slot * 1.71 + Math.floorMod(seed, 37L) * .103;
        double phase2 = slot * 2.13 + Math.floorMod(seed >>> 9, 43L) * .087;

        // Reserve room for the miniature's full visual footprint and the
        // Mobitat frame. Large residents are allowed a smaller wandering area.
        double margin = Math.min(.47, Math.max(.16, width * SCALE * .5 + .105));
        double range = Math.max(.015, .5 - margin);

        double ax = time * .017 + phase;
        double bx = time * .008 + phase2;
        double az = time * .014 + phase2;
        double bz = time * .011 + phase;
        double x = .5 + range * (.79 * Math.sin(ax) + .21 * Math.sin(bx));
        double z = .5 + range * (.74 * Math.cos(az) + .26 * Math.sin(bz));
        double vx = range * (.79 * .017 * Math.cos(ax) + .21 * .008 * Math.cos(bx));
        double vz = range * (-.74 * .014 * Math.sin(az) + .26 * .011 * Math.cos(bz));

        // Ground mobs walk on the inner floor. Flying/swimming mobs explore
        // height as well, but never fly outside the miniature glass enclosure.
        double y;
        if (flying) {
            double maxBase = Math.max(.20, .88 - Math.max(.05, height * SCALE));
            double center = (.20 + maxBase) * .5;
            double amplitude = (maxBase - .20) * .5;
            y = center + amplitude * Math.sin(time * .012 + phase);
        } else {
            y = .20;
        }
        return new Position((float)x, (float)y, (float)z, (float)vx, (float)vz);
    }
}
