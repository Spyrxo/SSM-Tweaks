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

/** Allows uers to toggle whether the on-screen game time will be shown. **/
public class CommandToggleOnScreenGameTime extends CommandBase {

    @Override
    public String getCommandName() {
        return "togglegametime";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "togglegametime";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("tgt");
        aliases.add("togglegt");
        aliases.add("tgametime");
        aliases.add("toggletime");
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        try {
            SSMTweaks.setShowingOnScreenGameTimer(!SSMTweaks.allowOnScreenGameTimer);
            String chatMessage = SSMTweaks.allowOnScreenGameTimer ?
                    EnumChatFormatting.GREEN + "On-screen game time enabled" :
                    EnumChatFormatting.RED + "On-screen game time disabled";
            SSMTweaks.getClientPlayer().addChatMessage(new ChatComponentText(chatMessage));
        } catch (Exception ex) {
            SSMTweaks.getClientPlayer().addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "Error when trying to toggle on-screen game time"));
            SSMTweaks.modLogger.log(Level.WARN, "Error when attempting to toggle on-screen game time");
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
