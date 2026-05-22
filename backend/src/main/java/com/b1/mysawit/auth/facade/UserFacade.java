package com.b1.mysawit.auth.facade;

import java.util.List;
import java.util.Optional;

public interface UserFacade {

    void validateMandorExists(Long userId);

    void validateSupirExists(Long userId);

    Optional<UserSummary> findUserSummaryById(Long userId);

    List<UserSummary> findUserSummariesByIds(List<Long> userIds, String searchNama);
}