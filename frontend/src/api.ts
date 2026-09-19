import type {
    CurrentPriceResponse,
    PushSubscriptionPayload,
    Stock,
    StockInput
} from "./types";


export async function getStocks(): Promise<Stock[]> {

    const response = await fetch("/api/stocks");

    if (!response.ok) {
        throw new Error("Failed to load stocks");
    }

    return response.json();
}


export async function getCurrentPrice(
    id: number
): Promise<CurrentPriceResponse> {

    const response =
        await fetch(`/api/stocks/${id}/price`);

    if (!response.ok) {
        throw new Error(
            `Failed to load price for stock ${id}`
        );
    }

    return response.json();
}


export async function createStock(
    stock: StockInput
): Promise<Stock> {

    const response =
        await fetch("/api/stocks", {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(stock)
        });

    if (!response.ok) {

        const message =
            await response.text();

        throw new Error(
            message || "Failed to create stock"
        );
    }

    return response.json();
}


export async function updateStock(
    id: number,
    stock: StockInput
): Promise<Stock> {

    const response =
        await fetch(`/api/stocks/${id}`, {
            method: "PUT",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(stock)
        });

    if (!response.ok) {

        const message =
            await response.text();

        throw new Error(
            message || "Failed to update stock"
        );
    }

    return response.json();
}


export async function deleteStock(
    id: number
): Promise<void> {

    const response =
        await fetch(`/api/stocks/${id}`, {
            method: "DELETE"
        });

    if (!response.ok) {
        throw new Error(
            "Failed to delete stock"
        );
    }
}


export async function getVapidPublicKey():
    Promise<string> {

    const response =
        await fetch("/api/push/public-key");

    if (!response.ok) {
        throw new Error(
            "Failed to retrieve VAPID public key"
        );
    }

    const data =
        await response.json();

    return data.publicKey;
}


export async function savePushSubscription(
    subscription: PushSubscriptionPayload
): Promise<void> {

    const response =
        await fetch(
            "/api/push-subscriptions",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify(subscription)
            }
        );

    if (!response.ok) {
        throw new Error(
            "Failed to save push subscription"
        );
    }
}


export async function removePushSubscription(
    endpoint: string
): Promise<void> {

    const response =
        await fetch(
            "/api/push-subscriptions?endpoint="
            + encodeURIComponent(endpoint),
            {
                method: "DELETE"
            }
        );

    if (!response.ok) {
        throw new Error(
            "Failed to remove push subscription"
        );
    }
}