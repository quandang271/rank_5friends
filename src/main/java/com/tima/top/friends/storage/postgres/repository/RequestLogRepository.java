package com.tima.top.friends.storage.postgres.repository;

import com.tima.top.friends.storage.postgres.entities.RequestLog;
import org.springframework.data.repository.CrudRepository;

public interface RequestLogRepository  extends CrudRepository<RequestLog, String> {
}
