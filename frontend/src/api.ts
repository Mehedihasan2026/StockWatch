import type {
    AuthUser,
    CurrentPriceResponse,
    LoginInput,
    PushSubscriptionPayload,
    RegisterInput,
    Stock,
    MarketMover,
    StockInput
} from "./types";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? "";


interface CsrfResponse {
    headerName: string;
    parameterName: string;
    token: string;
}


let csrfToken: CsrfResponse | null = null;


function apiUrl(path: string): string {
    return `${API_BASE_URL}${path}`;
}


function requiresCsrf(method: string): boolean {

    return [
        "POST",
        "PUT",
        "PATCH",
        "DELETE"
    ].includes(method);
}


async function loadCsrfToken(): Promise<CsrfResponse> {

    const response =
        await fetch(
            apiUrl("/api/auth/csrf"),
            {
                credentials: "include"
            }
        );

    if (!response.ok) {
        throw new Error(
            "Could not initialize security token."
        );
    }

    const token: CsrfResponse =
        await response.json();

    csrfToken = token;

    return token;
}


async function getCsrfToken():
    Promise<CsrfResponse> {

    if (csrfToken) {
        return csrfToken;
    }

    return loadCsrfToken();
}


function clearCsrfToken() {
    csrfToken = null;
}


async function apiFetch(
    path: string,
    options: RequestInit = {},
    retryCsrf = true
): Promise<Response> {

    const method =
        (
            options.method ??
            "GET"
        ).toUpperCase();

    const headers =
        new Headers(
            options.headers
        );


    if (requiresCsrf(method)) {

        const csrf =
            await getCsrfToken();

        headers.set(
            csrf.headerName,
            csrf.token
        );
    }


    const response =
        await fetch(
            apiUrl(path),
            {
                ...options,
                method,
                headers,
                credentials: "include"
            }
        );


    /*
     * A CSRF token is replaced after login,
     * logout, or session changes.
     *
     * Refresh it once and retry.
     */
    if (
        response.status === 403 &&
        requiresCsrf(method) &&
        retryCsrf
    ) {

        clearCsrfToken();

        await loadCsrfToken();

        return apiFetch(
            path,
            options,
            false
        );
    }


    return response;
}


/* =========================
   AUTHENTICATION
   ========================= */


export async function getCurrentUser():
    Promise<AuthUser | null> {

    const response =
        await apiFetch(
            "/api/auth/me"
        );

    if (response.status === 401) {
        return null;
    }

    if (!response.ok) {
        throw new Error(
            "Failed to check login state."
        );
    }

    return response.json();
}
export async function getTopGainers(
    limit = 20
): Promise<MarketMover[]> {

    const response =
        await apiFetch(
            `/api/market/top-gainers?limit=${limit}`
        );

    if (!response.ok) {
        throw new Error(
            "Could not load today's market movers."
        );
    }

    return response.json();
}

export async function login(
    input: LoginInput
): Promise<AuthUser> {

    const response =
        await apiFetch(
            "/api/auth/login",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify(input)
            }
        );


    if (response.status === 401) {

        throw new Error(
            "Invalid email or password."
        );
    }


    if (!response.ok) {

        throw new Error(
            "Login failed."
        );
    }


    const user: AuthUser =
        await response.json();


    /*
     * Spring Security replaces the CSRF
     * token after successful authentication.
     */
    clearCsrfToken();

    await loadCsrfToken();

    return user;
}


export async function register(
    input: RegisterInput
): Promise<AuthUser> {

    const response =
        await apiFetch(
            "/api/auth/register",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify(input)
            }
        );


    if (response.status === 409) {

        throw new Error(
            "An account with this email already exists."
        );
    }


    if (!response.ok) {

        throw new Error(
            "Registration failed."
        );
    }


    return response.json();
}


export async function logout():
    Promise<void> {

    const response =
        await apiFetch(
            "/api/auth/logout",
            {
                method: "POST"
            }
        );


    if (
        !response.ok &&
        response.status !== 204
    ) {

        throw new Error(
            "Logout failed."
        );
    }


    clearCsrfToken();
}


/* =========================
   STOCKS
   ========================= */


export async function getStocks():
    Promise<Stock[]> {

    const response =
        await apiFetch(
            "/api/stocks"
        );


    if (response.status === 401) {

        throw new Error(
            "Authentication required."
        );
    }


    if (!response.ok) {

        throw new Error(
            "Failed to load stocks"
        );
    }


    return response.json();
}


export async function getCurrentPrice(
    id: number
): Promise<CurrentPriceResponse> {

    const response =
        await apiFetch(
            `/api/stocks/${id}/price`
        );


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
        await apiFetch(
            "/api/stocks",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify(stock)
            }
        );


    if (!response.ok) {

        const message =
            await response.text();

        throw new Error(
            message ||
            "Failed to create stock"
        );
    }


    return response.json();
}


export async function updateStock(
    id: number,
    stock: StockInput
): Promise<Stock> {

    const response =
        await apiFetch(
            `/api/stocks/${id}`,
            {
                method: "PUT",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify(stock)
            }
        );


    if (!response.ok) {

        const message =
            await response.text();

        throw new Error(
            message ||
            "Failed to update stock"
        );
    }


    return response.json();
}


export async function deleteStock(
    id: number
): Promise<void> {

    const response =
        await apiFetch(
            `/api/stocks/${id}`,
            {
                method: "DELETE"
            }
        );


    if (!response.ok) {

        throw new Error(
            "Failed to delete stock"
        );
    }
}


/* =========================
   PUSH
   ========================= */


export async function getVapidPublicKey():
    Promise<string> {

    const response =
        await apiFetch(
            "/api/push/public-key"
        );


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
        await apiFetch(
            "/api/push-subscriptions",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body:
                    JSON.stringify(
                        subscription
                    )
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
        await apiFetch(
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