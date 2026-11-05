package Utils;

import java.util.Random;

// A tiny shared hub for "game feel" effects
public class Effects {
    public enum BannerStyle { RUN, BOSS, VICTORY, TOAST, SECRET, HINT }

    private static final Random random = new Random();

    private static float shakePower = 0;    // max pixels of offset
    private static int shakeTimer = 0;      // frames left

    private static BannerStyle bannerStyle;
    private static String bannerTitle, bannerSubtitle;
    private static int bannerTimer = 0, bannerLength = 1;

    private Effects() { }

    // shake the screen by up to 'power' pixels for 'frames' frames (stronger request wins)
    public static void shake(float power, int frames) {
        shakePower = Math.max(shakePower, power);
        shakeTimer = Math.max(shakeTimer, frames);
    }

    // show a big banner for 'frames' frames, replacing whatever banner was up
    public static void banner(BannerStyle style, String title, String subtitle, int frames) {
        bannerStyle = style;
        bannerTitle = title;
        bannerSubtitle = subtitle;
        bannerTimer = frames;
        bannerLength = Math.max(1, frames);
    }

    public static void reset() {
        shakePower = 0;
        shakeTimer = 0;
        bannerTimer = 0;
    }

    // called once a frame by the map: returns this frame's {x, y} shake offset and winds the shake down
    public static float[] nextShakeOffset() {
        if (shakeTimer <= 0) {
            shakePower = 0;
            return new float[] { 0, 0 };
        }
        shakeTimer--;
        float p = shakePower;
        if (shakeTimer < 8) {
            p *= shakeTimer / 8f;          // ease out
        }
        float dx = (random.nextFloat() * 2 - 1) * p;
        float dy = (random.nextFloat() * 2 - 1) * p;
        if (shakeTimer <= 0) {
            shakePower = 0;
        }
        return new float[] { dx, dy };
    }

    public static void tickBanner() {
        if (bannerTimer > 0) {
            bannerTimer--;
        }
    }

    public static boolean hasBanner() { return bannerTimer > 0; }
    public static BannerStyle getBannerStyle() { return bannerStyle; }
    public static String getBannerTitle() { return bannerTitle; }
    public static String getBannerSubtitle() { return bannerSubtitle; }
    // 0 when the banner just appeared, 1 when it's about to disappear
    public static float getBannerProgress() { return 1f - bannerTimer / (float) bannerLength; }
    public static int getBannerFramesShown() { return bannerLength - bannerTimer; }
    public static int getBannerFramesLeft() { return bannerTimer; }
}
