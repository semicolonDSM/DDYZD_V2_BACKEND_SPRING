package com.semicolon.spring.entity.feed;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.semicolon.spring.entity.club.Club;
import com.semicolon.spring.entity.feed.feed_flag.FeedFlag;
import com.semicolon.spring.entity.feed.feed_medium.FeedMedium;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "feed")
public class Feed {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "VARCHAR(4001)")
    private String contents;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean pin;

    @OneToMany(mappedBy = "feed", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private final Set<FeedMedium> media = new HashSet<>();

    @CreationTimestamp
    private Date uploadAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @JsonBackReference
    private Club club;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feed", cascade = CascadeType.REMOVE)
    @JsonManagedReference
    private final Set<FeedFlag> feedFlags = new HashSet<>();

    public void modify(String contents){
        this.contents = contents;
    }

    public void changePin(){
        this.pin = !this.pin;
    }

}
