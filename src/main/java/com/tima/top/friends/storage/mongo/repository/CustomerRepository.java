package com.tima.top.friends.storage.mongo.repository;

import com.tima.top.friends.storage.mongo.model.Vay1hCustomer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Vay1hCustomer, String> {

}
