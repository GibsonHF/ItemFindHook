package net.botwithus.debug;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.rs3.game.queries.builders.items.GroundItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class InventoryManagementTask implements TaskManager.Task {

    private MainScript mainScript;
    public InventoryManagementTask(MainScript mainScript) {
        this.mainScript = mainScript;

    }

    @Override
    public boolean validate() {
        mainScript.println("Validating InventoryManagementTask");
        if(!GroundItemQuery.newQuery().name("Scripture of Wen").results().isEmpty() || !GroundItemQuery.newQuery().name("Manuscript of Wen").results().isEmpty())
        {
            return true;
        }
        return false;
    }

    @Override
    public void perform() {
        mainScript.println("Performing InventoryManagementTask");
        //send discord webhook message
        if(!GroundItemQuery.newQuery().name("Scripture of Wen").results().isEmpty()) {
            sendDiscordWebhook("Found a Scripture of Wen");
            mainScript.totalCaughtScriptures++;
            Execution.delay(10000);
        }
        if(!GroundItemQuery.newQuery().name("Manuscript of Wen").results().isEmpty()) {
            sendDiscordWebhook("Found a Manuscript of Wen");
            mainScript.totalCaughtManuScripts++;
            Execution.delay(10000);
        }
    }


    public void sendDiscordWebhook(String content) {
        try {
            URL url = new URL("https://ptb.discord.com/api/webhooks/1205554175082233916/nm8Hpclv93b9yQtXv8OfFZa2BmMLXeTAPLhDhLFie7ZcPHnHWtrUmgqw9kDOfvDW1qvr");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            http.setDoOutput(true);
            String json = "{\"content\":\"```" + content + "```\"}"; // Wrap the content in triple backticks
            byte[] out = json.getBytes();
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}