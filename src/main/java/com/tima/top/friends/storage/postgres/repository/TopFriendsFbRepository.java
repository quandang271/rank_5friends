package com.tima.top.friends.storage.postgres.repository;

import com.tima.top.friends.storage.postgres.entities.TopFriendFb;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopFriendsFbRepository extends CrudRepository<TopFriendFb, String> {
    @Query("select r from TopFriendFb r where r.referenceCode =:referenceCode")
    TopFriendFb findByReferenceCode(@Param("referenceCode") String referenceCode);

    @Query("select r from TopFriendFb r where r.fid =:fid")
    TopFriendFb findByFacabookId(@Param("fid") String fid);
}
