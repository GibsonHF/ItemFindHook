package net.botwithus.debug;

import net.botwithus.api.game.hud.Dialog;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.rs3.game.queries.builders.items.GroundItemQuery;

import java.util.List;

public class TaskManager {

        private final List<Task> tasks;
    public enum GameState {
        DEFAULT, FoundScripture, FoundManuScript, FoundITEM
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
                    case FoundManuScript, FoundScripture:
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
        if (!GroundItemQuery.newQuery().name("Manuscript of Wen", String::contains).results().isEmpty()) {
            return GameState.FoundManuScript;
        }

        if (!GroundItemQuery.newQuery().name("Scripture of Wen").results().isEmpty())
        {
            return GameState.FoundScripture;
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
