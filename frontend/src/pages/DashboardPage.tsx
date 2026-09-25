import {
    type FormEvent,
    useEffect,
    useMemo,
    useRef,
    useState
} from "react";

import {
    createStock,
    deleteStock,
    getCurrentPrice,
    getStocks,
    getVapidPublicKey,
    removePushSubscription,
    savePushSubscription,
    searchStocks,
    updateStock
} from "../api";

import type {
    DashboardStock,
    Stock,
    StockInput,
    StockSearchResult
} from "../types";

import "../App.css";


interface DashboardPageProps {
    displayName: string;
    onLogout: () => Promise<void>;
}


interface StockFormState {
    ticker: string;
    companyName: string;
    shares: string;
    buyPrice: string;
    targetPrice: string;
    notes: string;
    alertEnabled: boolean;
}


const EMPTY_FORM: StockFormState = {
    ticker: "",
    companyName: "",
    shares: "",
    buyPrice: "",
    targetPrice: "",
    notes: "",
    alertEnabled: true
};


export default function DashboardPage({
                                          displayName,
                                          onLogout
                                      }: DashboardPageProps) {

    const [stocks, setStocks] =
        useState<DashboardStock[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [refreshing, setRefreshing] =
        useState(false);

    const [saving, setSaving] =
        useState(false);

    const [loggingOut, setLoggingOut] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const [message, setMessage] =
        useState<string | null>(null);

    const [editingStock, setEditingStock] =
        useState<Stock | null>(null);

    const [form, setForm] =
        useState<StockFormState>(
            EMPTY_FORM
        );

    const [
        tickerResults,
        setTickerResults
    ] = useState<StockSearchResult[]>([]);

    const [
        tickerSearching,
        setTickerSearching
    ] = useState(false);

    const [
        tickerSearchOpen,
        setTickerSearchOpen
    ] = useState(false);

    const [
        notificationEnabled,
        setNotificationEnabled
    ] = useState(false);

    const [
        notificationLoading,
        setNotificationLoading
    ] = useState(false);

    const skipNextTickerSearch =
        useRef(false);

    const formSectionRef =
        useRef<HTMLElement | null>(
            null
        );


    useEffect(() => {

        void loadPortfolio(true);
        void checkNotificationStatus();

        const interval =
            window.setInterval(
                () => {
                    void loadPortfolio(false);
                },
                60_000
            );

        return () => {
            window.clearInterval(interval);
        };

    }, []);


    useEffect(() => {

        if (skipNextTickerSearch.current) {
            skipNextTickerSearch.current = false;
            return;
        }

        const query =
            form.ticker.trim();

        if (query.length < 1) {
            setTickerResults([]);
            setTickerSearchOpen(false);
            return;
        }

        let cancelled = false;

        const timeout =
            window.setTimeout(
                async () => {

                    setTickerSearching(true);

                    try {

                        const results =
                            await searchStocks(
                                query,
                                6
                            );

                        if (!cancelled) {
                            setTickerResults(results);
                            setTickerSearchOpen(
                                results.length > 0
                            );
                        }

                    } catch (searchError) {

                        console.error(
                            "Could not search ticker",
                            searchError
                        );

                        if (!cancelled) {
                            setTickerResults([]);
                            setTickerSearchOpen(false);
                        }

                    } finally {

                        if (!cancelled) {
                            setTickerSearching(false);
                        }
                    }
                },
                350
            );

        return () => {
            cancelled = true;
            window.clearTimeout(timeout);
        };

    }, [form.ticker]);


    const portfolioSummary =
        useMemo(() => {

            let invested = 0;
            let currentValue = 0;

            for (const stock of stocks) {

                invested +=
                    Number(stock.buyPrice)
                    *
                    stock.shares;

                currentValue +=
                    (
                        stock.currentPrice
                        ??
                        Number(stock.buyPrice)
                    )
                    *
                    stock.shares;
            }

            const profitLoss =
                currentValue - invested;

            const returnPercent =
                invested > 0
                    ? (
                    profitLoss /
                    invested
                ) * 100
                    : 0;

            return {
                invested,
                currentValue,
                profitLoss,
                returnPercent
            };

        }, [stocks]);


    async function loadPortfolio(
        showLoading: boolean
    ) {

        if (showLoading) {
            setLoading(true);
        }

        setError(null);

        try {

            const stockData =
                await getStocks();

            const stocksWithPrices =
                await Promise.all(

                    stockData.map(
                        async stock => {

                            try {

                                const priceResponse =
                                    await getCurrentPrice(
                                        stock.id
                                    );

                                return {
                                    ...stock,
                                    currentPrice:
                                        Number(
                                            priceResponse.currentPrice
                                        ),
                                    lastUpdated:
                                    priceResponse.lastUpdated,
                                    marketStatus:
                                    priceResponse.marketStatus,
                                    exchangeTimezone:
                                    priceResponse.exchangeTimezone
                                };

                            } catch (priceError) {

                                console.error(
                                    `Could not load price for ${stock.ticker}`,
                                    priceError
                                );

                                return {
                                    ...stock,
                                    currentPrice: null,
                                    lastUpdated: null,
                                    marketStatus: null,
                                    exchangeTimezone: null
                                };
                            }
                        }
                    )
                );

            setStocks(stocksWithPrices);

        } catch (loadError) {

            console.error(loadError);

            setError(
                loadError instanceof Error
                    ? loadError.message
                    : "Could not load your portfolio."
            );

        } finally {

            if (showLoading) {
                setLoading(false);
            }
        }
    }


    async function refreshPrices() {

        setRefreshing(true);
        setMessage(null);

        try {

            await loadPortfolio(false);

            setMessage(
                "Prices refreshed."
            );

        } finally {

            setRefreshing(false);
        }
    }


    function resetForm() {

        setEditingStock(null);
        setForm(EMPTY_FORM);
        setTickerResults([]);
        setTickerSearchOpen(false);
    }


    function editStock(
        stock: Stock
    ) {

        skipNextTickerSearch.current =
            true;

        setEditingStock(stock);

        setForm({
            ticker: stock.ticker,
            companyName: stock.companyName,
            shares: String(stock.shares),
            buyPrice: String(stock.buyPrice),
            targetPrice: String(stock.targetPrice),
            notes: stock.notes ?? "",
            alertEnabled: stock.alertEnabled
        });

        setError(null);
        setMessage(null);

        window.setTimeout(
            () => {

                formSectionRef.current
                    ?.scrollIntoView({
                        behavior: "smooth",
                        block: "start"
                    });
            },
            0
        );
    }


    function selectTicker(
        result: StockSearchResult
    ) {

        skipNextTickerSearch.current =
            true;

        setForm(
            current => ({
                ...current,
                ticker: result.ticker,
                companyName:
                result.companyName
            })
        );

        setTickerResults([]);
        setTickerSearchOpen(false);
    }


    async function handleSubmit(
        event: FormEvent<HTMLFormElement>
    ) {

        event.preventDefault();

        setSaving(true);
        setError(null);
        setMessage(null);

        try {

            const shares =
                Number(form.shares);

            const buyPrice =
                Number(form.buyPrice);

            const targetPrice =
                Number(form.targetPrice);

            if (
                !Number.isInteger(shares)
                ||
                shares <= 0
            ) {

                throw new Error(
                    "Shares must be a positive whole number."
                );
            }

            if (
                !Number.isFinite(buyPrice)
                ||
                buyPrice <= 0
            ) {

                throw new Error(
                    "Buy price must be greater than zero."
                );
            }

            if (
                !Number.isFinite(targetPrice)
                ||
                targetPrice <= 0
            ) {

                throw new Error(
                    "Target price must be greater than zero."
                );
            }

            const input: StockInput = {
                ticker:
                    form.ticker
                        .trim()
                        .toUpperCase(),

                companyName:
                    form.companyName
                        .trim(),

                shares,
                buyPrice,
                currency: "USD",
                targetPrice,

                notes:
                    form.notes.trim(),

                alertEnabled:
                form.alertEnabled
            };

            if (editingStock) {

                await updateStock(
                    editingStock.id,
                    input
                );

                setMessage(
                    `${input.ticker} updated.`
                );

            } else {

                await createStock(input);

                setMessage(
                    `${input.ticker} added.`
                );
            }

            resetForm();

            await loadPortfolio(false);

        } catch (saveError) {

            console.error(saveError);

            setError(
                saveError instanceof Error
                    ? saveError.message
                    : "Could not save stock."
            );

        } finally {

            setSaving(false);
        }
    }


    async function handleDelete(
        stock: Stock
    ) {

        const confirmed =
            window.confirm(
                `Delete ${stock.ticker} from StockWatch?`
            );

        if (!confirmed) {
            return;
        }

        try {

            setError(null);
            setMessage(null);

            await deleteStock(stock.id);

            if (
                editingStock?.id ===
                stock.id
            ) {
                resetForm();
            }

            setMessage(
                `${stock.ticker} deleted.`
            );

            await loadPortfolio(false);

        } catch (deleteError) {

            console.error(deleteError);

            setError(
                deleteError instanceof Error
                    ? deleteError.message
                    : "Could not delete stock."
            );
        }
    }


    async function handleAlertToggle(
        stock: Stock
    ) {

        try {

            setError(null);
            setMessage(null);

            const input: StockInput = {
                ticker: stock.ticker,
                companyName:
                stock.companyName,
                shares: stock.shares,

                buyPrice:
                    Number(stock.buyPrice),

                currency: "USD",

                targetPrice:
                    Number(stock.targetPrice),

                notes:
                    stock.notes ?? "",

                alertEnabled:
                    !stock.alertEnabled
            };

            const updated =
                await updateStock(
                    stock.id,
                    input
                );

            setStocks(
                current =>
                    current.map(
                        item =>
                            item.id === stock.id
                                ? {
                                    ...item,
                                    ...updated
                                }
                                : item
                    )
            );

            setMessage(
                `${stock.ticker} alert ${
                    updated.alertEnabled
                        ? "enabled"
                        : "disabled"
                }.`
            );

        } catch (toggleError) {

            console.error(toggleError);

            setError(
                toggleError instanceof Error
                    ? toggleError.message
                    : "Could not update alert."
            );
        }
    }


    async function checkNotificationStatus() {

        if (
            !(
                "serviceWorker"
                in navigator
            )
        ) {
            return;
        }

        try {

            const registration =
                await navigator
                    .serviceWorker
                    .getRegistration();

            if (!registration) {
                return;
            }

            const subscription =
                await registration
                    .pushManager
                    .getSubscription();

            if (!subscription) {
                setNotificationEnabled(false);
                return;
            }

            const json =
                subscription.toJSON();

            if (
                json.endpoint
                &&
                json.keys?.p256dh
                &&
                json.keys?.auth
            ) {

                await savePushSubscription({
                    endpoint:
                    json.endpoint,
                    p256dh:
                    json.keys.p256dh,
                    auth:
                    json.keys.auth
                });
            }

            setNotificationEnabled(true);

        } catch (pushError) {

            console.error(
                "Could not check push status",
                pushError
            );
        }
    }


    async function enableNotifications() {

        setNotificationLoading(true);
        setError(null);
        setMessage(null);

        try {

            if (
                !(
                    "serviceWorker"
                    in navigator
                )
            ) {

                throw new Error(
                    "Service workers are not supported by this browser."
                );
            }

            if (
                !(
                    "PushManager"
                    in window
                )
            ) {

                throw new Error(
                    "Push notifications are not supported by this browser."
                );
            }

            if (
                !(
                    "Notification"
                    in window
                )
            ) {

                throw new Error(
                    "Browser notifications are not supported."
                );
            }

            const permission =
                await Notification
                    .requestPermission();

            if (
                permission !==
                "granted"
            ) {

                throw new Error(
                    "Notification permission was not granted."
                );
            }

            const registration =
                await navigator
                    .serviceWorker
                    .register(
                        "/service-worker.js"
                    );

            await navigator
                .serviceWorker
                .ready;

            let subscription =
                await registration
                    .pushManager
                    .getSubscription();

            if (!subscription) {

                const publicKey =
                    await getVapidPublicKey();

                subscription =
                    await registration
                        .pushManager
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
                !json.endpoint
                ||
                !json.keys?.p256dh
                ||
                !json.keys?.auth
            ) {

                throw new Error(
                    "Invalid browser push subscription."
                );
            }

            await savePushSubscription({
                endpoint:
                json.endpoint,
                p256dh:
                json.keys.p256dh,
                auth:
                json.keys.auth
            });

            setNotificationEnabled(true);

            setMessage(
                "Browser notifications enabled."
            );

        } catch (pushError) {

            console.error(pushError);

            setError(
                pushError instanceof Error
                    ? pushError.message
                    : "Could not enable notifications."
            );

        } finally {

            setNotificationLoading(false);
        }
    }


    async function disableNotifications() {

        setNotificationLoading(true);
        setError(null);
        setMessage(null);

        try {

            if (
                !(
                    "serviceWorker"
                    in navigator
                )
            ) {
                setNotificationEnabled(false);
                return;
            }

            const registration =
                await navigator
                    .serviceWorker
                    .getRegistration();

            if (!registration) {
                setNotificationEnabled(false);
                return;
            }

            const subscription =
                await registration
                    .pushManager
                    .getSubscription();

            if (subscription) {

                await removePushSubscription(
                    subscription.endpoint
                );

                await subscription.unsubscribe();
            }

            setNotificationEnabled(false);

            setMessage(
                "Browser notifications disabled."
            );

        } catch (pushError) {

            console.error(pushError);

            setError(
                pushError instanceof Error
                    ? pushError.message
                    : "Could not disable notifications."
            );

        } finally {

            setNotificationLoading(false);
        }
    }


    async function handleNotifications() {

        if (notificationEnabled) {
            await disableNotifications();
        } else {
            await enableNotifications();
        }
    }


    async function handleLogout() {

        setLoggingOut(true);
        setError(null);

        try {

            await onLogout();

        } catch (logoutError) {

            console.error(logoutError);

            setError(
                "Could not log out."
            );

        } finally {

            setLoggingOut(false);
        }
    }


    return (

        <main className="app">

            <header className="header">

                <div>

                    <h1>
                        StockWatch
                    </h1>

                    <p>
                        Monitor your positions,
                        targets and alerts.
                    </p>

                    <p className="dashboard-user">
                        Signed in as{" "}
                        <strong>
                            {displayName}
                        </strong>
                    </p>

                </div>


                <div className="header-actions">

                    <button
                        type="button"
                        className="secondary-button"
                        disabled={
                            notificationLoading
                        }
                        onClick={() =>
                            void handleNotifications()
                        }
                    >
                        {notificationLoading
                            ? "Please wait..."
                            : notificationEnabled
                                ? "Disable Notifications"
                                : "Enable Notifications"}
                    </button>


                    <button
                        type="button"
                        className="refresh-button"
                        disabled={
                            refreshing
                            ||
                            loading
                        }
                        onClick={() =>
                            void refreshPrices()
                        }
                    >
                        {refreshing
                            ? "Refreshing..."
                            : "Refresh"}
                    </button>


                    <button
                        type="button"
                        className="logout-button"
                        disabled={loggingOut}
                        onClick={() =>
                            void handleLogout()
                        }
                    >
                        {loggingOut
                            ? "Logging out..."
                            : "Log out"}
                    </button>

                </div>

            </header>


            {error && (
                <div className="error">
                    {error}
                </div>
            )}


            {message && (
                <div className="success">
                    {message}
                </div>
            )}


            <section className="portfolio-summary">

                <SummaryCard
                    label="Invested"
                    value={
                        formatMoney(
                            portfolioSummary.invested
                        )
                    }
                />

                <SummaryCard
                    label="Current Value"
                    value={
                        formatMoney(
                            portfolioSummary.currentValue
                        )
                    }
                />

                <SummaryCard
                    label="Profit / Loss"
                    value={
                        formatSignedMoney(
                            portfolioSummary.profitLoss
                        )
                    }
                    positive={
                        portfolioSummary.profitLoss >= 0
                    }
                />

                <SummaryCard
                    label="Total Return"
                    value={
                        formatPercent(
                            portfolioSummary.returnPercent
                        )
                    }
                    positive={
                        portfolioSummary.returnPercent >= 0
                    }
                />

            </section>


            <section
                className="stock-form-section"
                ref={formSectionRef}
            >

                <div className="section-heading">

                    <div>

                        <h2>
                            {editingStock
                                ? "Edit Stock"
                                : "Add Stock"}
                        </h2>

                        <p>
                            Prices are currently
                            tracked in USD.
                        </p>

                    </div>


                    {editingStock && (
                        <button
                            type="button"
                            className="text-button"
                            onClick={resetForm}
                        >
                            Cancel edit
                        </button>
                    )}

                </div>


                <form
                    className="stock-form"
                    onSubmit={handleSubmit}
                >

                    <div className="ticker-search-field">

                        <label>
                            Ticker

                            <input
                                required
                                autoComplete="off"
                                placeholder=
                                    "Search MU or Micron"
                                value={
                                    form.ticker
                                }
                                onFocus={() => {
                                    if (
                                        tickerResults.length > 0
                                    ) {
                                        setTickerSearchOpen(true);
                                    }
                                }}
                                onChange={
                                    event => {
                                        setForm(
                                            current => ({
                                                ...current,
                                                ticker:
                                                    event.target
                                                        .value
                                                        .toUpperCase()
                                            })
                                        );

                                        setTickerSearchOpen(true);
                                    }
                                }
                            />
                        </label>


                        {tickerSearching && (
                            <div className="ticker-search-status">
                                Searching...
                            </div>
                        )}


                        {
                            tickerSearchOpen
                            &&
                            tickerResults.length > 0
                            &&
                            (
                                <div className="ticker-search-results">

                                    {tickerResults.map(
                                        result => (
                                            <button
                                                key={
                                                    `${result.ticker}-${result.exchange}`
                                                }
                                                type="button"
                                                className=
                                                    "ticker-search-result"
                                                onClick={() =>
                                                    selectTicker(result)
                                                }
                                            >
                                                <div>
                                                    <strong>
                                                        {result.ticker}
                                                    </strong>

                                                    <span>
                                                        {
                                                            result.companyName
                                                        }
                                                    </span>
                                                </div>

                                                <small>
                                                    {result.exchange}
                                                </small>
                                            </button>
                                        )
                                    )}

                                </div>
                            )
                        }

                    </div>


                    <label>
                        Company Name

                        <input
                            required
                            placeholder=
                                "Micron Technology, Inc."
                            value={
                                form.companyName
                            }
                            onChange={
                                event =>
                                    setForm(
                                        current => ({
                                            ...current,
                                            companyName:
                                            event.target.value
                                        })
                                    )
                            }
                        />
                    </label>


                    <label>
                        Shares

                        <input
                            required
                            min="1"
                            step="1"
                            type="number"
                            value={
                                form.shares
                            }
                            onChange={
                                event =>
                                    setForm(
                                        current => ({
                                            ...current,
                                            shares:
                                            event.target.value
                                        })
                                    )
                            }
                        />
                    </label>


                    <label>
                        Buy Price (USD)

                        <input
                            required
                            min="0.0001"
                            step="0.0001"
                            type="number"
                            value={
                                form.buyPrice
                            }
                            onChange={
                                event =>
                                    setForm(
                                        current => ({
                                            ...current,
                                            buyPrice:
                                            event.target.value
                                        })
                                    )
                            }
                        />
                    </label>


                    <label>
                        Target Price (USD)

                        <input
                            required
                            min="0.0001"
                            step="0.0001"
                            type="number"
                            value={
                                form.targetPrice
                            }
                            onChange={
                                event =>
                                    setForm(
                                        current => ({
                                            ...current,
                                            targetPrice:
                                            event.target.value
                                        })
                                    )
                            }
                        />
                    </label>


                    <label className="notes-field">
                        Notes

                        <textarea
                            rows={3}
                            placeholder=
                                "Short-term target..."
                            value={
                                form.notes
                            }
                            onChange={
                                event =>
                                    setForm(
                                        current => ({
                                            ...current,
                                            notes:
                                            event.target.value
                                        })
                                    )
                            }
                        />
                    </label>


                    <label className="checkbox-label">

                        <input
                            type="checkbox"
                            checked={
                                form.alertEnabled
                            }
                            onChange={
                                event =>
                                    setForm(
                                        current => ({
                                            ...current,
                                            alertEnabled:
                                            event.target.checked
                                        })
                                    )
                            }
                        />

                        Enable price alert

                    </label>


                    <button
                        className="submit-button"
                        type="submit"
                        disabled={saving}
                    >
                        {saving
                            ? "Saving..."
                            : editingStock
                                ? "Save Changes"
                                : "Add Stock"}
                    </button>

                </form>

            </section>


            {loading && (
                <div className="empty">
                    Loading portfolio...
                </div>
            )}


            {!loading &&
                stocks.length === 0 && (
                    <div className="empty">
                        No stocks are currently
                        being monitored.
                    </div>
                )}


            {!loading && (
                <section className="stock-grid">

                    {stocks.map(
                        stock => (
                            <StockCard
                                key={stock.id}
                                stock={stock}
                                onEdit={() =>
                                    editStock(stock)
                                }
                                onDelete={() =>
                                    void handleDelete(stock)
                                }
                                onToggleAlert={() =>
                                    void handleAlertToggle(stock)
                                }
                            />
                        )
                    )}

                </section>
            )}

        </main>
    );
}


interface SummaryCardProps {
    label: string;
    value: string;
    positive?: boolean;
}


function SummaryCard({
                         label,
                         value,
                         positive
                     }: SummaryCardProps) {

    let className = "";

    if (positive === true) {
        className = "positive";
    }

    if (positive === false) {
        className = "negative";
    }

    return (
        <div className="summary-card">
            <span>{label}</span>
            <strong className={className}>
                {value}
            </strong>
        </div>
    );
}


interface StockCardProps {
    stock: DashboardStock;
    onEdit: () => void;
    onDelete: () => void;
    onToggleAlert: () => void;
}


function StockCard({
                       stock,
                       onEdit,
                       onDelete,
                       onToggleAlert
                   }: StockCardProps) {

    const currentPrice =
        stock.currentPrice;

    const invested =
        Number(stock.buyPrice)
        *
        stock.shares;

    const currentValue =
        currentPrice !== null
            ? currentPrice
            *
            stock.shares
            : null;

    const profitLoss =
        currentValue !== null
            ? currentValue
            -
            invested
            : null;

    const returnPercent =
        profitLoss !== null
        &&
        invested > 0
            ? (
            profitLoss /
            invested
        ) * 100
            : null;

    const targetReached =
        currentPrice !== null
        &&
        currentPrice >=
        Number(stock.targetPrice);

    return (
        <article className="stock-card">

            <div className="stock-heading">

                <div>
                    <h2>
                        {stock.ticker}
                    </h2>

                    <span>
                        {stock.companyName}
                    </span>
                </div>


                <div className="stock-statuses">

                    {stock.marketStatus && (

                        <span
                            className={
                                stock.marketStatus === "OPEN"
                                    ? "market-status open"
                                    : "market-status closed"
                            }
                        >
                            Market {
                            stock.marketStatus === "OPEN"
                                ? "Open"
                                : "Closed"
                        }
                        </span>
                    )}


                    <button
                        type="button"
                        className={
                            stock.alertEnabled
                                ? "badge active alert-badge-button"
                                : "badge alert-badge-button"
                        }
                        onClick={onToggleAlert}
                        title=
                            "Click to enable or disable this stock alert"
                    >
                        {stock.alertEnabled
                            ? "Alert On"
                            : "Alert Off"}
                    </button>

                </div>

            </div>


            <div className="current-price">

                <span>
                    Current price
                </span>

                <strong>
                    {currentPrice !== null
                        ? `${
                            currentPrice.toFixed(2)
                        } ${stock.currency}`
                        : "Unavailable"}
                </strong>


                <small className="last-updated">
                    Last updated: {
                    formatLastUpdated(
                        stock.lastUpdated
                    )
                }
                </small>

            </div>


            <div className="stats">

                <div>
                    <span>Buy price</span>
                    <strong>
                        {
                            Number(
                                stock.buyPrice
                            ).toFixed(2)
                        }
                    </strong>
                </div>

                <div>
                    <span>Target</span>
                    <strong>
                        {
                            Number(
                                stock.targetPrice
                            ).toFixed(2)
                        }
                    </strong>
                </div>

                <div>
                    <span>Shares</span>
                    <strong>
                        {stock.shares}
                    </strong>
                </div>

                <div>
                    <span>Return</span>
                    <strong
                        className={
                            returnPercent === null
                                ? ""
                                : returnPercent >= 0
                                    ? "positive"
                                    : "negative"
                        }
                    >
                        {returnPercent !== null
                            ? formatPercent(
                                returnPercent
                            )
                            : "—"}
                    </strong>
                </div>

            </div>


            {
                (
                    stock.alertTriggered
                    ||
                    targetReached
                )
                &&
                (
                    <div className="triggered">
                        Target reached
                    </div>
                )
            }


            {stock.notes && (
                <p className="notes">
                    {stock.notes}
                </p>
            )}


            <div className="card-actions">

                <button
                    type="button"
                    className="edit-button"
                    onClick={onEdit}
                >
                    Edit
                </button>

                <button
                    type="button"
                    className="delete-button"
                    onClick={onDelete}
                >
                    Delete
                </button>

            </div>

        </article>
    );
}


function formatMoney(
    value: number
): string {

    return new Intl.NumberFormat(
        "en-US",
        {
            style: "currency",
            currency: "USD",
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }
    ).format(value);
}


function formatSignedMoney(
    value: number
): string {

    const formatted =
        formatMoney(
            Math.abs(value)
        );

    if (value > 0) {
        return `+${formatted}`;
    }

    if (value < 0) {
        return `-${formatted}`;
    }

    return formatted;
}


function formatPercent(
    value: number
): string {

    const prefix =
        value > 0
            ? "+"
            : "";

    return `${prefix}${value.toFixed(2)}%`;
}


function formatLastUpdated(
    value: string | null
): string {

    if (!value) {
        return "Unavailable";
    }


    const date =
        new Date(value);


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {
        return "Unavailable";
    }


    return date.toLocaleString(
        [],
        {
            dateStyle: "medium",
            timeStyle: "short"
        }
    );
}


function urlBase64ToArrayBuffer(
    base64String: string
): ArrayBuffer {

    const padding =
        "=".repeat(
            (
                4 -
                (
                    base64String.length
                    %
                    4
                )
            )
            %
            4
        );

    const base64 =
        (
            base64String
            +
            padding
        )
            .replace(
                /-/g,
                "+"
            )
            .replace(
                /_/g,
                "/"
            );

    const rawData =
        window.atob(
            base64
        );

    const buffer =
        new ArrayBuffer(
            rawData.length
        );

    const bytes =
        new Uint8Array(
            buffer
        );

    for (
        let index = 0;
        index < rawData.length;
        index++
    ) {

        bytes[index] =
            rawData.charCodeAt(
                index
            );
    }

    return buffer;
}
