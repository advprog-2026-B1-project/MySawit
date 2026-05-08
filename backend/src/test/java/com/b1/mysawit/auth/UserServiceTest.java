package com.b1.mysawit.auth;

import com.b1.mysawit.auth.service.UserService;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.domain.WorkerAssignment;
import com.b1.mysawit.repository.UserRepository;
import com.b1.mysawit.repository.WorkerAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkerAssignmentRepository assignmentRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testAssignWorkerToMandor_Success() {
        User worker = new User(); worker.setId(1L); worker.setRole(User.Role.Buruh);
        User mandor = new User(); mandor.setId(2L); mandor.setRole(User.Role.Mandor);

        when(userRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mandor));
        
        when(assignmentRepository.findByWorkerIdAndUnassignedAtIsNull(1L)).thenReturn(Optional.empty());

        userService.assignWorkerToMandor(1L, 2L);

        verify(assignmentRepository, times(1)).save(any(WorkerAssignment.class)); 
    }

    @Test
    void testAssignWorkerToMandor_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        Exception e = assertThrows(NoSuchElementException.class, () -> {
            userService.assignWorkerToMandor(99L, 2L);
        });
    }

    @Test
    void testDeleteUser_Success() {
        User userToDelete = new User(); 
        userToDelete.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(userToDelete));

        userService.deleteUser(1L, 2L); 
        
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void testDeleteUser_Failed_CannotDeleteSelf() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1L, 1L); 
        });
    }

    // // Test Get User Detail 
    // @Test
    // void testGetUserDetail_NotFound() {
    //     when(userRepository.findById(99L)).thenReturn(Optional.empty());

    //     assertThrows(NoSuchElementException.class, () -> {
    //         userService.getUserDetail(99L);
    //     });
    // }
}