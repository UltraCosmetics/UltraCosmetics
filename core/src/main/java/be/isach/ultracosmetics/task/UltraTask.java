package be.isach.ultracosmetics.task;

import be.isach.ultracosmetics.UltraCosmeticsData;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;

public abstract class UltraTask {
    protected WrappedTask task = null;

    public abstract void run();
    public abstract void schedule();

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    protected PlatformScheduler getScheduler() {
        return UltraCosmeticsData.get().getPlugin().getScheduler();
    }
}
