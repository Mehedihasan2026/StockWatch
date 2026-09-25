import PublicHome from "./pages/PublicHome";
import {
    type FormEvent,
    useEffect,
    useState
} from "react";

import {
    Link,
    Navigate,
    Route,
    Routes,
    useNavigate
} from "react-router-dom";

import "./App.css";

import {
    getCurrentUser,
    login,
    logout,
    register
} from "./api";

import DashboardPage
    from "./pages/DashboardPage";

import type {
    AuthUser
} from "./types";


function App() {

    const [user, setUser] =
        useState<AuthUser | null>(
            null
        );

    const [authLoading, setAuthLoading] =
        useState(true);


    useEffect(() => {

        getCurrentUser()
            .then(setUser)
            .catch(error => {

                console.error(error);

                setUser(null);
            })
            .finally(() =>
                setAuthLoading(false)
            );

    }, []);


    async function handleLogout() {

        await logout();

        setUser(null);
    }


    if (authLoading) {

        return (
            <div className="auth-loading">
                Loading StockWatch...
            </div>
        );
    }


    return (

        <Routes>

            <Route
                path="/"
                element={
                    user
                        ? (
                            <Navigate
                                to="/dashboard"
                                replace
                            />
                        )
                        : (
                            <PublicHome />
                        )
                }
            />


            <Route
                path="/login"
                element={
                    user
                        ? (
                            <Navigate
                                to="/dashboard"
                                replace
                            />
                        )
                        : (
                            <LoginPage
                                onAuthenticated={
                                    setUser
                                }
                            />
                        )
                }
            />


            <Route
                path="/register"
                element={
                    user
                        ? (
                            <Navigate
                                to="/dashboard"
                                replace
                            />
                        )
                        : (
                            <RegisterPage
                                onAuthenticated={
                                    setUser
                                }
                            />
                        )
                }
            />


            <Route
                path="/dashboard"
                element={
                    user
                        ? (
                            <AuthenticatedDashboard
                                user={user}
                                onLogout={
                                    handleLogout
                                }
                            />
                        )
                        : (
                            <Navigate
                                to="/login"
                                replace
                            />
                        )
                }
            />


            <Route
                path="*"
                element={
                    <Navigate
                        to="/"
                        replace
                    />
                }
            />

        </Routes>
    );
}



interface AuthPageProps {

    onAuthenticated:
        (user: AuthUser) => void;
}


function LoginPage({
                       onAuthenticated
                   }: AuthPageProps) {

    const navigate =
        useNavigate();

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [error, setError] =
        useState<string | null>(null);

    const [loading, setLoading] =
        useState(false);


    async function submit(
        event: FormEvent
    ) {

        event.preventDefault();

        setLoading(true);
        setError(null);

        try {

            const user =
                await login({
                    email,
                    password
                });

            onAuthenticated(user);

            navigate(
                "/dashboard",
                {
                    replace: true
                }
            );

        } catch (error) {

            setError(
                error instanceof Error
                    ? error.message
                    : "Login failed."
            );

        } finally {

            setLoading(false);
        }
    }


    return (

        <AuthLayout
            title="Welcome back"
            subtitle="Log in to manage your portfolio."
        >

            <form
                className="auth-form"
                onSubmit={submit}
            >

                <label>
                    Email

                    <input
                        required
                        type="email"
                        autoComplete="email"
                        value={email}

                        onChange={event =>
                            setEmail(
                                event.target.value
                            )
                        }
                    />
                </label>


                <label>
                    Password

                    <input
                        required
                        type="password"
                        autoComplete=
                            "current-password"

                        value={password}

                        onChange={event =>
                            setPassword(
                                event.target.value
                            )
                        }
                    />
                </label>


                {error && (
                    <div className="error">
                        {error}
                    </div>
                )}


                <button
                    className="auth-submit"
                    disabled={loading}
                >
                    {loading
                        ? "Logging in..."
                        : "Log in"}
                </button>


                <p className="auth-switch">

                    No account?{" "}

                    <Link to="/register">
                        Create one
                    </Link>

                </p>

            </form>

        </AuthLayout>
    );
}


