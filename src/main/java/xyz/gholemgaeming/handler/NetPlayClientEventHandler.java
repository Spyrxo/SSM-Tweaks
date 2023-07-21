package xyz.gholemgaeming.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.INetHandler;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import xyz.gholemgaeming.SSMTweaks;
import xyz.gholemgaeming.client.CustomNetHandlerPlayClient;

@SideOnly(Side.CLIENT)
public class NetPlayClientEventHandler {

    private boolean hasInformedPlayerOfModLoad = false;

    /** Event called every tick by the client while the game is active. This handler
     * checks to make sure the player is in a valid world instance, and then attempts
     * to make sure that the netHandler() for handling server -> client-side packets
     * is using our {@link CustomNetHandlerPlayClient} class. Some dank stuff right there...
     * **/
    @SubscribeEvent
    public void onClientsideTickCheckAndGenerateCustomNetHandler(TickEvent.ClientTickEvent e) {
        if (e.isCanceled()) return;

        // make sure we are only running this when player is loaded into a world instance
        Minecraft minecraftInstance = Minecraft.getMinecraft();
        if (minecraftInstance.theWorld == null) return;

        // if the handler is already loaded as our custom one,
        // don't worry about doing the rest of the logic
        INetHandler parentHandler = minecraftInstance.thePlayer.sendQueue.getNetworkManager().getNetHandler();
        if (parentHandler instanceof CustomNetHandlerPlayClient) return;

        // [!] let's attempt to override the net handler with our own custom instance
        // so that way, we can control server-side packet behavior when intercepted
        try {
            minecraftInstance.thePlayer.sendQueue.getNetworkManager().setNetHandler(
                    new CustomNetHandlerPlayClient((NetHandlerPlayClient) parentHandler, minecraftInstance));
            SSMTweaks.modLogger.log(Level.INFO, "Successfully updated the NetHandler() for the client");

        } catch (Exception ex) {
            SSMTweaks.modLogger.log(Level.WARN, "Failed to override the current NetHandler() for the client");
        }
    }

    /** Event called whenever the client connects to a server instance. This handles whether the mod
     * is allowed to run its special tweaks on the client, determined if the server is "whitelisted".
     * <p>
     * Because this is an SSM-specific mod, whitelisted servers are the ones that specifically run ssm.
     * This means connecting to another server or network, won't actually let the mod run its tweaks.
     * **/
    @SubscribeEvent
    public void onClientConnectToServerCheckIfAllowedToRunMod(FMLNetworkEvent.ClientConnectedToServerEvent e) {

        // we don't want the mod working locally, as it affects regular gameplay,
        // so just keep it turned off
        if (e.isLocal) {
            SSMTweaks.theMasterModBoolean = false;
            SSMTweaks.modLogger.log(Level.INFO, "Client connected to a local server, ssm tweaks disabled");
            return;
        }

        // joining a new server
        String connectedServerIP = Minecraft.getMinecraft().getCurrentServerData().serverIP;

        // if we cannot verify the server ip, then by default just keep the mod off
        if (connectedServerIP == null) {
            SSMTweaks.theMasterModBoolean = false;
            SSMTweaks.modLogger.log(Level.INFO, "Could not verify server ip, ssm tweaks disabled");
            return;
        }

        hasInformedPlayerOfModLoad = false;

        // determine if the mod will handle tweaks to the current server
        SSMTweaks.modLogger.log(Level.INFO, "Client successfully connected to server (" + connectedServerIP + ") " +
                "- gathering server address info to check mod availability");
        SSMTweaks.theMasterModBoolean = isWhitelistedServer(connectedServerIP);
        String loggerMsg = SSMTweaks.theMasterModBoolean ?
                "SSM Tweaks is allowed on this server - mod is enabled" :
                "SSM Tweaks is not allowed on this server - mod is disabled";
        SSMTweaks.modLogger.log(Level.INFO, loggerMsg);
    }

    /** Event called whenever the client successfully connects to a server instance, and loads the client-side
     * entityPlayerMP into the world. This just informs the client-user via chat message if the mod is enabled/disabled.
     * **/
    @SubscribeEvent
    public void onClientJoinWorldInstanceInformUser(EntityJoinWorldEvent e) {
        if (e.world == null) return;

        if (!hasInformedPlayerOfModLoad) {
            hasInformedPlayerOfModLoad = true; // we no longer want to keep informing if they join other worlds on the same server

            // inform via chat message
            if (SSMTweaks.getClientPlayer() != null) {
                String chatMessage = SSMTweaks.theMasterModBoolean ?
                        EnumChatFormatting.GRAY + "[" + EnumChatFormatting.GREEN + "o" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.WHITE + "SSM-Tweaks loaded" :
                        EnumChatFormatting.GRAY + "[" + EnumChatFormatting.RED + "x" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.WHITE + "SSM-Tweaks not allowed";
                SSMTweaks.getClientPlayer().addChatMessage(new ChatComponentText(chatMessage));
            }
        }
    }

    /** @return <p>True</p> if the String in the constructor, the connected
     * to servers ip-address is matching a valid one we let the mod work with. **/
    private boolean isWhitelistedServer(String serverIP) {
        return serverIP.equalsIgnoreCase("us.smashreborn.com") ||
                serverIP.equalsIgnoreCase("51.161.76.173:25596") ||     // dedicated us ip
                serverIP.equalsIgnoreCase("eu.smashreborn.com") ||
                serverIP.equalsIgnoreCase("51.254.7.83:25580") ||       // dedicated eu ip
                serverIP.equalsIgnoreCase("au.smashreborn.com") ||
                serverIP.equalsIgnoreCase("139.99.234.140:25589") ||    // dedicated au ip
                serverIP.equalsIgnoreCase("localhost");
    }

}
