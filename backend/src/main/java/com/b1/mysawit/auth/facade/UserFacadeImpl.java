package com.b1.mysawit.auth.facade;

import com.b1.mysawit.repository.UserRepository;
import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.common.exception.ResourceNotFoundException;
import com.b1.mysawit.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserFacadeImpl implements UserFacade {

    private final UserRepository userRepository;

    public UserFacadeImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void validateMandorExists(Long userId) {
        validateUserRole(userId, User.Role.Mandor, "Mandor");
    }

    @Override
    public void validateSupirExists(Long userId) {
        validateUserRole(userId, User.Role.Supir, "Supir");
    }

    private void validateUserRole(Long userId, User.Role expectedRole, String roleName) {
        if (userId == null) {
            throw new IllegalArgumentException("userId tidak boleh kosong");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() != expectedRole) {
            throw new BusinessRuleViolationException(
                    "User dengan id '" + userId + "' bukan role " + roleName);
        }
    }
}