package net.botwithus.debug;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;

import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.script.*;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MainScript extends LoopingScript {



    public List<String> lootToPickup = new ArrayList<>();
    public Map<String, Integer> lootCount = new HashMap<>();

    public String WebHookURL = "";
    public boolean levelUpNotification;
    public ScriptConfig config;
    public boolean includeKillCount;

    public InventoryManagementTask inventoryManagementTask;

    //create a list of chat messages to check for from gui to set
    public List<String> chatMessagesToCheck = new ArrayList<>();


    public MainScript(String name, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {

        super(name, scriptConfig, scriptDefinition);
        config = scriptConfig;
    }

    public boolean runScript = false;
    public boolean LogoutNotification;

    public boolean hideTimestamp;

    public Map<Skills, Integer> previousSkillLevels = new HashMap<>();


    public int killCount;
    TaskManager taskManager;
    List<TaskManager.Task> tasks = new ArrayList<>();


    @Override
    public boolean initialize() {
        this.sgc = new MainGraphicsContext(getConsole(), this);
        isBackgroundScript = true;
        taskManager = new TaskManager(tasks, this);
        //do a starter task to get it started
        inventoryManagementTask = new InventoryManagementTask(this);
        tasks.add(new InventoryManagementTask(this));
        if(config.getProperty("lootToPickup") != null) {
            String lootToPickupString = config.getProperty("lootToPickup");
            lootToPickup = new ArrayList<>(Arrays.asList(lootToPickupString.split(",")));
            lootToPickup.removeIf(lootName -> lootName.trim().isEmpty() || !lootName.matches(".*[a-zA-Z]+.*"));
        }
        if(config.getProperty("chatMessagesToCheck") != null) {
            String chatMessageString = config.getProperty("chatMessagesToCheck");
            chatMessagesToCheck = new ArrayList<>(Arrays.asList(chatMessageString.split(",")));
            chatMessagesToCheck.removeIf(chatMessage -> chatMessage.trim().isEmpty());
        }
        if(config.getProperty("WebHookURL") != null)
             WebHookURL = config.getProperty("WebHookURL");

        subscribe(ChatMessageEvent.class, chatMessageEvent -> {
            String message = chatMessageEvent.getMessage().toLowerCase();
            message = message.replaceAll("<col=.*?>|</col>", ""); // This line removes the <col> and </col> tags
            for (String chatMessage : chatMessagesToCheck) {
                chatMessage = chatMessage.toLowerCase();
                if (message.contains(chatMessage)) {
                    sendDiscordWebhook("Chat Message", "Chat Message: "+message);
                    if(windowsNotification)
                    {
                        sendWindowsNotification("Chat Message", message);
                    }
                }
            }
            if (message.contains("you have killed")) {
                String[] splitMessage = message.split(" ");
                String killCountString = splitMessage[3].replace(",", "");
                killCount = Integer.parseInt(killCountString);
            }
        });

        return super.initialize();
    }



    public void saveConfig(){
        String lootToPickupString = String.join(",", lootToPickup);
        String chatMessageString = String.join(",", chatMessagesToCheck);
        config.addProperty("lootToPickup", lootToPickupString);
        config.addProperty("WebHookURL", WebHookURL);
        config.addProperty("chatMessagesToCheck", chatMessageString);
        config.save();
    }
    @Override
    public void onDeactivation() {
        saveConfig();
        super.uninitialize();
    }



    @Override
    public void onLoop() {
        if(!runScript)
        {
            return;
        }
        try {
            taskManager.runTasks();
            if(levelUpNotification) {
                checkSkillLevelUp();
                Execution.delay(RandomGenerator.nextInt(1000, 2000));
            }
            if(Client.getGameState() != Client.GameState.LOGGED_IN && LogoutNotification)
            {
                sendDiscordWebhook("Player Logged Out!", "Player Logged Out!");
                runScript = false;
            }
            if(Client.getLocalPlayer().getCurrentHealth() <= 0 && Client.getGameState() == Client.GameState.LOGGED_IN && LogoutNotification)
            {
                if(Backpack.isFull()) {
                    sendDiscordWebhook("Player Died With a full backpack", "Player Died With a full backpack!");
                }else {
                    sendDiscordWebhook("Player Died!", "Player Died!");
                }
                Execution.delayWhile(RandomGenerator.nextInt(6000, 12000), () -> Client.getLocalPlayer().getCurrentHealth() <= 0);
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
        if (!lootNameInput.trim().isEmpty() && lootNameInput.matches(".*[a-zA-Z]+.*")) {
            lootToPickup.add(lootNameInput);
            lootCount.put(lootNameInput, 0); // Initialize count to 0
        }
    }

    public void addChatMessageToCheck(String chatMessage) {
        if (!chatMessage.trim().isEmpty()) {
            chatMessagesToCheck.add(chatMessage);
        }
    }

    public void removeChatMessageToCheck(String chatMessage) {
        chatMessagesToCheck.remove(chatMessage);
    }

    public boolean windowsNotification;

    public void sendWindowsNotification(String title, String message) {
        if (windowsNotification) {
            if (SystemTray.isSupported()) {
                try {
                    SystemTray tray = SystemTray.getSystemTray();
                    // Create an empty image for the TrayIcon
                    Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                    TrayIcon trayIcon = new TrayIcon(image, "BotWithUs Notification");
                    trayIcon.setImageAutoSize(true);
                    tray.add(trayIcon);
                    trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("System tray not supported!");
            }
        }
    }

    public List<String> getLootToPickup() {
        return lootToPickup;
    }
}
