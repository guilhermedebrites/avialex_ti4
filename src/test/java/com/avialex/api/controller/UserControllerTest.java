package com.avialex.api.controller;

import com.avialex.api.model.entity.User;
import com.avialex.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setCpf("12345678900");
    }

    @Test
    void testGetAll_ReturnsListOfUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userService.listUsers(null, null, null, null)).thenReturn(users);

        List<User> response = userController.listUsers(null, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(userService, times(1)).listUsers(null, null, null, null);
    }

    @Test
    void testCreate_ReturnsCreatedUser() {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        ResponseEntity<User> response = userController.createUser(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUser.getEmail(), response.getBody().getEmail());
        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void testGetById_WhenUserExists_ReturnsUser() {
        when(userService.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<User> response = userController.findById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(userService, times(1)).findById(1L);
    }

    @Test
    void testGetById_WhenUserNotExists_ReturnsNotFound() {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.findById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(1)).findById(999L);
    }

    @Test
    void testUpdate_ReturnsUpdatedUser() {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(testUser);

        ResponseEntity<User> response = userController.updateUser(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    @Test
    void testDelete_ReturnsNoContent() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }
}
