package com.b1.mysawit.kebun.service;

import com.b1.mysawit.kebun.dto.KebunCreateRequest;
import com.b1.mysawit.kebun.dto.KebunResponse;
import com.b1.mysawit.kebun.dto.KebunUpdateRequest;

import java.util.List;

public interface KebunService {

    KebunResponse createKebun(KebunCreateRequest request);

    List<KebunResponse> getAllKebun(String nama, String kode);

    KebunResponse getKebunById(Long id);

    KebunResponse updateKebun(Long id, KebunUpdateRequest request);

    void deleteKebun(Long id);
}
