import {
    type FormEvent,
    useCallback,
    useEffect,
    useState
} from "react";

import "./App.css";

import {
    createStock,
    deleteStock,
    getCurrentPrice,
    getStocks,
    updateStock
} from "./api";

import {
    disablePush,
    enablePush,
    isPushEnabled
} from "./push";

import type {
    DashboardStock,
    Stock,
    StockInput
} from "./types";


interface StockFormState {
    ticker: string;
    companyName: string;
    shares: string;
    buyPrice: string;
    targetPrice: string;
    notes: string;
    alertEnabled: boolean;
}


const emptyForm: StockFormState = {
    ticker: "",
    companyName: "",
    shares: "",
    buyPrice: "",
    targetPrice: "",
    notes: "",
    alertEnabled: true
};


function App() {

    const [stocks, setStocks] =
        useState<DashboardStock[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [saving, setSaving] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const [message, setMessage] =
        useState<string | null>(null);

    const [editingId, setEditingId] =
        useState<number | null>(null);

    const [form, setForm] =
        useState<StockFormState>(
            emptyForm
        );

    const [
        notificationsEnabled,
        setNotificationsEnabled
    ] = useState(false);


    const loadStocks =
        useCallback(async () => {

            try {

                setLoading(true);
                setError(null);

                const stockList =
                    await getStocks();

                const dashboardStocks =
                    await Promise.all(
                        stockList.map(
                            async stock => {

                                try {

                                    const price =
                                        await getCurrentPrice(
                                            stock.id
                                        );

                                    return {
                                        ...stock,

                                        currentPrice:
                                            price.currentPrice
                                    };

                                } catch {

                                    return {
                                        ...stock,
                                        currentPrice: null
                                    };
                                }
                            }
                        )
                    );

                setStocks(
                    dashboardStocks
                );

            } catch (err) {

                console.error(err);

                setError(
                    "Could not load StockWatch data."
                );

            } finally {

                setLoading(false);
            }

        }, []);


    useEffect(() => {

        loadStocks();

        isPushEnabled()
            .then(setNotificationsEnabled)
            .catch(console.error);

        const interval =
            window.setInterval(
                loadStocks,
                60000
            );

        return () =>
            window.clearInterval(interval);

    }, [loadStocks]);


    function calculateReturn(
        stock: DashboardStock
    ) {

        if (stock.currentPrice === null) {
            return null;
        }

        return (
            (
                stock.currentPrice -
                stock.buyPrice
            )
            /
            stock.buyPrice
        ) * 100;
    }


    const totalInvested =
        stocks.reduce(
            (total, stock) =>
                total +
                stock.buyPrice *
                stock.shares,
            0
        );


    const currentPortfolioValue =
        stocks.reduce(
            (total, stock) =>
                total +
                (
                    stock.currentPrice ??
                    stock.buyPrice
                ) *
                stock.shares,
            0
        );


    const totalProfitLoss =
        currentPortfolioValue -
        totalInvested;


    const totalReturnPercent =
        totalInvested > 0
            ? (
                totalProfitLoss /
                totalInvested
            ) * 100
            : 0;


    function resetForm() {

        setForm(emptyForm);
        setEditingId(null);
    }


    function editStock(
        stock: Stock
    ) {

        setEditingId(stock.id);

        setForm({
            ticker: stock.ticker,

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

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }


    async function handleSubmit(
        event: FormEvent
    ) {

        event.preventDefault();

        setSaving(true);
        setError(null);
        setMessage(null);

        const request: StockInput = {

            ticker:
                form.ticker
                    .trim()
                    .toUpperCase(),

            companyName:
                form.companyName.trim(),

            shares:
                Number(form.shares),

            buyPrice:
                Number(form.buyPrice),

            currency: "USD",

            targetPrice:
                Number(form.targetPrice),

            notes:
                form.notes.trim(),

            alertEnabled:
                form.alertEnabled
        };

        try {

            if (editingId !== null) {

                await updateStock(
                    editingId,
                    request
                );

                setMessage(
                    `${request.ticker} updated.`
                );

            } else {

                await createStock(request);

                setMessage(
                    `${request.ticker} added.`
                );
            }

            resetForm();

            await loadStocks();

        } catch (err) {

            console.error(err);

            setError(
                err instanceof Error
                    ? err.message
                    : "Could not save stock."
            );

        } finally {

            setSaving(false);
        }
    }


    async function handleDelete(
        stock: DashboardStock
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

            await deleteStock(stock.id);

            setMessage(
                `${stock.ticker} deleted.`
            );

            if (
                editingId === stock.id
            ) {
                resetForm();
            }

            await loadStocks();

        } catch (err) {

            console.error(err);

            setError(
                "Could not delete stock."
            );
        }
    }


    async function handleNotifications() {

        try {

            setError(null);
            setMessage(null);

            if (
                notificationsEnabled
            ) {

                await disablePush();

                setNotificationsEnabled(
                    false
                );

                setMessage(
                    "Browser notifications disabled."
                );

            } else {

                await enablePush();

                setNotificationsEnabled(
                    true
                );

                setMessage(
                    "Browser notifications enabled."
                );
            }

        } catch (err) {

            console.error(err);

            setError(
                err instanceof Error
                    ? err.message
                    : "Notification setup failed."
            );
        }
    }


    return (

        <div className="app">

            <header className="header">

                <div>

                    <h1>StockWatch</h1>

                    <p>
                        Monitor your positions,
                        targets and alerts.
                    </p>

                </div>


                <div className="header-actions">

                    <button
                        className="secondary-button"
                        onClick={
                            handleNotifications
                        }
                    >
                        {notificationsEnabled
                            ? "Disable Notifications"
                            : "Enable Notifications"}
                    </button>


                    <button
                        className="refresh-button"
                        onClick={loadStocks}
                        disabled={loading}
                    >
                        {loading
                            ? "Loading..."
                            : "Refresh"}
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

                <div className="summary-card">

                    <span>
                        Invested
                    </span>

                    <strong>
                        ${totalInvested.toFixed(2)}
                    </strong>

                </div>


                <div className="summary-card">

                    <span>
                        Current Value
                    </span>

                    <strong>
                        ${currentPortfolioValue.toFixed(2)}
                    </strong>

                </div>


                <div className="summary-card">

                    <span>
                        Profit / Loss
                    </span>

                    <strong
                        className={
                            totalProfitLoss >= 0
                                ? "positive"
                                : "negative"
                        }
                    >
                        {totalProfitLoss >= 0
                            ? "+"
                            : ""}

                        ${totalProfitLoss.toFixed(2)}
                    </strong>

                </div>


                <div className="summary-card">

                    <span>
                        Total Return
                    </span>

                    <strong
                        className={
                            totalReturnPercent >= 0
                                ? "positive"
                                : "negative"
                        }
                    >
                        {totalReturnPercent >= 0
                            ? "+"
                            : ""}

                        {totalReturnPercent.toFixed(2)}%
                    </strong>

                </div>

            </section>


            <section className="stock-form-section">

                <div className="section-heading">

                    <div>

                        <h2>
                            {editingId !== null
                                ? "Edit Stock"
                                : "Add Stock"}
                        </h2>

                        <p>
                            Prices are currently
                            tracked in USD.
                        </p>

                    </div>


                    {editingId !== null && (

                        <button
                            className="text-button"
                            type="button"
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
                                    setForm({
                                        ...form,

                                        ticker:
                                            event
                                                .target
                                                .value
                                    })
                            }
                        />

                    </label>


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
                                    setForm({
                                        ...form,

                                        companyName:
                                            event
                                                .target
                                                .value
                                    })
                            }
                        />

                    </label>


                    <label>

                        Shares

                        <input
                            required
                            min="1"
                            type="number"

                            value={
                                form.shares
                            }

                            onChange={
                                event =>
                                    setForm({
                                        ...form,

                                        shares:
                                            event
                                                .target
                                                .value
                                    })
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
                                    setForm({
                                        ...form,

                                        buyPrice:
                                            event
                                                .target
                                                .value
                                    })
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
                                    setForm({
                                        ...form,

                                        targetPrice:
                                            event
                                                .target
                                                .value
                                    })
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
                                    setForm({
                                        ...form,

                                        notes:
                                            event
                                                .target
                                                .value
                                    })
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
                                    setForm({
                                        ...form,

                                        alertEnabled:
                                            event
                                                .target
                                                .checked
                                    })
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
                            : editingId !== null
                                ? "Save Changes"
                                : "Add Stock"}
                    </button>

                </form>

            </section>


            {!loading &&
             stocks.length === 0 && (

                <div className="empty">
                    No stocks are currently
                    being monitored.
                </div>

            )}


            <main className="stock-grid">

                {stocks.map(stock => {

                    const returnPercent =
                        calculateReturn(
                            stock
                        );

                    return (

                        <article
                            className="stock-card"
                            key={stock.id}
                        >

                            <div className="stock-heading">

                                <div>

                                    <h2>
                                        {stock.ticker}
                                    </h2>

                                    <span>
                                        {stock.companyName}
                                    </span>

                                </div>


                                <span
                                    className={
                                        stock.alertEnabled
                                            ? "badge active"
                                            : "badge"
                                    }
                                >
                                    {stock.alertEnabled
                                        ? "Alert On"
                                        : "Alert Off"}
                                </span>

                            </div>


                            <div className="current-price">

                                <span>
                                    Current price
                                </span>

                                <strong>

                                    {stock.currentPrice !== null
                                        ? `${stock.currentPrice.toFixed(2)} ${stock.currency}`
                                        : "Unavailable"}

                                </strong>

                            </div>


                            <div className="stats">

                                <div>

                                    <span>
                                        Buy price
                                    </span>

                                    <strong>
                                        {stock.buyPrice.toFixed(2)}
                                    </strong>

                                </div>


                                <div>

                                    <span>
                                        Target
                                    </span>

                                    <strong>
                                        {stock.targetPrice.toFixed(2)}
                                    </strong>

                                </div>


                                <div>

                                    <span>
                                        Shares
                                    </span>

                                    <strong>
                                        {stock.shares}
                                    </strong>

                                </div>


                                <div>

                                    <span>
                                        Return
                                    </span>

                                    <strong
                                        className={
                                            returnPercent !== null &&
                                            returnPercent >= 0
                                                ? "positive"
                                                : "negative"
                                        }
                                    >
                                        {returnPercent !== null
                                            ? `${returnPercent.toFixed(2)}%`
                                            : "—"}
                                    </strong>

                                </div>

                            </div>


                            {stock.alertTriggered && (

                                <div className="triggered">
                                    Target alert triggered
                                </div>

                            )}


                            {stock.notes && (

                                <p className="notes">
                                    {stock.notes}
                                </p>

                            )}


                            <div className="card-actions">

                                <button
                                    className="edit-button"

                                    onClick={() =>
                                        editStock(stock)
                                    }
                                >
                                    Edit
                                </button>


                                <button
                                    className="delete-button"

                                    onClick={() =>
                                        handleDelete(stock)
                                    }
                                >
                                    Delete
                                </button>

                            </div>

                        </article>

                    );

                })}

            </main>

        </div>
    );
}

export default App;