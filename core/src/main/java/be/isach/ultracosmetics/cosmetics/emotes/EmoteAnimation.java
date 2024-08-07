package be.isach.ultracosmetics.cosmetics.emotes;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Settings manager.
 *
 * @author iSach
 * @since 06-17-2016
 */
class EmoteAnimation extends BukkitRunnable {

    private static final int INTERVAL_BETWEEN_REPLAY = 20;

    private int ticks;
    private final int ticksPerFrame;
    private int currentFrame;
    private int intervalTick;
    private final Emote emote;
    private boolean running, up = true;

    EmoteAnimation(int ticksPerFrame, Emote emote) {
        this.ticksPerFrame = ticksPerFrame;
        this.emote = emote;
        this.ticks = 0;
        this.running = false;
    }

    @Override
    public void run() {
        if (ticks < ticksPerFrame) {
            ticks++;
        } else {
            ticks = 0;
            updateTexture();
        }
    }

    void start() {
        this.running = true;
        runTaskTimer(emote.getUltraCosmetics(), 0, ticksPerFrame);
    }

    void stop() {
        if (!running) {
            return;
        }

        this.running = false;

        try {
            cancel();
        } catch (IllegalStateException ignored) {
            // not scheduled yet
        }
    }

    private void updateTexture() {
        if (!running) return;

        emote.setItemStack(emote.getType().getFrames().get(currentFrame));

        if (up) {
            if (currentFrame >= emote.getType().getMaxFrames() - 1) {
                up = false;
            } else {
                currentFrame++;
            }
        } else {
            if (currentFrame <= 0) {
                if (intervalTick >= INTERVAL_BETWEEN_REPLAY / ticksPerFrame) {
                    up = true;
                    intervalTick = 0;
                } else {
                    intervalTick++;
                }
            } else {
                currentFrame--;
            }
        }
    }
}
