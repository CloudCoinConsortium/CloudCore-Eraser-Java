package com.cloudcore.eraser;

import com.cloudcore.eraser.core.FileSystem;

import java.io.File;
import java.io.IOException;

public class Eraser {

    public static void erase(String account) {
        // "default" is currently "DefaultUser".
        if (account == null || account.equals("default"))
            account = "";

        System.out.println("Erasing " + account);

        // Delete all program-level Logs.
        delete(new File(FileSystem.LogsFolder));
        // Delete all program-level Receipts.
        delete(new File(FileSystem.ReceiptsFolder));
        // Delete all program-level Logs.
        delete(new File(FileSystem.ProgramFolder + FileSystem.LogsPath));
        // Delete all program-level Receipts.
        delete(new File(FileSystem.ProgramFolder + FileSystem.ReceiptsPath));
        // Delete all commands.
        delete(new File(FileSystem.CommandFolder));
        // Delete all command history.
        delete(new File(FileSystem.CommandHistoryFolder));

        FileSystem.createDirectories();
        //FileSystem.createAccountDirectories(account);
    }

    private static void delete(File file) {
        try {
            if (file == null || !file.exists())
                return;

            File[] listFiles = file.listFiles();
            if (listFiles != null)
                for (File childFile : listFiles) {
                    if (childFile.isDirectory()) {
                        delete(childFile);
                    } else {
                        if (!childFile.delete()) {
                            throw new IOException();
                        }
                    }
                }
            if (!file.delete()) {
                System.out.println("cannot delete " + file.toString());
                throw new IOException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
