package com.b1.mysawit.auth.facade;

public interface UserFacade {

    void validateMandorExists(Long userId);

    void validateSupirExists(Long userId);
}