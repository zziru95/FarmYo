package com.ssafy.farmyo.entity;

import com.ssafy.farmyo.common.entity.BaseTime;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Notify extends BaseTime {

    // 식별 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer id;

    // 알람 내용 ?
    private String content;

    // ???
    private String url;

    // 읽음 여부
    @Column(nullable = false)
    private Boolean isRead;

    // 타입 ( 어디서 알림을 보냈는지 )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    // 수신자
    @ManyToOne
    @JoinColumn(name = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;


    @Builder
    public Notify(User receiver, NotificationType notificationType, String content, String url, Boolean isRead) {
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.content = content;
        this.url = url;
        this.isRead = isRead;
    }

    public enum NotificationType{
        CHAT
    }
}