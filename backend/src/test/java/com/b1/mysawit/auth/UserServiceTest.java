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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WorkerAssignmentRepository assignmentRepository;
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
    void testGetAllUsers_WithSearchAndRoleFilters() {
        User matching = buildUser(1L, "Budi Mandor", "budi@mysawit.com", User.Role.Mandor);
        User wrongName = buildUser(2L, "Ani Mandor", "ani@mysawit.com", User.Role.Mandor);
        User wrongEmail = buildUser(3L, "Budi Lain", "budi@example.com", User.Role.Mandor);
        User wrongRole = buildUser(4L, "Budi Buruh", "budi.buruh@mysawit.com", User.Role.Buruh);

        when(userRepository.findAll()).thenReturn(List.of(matching, wrongName, wrongEmail, wrongRole));

        List<UserResponse> result = userService.getAllUsers("budi", "mysawit", "Mandor");

        assertThat(result).extracting(UserResponse::getId).containsExactly(1L);
    }

    @Test
    void testGetAllUsers_BlankFiltersReturnAll() {
        User user = buildUser(1L, "Budi", "budi@mysawit.com", User.Role.Buruh);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.getAllUsers(" ", "", null);

        assertThat(result).hasSize(1);
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
    void testAssignWorkerToMandor_MandorNotFound() {
        User worker = new User(); worker.setId(1L); worker.setRole(User.Role.Buruh);

        when(userRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignWorkerToMandor(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void testAssignWorkerToMandor_InvalidMandorRole() {
        User worker = new User(); worker.setId(1L); worker.setRole(User.Role.Buruh);
        User notMandor = new User(); notMandor.setId(2L); notMandor.setRole(User.Role.Supir);

        when(userRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(userRepository.findById(2L)).thenReturn(Optional.of(notMandor));

        assertThatThrownBy(() -> userService.assignWorkerToMandor(1L, 2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("bukan Mandor");
    }

    @Test
    void testAssignWorkerToMandor_ReassignmentClosesExistingAssignment() {
        User worker = new User(); worker.setId(1L); worker.setRole(User.Role.Buruh);
        User mandor = new User(); mandor.setId(2L); mandor.setRole(User.Role.Mandor);
        WorkerAssignment existing = new WorkerAssignment();
        existing.setWorker(worker);

        when(userRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(userRepository.findById(2L)).thenReturn(Optional.of(mandor));
        when(assignmentRepository.findByWorkerIdAndUnassignedAtIsNull(1L)).thenReturn(Optional.of(existing));

        userService.assignWorkerToMandor(1L, 2L);

        assertThat(existing.getUnassignedAt()).isNotNull();
        verify(assignmentRepository, times(2)).save(any(WorkerAssignment.class));
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
    void testDeleteUser_NullTargetId() {
        assertThatThrownBy(() -> userService.deleteUser(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wajib diisi");
    }

    @Test
    void testDeleteUser_TargetNotFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(404L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
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

    @Test
    void testUpdateUser_WithoutPasswordKeepsExistingPassword() {
        User user = new User();
        user.setId(1L);
        user.setEmail("old@test.com");
        user.setPasswordHash("old_hash");
        user.setRole(User.Role.Buruh);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        RegisterRequest req = new RegisterRequest("user1", "new@test.com", "Nama Baru", "", "Supir", null);
        UserResponse result = userService.updateUser(1L, req);

        assertThat(result.getRole()).isEqualTo("Supir");
        assertThat(user.getPasswordHash()).isEqualTo("old_hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void testUpdateUser_InvalidRole() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        RegisterRequest req = new RegisterRequest("user1", "new@test.com", "Nama Baru", null, "Pemilik", null);

        assertThatThrownBy(() -> userService.updateUser(1L, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role tidak valid");
    }

    private User buildUser(Long id, String nama, String email, User.Role role) {
        User user = new User();
        user.setId(id);
        user.setNama(nama);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
