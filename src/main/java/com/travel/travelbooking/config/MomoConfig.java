package com.travel.travelbooking.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MomoConfig {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint; // https://test-payment.momo.vn/v2/gateway/api/create

    @Value("${momo.redirectUrl}")
    private String redirectUrl; // URL FE sau khi thanh toán xong

    @Value("${momo.ipnUrl}")
    private String ipnUrl; // URL BE nhận callback từ MoMo
}
