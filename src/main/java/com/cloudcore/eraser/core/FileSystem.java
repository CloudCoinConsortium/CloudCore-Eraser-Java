package com.cloudcore.eraser.core;

import com.cloudcore.eraser.server.Command;
import com.cloudcore.eraser.utils.CoinUtils;
import com.cloudcore.eraser.utils.FileUtils;
import com.cloudcore.eraser.utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class FileSystem {


    /* Fields */

    public static String RootPath = "C:\\Users\\Public\\Documents\\CloudCoin\\Accounts\\DefaultUser\\";
    public static String AccountFolder = "C:\\Users\\Public\\Documents\\CloudCoin\\";

    public static String DetectedPath = File.separator + Config.TAG_DETECTED + File.separator;
    public static String ImportPath = File.separator + Config.TAG_IMPORT + File.separator;
    public static String SuspectPath = File.separator + Config.TAG_SUSPECT + File.separator;

    public static String BankPath = File.separator + Config.TAG_BANK + File.separator;

    public static String LogsPath = File.separator + Config.TAG_LOGS + File.separator;
    public static String ReceiptsPath = File.separator + Config.TAG_RECEIPTS + File.separator;

    public static String CommandFolder = RootPath + Config.TAG_COMMAND + File.separator;
    public static String CommandHistoryFolder = RootPath + Config.TAG_COMMANDHISTORY + File.separator;
    public static String LogsFolder = RootPath + Config.TAG_LOGS + File.separator;
    public static String ReceiptsFolder = RootPath + Config.TAG_RECEIPTS + File.separator;


    /* Methods */

    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(AccountFolder));
            Files.createDirectories(Paths.get(CommandFolder));
            Files.createDirectories(Paths.get(CommandHistoryFolder));
            Files.createDirectories(Paths.get(LogsFolder));
            Files.createDirectories(Paths.get(ReceiptsFolder));

            Files.createDirectories(Paths.get(FileSystem.AccountFolder + FileSystem.LogsPath));
            Files.createDirectories(Paths.get(FileSystem.AccountFolder + FileSystem.ReceiptsPath));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void changeRootPath(String rootPath) {
        try {
            int index = rootPath.indexOf(Config.TAG_ACCOUNTS);
        if (index == -1) {
            RootPath = rootPath;
            if (!rootPath.endsWith(File.separator)) rootPath += File.separator;
            AccountFolder = rootPath + Config.TAG_ACCOUNTS + File.separator;
            Files.createDirectories(Paths.get(AccountFolder));
        }
        else {
            AccountFolder = rootPath;
            RootPath = rootPath.substring(0, index);
        }

        CommandFolder = RootPath + Config.TAG_COMMAND + File.separator;
        CommandHistoryFolder = RootPath + Config.TAG_COMMANDHISTORY + File.separator;
        LogsFolder = RootPath + Config.TAG_LOGS + File.separator;
        ReceiptsFolder = RootPath + Config.TAG_RECEIPTS + File.separator;
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public static boolean createAccountDirectories(String account) {
        try {
            Files.createDirectories(Paths.get(AccountFolder + account + DetectedPath));
            Files.createDirectories(Paths.get(AccountFolder + account + ImportPath));
            Files.createDirectories(Paths.get(AccountFolder + account + SuspectPath));

            Files.createDirectories(Paths.get(AccountFolder + account + BankPath));

            Files.createDirectories(Paths.get(AccountFolder + account + LogsPath));
            Files.createDirectories(Paths.get(AccountFolder + account + ReceiptsPath));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static ArrayList<Command> getCommands() {
        String[] commandFiles = FileUtils.selectFileNamesInFolder(CommandFolder);
        ArrayList<Command> commands = new ArrayList<>();

        for (int i = 0, j = commandFiles.length; i < j; i++) {
            if (!commandFiles[i].contains(Config.MODULE_NAME))
                continue;

            try {
                String json = new String(Files.readAllBytes(Paths.get(CommandFolder + commandFiles[i])));
                Command command = Utils.createGson().fromJson(json, Command.class);
                command.filename = commandFiles[i];
                commands.add(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return commands;
    }

    public static void archiveCommand(Command command) {
        try {
            Files.move(Paths.get(CommandFolder + command.filename),
                    Paths.get(LogsFolder + command.filename),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public static void moveFromImportToSuspectFolder(String account) {
        for (CloudCoin coin : loadFolderCoins(AccountFolder + account + ImportPath)) {
            String fileName = CoinUtils.generateFilename(coin);

            Stack stack = new Stack(coin);
            try {
                Files.write(Paths.get(AccountFolder + account + SuspectPath + fileName + ".stack"),
                        Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
                Files.deleteIfExists(Paths.get(AccountFolder + account + ImportPath + coin.currentFilename));
            } catch (IOException e) {
                System.out.println("FS#DPP: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads all CloudCoins from a specific folder.
     *
     * @param folder the folder to search for CloudCoin files.
     * @return an ArrayList of all CloudCoins in the specified folder.
     */
    public static ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] filenames = FileUtils.selectFileNamesInFolder(folder);
        for (String filename : filenames) {
            int index = filename.lastIndexOf('.');
            if (index == -1) continue;

            String extension = filename.substring(index + 1);

            switch (extension) {
                case "stack":
                    ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(folder, filename);
                    folderCoins.addAll(coins);
                    break;
            }
        }

        return folderCoins;
    }

    public static void updateCoin(CloudCoin coin) {
        updateCoin(coin, ".stack");
    }
    public static void updateCoin(CloudCoin coin, String extension) {
        Gson gson = Utils.createGson();
        try {
            Stack stack = new Stack(coin);
            Files.write(Paths.get(coin.folder + coin.currentFilename), gson.toJson(stack).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void moveCoin(CloudCoin coin, String sourceFolder, String targetFolder, String extension) {
        String fileName = FileUtils.ensureFilenameUnique(CoinUtils.generateFilename(coin), extension, targetFolder);

        try {
            Files.move(Paths.get(sourceFolder + coin.currentFilename), Paths.get(targetFolder + fileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }


    public static void moveAndUpdateCoin(CloudCoin coin, String sourceFolder, String targetFolder) {
        moveAndUpdateCoin(coin, sourceFolder, targetFolder, ".stack");
    }
    public static void moveAndUpdateCoin(CloudCoin coin, String sourceFolder, String targetFolder, String extension) {
        Gson gson = Utils.createGson();
        String fileName = FileUtils.ensureFilenameUnique(CoinUtils.generateFilename(coin), extension, targetFolder);
        try {
            Stack stack = new Stack(coin);
            Files.write(Paths.get(targetFolder + fileName), gson.toJson(stack).getBytes(), StandardOpenOption.CREATE_NEW);
            Files.deleteIfExists(Paths.get(sourceFolder + coin.currentFilename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

