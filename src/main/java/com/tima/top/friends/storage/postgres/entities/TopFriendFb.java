package com.tima.top.friends.storage.postgres.entities;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@ToString
@Table(name = "top_friends_fb")
public class TopFriendFb {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(name="reference_code")
    protected String referenceCode;

    @Column(name="fid")
    protected String fid;

    @Column(name="uid")
    protected String uid;

    @Column(name="top_friends")
    protected String topFriends;

    @Column(name="updated_at")
    protected Timestamp updatedAt;
}
