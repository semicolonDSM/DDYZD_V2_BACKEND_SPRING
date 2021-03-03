package com.semicolon.spring.entity.club.room;

import com.semicolon.spring.entity.club.Club;
import com.semicolon.spring.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity(name = "room")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean user_looked;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean club_looked;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('C', 'A', 'R', 'S', 'N')")
    private RoomStatus status;

    public void setStatus(String value){
        this.status = RoomStatus.valueOf(value);
    }

}
