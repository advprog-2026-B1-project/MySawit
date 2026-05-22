package com.b1.mysawit.kebun.facade;

public interface KebunAssignmentFacade {

    void validateMandorInKebun(Long mandorId);

    void validateSupirInKebun(Long supirId);

    void validateSameKebun(Long mandorId, Long supirId);
}
