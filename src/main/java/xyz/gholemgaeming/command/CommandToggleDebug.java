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

import java.util.List;

public class CommandToggleDebug extends CommandBase {

    @Override
    public String getCommandName() {
        return "togglessmtweaksdebug";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "togglessmtweaksdebug";
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) throws CommandException {
        try {
            SSMTweaks.setDebugMode(!SSMTweaks.debugMode);
            String chatMessage = SSMTweaks.debugMode ?
                    EnumChatFormatting.GREEN + "Debug mode toggled on" :
                    EnumChatFormatting.RED + "Debug mode toggled off";
            SSMTweaks.getClientPlayer().addChatMessage(new ChatComponentText(chatMessage));
        } catch (Exception ex) {
            SSMTweaks.modLogger.log(Level.WARN, "Error when attempting to toggle debug mode");
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
