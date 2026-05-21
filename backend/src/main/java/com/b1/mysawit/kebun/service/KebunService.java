package com.b1.mysawit.kebun.service;

import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunDetailResponse;
import com.b1.mysawit.kebun.dto.KebunResponse;
import com.b1.mysawit.kebun.dto.KebunUpdateRequest;

import java.util.List;

public interface KebunService {

    KebunResponse createKebun(KebunCreateRequest request);

    List<KebunResponse> getAllKebun(String nama, String kode);

    KebunResponse getKebunById(Long id);

    KebunDetailResponse getKebunDetail(Long id, String searchNamaSupir);

    KebunResponse updateKebun(Long id, KebunUpdateRequest request);

    void deleteKebun(Long id);

    void assignMandor(Long mandorId, Long kebunId);

    void assignSupir(Long supirId, Long kebunId);

    void reassignMandor(Long mandorId, Long oldKebunId, Long newKebunId);

    void reassignSupir(Long supirId, Long oldKebunId, Long newKebunId);
}
