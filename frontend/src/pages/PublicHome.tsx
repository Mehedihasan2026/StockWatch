import {
    useEffect,
    useState
} from "react";

import {
    Link
} from "react-router-dom";

import {
    getTopGainers
} from "../api";

import type {
    MarketMover
} from "../types";


export default function PublicHome() {

    const [movers, setMovers] =
        useState<MarketMover[]>([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState<string | null>(null);


    useEffect(() => {

        void loadMarketMovers();

    }, []);


    async function loadMarketMovers() {

        setLoading(true);
        setError(null);

        try {

            const data =
                await getTopGainers(20);

            setMovers(data);

        } catch (loadError) {

            console.error(
                loadError
            );

            setError(
                loadError instanceof Error
                    ? loadError.message
                    : "Could not load market data."
            );

        } finally {

            setLoading(false);
        }
    }


    return (

        <main className="public-page">

            <nav className="public-nav">

                <strong className="brand">
                    StockWatch
                </strong>

                <div>

                    <Link
                        to="/login"
                        className="nav-link"
                    >
                        Log in
                    </Link>

                    <Link
                        to="/register"
                        className="primary-link"
                    >
                        Create account
                    </Link>

                </div>

            </nav>


            <section className="hero">

                <p className="eyebrow">
                    STOCK MANAGEMENT SYSTEM
                </p>

                <h1>
                    Track your portfolio.
                    Set your targets.
                    Get notified.
                </h1>

                <p className="hero-copy">
                    Monitor live stock prices,
                    manage your own private portfolio,
                    and receive browser notifications
                    when your target prices are reached.
                </p>


                <div className="hero-actions">

                    <Link
                        to="/register"
                        className="hero-primary"
                    >
                        Start tracking
                    </Link>

                    <Link
                        to="/login"
                        className="hero-secondary"
                    >
                        Log in
                    </Link>

                </div>

            </section>


            <section className="market-section">

                <div className="market-section-header">

                    <div>

                        <p className="eyebrow">
                            MARKET TODAY
                        </p>

                        <h2>
                            Top 20 gainers
                        </h2>

                        <p className="market-description">
                            Stocks showing the
                            strongest daily percentage
                            gains in the U.S. market.
                        </p>

                    </div>


                    <button
                        type="button"
                        className="market-refresh-button"

                        disabled={loading}

                        onClick={() =>
                            void loadMarketMovers()
                        }
                    >
                        {loading
                            ? "Refreshing..."
                            : "Refresh"}
                    </button>

                </div>


                {error && (

                    <div className="market-error">

                        <span>
                            {error}
                        </span>

                        <button
                            type="button"

                            onClick={() =>
                                void loadMarketMovers()
                            }
                        >
                            Try again
                        </button>

                    </div>
                )}


                {loading && movers.length === 0
                    ? (

                        <MarketLoading />

                    )
                    : movers.length === 0
                        ? (

                            <div className="market-empty">

                                Market data is
                                currently unavailable.

                            </div>

                        )
                        : (

                            <MarketTable
                                movers={movers}
                            />
                        )
                }


                <p className="market-disclaimer">
                    Market data is provided for
                    informational purposes only and
                    is not investment advice.
                </p>

            </section>


            <section className="public-cta">

                <div>

                    <p className="eyebrow">
                        YOUR PORTFOLIO
                    </p>

                    <h2>
                        Want to track one of these
                        stocks?
                    </h2>

                    <p>
                        Create a free StockWatch
                        account, add your positions,
                        set target prices, and enable
                        browser alerts.
                    </p>

                </div>


                <Link
                    to="/register"
                    className="hero-primary"
                >
                    Create account
                </Link>

            </section>

        </main>
    );
}


interface MarketTableProps {

    movers: MarketMover[];
}


function MarketTable({
                         movers
                     }: MarketTableProps) {

    return (

        <div className="market-table-wrapper">

            <table className="market-table">

                <thead>

                <tr>

                    <th>
                        #
                    </th>

                    <th>
                        Stock
                    </th>

                    <th>
                        Price
                    </th>

                    <th>
                        Change
                    </th>

                    <th>
                        Change %
                    </th>

                    <th>
                        Volume
                    </th>

                    <th>
                        Exchange
                    </th>

                </tr>

                </thead>


                <tbody>

                {movers.map(
                    (
                        mover,
                        index
                    ) => (

                        <tr
                            key={
                                mover.ticker
                            }
                        >

                            <td className="market-rank">

                                {index + 1}

                            </td>


                            <td>

                                <div className="market-company">

                                    <strong>
                                        {mover.ticker}
                                    </strong>

                                    <span>
                                            {mover.companyName}
                                        </span>

                                </div>

                            </td>


                            <td>

                                {formatPrice(
                                    mover.price,
                                    mover.currency
                                )}

                            </td>


                            <td className="market-positive">

                                {formatChange(
                                    mover.change
                                )}

                            </td>


                            <td>

                                    <span className="gain-badge">

                                        {formatPercent(
                                            mover.changePercent
                                        )}

                                    </span>

                            </td>


                            <td>

                                {formatVolume(
                                    mover.volume
                                )}

                            </td>


                            <td className="market-exchange">

                                {mover.exchange ?? "—"}

                            </td>

                        </tr>
                    )
                )}

                </tbody>

            </table>

        </div>
    );
}


function MarketLoading() {

    return (

        <div className="market-loading">

            <div className="market-loading-row" />
            <div className="market-loading-row" />
            <div className="market-loading-row" />
            <div className="market-loading-row" />
            <div className="market-loading-row" />

        </div>
    );
}


function formatPrice(
    price: number | null,
    currency: string | null
): string {

    if (price === null) {
        return "—";
    }


    if (
        currency === "USD"
    ) {

        return new Intl.NumberFormat(
            "en-US",
            {
                style: "currency",
                currency: "USD",
                minimumFractionDigits: 2
            }
        ).format(price);
    }


    return `${price.toFixed(2)} ${
        currency ?? ""
    }`;
}


function formatChange(
    change: number | null
): string {

    if (change === null) {
        return "—";
    }

    const prefix =
        change > 0
            ? "+"
            : "";

    return `${prefix}${change.toFixed(2)}`;
}


function formatPercent(
    percent: number | null
): string {

    if (percent === null) {
        return "—";
    }

    const prefix =
        percent > 0
            ? "+"
            : "";

    return `${prefix}${percent.toFixed(2)}%`;
}


function formatVolume(
    volume: number | null
): string {

    if (volume === null) {
        return "—";
    }


    if (
        volume >= 1_000_000_000
    ) {

        return `${
            (
                volume /
                1_000_000_000
            ).toFixed(1)
        }B`;
    }


    if (
        volume >= 1_000_000
    ) {

        return `${
            (
                volume /
                1_000_000
            ).toFixed(1)
        }M`;
    }


    if (
        volume >= 1_000
    ) {

        return `${
            (
                volume /
                1_000
            ).toFixed(1)
        }K`;
    }


    return volume.toLocaleString();
}