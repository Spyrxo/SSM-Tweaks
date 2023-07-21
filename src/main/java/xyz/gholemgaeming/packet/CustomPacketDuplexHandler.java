package xyz.gholemgaeming.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/** i dont need this... for now... **/
@Deprecated
public class CustomPacketDuplexHandler {

    /** Duplex packet handler for minecraft pipeline (interception) **/
    private ChannelDuplexHandler channelDuplexHandler;

    /** {@link net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent} is called
     * whenever the client player connects to a minecraft server and successfully logs in and joins the world instance.
     * **/
    @SubscribeEvent
    public void onClientConnectToServerCreateDuplexInterceptor(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        if (e.isCanceled()) return;

        channelDuplexHandler = new ChannelDuplexHandler() {

            /** Incoming packets being received (server -> client) **/
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {

                //if (packet instanceof S0FPacketSpawnMob) {
                //    S0FPacketSpawnMob mobPacket = (S0FPacketSpawnMob) packet;

                //    SSMTweaks.modLogger.log(Level.INFO, "entitytypeid: " + mobPacket.getEntityType());
                //}

                super.channelRead(ctx, packet);
            }

            /** Outgoing packets being sent (client -> server) **/
            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                super.write(ctx, packet, promise);
            }
        };
    }

    /** {@link net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent} is called
     * whenever the client player disconnects successfully from a minecraft server instance.
     * <p>
     * We need to remove the duplex packet handler from our channel pipeline, so we don't cause any errors.
     * **/
    @SubscribeEvent
    public void onClientDisconnectFromServerRemoveInterceptor(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        if (e.isCanceled()) return;
        e.manager.channel().pipeline().remove(channelDuplexHandler);
    }
}
