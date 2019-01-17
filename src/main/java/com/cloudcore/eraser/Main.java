package com.cloudcore.eraser;

import com.cloudcore.eraser.core.FileSystem;
import com.cloudcore.eraser.server.Command;
import com.cloudcore.eraser.utils.SimpleLogger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        SimpleLogger.writeLog("ServantEraserStarted", "");
        singleRun = isSingleRun(args);
        if (args.length != 0 && Files.exists(Paths.get(args[0]))) {
            System.out.println("New root path: " + args[0]);
            FileSystem.changeRootPath(args[0]);
        }

        FileSystem.createDirectories();

        FolderWatcher watcher = new FolderWatcher(FileSystem.CommandFolder);

        ArrayList<Command> commands = FileSystem.getCommands();
        if (commands.size() > 0)
            for (Command command : commands) {
                Eraser.erase(command.account);
                FileSystem.archiveCommand(command);
                exitIfSingleRun();
            }

        System.out.println("Watching folders at " + FileSystem.CommandFolder + "...");

        while (true) {
            try {
                Thread.sleep(1000);

                if (watcher.newFileDetected()) {
                    commands = FileSystem.getCommands();
                    if (commands.size() > 0)
                        for (Command command : commands) {
                            Eraser.erase(command.account);
                            FileSystem.archiveCommand(command);
                            exitIfSingleRun();
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Uncaught exception - " + e.getLocalizedMessage());
            }
        }
    }

    public static boolean singleRun = false;
    public static boolean isSingleRun(String[] args) {
        for (String arg : args)
            if (arg.equals("singleRun"))
                return true;
        return false;
    }
    public static void exitIfSingleRun() {
        if (singleRun)
            System.exit(0);
    }
}
