package com.semicolon.spring.entity.club.club_head;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.semicolon.spring.entity.club.Club;
import com.semicolon.spring.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;



@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "club_head")
public class ClubHead {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "club_id")
    @JsonBackReference
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public ClubHead setUser(User user){
        this.user = user;
        return this;
    }

}
