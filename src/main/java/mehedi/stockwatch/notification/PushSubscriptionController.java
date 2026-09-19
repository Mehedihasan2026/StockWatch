package mehedi.stockwatch.notification;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push-subscriptions")
public class PushSubscriptionController {

    private final PushSubscriptionService service;

    public PushSubscriptionController(
            PushSubscriptionService service) {

        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveSubscription(
            @Valid @RequestBody PushSubscriptionRequest request) {

        service.saveSubscription(request);
    }
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubscription(
            @RequestParam String endpoint) {

        service.deleteSubscription(endpoint);
    }
}