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

public class CommandTest extends CommandBase {

    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "test";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("rtest");
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] string) throws CommandException {

        // they need to type something obviously
        if (string.length < 1) {
            SSMTweaks.getClientPlayer().addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You did not type anything idiot"));
            return;
        }

        //SSMTweaks.test = Integer.parseInt(string[0]);
        //SSMTweaks.test2 = Integer.parseInt(string[1]);
        SSMTweaks.disguisePlayerHandler.getAllFoundDisguises().values().forEach(disguise -> {
            SSMTweaks.modLogger.log(Level.INFO, "DisguisePlayer: " + disguise.playerDisplayName);
            SSMTweaks.modLogger.log(Level.INFO, "DisguiseKit: " + disguise.smashKit.name());
            SSMTweaks.modLogger.log(Level.INFO, "DisguiseStocks: " + disguise.amountOfStocks);
        });
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
