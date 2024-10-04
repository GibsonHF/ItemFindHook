package net.botwithus.debug;

import net.botwithus.rs3.game.Inventory;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.imgui.Vector2f;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainGraphicsContext extends ScriptGraphicsContext {

    private final MainScript script;
    private String lootNameInput = "Type Here...";

    private String chatMessageInput = "Type Chat Message Here...";


    public InventoryManagementTask inventoryManagementTask;


    public MainGraphicsContext(ScriptConsole console, MainScript script) {
        super(console);
        this.script = script;
        inventoryManagementTask = new InventoryManagementTask(script);
    }


    public void drawSettings() {
        ImGui.SetWindowSize(200.f, 200.f);
        if (ImGui.Begin("Notifier", 0)) {


            if (ImGui.BeginTabBar("SettingsTabBar", 0)) {

                if (ImGui.BeginTabItem("Settings", 0)) {
                    script.runScript = createCheckboxWithTooltip("Run Script", script.runScript, "Toggles the script on and off. When off, the script will not run.", "runChild");
                    script.saveConfig();
                    script.levelUpNotification = createCheckboxWithTooltip("Level Up Notification", script.levelUpNotification, "Notifies of level up and what skill in the webhook message.", "LevelUpChild");
                    script.saveConfig();
                    script.LogoutNotification = createCheckboxWithTooltip("Logout Notification", script.LogoutNotification, "Notifies player logout in the webhook message.", "LogoutChild");
                    script.saveConfig();
                    script.hideTimestamp = createCheckboxWithTooltip("Hide Timestamp", script.hideTimestamp, "Hides the timestamp of when drop was found in the webhook message.", "TimestampChild");
                    script.saveConfig();
                    script.includeKillCount = createCheckboxWithTooltip("Show Kill Count", script.includeKillCount, "Shows the kill count in the discord webhook message when drop is received, Please unfilter game messages.", "TooltipChild");
                    script.saveConfig();
                    script.windowsNotification = createCheckboxWithTooltip("Windows Notification", script.windowsNotification, "Toggles the Windows notification on and off. When on, a Windows notification will be sent.", "windowsNotificationChild");
                    script.saveConfig();
                    ImGui.Separator();
                    script.WebHookURL = ImGui.InputText("Webhook URL", script.WebHookURL, 256, 0);
                    ImGui.Separator();
                    if(ImGui.Button("Send test webhook"))
                    {
                        script.sendDiscordWebhook("Test", "Test");
                        script.println(script.WebHookURL);
                    }
                    ImGui.SameLine();
                    if(ImGui.Button("Save Settings"))
                    {
                        String lootToPickupString = String.join(",", script.lootToPickup);
                        script.config.addProperty("lootToPickup", lootToPickupString);
                        script.config.addProperty("WebHookURL", script.WebHookURL);
                        script.config.save();
                    }
                    ImGui.Separator();
                    lootNameInput = ImGui.InputText("Loot", lootNameInput);
                    if (ImGui.Button("Add Loot")) {
                        if(!lootNameInput.equals("Type Here...") && !lootNameInput.equals("")) {
                            script.addLoot(lootNameInput);
                            lootNameInput = "";
                        }
                    }
                    ImGui.Separator();

                    Iterator<String> lootIterator = script.getLootToPickup().iterator();
                    while (lootIterator.hasNext()) {
                        String lootName = lootIterator.next();
                        if (ImGui.Button(lootName)) {
                            lootIterator.remove();
                            script.lootToPickup.remove(lootName);
                            script.lootCount.remove(lootName);
                        }
                    }
                    ImGui.Separator();
                    chatMessageInput = ImGui.InputText("Chat Messages", chatMessageInput);
                    if (ImGui.Button("Add Chat Message")) {
                        if(!chatMessageInput.trim().isEmpty()) {
                            script.addChatMessageToCheck(chatMessageInput);
                            chatMessageInput = "";
                        }
                    }
                    ImGui.Separator();

                    Iterator<String> chatMessageIterator = script.chatMessagesToCheck.iterator();
                    while (chatMessageIterator.hasNext()) {
                        String chatMessage = chatMessageIterator.next();
                        if (ImGui.Button(chatMessage)) {
                            chatMessageIterator.remove();
                            script.removeChatMessageToCheck(chatMessage);
                        }
                    }
                    ImGui.EndTabItem();
                }

                if (ImGui.BeginTabItem("Statistics", 0)) {
                    for (Map.Entry<String, Integer> entry : script.lootCount.entrySet()) {
                        ImGui.Text(entry.getKey() + ": " + entry.getValue());
                    }


                    ImGui.EndTabItem();
                }

                ImGui.EndTabBar();
            }

            ImGui.End();
        }
    }

    private boolean createCheckboxWithTooltip(String label, boolean value, String tooltipText, String childId) {
        boolean result = ImGui.Checkbox(label, value);
        if(ImGui.IsItemHovered()) {
            ImGui.BeginTooltip();
            Vector2f textSize = ImGui.CalcTextSize(tooltipText);
            float textWidth = textSize.getX();
            float boxWidth = 500;
            float charWidth = textWidth / tooltipText.length();
            int charsPerLine = (int) (boxWidth / charWidth);
            int textLines = (int) Math.ceil((double) tooltipText.length() / charsPerLine);
            float textHeight = textLines * textSize.getY() + 20;
            ImGui.BeginChild(childId, boxWidth, textHeight, true, 0);
            ImGui.Text(tooltipText);
            ImGui.EndChild();
            ImGui.EndTooltip();
        }
        return result;
    }


    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }



}
