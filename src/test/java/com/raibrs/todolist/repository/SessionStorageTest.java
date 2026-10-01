package com.raibrs.todolist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SessionStorageTest {

    @TempDir
    Path temporaryDirectory;

    // Store only the normalized username and load it for the next application run.
    @Test
    void savesAndLoadsUsernameWithoutAnAtSign() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.txt");
        SessionStorage storage = new SessionStorage(sessionFile);

        storage.saveUsername("Rai.Dev");

        assertEquals("rai.dev", Files.readString(sessionFile));
        assertEquals(Optional.of("rai.dev"), storage.loadUsername());
    }

    // Logging out removes the remembered session.
    @Test
    void clearingSessionDeletesTheFile() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.txt");
        SessionStorage storage = new SessionStorage(sessionFile);
        storage.saveUsername("rai");

        storage.clearSession();

        assertFalse(Files.exists(sessionFile));
        assertTrue(storage.loadUsername().isEmpty());
    }

    // An invalid marker is discarded so the normal login menu remains available.
    @Test
    void malformedUsernameIsCleared() throws IOException {
        Path sessionFile = temporaryDirectory.resolve("session.txt");
        Files.writeString(sessionFile, "@rai");
        SessionStorage storage = new SessionStorage(sessionFile);

        assertTrue(storage.loadUsername().isEmpty());
        assertFalse(Files.exists(sessionFile));
    }
}
