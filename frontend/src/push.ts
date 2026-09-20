import {
    getVapidPublicKey,
    removePushSubscription,
    savePushSubscription
} from "./api";


function urlBase64ToArrayBuffer(
    base64String: string
): ArrayBuffer {

    const padding =
        "=".repeat(
            (4 - base64String.length % 4) % 4
        );

    const base64 =
        (base64String + padding)
            .replace(/-/g, "+")
            .replace(/_/g, "/");

    const rawData =
        window.atob(base64);

    const buffer =
        new ArrayBuffer(rawData.length);

    const bytes =
        new Uint8Array(buffer);

    for (
        let i = 0;
        i < rawData.length;
        i++
    ) {
        bytes[i] =
            rawData.charCodeAt(i);
    }

    return buffer;
}


export async function isPushEnabled():
Promise<boolean> {

    if (!("serviceWorker" in navigator)) {
        return false;
    }

    const registration =
        await navigator.serviceWorker
            .getRegistration();

    if (!registration) {
        return false;
    }

    const subscription =
        await registration.pushManager
            .getSubscription();

    return subscription !== null;
}


export async function enablePush():
Promise<void> {

    if (!("serviceWorker" in navigator)) {
        throw new Error(
            "Service workers are not supported."
        );
    }

    if (!("PushManager" in window)) {
        throw new Error(
            "Push notifications are not supported."
        );
    }

    const registration =
        await navigator.serviceWorker.register(
            "/service-worker.js"
        );

    let permission =
        Notification.permission;

    if (permission !== "granted") {
        permission =
            await Notification.requestPermission();
    }

    if (permission !== "granted") {
        throw new Error(
            "Notification permission was not granted."
        );
    }

    const publicKey =
        await getVapidPublicKey();

    let subscription =
        await registration.pushManager
            .getSubscription();

    if (!subscription) {

        subscription =
            await registration.pushManager
                .subscribe({
                    userVisibleOnly: true,

                    applicationServerKey:
                        urlBase64ToArrayBuffer(
                            publicKey
                        )
                });
    }

    const json =
        subscription.toJSON();

    if (
        !json.endpoint ||
        !json.keys?.p256dh ||
        !json.keys?.auth
    ) {
        throw new Error(
            "Browser returned an invalid push subscription."
        );
    }

    await savePushSubscription({
        endpoint: json.endpoint,
        p256dh: json.keys.p256dh,
        auth: json.keys.auth
    });
}


export async function disablePush():
Promise<void> {

    const registration =
        await navigator.serviceWorker
            .getRegistration();

    if (!registration) {
        return;
    }

    const subscription =
        await registration.pushManager
            .getSubscription();

    if (!subscription) {
        return;
    }

    await removePushSubscription(
        subscription.endpoint
    );

    await subscription.unsubscribe();
}