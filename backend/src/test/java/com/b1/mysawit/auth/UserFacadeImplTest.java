package com.b1.mysawit.auth;

import com.b1.mysawit.auth.facade.UserFacadeImpl;
import com.b1.mysawit.auth.facade.UserSummary;
import com.b1.mysawit.domain.User;
import com.b1.mysawit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFacadeImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserFacadeImpl userFacade;

    @Test
    void findUserSummaryById_Found() {
        User user = buildUser(1L, "Budi", "budi@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserSummary> result = userFacade.findUserSummaryById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getNama()).isEqualTo("Budi");
        assertThat(result.get().getEmail()).isEqualTo("budi@test.com");
    }

    @Test
    void findUserSummaryById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserSummary> result = userFacade.findUserSummaryById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findUserSummariesByIds_NullList_ReturnsEmpty() {
        List<UserSummary> result = userFacade.findUserSummariesByIds(null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void findUserSummariesByIds_EmptyList_ReturnsEmpty() {
        List<UserSummary> result = userFacade.findUserSummariesByIds(List.of(), null);
        assertThat(result).isEmpty();
    }

    @Test
    void findUserSummariesByIds_WithSearchFilter() {
        User u1 = buildUser(1L, "Budi Santoso", "budi@test.com");
        User u2 = buildUser(2L, "Ani Rahayu", "ani@test.com");
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

        List<UserSummary> result = userFacade.findUserSummariesByIds(List.of(1L, 2L), "budi");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNama()).isEqualTo("Budi Santoso");
    }

    @Test
    void findUserSummariesByIds_BlankSearchFilter_ReturnsAll() {
        User u1 = buildUser(1L, "Budi", "budi@test.com");
        User u2 = buildUser(2L, "Ani", "ani@test.com");
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(u1, u2));

        List<UserSummary> result = userFacade.findUserSummariesByIds(List.of(1L, 2L), "  ");

        assertThat(result).hasSize(2);
    }

    @Test
    void findUserSummariesByIds_NoFilter_ReturnsAll() {
        User u1 = buildUser(1L, "Budi", "budi@test.com");
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(u1));

        List<UserSummary> result = userFacade.findUserSummariesByIds(List.of(1L), null);

        assertThat(result).hasSize(1);
    }

    private User buildUser(Long id, String nama, String email) {
        User u = new User();
        u.setId(id);
        u.setNama(nama);
        u.setEmail(email);
        return u;
    }
}
