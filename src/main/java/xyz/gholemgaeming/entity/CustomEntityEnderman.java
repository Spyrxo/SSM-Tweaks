package xyz.gholemgaeming.entity;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.world.World;

public class CustomEntityEnderman extends EntityEnderman {

    public CustomEntityEnderman(World nmsWorld) {
        super(nmsWorld);
    }

    /** This method gets called a bunch of times by the enderman parent class,
     * which makes it randomly teleport around. Returning false prevents the teleport. **/
    @Override
    protected boolean teleportRandomly() {
        return false;
    }
}
