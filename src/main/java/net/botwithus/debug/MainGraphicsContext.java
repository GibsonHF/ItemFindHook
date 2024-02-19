package net.botwithus.debug;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainGraphicsContext extends ScriptGraphicsContext {

    private final MainScript script;
    private String lootNameInput = "Type Here...";


    public MainGraphicsContext(ScriptConsole console, MainScript script) {
        super(console);
        this.script = script;
    }


    public void drawSettings() {
        ImGui.SetWindowSize(200.f, 200.f);
        if (ImGui.Begin("Item Finder", 0)) {


            if (ImGui.BeginTabBar("SettingsTabBar", 0)) {

                if (ImGui.BeginTabItem("Settings", 0)) {
                    script.runScript = ImGui.Checkbox("Run Script", script.runScript);
                    if(ImGui.IsItemHovered())
                    {
                        String tooltipText = "Toggles the script on and off. When off, the script will not run.";
                        ImGui.BeginTooltip();
                        ImGui.BeginChild("runChild", 500, 40, true, 0); // Set the width of the child window to 500
                        ImGui.Text(tooltipText);
                        ImGui.EndChild();
                        ImGui.EndTooltip();
                    }
                    script.levelUpNotification = ImGui.Checkbox("Level Up Notification", script.levelUpNotification);
                    if(ImGui.IsItemHovered())
                    {
                        String tooltipText = "Notifies of level up and what skill in the webhook message.";
                        ImGui.BeginTooltip();
                        ImGui.BeginChild("LevelUpChild", 500, 40, true, 0); // Set the width of the child window to 500
                        ImGui.Text(tooltipText);
                        ImGui.EndChild();
                        ImGui.EndTooltip();
                    }
                    script.LogoutNotification = ImGui.Checkbox("Logout Notification", script.LogoutNotification);
                    if(ImGui.IsItemHovered())
                    {
                        String tooltipText = "Notifies player logout in the webhook message.";
                        ImGui.BeginTooltip();
                        ImGui.BeginChild("LogoutChild", 500, 40, true, 0); // Set the width of the child window to 500
                        ImGui.Text(tooltipText);
                        ImGui.EndChild();
                        ImGui.EndTooltip();
                    }
                    script.hideTimestamp = ImGui.Checkbox("Hide Timestamp", script.hideTimestamp);
                    if(ImGui.IsItemHovered())
                    {
                        String tooltipText = "Hides the timestamp of when drop was found in the webhook message.";
                        ImGui.BeginTooltip();
                        ImGui.BeginChild("TimestampChild", 500, 40, true, 0); // Set the width of the child window to 500
                        ImGui.Text(tooltipText);
                        ImGui.EndChild();
                        ImGui.EndTooltip();
                    }
                    script.includeKillCount = ImGui.Checkbox("Show Kill Count", script.includeKillCount);
                    if(ImGui.IsItemHovered())
                    {
                        String tooltipText = "Shows the kill count in the discord webhook message when drop is received, Please unfilter game messages.";
                        ImGui.BeginTooltip();
                        ImGui.BeginChild("TooltipChild", 500, 50, true, 0); // Set the width of the child window to 500
                        ImGui.Text(tooltipText);
                        ImGui.EndChild();
                        ImGui.EndTooltip();
                    }
                    //set length of inputtext to 256
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
                        script.addLoot(lootNameInput);
                        lootNameInput = "";
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



    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }



}
