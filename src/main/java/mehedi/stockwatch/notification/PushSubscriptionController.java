package mehedi.stockwatch.notification;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push-subscriptions")
public class PushSubscriptionController {

    private final PushSubscriptionService service;

    public PushSubscriptionController(
            PushSubscriptionService service
    ) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(
            @Valid
            @RequestBody
            PushSubscriptionRequest request,
            Authentication authentication
    ) {

        service.saveSubscription(
                request,
                authentication.getName()
        );
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(
            @RequestParam String endpoint,
            Authentication authentication
    ) {

        service.deleteSubscription(
                endpoint,
                authentication.getName()
        );
    }
}