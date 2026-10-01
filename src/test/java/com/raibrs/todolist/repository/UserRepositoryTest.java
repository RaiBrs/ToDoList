package com.raibrs.todolist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raibrs.todolist.model.Task;
import com.raibrs.todolist.model.User;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UserRepositoryTest {

    // Null users are rejected.
    @Test
    void nullUserIsRejected() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());

        assertThrows(IllegalArgumentException.class, () -> repository.addUser(null));
    }

    // Duplicate usernames are rejected after normalization.
    @Test
    void duplicateUsernameIsRejected() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());
        repository.addUser(new User("rai", "1234"));

        assertThrows(IllegalArgumentException.class,
                () -> repository.addUser(new User("RAI", "another-password")));
    }

    // Lookups also normalize the supplied username.
    @Test
    void normalizedUsernameFindsUser() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());
        User expectedUser = new User("rai", "1234");
        repository.addUser(expectedUser);

        User foundUser = repository.findByUsername(" RAI ");

        assertEquals(expectedUser, foundUser);
    }

    // A registered account remains available after recreating the repository.
    @Test
    void registeredUserIsSavedAndCanBeLoadedAgain() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserRepository firstRepository = new UserRepository(storage);
        firstRepository.registerUser("Rai.Dev", "A-safe-passphrase-2026");

        UserRepository reloadedRepository = new UserRepository(storage);
        User restoredUser = reloadedRepository.findByUsername("rai.dev");

        assertTrue(restoredUser.matchesPassword("A-safe-passphrase-2026"));
        assertThrows(IllegalArgumentException.class,
                () -> reloadedRepository.registerUser("RAI.DEV", "Another-safe-passphrase-2026"));
    }

    // Passwords shorter than eight characters are rejected.
    @Test
    void registrationRejectsShortPassword() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.registerUser("rai", "###"));

        assertEquals("Password must contain at least 8 characters.", exception.getMessage());
    }

    // The minimum length is inclusive, so an eight-character password is allowed.
    @Test
    void registrationAcceptsPasswordWithExactlyEightCharacters() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());

        User user = repository.registerUser("rai", "SafePass");

        assertTrue(user.matchesPassword("SafePass"));
    }

    // A password cannot contain the username, regardless of letter case.
    @Test
    void registrationRejectsPasswordContainingUsernameRegardlessOfCase() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.registerUser("raibarros", "RaiBarros2006--"));

        assertEquals("Password must not contain your username.", exception.getMessage());
    }

    // Common passwords are rejected without regard to letter case.
    @Test
    void registrationRejectsCommonPasswordRegardlessOfCase() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.registerUser("rai", "PASSWORD"));

        assertEquals("Choose a less common password.", exception.getMessage());
    }

    // A successful password change replaces the saved hash and preserves the user's tasks.
    @Test
    void passwordChangeReplacesOldPasswordAndPreservesTasks() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserRepository repository = new UserRepository(storage);
        User user = repository.registerUser("rai", "OldPass8");
        user.addTask(new Task("Study Java"));

        User updatedUser = repository.changePassword("RAI", "OldPass8", "NewPass9");

        assertFalse(updatedUser.matchesPassword("OldPass8"));
        assertTrue(updatedUser.matchesPassword("NewPass9"));
        assertEquals("Study Java", updatedUser.getTask(0).getTitle());

        User restoredUser = new UserRepository(storage).findByUsername("rai");
        assertFalse(restoredUser.matchesPassword("OldPass8"));
        assertTrue(restoredUser.matchesPassword("NewPass9"));
    }

    // An incorrect current password must leave the account unchanged.
    @Test
    void passwordChangeRejectsIncorrectCurrentPassword() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());
        User user = repository.registerUser("rai", "OldPass8");

        assertThrows(IllegalArgumentException.class,
                () -> repository.changePassword("rai", "wrong-pass", "NewPass9"));

        assertTrue(user.matchesPassword("OldPass8"));
        assertFalse(user.matchesPassword("NewPass9"));
    }

    // Reusing the current password would leave the old credential valid, so it is rejected.
    @Test
    void passwordChangeRejectsTheCurrentPasswordAsTheNewPassword() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());
        User user = repository.registerUser("rai", "OldPass8");

        assertThrows(IllegalArgumentException.class,
                () -> repository.changePassword("rai", "OldPass8", "OldPass8"));

        assertTrue(user.matchesPassword("OldPass8"));
    }

    // A failed save must not replace the in-memory password hash.
    @Test
    void passwordChangeLeavesOldPasswordWhenSavingFails() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserRepository repository = new UserRepository(storage);
        User user = repository.registerUser("rai", "OldPass8");
        storage.failOnSave = true;

        assertThrows(IllegalStateException.class,
                () -> repository.changePassword("rai", "OldPass8", "NewPass9"));

        assertTrue(user.matchesPassword("OldPass8"));
        assertFalse(user.matchesPassword("NewPass9"));
    }

    private static class InMemoryUserStorage implements UserStorage {
        private List<User> savedUsers = List.of();
        private boolean failOnSave;

        @Override
        public List<User> loadUsers() {
            return savedUsers;
        }

        @Override
        public void saveUsers(List<User> users) {
            if (failOnSave) {
                throw new IllegalStateException("Simulated storage failure.");
            }

            savedUsers = new ArrayList<>(users);
        }
    }
}
