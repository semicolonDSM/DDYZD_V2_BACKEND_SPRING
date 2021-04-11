package com.semicolon.spring.entity.club.room;

import com.semicolon.spring.entity.club.Club;
import com.semicolon.spring.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.util.Date;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "user_id"}))
@Entity(name = "room")
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
    private boolean userLooked;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean clubLooked;

    private int uOffset;

    private int cOffset;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('C', 'A', 'R', 'S', 'N') default 'C'")
    private RoomStatus status;

    private Date last_date;

    public void setStatus(String value){
        this.status = RoomStatus.valueOf(value);
    }

}
