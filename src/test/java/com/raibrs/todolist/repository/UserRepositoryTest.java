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
        firstRepository.registerUser("Rai.Dev", "secret");

        UserRepository reloadedRepository = new UserRepository(storage);
        User restoredUser = reloadedRepository.findByUsername("rai.dev");

        assertTrue(restoredUser.matchesPassword("secret"));
        assertThrows(IllegalArgumentException.class,
                () -> reloadedRepository.registerUser("RAI.DEV", "another-secret"));
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
