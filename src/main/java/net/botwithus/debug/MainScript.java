package net.botwithus.debug;

import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Coordinate;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.ScriptGraphicsContext;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.util.ArrayList;
import java.util.List;

public class MainScript extends LoopingScript {


    public int totalCaughtManuScripts;
    public int totalCaughtScriptures;


    public MainScript(String name, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(name, scriptConfig, scriptDefinition);
    }


    public boolean runScript = false;


    TaskManager taskManager;
    List<TaskManager.Task> tasks = new ArrayList<>();

    @Override
    public boolean initialize() {
        this.sgc = new MainGraphicsContext(getConsole(), this);
        this.loopDelay = 590;
        isBackgroundScript = true;
        taskManager = new TaskManager(tasks, this);
        //do a starter task to get it started
        tasks.add(new InventoryManagementTask(this));
        return super.initialize();
    }

    @Override
    public void onLoop() {
        if(!runScript)
        {
            return;
        }
        try {

            taskManager.runTasks();

        }catch (Exception e)
        {
            println(e.getMessage());
        }
    }


    public ScriptGraphicsContext getSgc() {
        return sgc;
    }


}
