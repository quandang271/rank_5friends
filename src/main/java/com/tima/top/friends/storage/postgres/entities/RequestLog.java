package com.tima.top.friends.storage.postgres.entities;


import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Data
@Table(name = "top_friends_request_logs")
@ToString
public class RequestLog {

    @Id
    @Column(name="reference_code")
    protected String referenceCode;
    @Column(name="status")
    protected String status;
    @Column(name="status_des")
    protected String statusDes;
    @Column(name="error_code")
    protected int errorCode;
    @Column(name="error_log")
    protected String errorLog;
    @Column(name="request_body")
    protected String requestBody;
    @Column(name="last_update")
    protected Timestamp lastUpdate;

    public RequestLog() {
    }

    public RequestLog(String referenceCode, String status, String statusDes) {
        this.referenceCode = referenceCode;
        this.status = status;
        this.statusDes = statusDes;
        this.errorCode=0;
        this.lastUpdate= new Timestamp(System.currentTimeMillis());
    }

    public RequestLog(String referenceCode, String status, String statusDes, int errorCode, String errorLog) {
        this.referenceCode = referenceCode;
        this.status = status;
        this.statusDes = statusDes;
        this.errorCode = errorCode;
        this.errorLog = errorLog;
        this.lastUpdate= new Timestamp(System.currentTimeMillis());
    }

}
