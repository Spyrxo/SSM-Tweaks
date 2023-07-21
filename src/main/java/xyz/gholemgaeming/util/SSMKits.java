package xyz.gholemgaeming.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityHorse;

public enum SSMKits {

    // legacy kits
    SKELETON,
    IRON_GOLEM,
    SPIDER,
    SLIME,
    CREEPER,
    SQUID,
    ENDERMAN,
    WOLF,
    SNOWMAN,
    MAGMA_CUBE,
    WITCH,
    ZOMBIE,
    WITHER_SKELETON,
    COW,
    SKELETON_HORSE,
    PIG,
    BLAZE,
    CHICKEN,
    GUARDIAN,
    SHEEP,
    VILLAGER,

    // reborn exclusive
    CAVE_SPIDER,
    DONKEY,
    //FAT_CREEPER,
    //BREWMASTER,
    UNDEAD_HORSEMAN,

    UNKNOWN
    ;

    //todo get kit based off current tools in owners inventory

    /** @return {@link SSMKits} based off disguised {@link Entity} and its data. **/
    public static SSMKits getKitFromEntityTypeID(Entity entity) {
        switch (EntityList.getEntityID(entity)) {
            default:
                return UNKNOWN;
            case 51: // skeleton or wither skeleton
                if (entity instanceof EntitySkeleton) {
                    return ((EntitySkeleton) entity).getSkeletonType() == 1 ? WITHER_SKELETON : SKELETON;
                }
            case 99: // iron gholem
                return IRON_GOLEM;
            case 52: // spider
                return SPIDER;
            case 55: // slime
                return SLIME;
            case 50: // creeper
                return CREEPER;
            case 94: // squid
                return SQUID;
            case 58: // enderman
                return ENDERMAN;
            case 95: // wolf
                return WOLF;
            case 97: // snowman
                return SNOWMAN;
            case 62: // magma cube
                return MAGMA_CUBE;
            case 66: // witch
                return WITCH;
            case 54: // zombie
                return ZOMBIE;
            case 92: // cow
                return COW;
            case 100: // horse variants (normal, skelly, zombie, donkey, mule)
                if (entity instanceof EntityHorse) {
                    switch (((EntityHorse) entity).getHorseType()) {
                        default:
                        case 4: // skelly horse
                            return SKELETON_HORSE;
                        case 3: // zombie horse
                            return UNDEAD_HORSEMAN;
                        case 1: // donkey
                            return DONKEY;
                    }
                }
            case 90: // pig
                return PIG;
            case 61: // blaze
                return BLAZE;
            case 93: // chicken
                return CHICKEN;
            case 68: // guardian
                return GUARDIAN;
            case 91: // sheep
                return SHEEP;
            case 120: // villager
                return VILLAGER;
            case 59: // cave spider
                return CAVE_SPIDER;
        }
    }
}
