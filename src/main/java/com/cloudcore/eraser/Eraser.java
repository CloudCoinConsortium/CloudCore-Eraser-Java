package com.cloudcore.eraser;

import com.cloudcore.eraser.core.FileSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Eraser {

    public static void Erase(String account) {
        // "default" is currently "DefaultUser".
        if (account.equals("default"))
            account = "DefaultUser";

        // Delete all program-level Logs.
        delete(new File(FileSystem.LogsFolder));
        // Delete all program-level Receipts.
        delete(new File(FileSystem.ReceiptsFolder));
        // Delete all account-level Logs.
        delete(new File(FileSystem.AccountsFolder + account + FileSystem.LogsPath));
        // Delete all account-level Receipts.
        delete(new File(FileSystem.AccountsFolder + account + FileSystem.ReceiptsPath));
        // Delete all commands.
        delete(new File(FileSystem.CommandsFolder));
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
