package kr.co.community.service;

import kr.co.community.model.File;
import kr.co.community.model.Post;
import kr.co.community.repository.FileRepository;
import kr.co.community.vo.PostVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${spring.servlet.multipart.location}")
    public String uploadPath;


    private final FileRepository fileRepository;

    @PostConstruct
    public void initialize(){
        Path attachmentPath = Paths.get(this.uploadPath);
        try{
            if(!Files.exists(attachmentPath)){
                Files.createDirectories(attachmentPath);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Transactional
    public void savePostFile(List<MultipartFile> files, List<Long> deleteFileIds, List<Long> saveImgIds, Post post) throws Exception {

        if(!deleteFileIds.isEmpty()){

            for(Long id: deleteFileIds){
                if(id==null){
                    log.error("null element");
                    continue;
                }
                File file = this.findById(id);
                boolean flag = deleteFile(file);
                if(!flag){
                    throw new FileNotFoundException("파일을 찾을 수 없습니다.");
                }else{
                    flush();
                }
            }
        }

        for(MultipartFile multipartFile : files){
            if(Objects.equals(multipartFile.getOriginalFilename(), "")){
                continue;
            }

            File file = createFile(multipartFile);

            if(file.getFileType().startsWith("image")){
                FileOutputStream thumbnail = new FileOutputStream(new java.io.File(uploadPath, "s_"+file.getFileName()));
                Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnail, 100, 100);
                thumbnail.close();
            }
            file.assignPost(post);
            file = fileRepository.saveAndFlush(file);
            uploadFile(multipartFile, file);
        }

        //썸머노트 이미지 처리
        if(!saveImgIds.isEmpty()){
            for(Long id: saveImgIds){
                File file = this.findById(id);
                file.assignPost(post);
                fileRepository.saveAndFlush(file);
            }
        }
    }

    @Transactional
    public List<File> saveImages(MultipartFile[] multipartFiles) throws IOException {
        List<File> files = new ArrayList<>();
        for (MultipartFile multipartFile: multipartFiles){
            File file = createFile(multipartFile);
            FileOutputStream thumbnail = new FileOutputStream(new java.io.File(uploadPath, "s_"+file.getFileName()));
            Thumbnailator.createThumbnail(multipartFile.getInputStream(), thumbnail, 100, 100);
            thumbnail.close();
            file = fileRepository.save(file);
            uploadFile(multipartFile, file);
            files.add(file);
        }
        return files;
    }


    private File createFile(MultipartFile multipartFile){
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String fileName = String.format("%s", timeStamp);
        String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());

        File file = File.builder()
                .fileName(fileName)
                .originalName(multipartFile.getOriginalFilename())
                .size(multipartFile.getSize())
                .extension(extension)
                .relativePath(String.format("/%s.%s", fileName, extension))
                .fileType(multipartFile.getContentType()).build();
        return file;
    }

    public File findById(Long id) {
        Optional<File> optionalFile = fileRepository.findById(id);
        return optionalFile.orElse(null);
    }

    public List<File> findByPostId(Long id) {
        return fileRepository.findByPostId(id);
    }


    @Transactional
    public boolean deleteFile(File deleteFile){
        fileRepository.delete(deleteFile);

        String path = this.uploadPath + deleteFile.getRelativePath();
        if(path == null){
            return false;
        }
        //썸네일 파일 지우기
        if(deleteFile.getFileType().startsWith("image")){
            String realname = deleteFile.getRelativePath().substring(1,20);
            String thumbnailPath = this.uploadPath + "/s_" + realname;
            FileUtils.deleteQuietly(FileUtils.getFile(thumbnailPath));
        }
        return FileUtils.deleteQuietly(FileUtils.getFile(path));
    }

    public void deleteByPostId(Long id) {
        fileRepository.deleteByPostId(id);
    }

    public void uploadFile(MultipartFile multipartFile, File file) throws IOException {
        Path path = Paths.get(this.uploadPath + file.getRelativePath());
        Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    }

    public ResponseEntity<?> downloadFileById(Long id) throws IOException {
        File file = this.findById(id);
        String encodedOriginalFileName = URLEncoder
                .encode(file.getOriginalName(), "UTF-8")
                .replaceAll("\\+", "%20");
        Path downloadPath = Paths.get(uploadPath + file.getRelativePath());
        Resource resource = new InputStreamResource(Files.newInputStream(downloadPath));
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(file.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedOriginalFileName +"\"")
                .body(resource);
    }

    public void flush() {
        fileRepository.flush();
    }

}
