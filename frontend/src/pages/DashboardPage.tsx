import {
    type FormEvent,
    useEffect,
    useMemo,
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
    updateStock
} from "../api";

import type {
    DashboardStock,
    Stock,
    StockInput
} from "../types";

import "../App.css";


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


export default function DashboardPage() {

    const [stocks, setStocks] =
        useState<DashboardStock[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [refreshing, setRefreshing] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const [formOpen, setFormOpen] =
        useState(false);

    const [editingStock, setEditingStock] =
        useState<Stock | null>(null);

    const [form, setForm] =
        useState<StockFormState>(
            EMPTY_FORM
        );

    const [saving, setSaving] =
        useState(false);

    const [
        notificationEnabled,
        setNotificationEnabled
    ] = useState(false);

    const [
        notificationLoading,
        setNotificationLoading
    ] = useState(false);


    /* =========================
       INITIAL LOAD
       ========================= */

    useEffect(() => {

        void loadPortfolio();

        void checkNotificationStatus();


        const interval =
            window.setInterval(
                () => {
                    void refreshPrices();
                },
                60_000
            );


        return () => {
            window.clearInterval(
                interval
            );
        };

    }, []);


    /* =========================
       PORTFOLIO CALCULATIONS
       ========================= */

    const portfolioSummary =
        useMemo(() => {

            let invested = 0;
            let currentValue = 0;

            for (const stock of stocks) {

                invested +=
                    Number(stock.buyPrice)
                    *
                    stock.shares;


                if (
                    stock.currentPrice !== null
                ) {

                    currentValue +=
                        Number(
                            stock.currentPrice
                        )
                        *
                        stock.shares;

                } else {

                    /*
                     * If Yahoo temporarily fails,
                     * don't make portfolio value
                     * appear as zero.
                     */
                    currentValue +=
                        Number(stock.buyPrice)
                        *
                        stock.shares;
                }
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


    /* =========================
       LOAD STOCKS + PRICES
       ========================= */

    async function loadPortfolio() {

        setLoading(true);
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
                                            priceResponse
                                                .currentPrice
                                        )
                                };

                            } catch (priceError) {

                                console.error(
                                    `Could not load price for ${stock.ticker}`,
                                    priceError
                                );


                                return {
                                    ...stock,
                                    currentPrice: null
                                };
                            }
                        }
                    )
                );


            setStocks(
                stocksWithPrices
            );

        } catch (loadError) {

            console.error(
                loadError
            );

            setError(
                loadError instanceof Error
                    ? loadError.message
                    : "Could not load your portfolio."
            );

        } finally {

            setLoading(false);
        }
    }


    async function refreshPrices() {

        if (stocks.length === 0) {
            return;
        }


        setRefreshing(true);

        try {

            const refreshed =
                await Promise.all(

                    stocks.map(
                        async stock => {

                            try {

                                const response =
                                    await getCurrentPrice(
                                        stock.id
                                    );


                                return {
                                    ...stock,
                                    currentPrice:
                                        Number(
                                            response
                                                .currentPrice
                                        )
                                };

                            } catch (priceError) {

                                console.error(
                                    `Could not refresh ${stock.ticker}`,
                                    priceError
                                );

                                return stock;
                            }
                        }
                    )
                );


            setStocks(refreshed);

        } finally {

            setRefreshing(false);
        }
    }


    /* =========================
       CREATE / EDIT
       ========================= */

    function openCreateForm() {

        setEditingStock(null);

        setForm(
            EMPTY_FORM
        );

        setError(null);
        setFormOpen(true);
    }


    function openEditForm(
        stock: Stock
    ) {

        setEditingStock(stock);

        setForm({
            ticker:
            stock.ticker,

            companyName:
            stock.companyName,

            shares:
                String(stock.shares),

            buyPrice:
                String(stock.buyPrice),

            targetPrice:
                String(stock.targetPrice),

            notes:
                stock.notes ?? "",

            alertEnabled:
            stock.alertEnabled
        });

        setError(null);
        setFormOpen(true);
    }


    function closeForm() {

        setFormOpen(false);
        setEditingStock(null);

        setForm(
            EMPTY_FORM
        );
    }


    async function handleSubmit(
        event: FormEvent<HTMLFormElement>
    ) {

        event.preventDefault();

        setSaving(true);
        setError(null);


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

            } else {

                await createStock(
                    input
                );
            }


            closeForm();

            await loadPortfolio();

        } catch (saveError) {

            console.error(
                saveError
            );

            setError(
                saveError instanceof Error
                    ? saveError.message
                    : "Could not save stock."
            );

        } finally {

            setSaving(false);
        }
    }


    /* =========================
       DELETE
       ========================= */

    async function handleDelete(
        stock: Stock
    ) {

        const confirmed =
            window.confirm(
                `Remove ${stock.ticker} from your portfolio?`
            );


        if (!confirmed) {
            return;
        }


        try {

            setError(null);

            await deleteStock(
                stock.id
            );


            setStocks(
                current =>
                    current.filter(
                        item =>
                            item.id !==
                            stock.id
                    )
            );

        } catch (deleteError) {

            console.error(
                deleteError
            );

            setError(
                deleteError instanceof Error
                    ? deleteError.message
                    : "Could not delete stock."
            );
        }
    }


    /* =========================
       ALERT TOGGLE
       ========================= */

    async function handleAlertToggle(
        stock: Stock
    ) {

        try {

            setError(null);


            const input: StockInput = {

                ticker:
                stock.ticker,

                companyName:
                stock.companyName,

                shares:
                stock.shares,

                buyPrice:
                    Number(
                        stock.buyPrice
                    ),

                currency: "USD",

                targetPrice:
                    Number(
                        stock.targetPrice
                    ),

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
                            item.id ===
                            stock.id
                                ? {
                                    ...item,
                                    ...updated
                                }
                                : item
                    )
            );

        } catch (toggleError) {

            console.error(
                toggleError
            );

            setError(
                toggleError instanceof Error
                    ? toggleError.message
                    : "Could not update alert."
            );
        }
    }


    /* =========================
       WEB PUSH
       ========================= */

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

                setNotificationEnabled(
                    false
                );

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

                /*
                 * Re-sync this browser subscription
                 * with the currently logged-in user.
                 */
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

                            userVisibleOnly:
                                true,

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


            setNotificationEnabled(
                true
            );

        } catch (pushError) {

            console.error(
                pushError
            );

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


        try {

            if (
                !(
                    "serviceWorker"
                    in navigator
                )
            ) {

                setNotificationEnabled(
                    false
                );

                return;
            }


            const registration =
                await navigator
                    .serviceWorker
                    .getRegistration();


            if (!registration) {

                setNotificationEnabled(
                    false
                );

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


                await subscription
                    .unsubscribe();
            }


            setNotificationEnabled(
                false
            );

        } catch (pushError) {

            console.error(
                pushError
            );

            setError(
                pushError instanceof Error
                    ? pushError.message
                    : "Could not disable notifications."
            );

        } finally {

            setNotificationLoading(false);
        }
    }


    /* =========================
       UI
       ========================= */

    return (

        <main className="app">

            <header className="app-header">

                <div>

                    <p className="eyebrow">
                        STOCK MANAGEMENT SYSTEM
                    </p>

                    <h1>
                        StockWatch
                    </h1>

                    <p className="subtitle">
                        Track your portfolio and
                        receive alerts when your
                        targets are reached.
                    </p>

                </div>


                <div className="header-actions">

                    <button
                        type="button"
                        className="secondary-button"
                        disabled={refreshing}
                        onClick={() =>
                            void refreshPrices()
                        }
                    >
                        {refreshing
                            ? "Refreshing..."
                            : "Refresh prices"}
                    </button>


                    <button
                        type="button"
                        className="primary-button"
                        onClick={
                            openCreateForm
                        }
                    >
                        + Add stock
                    </button>

                </div>

            </header>


            {error && (

                <div className="error-banner">

                    <span>
                        {error}
                    </span>

                    <button
                        type="button"
                        onClick={() =>
                            setError(null)
                        }
                    >
                        ×
                    </button>

                </div>
            )}


            {/* PORTFOLIO SUMMARY */}

            <section className="summary-grid">

                <SummaryCard
                    label="Invested"
                    value={
                        formatMoney(
                            portfolioSummary
                                .invested
                        )
                    }
                />


                <SummaryCard
                    label="Current value"
                    value={
                        formatMoney(
                            portfolioSummary
                                .currentValue
                        )
                    }
                />


                <SummaryCard
                    label="Profit / Loss"
                    value={
                        formatSignedMoney(
                            portfolioSummary
                                .profitLoss
                        )
                    }

                    positive={
                        portfolioSummary
                            .profitLoss >= 0
                    }
                />


                <SummaryCard
                    label="Total return"
                    value={
                        formatPercent(
                            portfolioSummary
                                .returnPercent
                        )
                    }

                    positive={
                        portfolioSummary
                            .returnPercent >= 0
                    }
                />

            </section>


            {/* PUSH SETTINGS */}

            <section className="notification-panel">

                <div>

                    <h2>
                        Price notifications
                    </h2>

                    <p>
                        Receive browser notifications
                        when your stocks reach their
                        target prices.
                    </p>

                </div>


                {notificationEnabled
                    ? (

                        <button
                            type="button"
                            className=
                                "secondary-button"

                            disabled={
                                notificationLoading
                            }

                            onClick={() =>
                                void disableNotifications()
                            }
                        >

                            {notificationLoading
                                ? "Disabling..."
                                : "Disable notifications"}

                        </button>

                    )
                    : (

                        <button
                            type="button"
                            className=
                                "primary-button"

                            disabled={
                                notificationLoading
                            }

                            onClick={() =>
                                void enableNotifications()
                            }
                        >

                            {notificationLoading
                                ? "Enabling..."
                                : "Enable notifications"}

                        </button>
                    )
                }

            </section>


            {/* STOCKS */}

            <section className="portfolio-section">

                <div className="section-heading">

                    <div>

                        <h2>
                            My portfolio
                        </h2>

                        <p>
                            {stocks.length}
                            {" "}
                            {stocks.length === 1
                                ? "stock"
                                : "stocks"}
                        </p>

                    </div>

                </div>


                {loading
                    ? (

                        <div className="empty-state">
                            Loading portfolio...
                        </div>

                    )
                    : stocks.length === 0
                        ? (

                            <div className="empty-state">

                                <h3>
                                    Your portfolio is empty
                                </h3>

                                <p>
                                    Add your first stock
                                    to start monitoring
                                    prices.
                                </p>

                                <button
                                    type="button"
                                    className=
                                        "primary-button"

                                    onClick={
                                        openCreateForm
                                    }
                                >
                                    Add first stock
                                </button>

                            </div>

                        )
                        : (

                            <div className="stock-grid">

                                {stocks.map(
                                    stock => (

                                        <StockCard
                                            key={
                                                stock.id
                                            }

                                            stock={
                                                stock
                                            }

                                            onEdit={() =>
                                                openEditForm(
                                                    stock
                                                )
                                            }

                                            onDelete={() =>
                                                void handleDelete(
                                                    stock
                                                )
                                            }

                                            onToggleAlert={() =>
                                                void handleAlertToggle(
                                                    stock
                                                )
                                            }
                                        />
                                    )
                                )}

                            </div>
                        )
                }

            </section>


            {/* ADD / EDIT MODAL */}

            {formOpen && (

                <div
                    className="modal-backdrop"
                    onMouseDown={
                        event => {

                            if (
                                event.target ===
                                event.currentTarget
                            ) {
                                closeForm();
                            }
                        }
                    }
                >

                    <section className="stock-modal">

                        <div className="modal-header">

                            <div>

                                <p className="eyebrow">
                                    {editingStock
                                        ? "EDIT POSITION"
                                        : "NEW POSITION"}
                                </p>

                                <h2>
                                    {editingStock
                                        ? "Edit stock"
                                        : "Add stock"}
                                </h2>

                            </div>


                            <button
                                type="button"
                                className="close-button"
                                onClick={
                                    closeForm
                                }
                            >
                                ×
                            </button>

                        </div>


                        <form
                            className="stock-form"
                            onSubmit={
                                handleSubmit
                            }
                        >

                            <div className="form-grid">

                                <label>
                                    Ticker

                                    <input
                                        required
                                        placeholder="MU"
                                        value={
                                            form.ticker
                                        }

                                        onChange={
                                            event =>
                                                setForm(
                                                    current => ({
                                                        ...current,
                                                        ticker:
                                                        event
                                                            .target
                                                            .value
                                                    })
                                                )
                                        }
                                    />
                                </label>


                                <label>
                                    Company name

                                    <input
                                        required
                                        placeholder=
                                            "Micron Technology"

                                        value={
                                            form.companyName
                                        }

                                        onChange={
                                            event =>
                                                setForm(
                                                    current => ({
                                                        ...current,
                                                        companyName:
                                                        event
                                                            .target
                                                            .value
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
                                                        event
                                                            .target
                                                            .value
                                                    })
                                                )
                                        }
                                    />
                                </label>


                                <label>
                                    Buy price (USD)

                                    <input
                                        required
                                        min="0.01"
                                        step="0.01"
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
                                                        event
                                                            .target
                                                            .value
                                                    })
                                                )
                                        }
                                    />
                                </label>


                                <label>
                                    Target price (USD)

                                    <input
                                        required
                                        min="0.01"
                                        step="0.01"
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
                                                        event
                                                            .target
                                                            .value
                                                    })
                                                )
                                        }
                                    />
                                </label>

                            </div>


                            <label>
                                Notes

                                <textarea
                                    rows={4}
                                    placeholder=
                                        "Optional notes..."

                                    value={
                                        form.notes
                                    }

                                    onChange={
                                        event =>
                                            setForm(
                                                current => ({
                                                    ...current,
                                                    notes:
                                                    event
                                                        .target
                                                        .value
                                                })
                                            )
                                    }
                                />
                            </label>


                            <label className="checkbox-row">

                                <input
                                    type="checkbox"

                                    checked={
                                        form
                                            .alertEnabled
                                    }

                                    onChange={
                                        event =>
                                            setForm(
                                                current => ({
                                                    ...current,
                                                    alertEnabled:
                                                    event
                                                        .target
                                                        .checked
                                                })
                                            )
                                    }
                                />

                                Enable target-price alerts

                            </label>


                            <div className="modal-actions">

                                <button
                                    type="button"
                                    className=
                                        "secondary-button"

                                    onClick={
                                        closeForm
                                    }

                                    disabled={
                                        saving
                                    }
                                >
                                    Cancel
                                </button>


                                <button
                                    type="submit"
                                    className=
                                        "primary-button"

                                    disabled={
                                        saving
                                    }
                                >
                                    {saving
                                        ? "Saving..."
                                        : editingStock
                                            ? "Save changes"
                                            : "Add stock"}
                                </button>

                            </div>

                        </form>

                    </section>

                </div>
            )}

        </main>
    );
}


