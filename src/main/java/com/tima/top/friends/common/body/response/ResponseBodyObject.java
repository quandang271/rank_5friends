package com.tima.top.friends.common.body.response;

public interface ResponseBodyObject {

    enum Status{
        DONE,
        TIME_OUT,
        PROCESSING,
        FAIL,
        BAD,
        NOT_FOUND
    }

    static class StatusDefaultDone {
        public static final ResponseBodyObject.Status STATUS= Status.DONE;
        public static final String STATUS_DES= "Thành công";

    }

    static class StatusDefaultBad {
        public static final ResponseBodyObject.Status STATUS= Status.BAD;
        public static final String STATUS_DES= "Facebook tương tác kém, không tìm được top friends";

    }

    static class StatusDefaultTimeout {
        public static final ResponseBodyObject.Status STATUS= Status.TIME_OUT;
        public static final String STATUS_DES= "Không tìm thấy dữ liệu trên redis";

    }

    static class StatusDefaultFail {
        public static final ResponseBodyObject.Status STATUS= Status.FAIL;
        public static final String STATUS_DES_VALIDATE= "Lỗi truy vấn, kiểm tra lại thông tin. Hoặc liên hệ admin";
        public static final String STATUS_DES_SERVICE= "Lỗi service. Liên hệ admin";

    }
    static class StatusDefaultProcessing {
        public static final ResponseBodyObject.Status STATUS= Status.PROCESSING;
        public static final String STATUS_DES= "Đang chờ xử lý";

    }

    static class StatusDefaultNotFound {
        public static final ResponseBodyObject.Status STATUS= Status.NOT_FOUND;
        public static final String STATUS_DES= "uid không có fababook";

    }
}
