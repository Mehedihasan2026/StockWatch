package mehedi.stockwatch.config;

import com.interaso.webpush.VapidKeys;
import com.interaso.webpush.WebPushService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebPushConfig {

    @Bean
    public VapidKeys vapidKeys(
            VapidProperties vapidProperties) {

        return VapidKeys.fromUncompressedBytes(
                vapidProperties.getVapidPublicKey(),
                vapidProperties.getVapidPrivateKey()
        );
    }

    @Bean
    public WebPushService webPushService(
            VapidProperties vapidProperties,
            VapidKeys vapidKeys) {

        return new WebPushService(
                vapidProperties.getVapidSubject(),
                vapidKeys
        );
    }
}