package kr.co.community.vo;

import kr.co.community.model.File;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class PostVo {
    private Long id;

    @NotEmpty(message = "제목은 빈칸일 수 없습니다.")
    private String title;

    @NotEmpty(message = "내용은 빈칸일 수 없습니다.")
    private String content;

    private List<MultipartFile> fs;

    private Long[] deleteFileIds;

    private Long[] saveImgIds;

    public List<Long> getDeleteFileIds(){
        if(Objects.isNull(this.deleteFileIds)){
            return new ArrayList<>();
        }
        return Arrays.asList(this.deleteFileIds);
    }

    public List<Long> getsaveImgIds(){
        if(Objects.isNull(this.saveImgIds)){
            return new ArrayList<>();
        }
        return Arrays.asList(this.saveImgIds);
    }

    public boolean hasFile(){
        return this.fs.isEmpty();
    }
}
