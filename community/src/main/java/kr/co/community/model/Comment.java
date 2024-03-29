package kr.co.community.model;

import kr.co.community.vo.CommentVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 500)
    private String content;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    public Comment(CommentVo commentvo){
        this.content = commentvo.getContent();
    }

    public void setMember(Member member){
        this.member = member;
    }

    public void setPost(Post post){
        this.post = post;
    }

    public boolean isWriter(Member currentMember){
        return this.member.getId() == currentMember.getId();
    }

    public void update(String content){
        this.content=content;
    }
}
