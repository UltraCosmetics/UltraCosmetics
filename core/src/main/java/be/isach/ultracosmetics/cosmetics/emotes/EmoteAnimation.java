package be.isach.ultracosmetics.cosmetics.emotes;

import com.tcoded.folialib.wrapper.task.WrappedTask;

/**
 * Settings manager.
 *
 * @author iSach
 * @since 06-17-2016
 */
class EmoteAnimation {

    private static final int INTERVAL_BETWEEN_REPLAY = 20;

    private int ticks;
    private final int ticksPerFrame;
    private int currentFrame;
    private int intervalTick;
    private final Emote emote;
    private boolean running, up = true;

    private WrappedTask task;

    EmoteAnimation(int ticksPerFrame, Emote emote) {
        this.ticksPerFrame = ticksPerFrame;
        this.emote = emote;
        this.ticks = 0;
        this.running = false;
    }

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
        task = emote.getUltraCosmetics().getScheduler().runAtEntityTimer(emote.getPlayer(), this::run, 0, ticksPerFrame);
    }

    void stop() {
        if (!running) {
            return;
        }

        this.running = false;

        try {
            if (task != null) task.cancel();
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
