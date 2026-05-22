package com.b1.mysawit.auth;

import com.b1.mysawit.auth.dto.RegisterRequest;
import com.b1.mysawit.auth.dto.UserResponse;
import com.b1.mysawit.auth.service.UserService;
import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.common.exception.ResourceNotFoundException;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.domain.WorkerAssignment;
import com.b1.mysawit.repository.UserRepository;
import com.b1.mysawit.repository.WorkerAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WorkerAssignmentRepository assignmentRepository;
    @Mock private com.b1.mysawit.repository.MandorDetailRepository mandorDetailRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    void testGetAllUsers_ReturnsSafeResponse() {
        User u = new User();
        u.setId(1L);
        u.setEmail("a@b.com");
        u.setPasswordHash("secret");
        u.setRole(User.Role.Buruh);

        when(userRepository.findAll()).thenReturn(List.of(u));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("a@b.com");
    }

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
    void testAssignWorkerToMandor_WorkerNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignWorkerToMandor(99L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testAssignWorkerToMandor_InvalidRole() {
        User notBuruh = new User(); notBuruh.setId(1L); notBuruh.setRole(User.Role.Supir);
        User mandor = new User(); mandor.setId(2L); mandor.setRole(User.Role.Mandor);

        when(userRepository.findById(1L)).thenReturn(Optional.of(notBuruh));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mandor));

        assertThatThrownBy(() -> userService.assignWorkerToMandor(1L, 2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("bukan Buruh");
    }

    @Test
    void testDeleteUser_Success() {
        lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));

        userService.deleteUser(2L, 1L);

        verify(userRepository, times(1)).deleteById(2L);
    }

    @Test
    void testDeleteUser_CannotDeleteSelf() {
        assertThatThrownBy(() -> userService.deleteUser(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tidak dapat menghapus");
    }

    @Test
    void testGetUserByEmail_Found() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setRole(User.Role.Buruh);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserByEmail("test@test.com");

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getRole()).isEqualTo("Buruh");
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("x@x.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testGetUsersByRole_FilteredByRole() {
        User mandor = new User(); mandor.setId(1L); mandor.setRole(User.Role.Mandor);
        User buruh = new User(); buruh.setId(2L); buruh.setRole(User.Role.Buruh);

        when(userRepository.findAll()).thenReturn(List.of(mandor, buruh));

        List<UserResponse> result = userService.getUsersByRole("Mandor");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo("Mandor");
    }

    @Test
    void testGetUsersByRole_NullRole_ReturnsAll() {
        User u1 = new User(); u1.setId(1L); u1.setRole(User.Role.Admin);
        User u2 = new User(); u2.setId(2L); u2.setRole(User.Role.Buruh);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> result = userService.getUsersByRole(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void testUpdateUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("old@test.com");
        user.setRole(User.Role.Buruh);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(passwordEncoder.encode("newpass")).thenReturn("hashed_new");

        RegisterRequest req = new RegisterRequest("user1", "new@test.com", "Nama Baru", "newpass", "Mandor", null);
        UserResponse result = userService.updateUser(1L, req);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getRole()).isEqualTo("Mandor");
    }
}
