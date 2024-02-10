package net.botwithus.debug;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.rs3.game.queries.builders.items.GroundItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.scene.entities.item.GroundItem;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InventoryManagementTask implements TaskManager.Task {

    private MainScript mainScript;
    public InventoryManagementTask(MainScript mainScript) {
        this.mainScript = mainScript;

    }

    @Override
    public boolean validate() {
        mainScript.println("Validating InventoryManagementTask");
        for(String lootName : mainScript.lootToPickup) {
            int originalId = nametoidconverter(lootName);
            int notedId = originalId + 1;
            EntityResultSet<GroundItem> loot = GroundItemQuery.newQuery().ids(originalId, notedId).results();
            for (GroundItem item : loot) {
                if (item != null) {
                    mainScript.println("Found a " + lootName);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void perform() {
        mainScript.println("Performing InventoryManagementTask");
        //send discord webhook message
        for(String lootName : mainScript.lootToPickup) {
            int originalId = nametoidconverter(lootName);
            int notedId = originalId + 1;
            EntityResultSet<GroundItem> loot = GroundItemQuery.newQuery().ids(originalId, notedId).results();
            for (GroundItem item : loot) {
                if (item != null) {
                    sendDiscordWebhook(lootName, item.getStackSize());
                    mainScript.println("Id of item " + nametoidconverter(lootName));
                    mainScript.lootCount.put(lootName, mainScript.lootCount.get(lootName) + item.getStackSize()); // Increment count
                    Execution.delay(10000);
                }
            }
        }
    }

    public int nametoidconverter(String name) {
        //converts the name of the item to the id of the item
        //this is used to get the id of the item to use in the grounditemquery
        GroundItemQuery query = GroundItemQuery.newQuery().name(name, String::contains);
        EntityResultSet<GroundItem> results = query.results();
        for (GroundItem item : results) {
            mainScript.println("Item: " + item.getName() + " ID: " + item.getId());
            return item.getId();
        }

        return 0;



    }


    public void sendDiscordWebhook(String itemName, int amount) {
        try {
            URL url = new URL(mainScript.WebHookURL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            http.setDoOutput(true);

            String jsonPayload = buildJsonPayloadWithoutImage(itemName, amount);
            byte[] out = jsonPayload.getBytes(StandardCharsets.UTF_8);
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

    private String buildJsonPayloadWithoutImage(String itemName, int amount) {
        // Format the timestamp
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.now());
        return "{\"embeds\":[{"
                + "\"title\":\"Item Drop!\","
                + "\"description\":\"An item has been found!\","
                + "\"color\": 5814783," // Grey color, you can change it as per your preference
                + "\"fields\":["
                + "    {\"name\":\"Item\", \"value\":\"" + itemName + "\", \"inline\":true},"
                + "    {\"name\":\"Amount\", \"value\":\"" + amount + "\", \"inline\":true},"
                + "    {\"name\":\"Time\", \"value\":\"" + timestamp + "\", \"inline\":false}"
                + "]"
                + "}]}";
    }



}