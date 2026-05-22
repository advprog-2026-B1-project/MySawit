package com.b1.mysawit.kebun.facade;

import com.b1.mysawit.common.exception.BusinessRuleViolationException;
import com.b1.mysawit.domain.DriverAssignment;
import com.b1.mysawit.domain.MandorAssignment;
import com.b1.mysawit.kebun.repository.DriverAssignmentRepository;
import com.b1.mysawit.kebun.repository.MandorAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KebunAssignmentFacadeImpl implements KebunAssignmentFacade {

    private final MandorAssignmentRepository mandorAssignmentRepository;
    private final DriverAssignmentRepository driverAssignmentRepository;

    public KebunAssignmentFacadeImpl(
            MandorAssignmentRepository mandorAssignmentRepository,
            DriverAssignmentRepository driverAssignmentRepository) {
        this.mandorAssignmentRepository = mandorAssignmentRepository;
        this.driverAssignmentRepository = driverAssignmentRepository;
    }

    @Override
    public void validateMandorInKebun(Long mandorId) {
        boolean assigned = mandorAssignmentRepository
                .existsByMandorIdAndUnassignedAtIsNull(mandorId);
        if (!assigned) {
            throw new BusinessRuleViolationException(
                    "Mandor dengan id '" + mandorId + "' belum ditempatkan di kebun manapun");
        }
    }

    @Override
    public void validateSupirInKebun(Long supirId) {
        boolean assigned = driverAssignmentRepository
                .existsByDriverIdAndUnassignedAtIsNull(supirId);
        if (!assigned) {
            throw new BusinessRuleViolationException(
                    "Supir dengan id '" + supirId + "' belum ditempatkan di kebun manapun");
        }
    }

    @Override
    public void validateSameKebun(Long mandorId, Long supirId) {
        MandorAssignment mandorAssignment = mandorAssignmentRepository
                .findByMandorIdAndUnassignedAtIsNull(mandorId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Mandor dengan id '" + mandorId + "' belum ditempatkan di kebun manapun"));

        DriverAssignment supirAssignment = driverAssignmentRepository
                .findByDriverIdAndUnassignedAtIsNull(supirId)
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "Supir dengan id '" + supirId + "' belum ditempatkan di kebun manapun"));

        Long mandorKebunId = mandorAssignment.getKebun().getId();
        Long supirKebunId = supirAssignment.getKebun().getId();

        if (!mandorKebunId.equals(supirKebunId)) {
            throw new BusinessRuleViolationException(
                    "Mandor dan Supir harus berada di kebun yang sama untuk membuat pengiriman");
        }
    }
}
