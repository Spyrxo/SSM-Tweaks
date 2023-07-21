package xyz.gholemgaeming.util;

import com.google.common.collect.ComparisonChain;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Comparator;

@SideOnly(Side.CLIENT)
public class NetworkPlayerInfoComparator implements Comparator<NetworkPlayerInfo> {

    @Override
    public int compare(NetworkPlayerInfo player1, NetworkPlayerInfo player2) {
        ScorePlayerTeam team1 = player1.getPlayerTeam();
        ScorePlayerTeam team2 = player2.getPlayerTeam();
        return ComparisonChain.start()
                .compareTrueFirst(player1.getGameType() != WorldSettings.GameType.SPECTATOR, player2.getGameType() != WorldSettings.GameType.SPECTATOR)
                .compare(team1 != null ? team1.getTeamName() : "", team2 != null ? team2.getTeamName() : "")
                .compare(player1.getGameProfile().getName(), player2.getGameProfile().getName()).result();
    }
}