function RegisterPage({
                          onAuthenticated
                      }: AuthPageProps) {

    const navigate =
        useNavigate();

    const [displayName, setDisplayName] =
        useState("");

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [error, setError] =
        useState<string | null>(null);

    const [loading, setLoading] =
        useState(false);


    async function submit(
        event: FormEvent
    ) {

        event.preventDefault();

        setLoading(true);
        setError(null);

        try {

            await register({
                displayName,
                email,
                password
            });


            /*
             * Registration creates the user.
             * Then immediately authenticate them.
             */
            const user =
                await login({
                    email,
                    password
                });


            onAuthenticated(user);

            navigate(
                "/dashboard",
                {
                    replace: true
                }
            );

        } catch (error) {

            setError(
                error instanceof Error
                    ? error.message
                    : "Registration failed."
            );

        } finally {

            setLoading(false);
        }
    }


    return (

        <AuthLayout
            title="Create your account"
            subtitle=
                "Build your own private StockWatch portfolio."
        >

            <form
                className="auth-form"
                onSubmit={submit}
            >

                <label>
                    Name

                    <input
                        required
                        maxLength={100}
                        autoComplete="name"

                        value={displayName}

                        onChange={event =>
                            setDisplayName(
                                event.target.value
                            )
                        }
                    />
                </label>


                <label>
                    Email

                    <input
                        required
                        type="email"
                        autoComplete="email"

                        value={email}

                        onChange={event =>
                            setEmail(
                                event.target.value
                            )
                        }
                    />
                </label>


                <label>
                    Password

                    <input
                        required
                        minLength={8}
                        maxLength={72}
                        type="password"
                        autoComplete=
                            "new-password"

                        value={password}

                        onChange={event =>
                            setPassword(
                                event.target.value
                            )
                        }
                    />
                </label>


                <small className="password-help">
                    Minimum 8 characters.
                </small>


                {error && (
                    <div className="error">
                        {error}
                    </div>
                )}


                <button
                    className="auth-submit"
                    disabled={loading}
                >
                    {loading
                        ? "Creating..."
                        : "Create account"}
                </button>


                <p className="auth-switch">

                    Already registered?{" "}

                    <Link to="/login">
                        Log in
                    </Link>

                </p>

            </form>

        </AuthLayout>
    );
}


interface AuthLayoutProps {

    title: string;
    subtitle: string;
    children: React.ReactNode;
}


function AuthLayout({
                        title,
                        subtitle,
                        children
                    }: AuthLayoutProps) {

    return (

        <main className="auth-page">

            <Link
                to="/"
                className="auth-brand"
            >
                StockWatch
            </Link>


            <section className="auth-card">

                <h1>
                    {title}
                </h1>

                <p>
                    {subtitle}
                </p>

                {children}

            </section>

        </main>
    );
}


interface DashboardWrapperProps {

    user: AuthUser;

    onLogout:
        () => Promise<void>;
}


function AuthenticatedDashboard({
                                    user,
                                    onLogout
                                }: DashboardWrapperProps) {

    const [loggingOut, setLoggingOut] =
        useState(false);


    async function handleLogout() {

        setLoggingOut(true);

        try {

            await onLogout();

        } finally {

            setLoggingOut(false);
        }
    }


    return (

        <div>

            <div className="session-bar">

                <span>
                    Signed in as{" "}
                    <strong>
                        {user.displayName}
                    </strong>
                </span>


                <button
                    onClick={handleLogout}
                    disabled={loggingOut}
                >
                    {loggingOut
                        ? "Logging out..."
                        : "Log out"}
                </button>

            </div>


            <DashboardPage />

        </div>
    );
}


export default App;