package xyz.gholemgaeming.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.Level;
import xyz.gholemgaeming.SSMTweaks;

import java.util.ArrayList;
import java.util.List;

/** Allows users to toggle whether entities will render from far away. **/
public class CommandToggleEntityDistRender extends CommandBase {

    @Override
    public String getCommandName() {
        return "toggleentitydistancerender";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "toggleentitydistancerender";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("tedr");
        aliases.add("toggleentityrender");
        aliases.add("tentityrender");
        aliases.add("toggletrackingrender");
        aliases.add("toggledistancerender");
        aliases.add("toggleentitydistance");
        aliases.add("ted");
        aliases.add("entdistrend");
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        try {
            SSMTweaks.setModifyingTrackingRange(!SSMTweaks.allowCustomRenderDistance);
            String chatMessage = SSMTweaks.allowCustomRenderDistance ?
                    EnumChatFormatting.GREEN + "Long distance entity tracking enabled" :
                    EnumChatFormatting.RED + "Long distance entity tracking disabled";
            SSMTweaks.getClientPlayer().addChatMessage(new ChatComponentText(chatMessage));
        } catch (Exception ex) {
            SSMTweaks.getClientPlayer().addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "Error when trying to toggle long distance rendering"));
            SSMTweaks.modLogger.log(Level.WARN, "Error when attempting to toggle entity tracking mod");
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
