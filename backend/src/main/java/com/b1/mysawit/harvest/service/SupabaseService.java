package com.b1.mysawit.harvest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;


    public String uploadPhoto(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        String endpoint = "/storage/v1/object/" + bucketName + "/" + fileName;

        try {
            WebClient webClient = WebClient.create(supabaseUrl);

            webClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca data file foto: " + e.getMessage());
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Gagal upload ke Supabase: " + e.getResponseBodyAsString());
        }
    }
}
