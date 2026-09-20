self.addEventListener(
    "push",
    event => {

        if (!event.data) {
            return;
        }

        const data =
            event.data.json();

        const options = {

            body:
                data.body,

            data: {
                ticker:
                    data.ticker
            }
        };

        event.waitUntil(
            self.registration
                .showNotification(
                    data.title,
                    options
                )
        );
    }
);


self.addEventListener(
    "notificationclick",
    event => {

        event.notification.close();

        event.waitUntil(
            clients.openWindow("/")
        );
    }
);