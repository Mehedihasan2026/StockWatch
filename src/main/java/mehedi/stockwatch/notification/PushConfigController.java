package mehedi.stockwatch.notification;

import mehedi.stockwatch.config.VapidProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push")
public class PushConfigController {

    private final VapidProperties vapidProperties;

    public PushConfigController(
            VapidProperties vapidProperties) {

        this.vapidProperties = vapidProperties;
    }

    @GetMapping("/public-key")
    public VapidPublicKeyResponse getPublicKey() {

        return new VapidPublicKeyResponse(
                vapidProperties.getVapidPublicKey()
        );
    }
}