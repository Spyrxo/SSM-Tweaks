package xyz.gholemgaeming.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import xyz.gholemgaeming.SSMTweaks;

import java.util.ArrayList;
import java.util.List;

/** Allows users to change the long render distance value of entities. **/
public class CommandChangeEntityDistRenderValue extends CommandBase {

    @Override
    public String getCommandName() {
        return "longdistrendervalue";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "longdistrendervalue";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("ldrv");
        aliases.add("rendervalue");
        aliases.add("trackingdist");
        aliases.add("viewentitydist");
        aliases.add("longdistvalue");
        aliases.add("entdist");
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {

        // player needs to have typed in a value
        if (strings.length < 1) {
            SSMTweaks.getClientPlayer().addChatMessage(
                    new ChatComponentText(EnumChatFormatting.RED + "You did not type a value"));
            return;
        }

        // attempt to handle the value change
        try {

            // get the value they typed, make sure it's not too big or too small
            int value = Integer.parseInt(strings[0]);
            value = Math.min(SSMTweaks.FIXED_MAX_DISTANCE_ALLOWED, value);
            value = Math.max(1, value);

            // modify the render distance weight and inform the player
            SSMTweaks.customRenderDistanceWeight = value;
            SSMTweaks.getClientPlayer().addChatMessage(
                    new ChatComponentText(EnumChatFormatting.GREEN + "Changed the tracking distance value to " + value));

        } catch (Exception ex) {
            SSMTweaks.getClientPlayer().addChatMessage(
                    new ChatComponentText(EnumChatFormatting.RED + "An error has occurred when trying to change the value"));
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender iCommandSender, String[] strings, BlockPos blockPos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }

}
