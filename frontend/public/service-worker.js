self.addEventListener("push", event => {

    if (!event.data) {
        return;
    }

    const data = event.data.json();

    event.waitUntil(
        self.registration.showNotification(
            data.title,
            {
                body: data.body,

                data: {
                    ticker: data.ticker
                }
            }
        )
    );
});


self.addEventListener(
    "notificationclick",
    event => {

        event.notification.close();

        event.waitUntil(
            clients.matchAll({
                type: "window",
                includeUncontrolled: true
            })
                .then(windowClients => {

                    for (
                        const client of windowClients
                        ) {

                        if (
                            "focus" in client
                        ) {
                            return client.focus();
                        }
                    }

                    return clients.openWindow("/");
                })
        );
    }
);