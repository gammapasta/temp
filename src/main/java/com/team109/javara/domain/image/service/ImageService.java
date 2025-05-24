package com.team109.javara.domain.image.service;

import com.team109.javara.domain.image.entity.Image;
import com.team109.javara.domain.image.repository.ImageRepository;
import com.team109.javara.domain.member.entity.Member;
import com.team109.javara.domain.vehicle.entity.WantedVehicle;
import com.team109.javara.global.common.exception.ErrorCode;
import com.team109.javara.global.common.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Slf4j
@Service
public class ImageService {

    private final Path fileStorageLocation;
    private final ImageRepository imageRepository;

    @Value("${spring.file.serverImageUrl}")
    private String serverImageUrl;

    public ImageService(@Value("${spring.file.directory}") String fileDirectory, ImageRepository imageRepository) {
        this.fileStorageLocation = Paths.get(fileDirectory).toAbsolutePath().normalize();
        this.imageRepository = imageRepository;

        try {
            Files.createDirectories(this.fileStorageLocation); // 디렉토리가 없으면 생성
        } catch (Exception e) {
            throw new RuntimeException("디렉토리 생성 불가", e);
        }
    }
    @Transactional
    public void saveImage(MultipartFile file, Member member, WantedVehicle wantedVehicle) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // 파일 이름 유효성 검사
        if (originalFileName.contains("..")) {
            throw new GlobalException(ErrorCode.INVALID_FILE_NAME);
        }

        // 파일 확장자 추출
        String fileName= "";
        String fileExtension = "";
        try {
            fileName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            fileExtension = "";
        }

        // 현재시간포함
        String storedFileName = fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + fileExtension;

        // 파일 저장 경로
        Path storedLocation = this.fileStorageLocation.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), storedLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("파일 저장 불가 [" + originalFileName + "] 다시 시도하세요", e);
            throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 불가. 다시 시도하세요.");
        }

        // 프론트엔드에서 접근할 경로
        String imageUrl = serverImageUrl + storedFileName;


        Image image = new Image();
        image.setImageUrl(imageUrl);
        image.setMember(member);
        image.setWantedVehicle(wantedVehicle);
        imageRepository.save(image);
    }


}