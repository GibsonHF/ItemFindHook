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
                    script.levelUpNotification = ImGui.Checkbox("Level Up Notification", script.levelUpNotification);
                    script.LogoutNotification = ImGui.Checkbox("Logout Notification", script.LogoutNotification);
                    script.hideTimestamp = ImGui.Checkbox("Hide Timestamp", script.hideTimestamp);
                    //set length of inputtext to 256
                    script.WebHookURL = ImGui.InputText("Webhook URL", script.WebHookURL, 256, 0);
                    ImGui.Separator();
                    if(ImGui.Button("Send test webhook"))
                    {
                        script.sendDiscordWebhook("Test", "Test");
                        script.println(script.WebHookURL);
                    }
                    ImGui.SameLine();
                    if(ImGui.Button("Save Loot Settings"))
                    {
                        String lootToPickupString = String.join(",", script.lootToPickup);
                        script.config.addProperty("lootToPickup", lootToPickupString);
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
