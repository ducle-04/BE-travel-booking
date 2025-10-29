package com.travel.travelbooking.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được rỗng");
        }
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ hỗ trợ file ảnh (jpg, png, ...)");
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "image",
                "folder", "destinations"
        ));
        return (String) uploadResult.get("secure_url");
    }

    public String uploadVideo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File video không được rỗng");
        }
        if (!file.getContentType().startsWith("video/")) {
            throw new IllegalArgumentException("Chỉ hỗ trợ file video (mp4, mov, ...)");
        }
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "video",
                "folder", "tours/videos"
        ));
        return (String) uploadResult.get("secure_url");
    }
}