package mehedi.stockwatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "stockwatch.push")
public class VapidProperties {

    private String vapidPublicKey;
    private String vapidPrivateKey;
    private String vapidSubject;

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    public void setVapidPublicKey(String vapidPublicKey) {
        this.vapidPublicKey = vapidPublicKey;
    }

    public String getVapidPrivateKey() {
        return vapidPrivateKey;
    }

    public void setVapidPrivateKey(String vapidPrivateKey) {
        this.vapidPrivateKey = vapidPrivateKey;
    }

    public String getVapidSubject() {
        return vapidSubject;
    }

    public void setVapidSubject(String vapidSubject) {
        this.vapidSubject = vapidSubject;
    }
}