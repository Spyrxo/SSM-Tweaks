package xyz.gholemgaeming.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import xyz.gholemgaeming.SSMTweaks;
import xyz.gholemgaeming.util.ClientUtil;
import xyz.gholemgaeming.util.SSMKits;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

@SideOnly(Side.CLIENT)
public class DisguisePlayerHandler {

    /** {@link SSMPlayerCustomDisguise} that have been detected and verified by the client
     * [key = disguiseEntity] [value = customDisguiseInstance] (this just checks all entities in the loaded world) **/
    private HashMap<Entity, SSMPlayerCustomDisguise> detectedPlayerDisguises = new HashMap<>();

    /** Max amount of ticks the iterator should wait before fully removing a disguise.
     * <p>
     * reason: on 1.8 versions of ssm, the games that use the "mp" style of name-tag utilise
     * the squid on the disguise entity, then an armor-stand on top of the squid. the problem is,
     * these name-tags don't always set straight away on the spawning tick of the client. so in order,
     * to prevent any potential disguise accidents where it mistakenly removes a disguise because it thought
     * the entity didn't have a custom name-tag, even though they did, and it was just delayed by a few ticks,
     * the iterator will increment until a second a passed, and only then will it remove the disguise instance.
     * that way, in case the name-tag was just sliiightly delayed, it won't matter because it will properly set.
     * **/
    private final int DELAYED_NAMETAG_CHECKING_TICKER_MAX = 50;

    /** this **/
    @SubscribeEvent
    public void onPlayerClientsideTickHandleCheckingAndGettingDisguisedPlayers(TickEvent.PlayerTickEvent e) {

        // we only care about handling this on the client only
        if (!e.side.equals(Side.CLIENT)) return;

        // if the mods disabled, we don't want to handle anything else
        if (!SSMTweaks.isModAllowedToDoItsMagic(SSMTweaks.TweaksOptions.ENTITY_RENDER_DISTANCE)) return;

        // [] players can only be loaded in 1 world at a time
        // so get the current world instance and make sure it's valid
        World currentWorldInstance = Minecraft.getMinecraft().theWorld;
        if (currentWorldInstance == null) return;

        // [!] if the game has valid disguises it has loaded, check and clean first
        // before we handle any future disguise checking via the loaded entity list
        if (!detectedPlayerDisguises.keySet().isEmpty()) {
            Iterator<Entity> disguisedEntityIterator = detectedPlayerDisguises.keySet().iterator();
            while (disguisedEntityIterator.hasNext()) {
                Entity nextDisguiseEntity = disguisedEntityIterator.next();
                SSMPlayerCustomDisguise disguiseInstance = detectedPlayerDisguises.get(nextDisguiseEntity);

                // [] removal clear
                // entity no longer exists OR cannot find a valid name-tag, assume disguise is removed or player left the server
                if (nextDisguiseEntity.isDead || disguiseInstance.delayedCheckingTicker >= DELAYED_NAMETAG_CHECKING_TICKER_MAX) {
                    disguisedEntityIterator.remove();
                    //SSMTweaks.modLogger.log(Level.INFO, "removed disguise data");
                    continue;
                }

                // [] checking for name-tag
                // if the disguise instance has a valid squid on its head
                // check and reset the delayed checking ticker
                if (nextDisguiseEntity.riddenByEntity instanceof EntitySquid && disguiseInstance.delayedCheckingTicker > 0) {
                    disguiseInstance.delayedCheckingTicker = 0;
                    //SSMTweaks.modLogger.log(Level.INFO, "reset the delayed checking ticker");
                }

                // assume the player either isn't disguised OR it's not actually a disguise OR disguise is still being constructed
                // incremented the ticker, and if it reaches max, then assume its just not a disguise and remove it from the hashmap
                else {
                    disguiseInstance.delayedCheckingTicker++;
                    //SSMTweaks.modLogger.log(Level.INFO, "incremented delayed checking ticker");
                }

                // [] updating current stats (lives, etc)
                Scoreboard worldScoreboard = SSMTweaks.getClientPlayer().getWorldScoreboard();
                if (worldScoreboard != null) {
                    for (String scoreLineLore : ClientUtil.getSidebarScoreContent(worldScoreboard)) {

                        // todo this shit ain't working, figure out why its always still 0

                        // if there is a score line that contains the players name, then that is their "stock" counter,
                        // so then we will get the integer value at the start of the string and use that as the lives value
                        if (scoreLineLore.contains(disguiseInstance.playerDisplayName)) {
                            disguiseInstance.amountOfStocks = Integer.parseInt(scoreLineLore.substring(0, 1));
                        }

                        // the scoreboard doesn't have the players name on it, so they either left or fully died,
                        // but in both cases, we can assume they aren't in the game, so reset their lives
                        else {
                            if (disguiseInstance.amountOfStocks > 0) disguiseInstance.amountOfStocks = 0;
                        }
                    }
                }
            }
        }

        for (Entity loadedEntity : currentWorldInstance.loadedEntityList) {

            // if our entity already has a valid disguise, then don't bother
            if (detectedPlayerDisguises.containsKey(loadedEntity)) continue;

            // only check for entities that have something on their head
            if (loadedEntity.riddenByEntity == null) continue;

            // only check for entities that have squids on their heads
            if (!(loadedEntity.riddenByEntity instanceof EntitySquid)) continue;

            // only check for squids that have something on their head
            Entity nametagDividerSquid = loadedEntity.riddenByEntity;
            if (nametagDividerSquid.riddenByEntity == null) continue;

            // only check for squids that have armor-stands on their heads
            if (!(nametagDividerSquid.riddenByEntity instanceof EntityArmorStand)) continue;
            EntityArmorStand nametagArmorStand = (EntityArmorStand) nametagDividerSquid.riddenByEntity;

            // found a valid disguise, add data
            detectedPlayerDisguises.put(loadedEntity, new SSMPlayerCustomDisguise(loadedEntity, nametagArmorStand));
        }
    }

