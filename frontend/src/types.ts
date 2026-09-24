export interface AuthUser {
    id: number;
    email: string;
    displayName: string;
}

export interface LoginInput {
    email: string;
    password: string;
}

export interface RegisterInput {
    email: string;
    password: string;
    displayName: string;
}

export interface Stock {
    id: number;
    ticker: string;
    companyName: string;
    shares: number;
    buyPrice: number;
    currency: string;
    targetPrice: number;
    notes: string | null;
    alertEnabled: boolean;
    alertTriggered: boolean;
    lastAlertPrice: number | null;
    createdAt: string;
    updatedAt: string;
}

export interface CurrentPriceResponse {
    ticker: string;
    currentPrice: number;
    currency: string;
}

export interface DashboardStock extends Stock {
    currentPrice: number | null;
}

export interface StockInput {
    ticker: string;
    companyName: string;
    shares: number;
    buyPrice: number;
    currency: "USD";
    targetPrice: number;
    notes: string;
    alertEnabled: boolean;
}

export interface PushSubscriptionPayload {
    endpoint: string;
    p256dh: string;
    auth: string;
}