package xyz.gholemgaeming.handler;

import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import xyz.gholemgaeming.SSMTweaks;
import xyz.gholemgaeming.util.ClientUtil;
import xyz.gholemgaeming.util.NetworkPlayerInfoComparator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GameUIDisplayHandler {

    /** {@link Ordering} {@link NetworkPlayerInfoComparator} entry, handles ordering tab list elements **/
    private static final Ordering<NetworkPlayerInfo> ENTRY_ORDERING = Ordering.from(new NetworkPlayerInfoComparator());

    /** Color of ping text & format of ping text ([value]ms) **/
    private static final int DEFAULT_PING_TEXT_COLOR = 0xA0A0A0;
    private static final String DEFAULT_PING_TEXT_FORMAT = "%dms";

    /** Various offset and holder values for the tab list element rendering **/
    private static final int PING_TEXT_RENDER_OFFSET = -13;
    private static final int PLAYER_SLOT_EXTRA_WIDTH = 60; // 45 was default, needed to be longer
    private static final int PLAYER_ICON_WIDTH = 9;
    private static final int EXTRA_WIDTH_BETWEEN_ICON_AND_NAME = 9;

    /** the thicc list of all mob heads you can load into the tab list **/
    private ResourceLocation skeletonTileGraphic = SSMTweaks.getResource("skeletonheadicon.png");
    private ResourceLocation ironGolemTileGraphic = SSMTweaks.getResource("irongolemheadicon.png");
    private ResourceLocation spiderTileGraphic = SSMTweaks.getResource("spiderheadicon.png");
    private ResourceLocation slimeTileGraphic = SSMTweaks.getResource("slimeheadicon.png");
    private ResourceLocation creeperTileGraphic = SSMTweaks.getResource("creeperheadicon.png");
    private ResourceLocation squidTileGraphic = SSMTweaks.getResource("squidheadicon.png");
    private ResourceLocation endermanTileGraphic = SSMTweaks.getResource("endermanheadicon.png");
    private ResourceLocation wolfTileGraphic = SSMTweaks.getResource("wolfheadicon.png");
    private ResourceLocation snowmanTileGraphic = SSMTweaks.getResource("snowmanheadicon.png");
    private ResourceLocation magmaCubeTileGraphic = SSMTweaks.getResource("magmacubeheadicon.png");
    private ResourceLocation witchTileGraphic = SSMTweaks.getResource("witchheadicon.png");
    private ResourceLocation zombieTileGraphic = SSMTweaks.getResource("zombieheadicon.png");
    private ResourceLocation witherSkeletonTileGraphic = SSMTweaks.getResource("witherskeletonheadicon.png");
    private ResourceLocation cowTileGraphic = SSMTweaks.getResource("cowheadicon.png");
    private ResourceLocation skeletonHorseTileGraphic = SSMTweaks.getResource("skeletonhorseheadicon.png");
    private ResourceLocation pigTileGraphic = SSMTweaks.getResource("pigheadicon.png");
    private ResourceLocation blazeTileGraphic = SSMTweaks.getResource("blazeheadicon.png");
    private ResourceLocation chickenTileGraphic = SSMTweaks.getResource("chickenheadicon.png");
    private ResourceLocation guardianTileGraphic = SSMTweaks.getResource("guardianheadicon.png");
    private ResourceLocation sheepTileGraphic = SSMTweaks.getResource("sheepheadicon.png");
    private ResourceLocation villagerTileGraphic = SSMTweaks.getResource("villagerheadicon.png");
    private ResourceLocation caveSpiderTileGraphic = SSMTweaks.getResource("cavespiderheadicon.png");
    private ResourceLocation donkeyTileGraphic = SSMTweaks.getResource("donkeyheadicon.png");
    private ResourceLocation undeadHorsemanTileGraphic = SSMTweaks.getResource("undeadhorsemanheadicon.png");
    private ResourceLocation unknownEntityTileGraphic = SSMTweaks.getResource("unknownheadicon.png");

    /** Is the user currently viewing the tab list? **/
    private boolean isPlayerRenderingTabList = false;

    @SubscribeEvent
    public void onRenderPlayersScreenHandleUITabList(RenderGameOverlayEvent.Pre e) {
        EntityPlayer player = SSMTweaks.getClientPlayer();
        if (player == null) return;

        // if the mods disabled, we don't want to handle anything else
        if (!SSMTweaks.isModAllowedToDoItsMagic(SSMTweaks.TweaksOptions.GAME_ONSCREEN_TIMER)) {
            SSMTweaks.modLogger.log(Level.INFO, "IS SET TO FALSE");
            return;
        }

        if (e.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {

            // cancel the initial list render
            // then make sure the client knows our new tab list is active
            e.setCanceled(true);
            isPlayerRenderingTabList = true;

            // finally, render our custom tab list o_O
            renderPlayerList(e.resolution);
        }

        // player is no longer rendering the tab list
        // so we can set this to false, allowing other elements to render
        else if (isPlayerRenderingTabList) {
            isPlayerRenderingTabList = false;
        }
    }

    /** Copied and modified from {@link net.minecraft.client.gui.GuiPlayerTabOverlay#renderPlayerlist}. */
    private void renderPlayerList(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiPlayerTabOverlay playerListGui = mc.ingameGUI.getTabList();
        FontRenderer fontRenderer = mc.ingameGUI.getFontRenderer();
        int width = scaledResolution.getScaledWidth();
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(0);

        NetHandlerPlayClient handler = mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> playerList = ENTRY_ORDERING.sortedCopy(handler.getPlayerInfoMap());
        int i = 0;
        int j = 0;

        for (NetworkPlayerInfo player : playerList) {
            int k = fontRenderer.getStringWidth(playerListGui.getPlayerName(player));
            i = Math.max(i, k);

            if (objective != null && objective.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                k = fontRenderer.getStringWidth(" " + scoreboard.getValueFromObjective(player.getGameProfile().getName(), objective).getScorePoints());
                j = Math.max(j, k);
            }
        }

        playerList = playerList.subList(0, Math.min(playerList.size(), 80));
        int playerCount = playerList.size();
        int i4 = playerCount;
        int j4;

        for (j4 = 1; i4 > 20; i4 = (playerCount + j4 - 1) / j4) {
            ++j4;
        }

        boolean displayPlayerIcons = mc.isIntegratedServerRunning() || mc.getNetHandler().getNetworkManager().getIsencrypted();
        int l;

        if (objective != null) {
            if (objective.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                l = 90;
            }
            else {
                l = j;
            }
        }
        else {
            l = 0;
        }

        int i1 = Math.min(j4 * ((displayPlayerIcons ? PLAYER_ICON_WIDTH : 0) + i + l + 13 + PLAYER_SLOT_EXTRA_WIDTH), width - 50) / j4;
        int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
        int k1 = 10;
        int l1 = i1 * j4 + (j4 - 1) * 5;

        List<String> headerList = null;
        if (SSMTweaks.netPlayClientHandler.tabListHeader != null) {
            headerList = fontRenderer.listFormattedStringToWidth(SSMTweaks.netPlayClientHandler.tabListHeader.getFormattedText(), width - 50);

            for (String s : headerList) {
                l1 = Math.max(l1, fontRenderer.getStringWidth(s));
            }
        }

        List<String> list2 = null;
        if (SSMTweaks.netPlayClientHandler.tabListFooter != null) {
            list2 = fontRenderer.listFormattedStringToWidth(SSMTweaks.netPlayClientHandler.tabListFooter.getFormattedText(), width - 50);

            for (String s1 : list2) {
                l1 = Math.max(l1, fontRenderer.getStringWidth(s1));
            }
        }

        if (headerList != null) {
            GuiPlayerTabOverlay.drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + headerList.size() * fontRenderer.FONT_HEIGHT, Integer.MIN_VALUE);
            for (String s2 : headerList) {
                int i2 = fontRenderer.getStringWidth(s2);
                fontRenderer.drawStringWithShadow(s2, (float)(width / 2 - i2 / 2), (float) k1, -1);
                k1 += fontRenderer.FONT_HEIGHT;
            }
            ++k1;
        }

        // this draws the actual grey box behind, the thing en-casing the tab list
        GuiPlayerTabOverlay.drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

        for (int playerIndex = 0; playerIndex < playerCount; ++playerIndex) {
            int l4 = playerIndex / i4;
            int i5 = playerIndex % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            int k2 = k1 + i5 * 9;
            GuiPlayerTabOverlay.drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            if (playerIndex < playerList.size()) {
                NetworkPlayerInfo player = playerList.get(playerIndex);
                GameProfile gameprofile = player.getGameProfile();

                if (displayPlayerIcons) {
                    EntityPlayer entityplayer = mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
                    mc.getTextureManager().bindTexture(player.getLocationSkin());
                    Gui.drawScaledCustomSizeModalRect(j2, k2,
                            8.0F, 8,
                            8, 8,
                            8, 8,
                            64.0F, 64.0F);

                    if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT)) {
                        Gui.drawScaledCustomSizeModalRect(j2, k2,
                                40.0F, 8,
                                8, 8,
                                8, 8,
                                64.0F, 64.0F);
                    }

                    // spawns the disguise entity head next to the players
                    if (entityplayer != null) {
                        mc.getTextureManager().bindTexture(getMobHeadGraphicBasedOnDisguise(entityplayer));
                        Gui.drawScaledCustomSizeModalRect(j2 + PLAYER_ICON_WIDTH, k2,
                                0, 0,                               // textureX&Z
                                64, 64,                                                           // width&height
                                8, 8,                                                           // textureAreaWidth&Height
                                64.0f, 64.0f);                                                  // textureWidth&Height

                    }

                    j2 += PLAYER_ICON_WIDTH;
                }

                String s4 = playerListGui.getPlayerName(player);

                if (player.getGameType() == WorldSettings.GameType.SPECTATOR) {
                    fontRenderer.drawStringWithShadow(EnumChatFormatting.ITALIC + s4, (float)j2 + EXTRA_WIDTH_BETWEEN_ICON_AND_NAME, (float)k2, -1862270977);
                }
                else {
                    fontRenderer.drawStringWithShadow(s4, (float)j2 + EXTRA_WIDTH_BETWEEN_ICON_AND_NAME, (float)k2, -1);
                }

                if (objective != null && player.getGameType() != WorldSettings.GameType.SPECTATOR) {
                    int k5 = j2 + i + 1;
                    int l5 = k5 + l;

                    if (l5 - k5 > 5) {
                        //playerListGui.drawScoreboardValues(objective, k2, gameprofile.getName(), k5, l5, player);
                        try {
                            Method drawScoreboardValuesMethod = playerListGui.getClass()
                                    .getDeclaredMethod("func_175247_a",
                                            ScoreObjective.class, int.class, String.class, int.class, int.class, NetworkPlayerInfo.class);
                            drawScoreboardValuesMethod.setAccessible(true);
                            drawScoreboardValuesMethod.invoke(playerListGui,
                                    objective, k2, gameprofile.getName(), k5, l5, player);

                        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                            SSMTweaks.modLogger.log(Level.WARN, "Error when attempting to access drawScoreboardValues() via reflection for tab-list");
                        }
                    }
                }

                // Here is the magic, rendering the ping text
                String pingString = String.format(DEFAULT_PING_TEXT_FORMAT, player.getResponseTime());
                int pingStringWidth = fontRenderer.getStringWidth(pingString);
                fontRenderer.drawStringWithShadow(
                        pingString,
                        (float) i1 + j2 - pingStringWidth + PING_TEXT_RENDER_OFFSET - (displayPlayerIcons ? PLAYER_ICON_WIDTH : 0),
                        (float) k2,
                        DEFAULT_PING_TEXT_COLOR);

                // Render the vanilla ping bars as well
                //playerListGui.drawPing(i1, j2 - (displayPlayerIcons ? PLAYER_ICON_WIDTH : 0), k2, player);
                try {
                    Method drawPingMethod = playerListGui.getClass()
                            .getDeclaredMethod("func_175245_a", int.class, int.class, int.class, NetworkPlayerInfo.class);
                    drawPingMethod.setAccessible(true);
                    drawPingMethod.invoke(playerListGui, i1, j2 - (displayPlayerIcons ? PLAYER_ICON_WIDTH : 0), k2, player);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    SSMTweaks.modLogger.log(Level.WARN, "Error when attempting to access drawPing() via reflection for tab-list");
                }
            }
        }

        if (list2 != null) {
            k1 = k1 + i4 * 9 + 1;
            GuiPlayerTabOverlay.drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list2.size() * fontRenderer.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s3 : list2)
            {
                int j5 = fontRenderer.getStringWidth(s3);
                fontRenderer.drawStringWithShadow(s3, (float)(width / 2 - j5 / 2), (float)k1, -1);
                k1 += fontRenderer.FONT_HEIGHT;
            }
        }
    }

    private ResourceLocation getMobHeadGraphicBasedOnDisguise(EntityPlayer entityPlayer) {
        switch (SSMTweaks.disguisePlayerHandler.getDisguiseKitForPlayerEntity(entityPlayer)) {
            default:
            case UNKNOWN:
                return unknownEntityTileGraphic;
            case SKELETON:
                return skeletonTileGraphic;
            case IRON_GOLEM:
                return ironGolemTileGraphic;
            case SPIDER:
                return spiderTileGraphic;
            case SLIME:
                return slimeTileGraphic;
            case CREEPER:
                return creeperTileGraphic;
            case SQUID:
                return squidTileGraphic;
            case ENDERMAN:
                return endermanTileGraphic;
            case WOLF:
                return wolfTileGraphic;
            case SNOWMAN:
                return snowmanTileGraphic;
            case MAGMA_CUBE:
                return magmaCubeTileGraphic;
            case WITCH:
                return witchTileGraphic;
            case ZOMBIE:
                return zombieTileGraphic;
            case WITHER_SKELETON:
                return witherSkeletonTileGraphic;
            case COW:
                return cowTileGraphic;
            case SKELETON_HORSE:
                return skeletonHorseTileGraphic;
            case PIG:
                return pigTileGraphic;
            case BLAZE:
                return blazeTileGraphic;
            case CHICKEN:
                return chickenTileGraphic;
            case GUARDIAN:
                return guardianTileGraphic;
            case SHEEP:
                return sheepTileGraphic;
            case VILLAGER:
                return villagerTileGraphic;
            case CAVE_SPIDER:
                return caveSpiderTileGraphic;
            case DONKEY:
                return donkeyTileGraphic;
            case UNDEAD_HORSEMAN:
                return undeadHorsemanTileGraphic;
        }
    }

    @SubscribeEvent
    public void onRenderPlayersScreenHandleUIGameTime(RenderGameOverlayEvent.Pre e) {
        EntityPlayer player = SSMTweaks.getClientPlayer();
        if (player == null) return;

        // if the mods disabled, we don't want to handle anything else
        if (!SSMTweaks.isModAllowedToDoItsMagic(SSMTweaks.TweaksOptions.GAME_ONSCREEN_TIMER)) {
            SSMTweaks.modLogger.log(Level.INFO, "IS SET TO FALSE");
            return;
        }

        // we want to render the game time in conjunction with the hotbar
        // but also the tab list takes priority, so only if the tab list isn't being shown
        if (e.type == RenderGameOverlayEvent.ElementType.HOTBAR && !isPlayerRenderingTabList) {

            // [] let's attempt to render our custom elements on screen
            try {

                // get the current scoreboard for the player
                Scoreboard scoreboard = player.getWorldScoreboard();
                if (scoreboard == null) return;

                // we are going to loop through all the scoreboard elements and try to find the one that says "Game time"
                // and once we have that, we can just break the loop because we don't need to continue
                String timeLine = null;
                for (String scoreLineLore : ClientUtil.getSidebarScoreContent(scoreboard)) {
                    if (scoreLineLore.contains("Game Time:")) {
                        timeLine = scoreLineLore;
                        break;
                    }
                }

                // if we found a score that said "Game time", then we are going
                // to attempt to render that on the top of the players screen
                if (timeLine == null) return;
                renderScaledOnScreenTimerText(e.resolution, getTimerDisplayContent(timeLine));

            } catch (Exception ex) {
                SSMTweaks.modLogger.log(Level.INFO, "Error while trying to render custom UI elements");
            }
        }
    }

    /** @return Content to display at the top of the players screen.
     * The method will also change the color depending on how low the timer is.
     * **/
    private String getTimerDisplayContent(String initialTimeString) {

        // (1) remove all the spaces from the line, so it will look like:   GameTime:00:00
        // (2) then get the content after the "e:", which will just be:     00:00
        String content = initialTimeString.replaceAll("\\s", "");
        content = content.substring(content.lastIndexOf("e:") + 4, content.length());

        // (3) now we will determine color and content
        EnumChatFormatting color = EnumChatFormatting.WHITE;

        // first 2 values are 00, which means the game timer is quite low
        if (content.charAt(0) == '0' && content.charAt(1) == '0') {

            // if the 3rd value is 2 or 1, means the timer is at 00:29 - 00:10 so make it red
            // else if the 3rd value is 0, means the timer is at 00:09 - 00:00 so make it dark red
            color = content.charAt(3) == '2' || content.charAt(3) == '1' ? EnumChatFormatting.RED :
                    content.charAt(3) == '0' ? EnumChatFormatting.DARK_RED :
                            EnumChatFormatting.WHITE;
        }

        return color + content;
    }

    /** Method allows the client to render custom scaled text on the players
     * screen. In this case, it's used to show the in-game timer at the top of the screen.
     * **/
    private void renderScaledOnScreenTimerText(ScaledResolution scaledResolution, String string) {
        GuiIngame playerGUI = Minecraft.getMinecraft().ingameGUI;
        if (playerGUI == null) return;

        // if the mods disabled, we don't want to handle anything else
        if (!SSMTweaks.isModAllowedToDoItsMagic(SSMTweaks.TweaksOptions.GAME_ONSCREEN_TIMER)) return;

        // allow render engine changes
        GL11.glPushMatrix();

        // scale the font, then figure out its scaled size
        float size = 1.6f;
        GL11.glScalef(size, size, size);
        float mSize = (float) Math.pow(size, -1);

        // render the text on the players screen
        playerGUI.drawString(
                playerGUI.getFontRenderer(),
                string,
                Math.round((scaledResolution.getScaledWidth() / 2.0f - playerGUI.getFontRenderer().getStringWidth(string) / 2.0f) / size - 7), // 7 makes it nearly perfect centered
                Math.round(6 / size),
                16777215);

        // re-scale based on calculated size
        GL11.glScalef(mSize, mSize, mSize);

        // pop the render engine, no longer allowing changes
        GL11.glPopMatrix();

        //SSMTweaks.modLogger.log(Level.INFO, "rendered text");
    }
}
