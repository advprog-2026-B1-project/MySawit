package com.b1.mysawit.kebun.facade;

import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.domain.DriverAssignment;
import com.b1.mysawit.domain.Kebun;
import com.b1.mysawit.domain.MandorAssignment;
import com.b1.mysawit.kebun.repository.DriverAssignmentRepository;
import com.b1.mysawit.kebun.repository.MandorAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KebunAssignmentFacadeImplTest {

    @Mock private MandorAssignmentRepository mandorRepo;
    @Mock private DriverAssignmentRepository driverRepo;
    @InjectMocks private KebunAssignmentFacadeImpl facade;

    @Test
    void validateMandorInKebun_whenAssigned_doesNotThrow() {
        when(mandorRepo.existsByMandorIdAndUnassignedAtIsNull(1L)).thenReturn(true);
        assertThatCode(() -> facade.validateMandorInKebun(1L)).doesNotThrowAnyException();
    }

    @Test
    void validateMandorInKebun_whenNotAssigned_throwsBusinessRuleViolation() {
        when(mandorRepo.existsByMandorIdAndUnassignedAtIsNull(1L)).thenReturn(false);
        assertThatThrownBy(() -> facade.validateMandorInKebun(1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("belum ditempatkan");
    }

    @Test
    void validateSupirInKebun_whenAssigned_doesNotThrow() {
        when(driverRepo.existsByDriverIdAndUnassignedAtIsNull(2L)).thenReturn(true);
        assertThatCode(() -> facade.validateSupirInKebun(2L)).doesNotThrowAnyException();
    }

    @Test
    void validateSupirInKebun_whenNotAssigned_throwsBusinessRuleViolation() {
        when(driverRepo.existsByDriverIdAndUnassignedAtIsNull(2L)).thenReturn(false);
        assertThatThrownBy(() -> facade.validateSupirInKebun(2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("belum ditempatkan");
    }

    @Test
    void validateSameKebun_whenSameKebun_doesNotThrow() {
        Kebun kebun = new Kebun(); kebun.setId(10L);

        MandorAssignment ma = new MandorAssignment(); ma.setKebun(kebun);
        DriverAssignment da = new DriverAssignment(); da.setKebun(kebun);

        when(mandorRepo.findByMandorIdAndUnassignedAtIsNull(1L)).thenReturn(Optional.of(ma));
        when(driverRepo.findByDriverIdAndUnassignedAtIsNull(2L)).thenReturn(Optional.of(da));

        assertThatCode(() -> facade.validateSameKebun(1L, 2L)).doesNotThrowAnyException();
    }

    @Test
    void validateSameKebun_whenDifferentKebun_throwsBusinessRuleViolation() {
        Kebun kebun1 = new Kebun(); kebun1.setId(10L);
        Kebun kebun2 = new Kebun(); kebun2.setId(20L);

        MandorAssignment ma = new MandorAssignment(); ma.setKebun(kebun1);
        DriverAssignment da = new DriverAssignment(); da.setKebun(kebun2);

        when(mandorRepo.findByMandorIdAndUnassignedAtIsNull(1L)).thenReturn(Optional.of(ma));
        when(driverRepo.findByDriverIdAndUnassignedAtIsNull(2L)).thenReturn(Optional.of(da));

        assertThatThrownBy(() -> facade.validateSameKebun(1L, 2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("kebun yang sama");
    }

    @Test
    void validateSameKebun_whenMandorNotInKebun_throwsBusinessRuleViolation() {
        when(mandorRepo.findByMandorIdAndUnassignedAtIsNull(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> facade.validateSameKebun(1L, 2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("belum ditempatkan");
    }

    @Test
    void validateSameKebun_whenSupirNotInKebun_throwsBusinessRuleViolation() {
        Kebun kebun = new Kebun(); kebun.setId(10L);
        MandorAssignment ma = new MandorAssignment(); ma.setKebun(kebun);

        when(mandorRepo.findByMandorIdAndUnassignedAtIsNull(1L)).thenReturn(Optional.of(ma));
        when(driverRepo.findByDriverIdAndUnassignedAtIsNull(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.validateSameKebun(1L, 2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("belum ditempatkan");
    }
}
