package xyz.gholemgaeming.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import xyz.gholemgaeming.SSMTweaks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class LongDistanceEntityHandler {

    /** List of entities that have been modified by the handler
     * <p>
     * (these entities will not get handled more than one time, and they
     * are only removed from the list if they become invalid or are removed)
     * **/
    private List<Entity> modifiedEntities = new ArrayList<>();

    /** Event handler called every tick by the player's client. Whenever the
     * tick is handled (20 ticks every 1 second) the game will check all the loaded
     * entities within the current world instance for the player. It will attempt
     * to change the renderWeight for allowed entities, causing them to be seen
     * from much farther away. Entities that are handled then get put into a
     * list, which prevents them from being handled multiple times.
     * <p>
     * After all entities are looped through, the modified list is then iterated over
     * and all entity instances within are checked to see if they are invalid or dead,
     * and if so, remove them from the list, to prevent any memory leaks. **/
    @SubscribeEvent
    public void onPlayerClientsideTickHandleTracking(TickEvent.PlayerTickEvent e) {

        // we only care about handling this on the client only
        if (!e.side.equals(Side.CLIENT)) return;

        // if the mods disabled, we don't want to handle anything else
        if (!SSMTweaks.isModAllowedToDoItsMagic(SSMTweaks.TweaksOptions.ENTITY_RENDER_DISTANCE)) return;

        // [] players can only be loaded in 1 world at a time
        // so get the current world instance and make sure it's valid
        World currentWorldInstance = Minecraft.getMinecraft().theWorld;
        if (currentWorldInstance == null) return;

        // [] loop through all current loaded entities in the world instance
        // we are going to check and see if we can modify their render distance value
        for (Entity loadedEntity : currentWorldInstance.loadedEntityList) {

            // if the entity isn't one of the ones we want to customise the render for
            // then don't bother continuing with logic, just skip to the next one
            if (!isEntityAllowedToBeModified(loadedEntity)) continue;

            // if our entities is in this list, it means its already been modified
            // so fuck that shit and just skip to the next one
            if (modifiedEntities.contains(loadedEntity)) continue;

            // [!] modify the render distance weight (boundingBoxAverageLength * 64.0d * renderDistWeight)
            // [] then add the entity to the modified list, so it won't be modified again
            loadedEntity.renderDistanceWeight = SSMTweaks.customRenderDistanceWeight;
            modifiedEntities.add(loadedEntity);

            if (SSMTweaks.debugMode) {
                SSMTweaks.modLogger.log(Level.INFO,
                        "Render distance value changed for entity: " + EntityList.getEntityString(loadedEntity) + ", (" + loadedEntity.getEntityId() + ")");
            }
        }

        // [] we must iterate because the mod could already be utilising the arraylist in mem
        // so to avoid any concurrent modification ur mum exceptions, just use the iterator
        Iterator<Entity> existingModifiedEntities = modifiedEntities.iterator();
        while (existingModifiedEntities.hasNext()) {
            Entity nextEntity = existingModifiedEntities.next();

            // entity is no longer valid or removed, we can remove it from the list!
            if (nextEntity == null || nextEntity.isDead) {
                existingModifiedEntities.remove();
            }
        }
    }

    /** Event handler called whenever the client player swaps between dimensions or in
     * most cases different worlds. Because entities are handled by a per world instance,
     * we want to clear the modified entities list to prevent any memory leaks from occurring. **/
    @SubscribeEvent
    public void onPlayerClientsideChangedWorldClearList(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.isCanceled()) return;
        modifiedEntities.clear();
    }

    /** @return <p>True</p> if the {@link Entity} in the constructor
     * is allowed to be modified by our custom render distance handler. **/
    private boolean isEntityAllowedToBeModified(Entity entity) {
        switch (EntityList.getEntityID(entity)) {
            default:
                return false;
            case 1: // item
            case 7: // egg
            case 10: // arrow
            case 11: // snowball
            case 12: // fireball
            case 13: // small fireball
            case 14: // enderpearl
            case 16: // potion
            case 19: // wither skull
            case 21: // falling block
            case 50: // creeper
            case 51: // skeleton
            case 52: // spider
            case 53: // giant
            case 54: // zombie
            case 55: // slime
            case 56: // ghast
            case 57: // zombie pigman
            case 58: // enderman
            case 59: // cave spider
            case 60: // silverfish
            case 61: // blaze
            case 62: // magma cube
            case 65: // bat
            case 66: // witch
            case 67: // endermite
            case 68: // guardian
            case 90: // pig
            case 91: // shemp
            case 92: // cow
            case 93: // chicken
            case 94: // squid
            case 95: // wolf
            case 96: // mooshroom
            case 97: // snowman
            case 98: // ocelot
            case 99: // iron gholem
            case 100: // horse
            case 101: // rabbit
            case 120: // villager
                return true;
        }
    }
}
