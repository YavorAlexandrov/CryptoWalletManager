package bg.sofia.uni.fmi.mjt.crypto.wallet;

import bg.sofia.uni.fmi.mjt.crypto.wallet.storage.Storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DaemonUsersSaver extends Thread {

    private final String fileName;
    private final Storage usersStorage;

    public DaemonUsersSaver(String fileName, Storage usersStorage) {
        this.fileName = fileName;
        this.usersStorage = usersStorage;
    }


    @Override
    public void run() {
        try (var writer = new BufferedWriter(new FileWriter(fileName))) {

            usersStorage.saveStorage(writer);

        } catch (IOException e) {
            throw new RuntimeException("A problem occurred while saving the users", e);
        }

    }
}
