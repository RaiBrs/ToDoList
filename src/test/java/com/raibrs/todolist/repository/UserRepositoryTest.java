package com.raibrs.todolist.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void registrationRejectsShortPassword() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.registerUser("rai", "###"));

        assertEquals("Password must contain at least 12 characters.", exception.getMessage());
    }

    @Test
    void registrationRejectsPasswordContainingUsernameRegardlessOfCase() {
        UserRepository repository = new UserRepository(new InMemoryUserStorage());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> repository.registerUser("raibarros", "RaiBarros2006--"));

        assertEquals("Password must not contain your username.", exception.getMessage());
    }

    private static class InMemoryUserStorage implements UserStorage {
        private List<User> savedUsers = List.of();

        @Override
        public List<User> loadUsers() {
            return savedUsers;
        }

        @Override
        public void saveUsers(List<User> users) {
            savedUsers = new ArrayList<>(users);
        }
    }
}
