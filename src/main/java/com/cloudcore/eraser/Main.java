package com.cloudcore.eraser;

import com.cloudcore.eraser.core.FileSystem;
import com.cloudcore.eraser.server.Command;
import com.cloudcore.eraser.utils.SimpleLogger;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        SimpleLogger.writeLog("ServantEraserStarted", "");
        ArrayList<Command> commands;
        try {
            FileSystem.createDirectories();

            FolderWatcher watcher = new FolderWatcher(FileSystem.CommandsFolder);
            boolean stop = false;

            commands = FileSystem.getCommands();
            if (commands.size() > 0)
                for (Command command : commands) {
                    FileSystem.createAccountDirectories(command.account);
                    Eraser.erase(command.account);
                    FileSystem.archiveCommand(command);
                }

            System.out.println("Watching folders at " + FileSystem.CommandsFolder + "...");

            while (!stop) {
                if (watcher.newFileDetected()) {
                    commands = FileSystem.getCommands();
                    if (commands.size() > 0)
                        for (Command command : commands) {
                            FileSystem.createAccountDirectories(command.account);
                            Eraser.erase(command.account);
                            FileSystem.archiveCommand(command);
                        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Uncaught exception - " + e.getLocalizedMessage());
        }

    }
}
