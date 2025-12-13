package be.isach.ultracosmetics.v1_21_R7.customentities;

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
