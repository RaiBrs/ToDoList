package com.todolist.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.todolist.User;
import com.todolist.repository.UserRepository;
import org.junit.jupiter.api.Test;

class UserRepositoryTest {

    // Null users are invalid.
    @Test
    void nullUserIsRejected() {
        UserRepository repository = new UserRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.addUser(null));
    }

    // Duplicate usernames are invalid.
    @Test
    void duplicateUsernameIsRejected() {
        UserRepository repository = new UserRepository();
        repository.addUser(new User("rai", "1234"));

        assertThrows(IllegalArgumentException.class,
                () -> repository.addUser(new User("RAI", "another-password")));
    }

    // Repository searches normalize usernames too.
    @Test
    void normalizedUsernameFindsUser() {
        UserRepository repository = new UserRepository();
        User expectedUser = new User("rai", "1234");
        repository.addUser(expectedUser);

        User foundUser = repository.findByUsername(" RAI ");

        assertEquals(expectedUser, foundUser);
    }
}
