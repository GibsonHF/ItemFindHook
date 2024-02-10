package net.botwithus.debug;

import net.botwithus.api.game.hud.Dialog;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.queries.builders.items.GroundItemQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.scene.entities.item.GroundItem;

import java.util.List;

public class TaskManager {

        private final List<Task> tasks;
    public enum GameState {
        DEFAULT, FoundITEM
    }
    private final MainScript mainScript;

        public TaskManager(List<Task> tasks, MainScript mainScript) {
            this.tasks = tasks;
            this.mainScript = mainScript;

        }

    public void runTasks() {
        for (Task task : tasks) {
            try {
                GameState gameState = getGameState();

                switch (gameState) {
                    case FoundITEM:
                        switchTo(new InventoryManagementTask(mainScript));
                        break;

                }
                if (task.validate()) {
                    task.perform();
                }
            } catch (Exception e) {
                System.out.println("Exception occurred while executing task: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private GameState getGameState() {
        for(String lootName : mainScript.lootToPickup) {
            EntityResultSet<GroundItem> loot = GroundItemQuery.newQuery().name(lootName, String::contains).results();
            for (GroundItem item : loot) {
                if (item != null) {
                    return GameState.FoundITEM;
                }
            }
        }


        return GameState.DEFAULT;
    }

    public void switchTo(Task task) {
        tasks.clear();
        tasks.add(task);
    }

    public interface Task {
        boolean validate();
        void perform();
    }


}
