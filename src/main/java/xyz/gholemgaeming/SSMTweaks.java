package xyz.gholemgaeming;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.gholemgaeming.command.CommandChangeEntityDistRenderValue;
import xyz.gholemgaeming.command.CommandToggleDebug;
import xyz.gholemgaeming.command.CommandToggleEntityDistRender;
import xyz.gholemgaeming.handler.LongDistanceEntityHandler;
import xyz.gholemgaeming.handler.NetPlayClientEventHandler;

/* Impressive... very nice... now let's see paul allans minecraft mod... */
@Mod(
        modid = SSMTweaks.MOD_ID,
        name = SSMTweaks.MOD_NAME,
        version = SSMTweaks.VERSION,
        clientSideOnly = true
)
public class SSMTweaks {

    /** Mod dependent signing information **/
    public static final String MOD_ID = "ssmtweaks";
    public static final String MOD_NAME = "SSMTweaks";
    public static final String VERSION = "1.0";

    /** Environment logger (client-console) **/
    public static final Logger modLogger = LogManager.getLogger(MOD_ID);

    /** Mod instance created by forge **/
    @Mod.Instance
    public static SSMTweaks MOD_INSTANCE;

    /** Is the mod allowed to even run its tweaks and logic on the currently connected server?
     * (the boolean to behold against all other booleans, nothing is immune to its power!) **/
    public static boolean theMasterModBoolean = false;

    /** Is the mod allowed to modify certain entities on the client to fix behavior? **/
    public static boolean allowCustomEntityFixes = true;

    /** Render Distance weight value - affects how far entities will be allowed to render
     * (1 = 16blocks, 2 = 32blocks, 3 = 48blocks, 4 = 64 blocks) [increments by 16 every time] **/
    public static int customRenderDistanceWeight = 12;
    public static int FIXED_MAX_DISTANCE_ALLOWED = 16; // the maximum will be 256 blocks (idk why you would want this, but good luck)

    /** Is the mod allowed to handle how entities are rendered? **/
    public static boolean allowCustomRenderDistance = true;

    /** Are we checking for spicy bugs? **/
    public static boolean debugMode = false;

    /** {@link SSMTweaks} Mod Constructor **/
    public SSMTweaks() {
        MOD_INSTANCE = this;
    }

    /** Initialization event called after the {@link net.minecraftforge.fml.common.event.FMLPreInitializationEvent}
     * has successfully run. The registry events in this method run after the aforementioned event has passed. **/
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {

        // register and load our listeners - allows the mod to handle event based logic
        MinecraftForge.EVENT_BUS.register(new NetPlayClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new LongDistanceEntityHandler());

        // register and load our commands - allow the player to control aspects of the mod in-game
        ClientCommandHandler.instance.registerCommand(new CommandToggleDebug());
        ClientCommandHandler.instance.registerCommand(new CommandChangeEntityDistRenderValue());
        ClientCommandHandler.instance.registerCommand(new CommandToggleEntityDistRender());
    }

    /** Event called after the {@link net.minecraftforge.fml.common.event.FMLPostInitializationEvent}
     * has successfully run. The events in this method run always after the entire mod has been loaded. **/
    @Mod.EventHandler
    public void initComplete(FMLLoadCompleteEvent e) {

        // log to console - successfully loaded the mod
        modLogger.log(Level.INFO, "Successfully loaded " + MOD_NAME + " mod  [v" + VERSION + "]");
    }

    /** Are we allowing the mod to give players 20/20 vision? **/
    public static void setModifyingTrackingRange(boolean allow) {
        allowCustomRenderDistance = allow;
    }

    /** Are we doing the spooky? **/
    public static void setDebugMode(boolean allow) {
        debugMode = allow;
    }

    /** @return {@link EntityPlayerSP} instance (client-side)
     * (the user who is enjoying the mod!) **/
    public static EntityPlayerSP getClientPlayer() {
        return Minecraft.getMinecraft().thePlayer;
    }

    /** Returns True/False depending on if the mod is allowed to tweak the client-side gameplay.
     * This boolean return checks in 2 stages, in order to successfully reach a decision.
     * <p>
     * Step 1: is the master boolean true or false? - Controlled by the current server instance
     *         the client is connected too. This mod only works for legalised SSM servers.
     *         If the player client is connected to another server, none of the tweaks will work.
     * <p>
     * Step 2: check the {@link TweaksOptions} in the constructor for its designated boolean,
     *         which is set by the config file or the appropriate toggle command.
     * **/
    public static boolean isModAllowedToDoItsMagic(TweaksOptions tweaksOption) {
        if (theMasterModBoolean) return true;
        else {
            switch (tweaksOption) {
                case ENTITY_RENDER_DISTANCE:
                    return allowCustomRenderDistance;
                case ENTITY_SPECIFIC_FIXES:
                    return allowCustomEntityFixes;
            }
        }

        return false;
    }

    /** {@link TweaksOptions} enum which is directly linked
     * to config and mod tweak booleans, which allow logic to process.
     * **/
    public enum TweaksOptions {

        ENTITY_RENDER_DISTANCE,
        ENTITY_SPECIFIC_FIXES,
        ;
    }

}
