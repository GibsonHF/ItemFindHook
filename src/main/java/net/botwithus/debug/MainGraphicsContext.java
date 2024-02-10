package net.botwithus.debug;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.NativeInteger;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;
import java.util.concurrent.TimeUnit;

public class MainGraphicsContext extends ScriptGraphicsContext {

    private final MainScript script;


    public MainGraphicsContext(ScriptConsole console, MainScript script) {
        super(console);
        this.script = script;
    }


    public void drawSettings() {
        ImGui.SetWindowSize(200.f, 200.f);
        if (ImGui.Begin("ItemFinder", 0)) {


            if (ImGui.BeginTabBar("SettingsTabBar", 0)) {

                if (ImGui.BeginTabItem("Settings", 0)) {
                    script.runScript = ImGui.Checkbox("Run Script", script.runScript);
                    ImGui.EndTabItem();
                }

                if (ImGui.BeginTabItem("Statistics", 0)) {
                    ImGui.Text("Total Scriptures: " + script.totalCaughtScriptures);
                    ImGui.Text("Total Manuscripts: " + script.totalCaughtManuScripts);


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
