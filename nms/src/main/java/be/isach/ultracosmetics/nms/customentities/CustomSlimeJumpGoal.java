package be.isach.ultracosmetics.nms.customentities;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class CustomSlimeJumpGoal extends Goal {
    public CustomSlimeJumpGoal() {
        ((Goal) this).setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // TODO: does this class even do anything?
        return false;
    }
}