    public SSMKits getDisguiseKitForPlayerEntity(EntityPlayer entityPlayer) {
        SSMPlayerCustomDisguise playerDisguise = getDisguiseForPlayerEntity(entityPlayer);
        return playerDisguise != null ? playerDisguise.smashKit : SSMKits.UNKNOWN;
    }

    public SSMPlayerCustomDisguise getDisguiseForPlayerEntity(EntityPlayer entityPlayer) {
        if (detectedPlayerDisguises.values().isEmpty() || entityPlayer == null) return null;
        Optional<SSMPlayerCustomDisguise> validDisguiseData =
                detectedPlayerDisguises.values().stream()
                        .filter(type -> type != null && type.playerDisplayName != null && entityPlayer.getName() != null && type.playerDisplayName.equalsIgnoreCase(entityPlayer.getName()))
                        .findAny();
        return validDisguiseData.orElse(null);
    }

    public HashMap<Entity, SSMPlayerCustomDisguise> getAllFoundDisguises() {
        return detectedPlayerDisguises;
    }

    public static class SSMPlayerCustomDisguise {

        /** Player display name
         * (this is set via the name-tags display name data)
         * **/
        public String playerDisplayName;

        /**
         * Player Disguise entity
         * (what entity they are disguised as in ssm; creeper, gholem, spider, etc)
         */
        public Entity disguisedEntity;

        /**
         * Disguise name-tag armorstand
         * (the entity that shows the players name-tag)
         */
        public EntityArmorStand nametagDisguiseEntity;

        /**
         * Kit type the disguise belongs too
         * (this is handled by checking the entity type ID)
         */
        public SSMKits smashKit;

        /**
         * How many lives does the player currently have
         * (this is constantly updated by the client when checking the scoreboard)
         */
        public int amountOfStocks = 0;

        /**
         * Incrementing checking ticker
         * (used for delayed checking if the disguise is invalid or altered)
         */
        public int delayedCheckingTicker = 0;

        SSMPlayerCustomDisguise(Entity disguisedEntity, EntityArmorStand nametagEntity) {
            this.disguisedEntity = disguisedEntity;
            this.nametagDisguiseEntity = nametagEntity;
            this.playerDisplayName = nametagEntity.getName();
            this.smashKit = SSMKits.getKitFromEntityTypeID(disguisedEntity);
        }
    }
}