/* =========================
   COMPONENTS
   ========================= */


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

    let className =
        "summary-value";


    if (positive === true) {
        className += " positive";
    }

    if (positive === false) {
        className += " negative";
    }


    return (

        <article className="summary-card">

            <span>
                {label}
            </span>

            <strong className={className}>
                {value}
            </strong>

        </article>
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
            ? (
                currentPrice
                *
                stock.shares
            )
            : null;


    const profitLoss =
        currentValue !== null
            ? currentValue - invested
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
        Number(
            stock.targetPrice
        );


    return (

        <article className="stock-card">

            <div className="stock-card-header">

                <div>

                    <div className="ticker-row">

                        <h3>
                            {stock.ticker}
                        </h3>

                        {targetReached && (

                            <span className="target-badge">
                                Target reached
                            </span>
                        )}

                    </div>


                    <p>
                        {stock.companyName}
                    </p>

                </div>


                <div className="stock-actions">

                    <button
                        type="button"
                        onClick={onEdit}
                    >
                        Edit
                    </button>

                    <button
                        type="button"
                        className=
                            "danger-button"

                        onClick={
                            onDelete
                        }
                    >
                        Delete
                    </button>

                </div>

            </div>


            <div className="current-price">

                <span>
                    Current price
                </span>

                <strong>
                    {currentPrice !== null
                        ? formatMoney(
                            currentPrice
                        )
                        : "Unavailable"}
                </strong>

            </div>


            <div className="stock-metrics">

                <Metric
                    label="Buy price"
                    value={
                        formatMoney(
                            Number(
                                stock.buyPrice
                            )
                        )
                    }
                />


                <Metric
                    label="Target"
                    value={
                        formatMoney(
                            Number(
                                stock.targetPrice
                            )
                        )
                    }
                />


                <Metric
                    label="Shares"
                    value={
                        String(
                            stock.shares
                        )
                    }
                />


                <Metric
                    label="Position value"
                    value={
                        currentValue !== null
                            ? formatMoney(
                                currentValue
                            )
                            : "—"
                    }
                />

            </div>


            <div className="stock-return">

                <span>
                    Return
                </span>

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


            {stock.notes && (

                <p className="stock-notes">
                    {stock.notes}
                </p>
            )}


            <div className="alert-row">

                <div>

                    <strong>
                        Price alert
                    </strong>

                    <span>
                        {stock.alertEnabled
                            ? "Enabled"
                            : "Disabled"}
                    </span>

                </div>


                <label className="switch">

                    <input
                        type="checkbox"

                        checked={
                            stock.alertEnabled
                        }

                        onChange={
                            onToggleAlert
                        }
                    />

                    <span className="slider" />

                </label>

            </div>

        </article>
    );
}


interface MetricProps {
    label: string;
    value: string;
}


function Metric({
                    label,
                    value
                }: MetricProps) {

    return (

        <div className="metric">

            <span>
                {label}
            </span>

            <strong>
                {value}
            </strong>

        </div>
    );
}


/* =========================
   HELPERS
   ========================= */


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