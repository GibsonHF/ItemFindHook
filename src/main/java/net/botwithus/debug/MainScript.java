package net.botwithus.debug;

import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;

import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.ScriptGraphicsContext;
import net.botwithus.rs3.script.TickingScript;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScript extends TickingScript {



    public List<String> lootToPickup = new ArrayList<>();
    public Map<String, Integer> lootCount = new HashMap<>();

    public String WebHookURL = "";
    public boolean levelUpNotification;


    public MainScript(String name, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(name, scriptConfig, scriptDefinition);
    }


    public boolean runScript = false;
    public boolean LogoutNotification;

    public Map<Skills, Integer> previousSkillLevels = new HashMap<>();



    TaskManager taskManager;
    List<TaskManager.Task> tasks = new ArrayList<>();

    @Override
    public boolean onInitialize() {
        this.sgc = new MainGraphicsContext(getConsole(), this);
        isBackgroundScript = true;
        taskManager = new TaskManager(tasks, this);
        //do a starter task to get it started
        tasks.add(new InventoryManagementTask(this));
        return super.onInitialize();
    }

    @Override
    public void onTick(LocalPlayer localPlayer) {
        if(!runScript)
        {
            return;
        }
        try {

            taskManager.runTasks();
            if(levelUpNotification) {
                checkSkillLevelUp();
                Execution.delay(1000);
            }
            if(Client.getGameState() != Client.GameState.LOGGED_IN && LogoutNotification)
            {
                //only send 1 webhook saying Player logged out
                sendDiscordWebhook("Player Logged Out!", "Player Logged Out!");
                runScript = false;
            }
            if(localPlayer.getCurrentHealth() <= 0 && Client.getGameState() == Client.GameState.LOGGED_IN && LogoutNotification)
            {
                sendDiscordWebhook("Player Died!", "Player Died!");
                Execution.delayWhile(10000, () -> localPlayer.getCurrentHealth() <= 0);
            }
        }catch (Exception e)
        {
            println(e.getMessage());
        }
    }

    public void checkSkillLevelUp() {
        for (Skills skill : Skills.values()) {
            int currentLevel = skill.getLevel();
            if (previousSkillLevels.containsKey(skill) && currentLevel > previousSkillLevels.get(skill)) {
                sendDiscordWebhook("Level Up!", "Level Up! Your " + skill.name() + " level is now " + currentLevel);
            }
            previousSkillLevels.put(skill, currentLevel);
        }
    }

    public void sendDiscordWebhook(String title, String content) {
        try {
            URL url = new URL(WebHookURL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            http.setDoOutput(true);

            String jsonPayload = String.format("{\"content\": \"%s\", \"\": \"%s\"}", content, title);

            try (OutputStream os = http.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = http.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println(response.toString());
            } else {
                System.out.println("POST request not worked");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ScriptGraphicsContext getSgc() {
        return sgc;
    }

    public void addLoot(String lootNameInput) {
        lootToPickup.add(lootNameInput);
        lootCount.put(lootNameInput, 0); // Initialize count to 0

    }

    public List<String> getLootToPickup() {
        return lootToPickup;
    }


}
