package xyz.gholemgaeming.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** This class is not needed to achieve what I wanted to do, but I've
 * kept the code just in case I ever need to use it for something else.
 * **/
@Deprecated
@SideOnly(Side.CLIENT)
public class CustomPlayerControllerMP extends PlayerControllerMP {

    private Minecraft minecraft;

    private NetHandlerPlayClient customNetHandler;

    /** {@link CustomPlayerControllerMP} is just a bridging child constructor class used to
     * initialize our {@link NetHandlerPlayClient} child class, which allows us to manipulate
     * how entities are spawned and handled by the client.
     * <p>
     * You could also use this class to manipulate how the client handles interactions from the server.
     * **/
    public CustomPlayerControllerMP(Minecraft minecraft, NetHandlerPlayClient customNetHandler) {
        super(minecraft, customNetHandler);
        this.minecraft = minecraft;
        this.customNetHandler = customNetHandler;
    }

    @Override
    public EntityPlayerSP func_178892_a(World nmsWorldInstance, StatFileWriter statFileWriter) {
        return new EntityPlayerSP(this.minecraft, nmsWorldInstance, this.customNetHandler, statFileWriter);
    }
}
