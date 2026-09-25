package mehedi.stockwatch.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interaso.webpush.WebPushService;
import mehedi.stockwatch.dto.StockResponse;
import mehedi.stockwatch.entity.Currency;
import mehedi.stockwatch.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class WebPushNotificationServiceTest {

    private PushSubscriptionRepository repository;

    private StockRepository stockRepository;

    private WebPushService webPushService;

    private WebPushNotificationService service;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        PushSubscriptionRepository.class
                );

        stockRepository =
                mock(
                        StockRepository.class
                );

        webPushService =
                mock(
                        WebPushService.class
                );


        service =
                new WebPushNotificationService(
                        repository,
                        stockRepository,
                        webPushService,
                        new ObjectMapper()
                );
    }


    @Test
    void shouldNotSendNotificationWhenStockHasNoOwner() {

        StockResponse stock =
                createStock();


        when(
                stockRepository
                        .findOwnerUserIdByStockId(
                                stock.id()
                        )
        ).thenReturn(
                Optional.empty()
        );


        service.sendStockAlert(
                stock,
                new BigDecimal(
                        "105.00"
                )
        );


        verifyNoInteractions(
                repository
        );

        verifyNoInteractions(
                webPushService
        );
    }


    @Test
    void shouldNotSendWhenOwnerHasNoSubscriptions() {

        StockResponse stock =
                createStock();

        Long ownerId =
                42L;


        when(
                stockRepository
                        .findOwnerUserIdByStockId(
                                stock.id()
                        )
        ).thenReturn(
                Optional.of(
                        ownerId
                )
        );


        when(
                repository
                        .findAllByUser_Id(
                                ownerId
                        )
        ).thenReturn(
                List.of()
        );


        service.sendStockAlert(
                stock,
                new BigDecimal(
                        "105.00"
                )
        );


        verify(repository)
                .findAllByUser_Id(
                        ownerId
                );


        verifyNoInteractions(
                webPushService
        );
    }


    @Test
    void shouldQueryOnlyStockOwnersSubscriptions() {

        StockResponse stock =
                createStock();

        Long ownerId =
                42L;


        when(
                stockRepository
                        .findOwnerUserIdByStockId(
                                stock.id()
                        )
        ).thenReturn(
                Optional.of(
                        ownerId
                )
        );


        when(
                repository
                        .findAllByUser_Id(
                                ownerId
                        )
        ).thenReturn(
                List.of()
        );


        service.sendStockAlert(
                stock,
                new BigDecimal(
                        "105.00"
                )
        );


        verify(
                stockRepository
        ).findOwnerUserIdByStockId(
                stock.id()
        );


        verify(
                repository
        ).findAllByUser_Id(
                ownerId
        );


        verify(
                repository,
                never()
        ).findAll();
    }


    private StockResponse createStock() {

        LocalDateTime now =
                LocalDateTime.now();


        return new StockResponse(
                1L,
                "MU",
                "Micron Technology",
                5,
                new BigDecimal(
                        "100.00"
                ),
                Currency.USD,
                new BigDecimal(
                        "103.00"
                ),
                "Test stock",
                true,
                false,
                null,
                now,
                now
        );
    }
}