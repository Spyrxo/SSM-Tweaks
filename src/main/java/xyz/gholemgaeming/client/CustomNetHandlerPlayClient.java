package xyz.gholemgaeming.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.server.*;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xyz.gholemgaeming.SSMTweaks;
import xyz.gholemgaeming.entity.CustomEntityArrow;
import xyz.gholemgaeming.entity.CustomEntityEnderman;

import java.lang.reflect.Field;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CustomNetHandlerPlayClient extends NetHandlerPlayClient {

    /** Minecraft-Client instance **/
    private Minecraft clientsideMinecraft;

    /** Parent {@link NetHandlerPlayClient} class, initialized whenever the
     * client does things like handshake login, server data, load worlds, etc **/
    private NetHandlerPlayClient parentInstance;

    /** {@link EntityArrow} entity type ID **/
    private final int ENTITY_ARROW_TYPE_ID = 60;

    /** {@link net.minecraft.entity.monster.EntityEnderman} entity type ID **/
    private final int ENTITY_ENDERMAN_TYPE_ID = 58;

    /** {@link CustomNetHandlerPlayClient} Constructor.
     *
     * @param parentPlayClientHandler - Initialized Forge/Minecraft Client Handler (get from existing network manager)
     * @param minecraft - Minecraft Client instance that is currently running
     * **/
    public CustomNetHandlerPlayClient(NetHandlerPlayClient parentPlayClientHandler, Minecraft minecraft) {
        super(minecraft, getGUIScreenViaReflection(parentPlayClientHandler),
                parentPlayClientHandler.getNetworkManager(), parentPlayClientHandler.getGameProfile());
        this.clientsideMinecraft = minecraft;
        this.parentInstance = parentPlayClientHandler;
    }

    /** This handler is what controls what entities to use for the client when a spawn packet
     * is received from the server, to generate an entity. Any custom entities we want to initialize
     * on-top of vanilla ones, and therefore override their functionality, we can do so within here. **/
    @Override
    public void handleSpawnObject(S0EPacketSpawnObject objectSpawnPacket) {
        PacketThreadUtil.checkThreadAndEnqueue(objectSpawnPacket, this, this.clientsideMinecraft);

        // [] if the mod isn't allowed to modify entities, then just call the
        // parent class super method and let it handle it like normal vanilla
        if (!SSMTweaks.isModAllowedToDoItsMagic(SSMTweaks.TweaksOptions.ENTITY_SPECIFIC_FIXES)) {
            this.parentInstance.handleSpawnObject(objectSpawnPacket);
            return;
        }

        // entity nms functions
        double posX = (double) objectSpawnPacket.getX() / 32.0;
        double posY = (double) objectSpawnPacket.getY() / 32.0;
        double posZ = (double) objectSpawnPacket.getZ() / 32.0;
        Entity nmsSpawnedEntity = null;

        // [] if the entity the server is attempting to tell us to spawn is an arrow
        // we are going to use our custom arrow nms class to handle all the logic
        if (objectSpawnPacket.getType() == ENTITY_ARROW_TYPE_ID) {
            nmsSpawnedEntity = new CustomEntityArrow(this.clientsideMinecraft.theWorld, posX, posY, posZ);
        }

        // [] else it's just another vanilla entity, so let the super class handle the logic
        else {
            this.parentInstance.handleSpawnObject(objectSpawnPacket);
            return;
        }

        // [!] ONLY DO THE LOGIC BELOW IF WE ARE USING OUR CUSTOM ARROW
        // (the rest of the logic is basically just copy-pasted from forge, no need to change it)
        if (nmsSpawnedEntity != null) {
            nmsSpawnedEntity.serverPosX = objectSpawnPacket.getX();
            nmsSpawnedEntity.serverPosY = objectSpawnPacket.getY();
            nmsSpawnedEntity.serverPosZ = objectSpawnPacket.getZ();
            nmsSpawnedEntity.rotationPitch = (float) (objectSpawnPacket.getPitch() * 360) / 256.0f;
            nmsSpawnedEntity.rotationYaw = (float) (objectSpawnPacket.getYaw() * 360) / 256.0f;

            Entity[] aentity = nmsSpawnedEntity.getParts();
            if (aentity != null) {
                int i = objectSpawnPacket.getEntityID() - nmsSpawnedEntity.getEntityId();

                for (int j = 0; j < aentity.length; ++j) {
                    aentity[j].setEntityId(aentity[j].getEntityId() + i);
                }
            }

            nmsSpawnedEntity.setEntityId(objectSpawnPacket.getEntityID());
            this.clientsideMinecraft.theWorld.addEntityToWorld(objectSpawnPacket.getEntityID(), nmsSpawnedEntity);

            if (objectSpawnPacket.func_149009_m() > 0) {
                if (objectSpawnPacket.getType() == ENTITY_ARROW_TYPE_ID) {
                    Entity entity2 = this.clientsideMinecraft.theWorld.getEntityByID(objectSpawnPacket.func_149009_m());
                    if (entity2 instanceof EntityLivingBase && nmsSpawnedEntity instanceof EntityArrow) {
                        ((EntityArrow)nmsSpawnedEntity).shootingEntity = entity2;
                    }
                }

                nmsSpawnedEntity.setVelocity(
                        (double) objectSpawnPacket.getSpeedX() / 8000.0d,
                        (double) objectSpawnPacket.getSpeedY() / 8000.0d,
                        (double) objectSpawnPacket.getSpeedZ() / 8000.0d);
            }
        }
    }

    /** This handler is what controls what living-entities to use for the client when a spawn packet
     * is received from the server, to generate an entity. Any custom entities we want to initialize
     * on-top of vanilla ones, and therefore override their functionality, we can do so within here. **/
    @Override
    public void handleSpawnMob(S0FPacketSpawnMob livingEntitySpawnPacket) {
        PacketThreadUtil.checkThreadAndEnqueue(livingEntitySpawnPacket, this, this.clientsideMinecraft);

        // [] if the mod isn't allowed to modify entities, then just call the
        // parent class super method and let it handle it like normal vanilla
        if (!SSMTweaks.isModAllowedToDoItsMagic(SSMTweaks.TweaksOptions.ENTITY_SPECIFIC_FIXES)) {
            this.parentInstance.handleSpawnMob(livingEntitySpawnPacket);
            return;
        }

        // [] (the rest of the logic is basically just copy-pasted from forge, no need to change it)

        double posX = livingEntitySpawnPacket.getX() / 32.0d;
        double posY = livingEntitySpawnPacket.getY() / 32.0d;
        double posZ = livingEntitySpawnPacket.getZ() / 32.0d;
        float yaw = (livingEntitySpawnPacket.getYaw() * 360) / 256.0f;
        float pitch = (livingEntitySpawnPacket.getPitch() * 360) / 256.0f;

        EntityLivingBase livingBase = this.checkAndCreateEntityByID(livingEntitySpawnPacket.getEntityType(), this.clientsideMinecraft.theWorld);
        livingBase.serverPosX = livingEntitySpawnPacket.getX();
        livingBase.serverPosY = livingEntitySpawnPacket.getY();
        livingBase.serverPosZ = livingEntitySpawnPacket.getZ();
        livingBase.renderYawOffset = livingBase.rotationYawHead = (float) (livingEntitySpawnPacket.getHeadPitch() * 360) / 256.0f;
        Entity[] entArray = livingBase.getParts();

        if (entArray != null) {
            int i = livingEntitySpawnPacket.getEntityID() - livingBase.getEntityId();
            for (int j = 0; j < entArray.length; ++j) {
                entArray[j].setEntityId(entArray[j].getEntityId() + i);
            }
        }

        livingBase.setEntityId(livingEntitySpawnPacket.getEntityID());
        livingBase.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
        livingBase.motionX = (double) ((float) livingEntitySpawnPacket.getVelocityX() / 8000.0f);
        livingBase.motionY = (double) ((float) livingEntitySpawnPacket.getVelocityY() / 8000.0f);
        livingBase.motionZ = (double) ((float) livingEntitySpawnPacket.getVelocityZ() / 8000.0f);

        this.clientsideMinecraft.theWorld.addEntityToWorld(livingEntitySpawnPacket.getEntityID(), livingBase);
        List<DataWatcher.WatchableObject> list = livingEntitySpawnPacket.func_149027_c();

        if (list != null) {
            livingBase.getDataWatcher().updateWatchedObjectsFromList(list);
        }
    }

    /** @return {@link EntityLivingBase} instance by checking the corresponding
     * <value>entityTypeID</value> and seeing if we have a custom one we can load
     * or just to default to the vanilla way of generating normal entities. **/
    private EntityLivingBase checkAndCreateEntityByID(int entityTypeID, World worldInstance) {
        switch (entityTypeID) {
            default:
                return (EntityLivingBase) EntityList.createEntityByID(entityTypeID, worldInstance);
            case ENTITY_ENDERMAN_TYPE_ID: // enderman
                return new CustomEntityEnderman(worldInstance);
        }
    }

    /** We need to use reflection to get the current GUI instance that is being
     * used for the parent class, else the client will throw null-pointers. **/
    private static GuiScreen getGUIScreenViaReflection(NetHandlerPlayClient parent) {
        Field[] var1 = parent.getClass().getDeclaredFields();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            Field field = var1[var3];
            if (field.getType().equals(GuiScreen.class)) {
                field.setAccessible(true);

                try {
                    return (GuiScreen)field.get(parent);
                } catch (Exception var6) {
                    return null;
                }
            }
        }

        return null;
    }

    //region Super methods

    /*
     * these are needed because we call through the parent super
     * in order to run these methods in runtime. The problem I was having
     * was trying to override without calling an already existing super
     * class was throwing a bunch of nullpointers, so this fixes that ig
     */

    public void handleJoinGame(S01PacketJoinGame p_147282_1_) {
        this.parentInstance.handleJoinGame(p_147282_1_);
    }

    public void handleSpawnExperienceOrb(S11PacketSpawnExperienceOrb p_147286_1_) {
        this.parentInstance.handleSpawnExperienceOrb(p_147286_1_);
    }

    public void handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity p_147292_1_) {
        this.parentInstance.handleSpawnGlobalEntity(p_147292_1_);
    }

    public void handleSpawnPainting(S10PacketSpawnPainting p_147288_1_) {
        this.parentInstance.handleSpawnPainting(p_147288_1_);
    }

    public void handleEntityVelocity(S12PacketEntityVelocity p_147244_1_) {
        this.parentInstance.handleEntityVelocity(p_147244_1_);
    }

    public void handleEntityMetadata(S1CPacketEntityMetadata p_147284_1_) {
        this.parentInstance.handleEntityMetadata(p_147284_1_);
    }

    public void handleSpawnPlayer(S0CPacketSpawnPlayer p_147237_1_) {
        this.parentInstance.handleSpawnPlayer(p_147237_1_);
    }

    public void handleEntityTeleport(S18PacketEntityTeleport p_147275_1_) {
        this.parentInstance.handleEntityTeleport(p_147275_1_);
    }

    public void handleHeldItemChange(S09PacketHeldItemChange p_147257_1_) {
        this.parentInstance.handleHeldItemChange(p_147257_1_);
    }

    public void handleEntityMovement(S14PacketEntity p_147259_1_) {
        this.parentInstance.handleEntityMovement(p_147259_1_);
    }

    public void handleEntityHeadLook(S19PacketEntityHeadLook p_147267_1_) {
        this.parentInstance.handleEntityHeadLook(p_147267_1_);
    }

    public void handleDestroyEntities(S13PacketDestroyEntities p_147238_1_) {
        this.parentInstance.handleDestroyEntities(p_147238_1_);
    }

    public void handlePlayerPosLook(S08PacketPlayerPosLook p_147258_1_) {
        this.parentInstance.handlePlayerPosLook(p_147258_1_);
    }

    public void handleMultiBlockChange(S22PacketMultiBlockChange p_147287_1_) {
        this.parentInstance.handleMultiBlockChange(p_147287_1_);
    }

    public void handleChunkData(S21PacketChunkData p_147263_1_) {
        this.parentInstance.handleChunkData(p_147263_1_);
    }

    public void handleBlockChange(S23PacketBlockChange p_147234_1_) {
        this.parentInstance.handleBlockChange(p_147234_1_);
    }

    public void handleDisconnect(S40PacketDisconnect p_147253_1_) {
        this.parentInstance.handleDisconnect(p_147253_1_);
    }

    public void addToSendQueue(Packet p_147297_1_) {
        this.parentInstance.addToSendQueue(p_147297_1_);
    }

    public void onDisconnect(IChatComponent p_147231_1_) {
        this.parentInstance.onDisconnect(p_147231_1_);
    }

    public void handleCollectItem(S0DPacketCollectItem p_147246_1_) {
        this.parentInstance.handleCollectItem(p_147246_1_);
    }

    public void handleChat(S02PacketChat p_147251_1_) {
        this.parentInstance.handleChat(p_147251_1_);
    }

    public void handleAnimation(S0BPacketAnimation p_147279_1_) {
        this.parentInstance.handleAnimation(p_147279_1_);
    }

    public void handleUseBed(S0APacketUseBed p_147278_1_) {
        this.parentInstance.handleUseBed(p_147278_1_);
    }

    public void handleTimeUpdate(S03PacketTimeUpdate p_14753_1_) {
        this.parentInstance.handleTimeUpdate(p_14753_1_);
    }

    public void handleSpawnPosition(S05PacketSpawnPosition p_147271_1_) {
        this.parentInstance.handleSpawnPosition(p_147271_1_);
    }

    public void handleEntityAttach(S1BPacketEntityAttach p_147243_1_) {
        this.parentInstance.handleEntityAttach(p_147243_1_);
    }

    public void handleEntityStatus(S19PacketEntityStatus p_147236_1_) {
        this.parentInstance.handleEntityStatus(p_147236_1_);
    }

    public void handleUpdateHealth(S06PacketUpdateHealth p_147249_1_) {
        this.parentInstance.handleUpdateHealth(p_147249_1_);
    }

    public void handleSetExperience(S1FPacketSetExperience p_147295_1_) {
        this.parentInstance.handleSetExperience(p_147295_1_);
    }

    public void handleRespawn(S07PacketRespawn p_147280_1_) {
        this.parentInstance.handleRespawn(p_147280_1_);
    }

    public void handleExplosion(S27PacketExplosion p_147283_1_) {
        this.parentInstance.handleExplosion(p_147283_1_);
    }

    public void handleOpenWindow(S2DPacketOpenWindow p_147265_1_) {
        this.parentInstance.handleOpenWindow(p_147265_1_);
    }

    public void handleSetSlot(S2FPacketSetSlot p_147266_1_) {
        this.parentInstance.handleSetSlot(p_147266_1_);
    }

    public void handleConfirmTransaction(S32PacketConfirmTransaction p_147239_1_) {
        this.parentInstance.handleConfirmTransaction(p_147239_1_);
    }

    public void handleWindowItems(S30PacketWindowItems p_147241_1_) {
        this.parentInstance.handleWindowItems(p_147241_1_);
    }

    public void handleSignEditorOpen(S36PacketSignEditorOpen p_147268_1_) {
        this.parentInstance.handleSignEditorOpen(p_147268_1_);
    }

    public void handleUpdateSign(S33PacketUpdateSign p_147248_1_) {
        this.parentInstance.handleUpdateSign(p_147248_1_);
    }

    public void handleUpdateTileEntity(S35PacketUpdateTileEntity p_147273_1_) {
        this.parentInstance.handleUpdateTileEntity(p_147273_1_);
    }

    public void handleWindowProperty(S31PacketWindowProperty p_147245_1_) {
        this.parentInstance.handleWindowProperty(p_147245_1_);
    }

    public void handleEntityEquipment(S04PacketEntityEquipment p_147242_1_) {
        this.parentInstance.handleEntityEquipment(p_147242_1_);
    }

    public void handleCloseWindow(S2EPacketCloseWindow p_147276_1_) {
        this.parentInstance.handleCloseWindow(p_147276_1_);
    }

    public void handleBlockAction(S24PacketBlockAction p_147261_1_) {
        this.parentInstance.handleBlockAction(p_147261_1_);
    }

    public void handleBlockBreakAnim(S25PacketBlockBreakAnim p_147294_1_) {
        this.parentInstance.handleBlockBreakAnim(p_147294_1_);
    }

    public void handleMapChunkBulk(S26PacketMapChunkBulk p_147269_1_) {
        this.parentInstance.handleMapChunkBulk(p_147269_1_);
    }

    public void handleChangeGameState(S2BPacketChangeGameState packet) {
        this.parentInstance.handleChangeGameState(packet);
    }

    public void handleMaps(S34PacketMaps p_147264_1_) {
        this.parentInstance.handleMaps(p_147264_1_);
    }

    public void handleEffect(S28PacketEffect p_147277_1_) {
        this.parentInstance.handleEffect(p_147277_1_);
    }

    public void handleCombatEvent(S42PacketCombatEvent packetIn) {
        this.parentInstance.handleCombatEvent(packetIn);
    }

    public void handleServerDifficulty(S41PacketServerDifficulty packetIn) {
        this.parentInstance.handleServerDifficulty(packetIn);
    }

    public void handleCamera(S43PacketCamera packetIn) {
        this.parentInstance.handleCamera(packetIn);
    }

    public void handleWorldBorder(S44PacketWorldBorder packetIn) {
        this.parentInstance.handleWorldBorder(packetIn);
    }

    public void handleTitle(S45PacketTitle packetIn) {
        this.parentInstance.handleTitle(packetIn);
    }

    public void handleStatistics(S37PacketStatistics p_147293_1_) {
        this.parentInstance.handleStatistics(p_147293_1_);
    }

    public void handleSetCompressionLevel(S46PacketSetCompressionLevel packetIn) {
        this.parentInstance.handleSetCompressionLevel(packetIn);
    }

    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn) {

        // first, we need to set these IChatComponent header/footer functions
        // this is because our custom tab list UI display needs this data, but its private in the GuiTabList class
        // so instead of using slow reflection to access those fields, instead we can just pull the data from here
        SSMTweaks.netPlayClientHandler.tabListHeader = packetIn.getHeader().getFormattedText().length() == 0 ? null : packetIn.getHeader();
        SSMTweaks.netPlayClientHandler.tabListFooter = packetIn.getFooter().getFormattedText().length() == 0 ? null : packetIn.getFooter();
        this.parentInstance.handlePlayerListHeaderFooter(packetIn);
    }

    public void handleEntityEffect(S1DPacketEntityEffect p_147260_1_) {
        this.parentInstance.handleEntityEffect(p_147260_1_);
    }

    public void handleRemoveEntityEffect(S1EPacketRemoveEntityEffect p_147262_1_) {
        this.parentInstance.handleRemoveEntityEffect(p_147262_1_);
    }

    public void handlePlayerListItem(S38PacketPlayerListItem p_147256_1_) {
        this.parentInstance.handlePlayerListItem(p_147256_1_);
    }

    public void handleKeepAlive(S00PacketKeepAlive p_147272_1_) {
        this.parentInstance.handleKeepAlive(p_147272_1_);
    }

    public void handlePlayerAbilities(S39PacketPlayerAbilities p_147270_1_) {
        this.parentInstance.handlePlayerAbilities(p_147270_1_);
    }

    public void handleTabComplete(S3APacketTabComplete p_147274_1_) {
        this.parentInstance.handleTabComplete(p_147274_1_);
    }

    public void handleSoundEffect(S29PacketSoundEffect p_147255_1_) {
        this.parentInstance.handleSoundEffect(p_147255_1_);
    }

    public void handleResourcePack(S48PacketResourcePackSend packetIn) {
        this.parentInstance.handleResourcePack(packetIn);
    }

    public void handleEntityNBT(S49PacketUpdateEntityNBT packetIn) {
        this.parentInstance.handleEntityNBT(packetIn);
    }

    public void handleCustomPayload(S3FPacketCustomPayload p_147240_1_) {
        this.parentInstance.handleCustomPayload(p_147240_1_);
    }

    public void handleScoreboardObjective(S3BPacketScoreboardObjective p_147291_1_) {
        this.parentInstance.handleScoreboardObjective(p_147291_1_);
    }

    public void handleUpdateScore(S3CPacketUpdateScore p_147250_1_) {
        this.parentInstance.handleUpdateScore(p_147250_1_);
    }

    public void handleDisplayScoreboard(S3DPacketDisplayScoreboard p_147254_1_) {
        this.parentInstance.handleDisplayScoreboard(p_147254_1_);
    }

    public void handleTeams(S3EPacketTeams p_147247_1_) {
        this.parentInstance.handleTeams(p_147247_1_);
    }

    public void handleParticles(S2APacketParticles p_147289_1_) {
        this.parentInstance.handleParticles(p_147289_1_);
    }

    public void handleEntityProperties(S20PacketEntityProperties p_147290_1_) {
        this.parentInstance.handleEntityProperties(p_147290_1_);
    }

    //endregion

}
