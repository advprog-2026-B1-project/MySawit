"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";

export default function LoginPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const res = await fetch(`${API}/api/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password }),
            });

            if (!res.ok) {
                setError("Email atau password salah.");
                return;
            }

            // Ambil info user untuk redirect sesuai role
            const meRes = await fetch(`${API}/api/me`, { credentials: "include" });
            if (meRes.ok) {
                const user = await meRes.json();
                if (user.role === "Admin") router.push("/kebun");
                else if (user.role === "Mandor") router.push("/harvest/mandor");
                else if (user.role === "Supir") router.push("/delivery/supir");
                else router.push("/harvest/buruh/lapor");
            } else {
                router.push("/");
            }
        } catch {
            setError("Tidak dapat terhubung ke server.");
        } finally {
            setLoading(false);
        }
    };

    const inputCls = "w-full bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition";

    return (
        <div className="min-h-full flex items-center justify-center px-4 py-16">
            <div className="w-full max-w-sm">

                {/* Header */}
                <div className="mb-8">
                    <p className="text-xs font-semibold text-verdant uppercase tracking-wider mb-2">
                        MySawit
                    </p>
                    <h1 className="text-2xl font-bold text-bone">Masuk ke akun kamu</h1>
                    <p className="text-sm text-bone/40 mt-1">
                        Belum punya akun?{" "}
                        <Link href="/register" className="text-verdant hover:underline">
                            Daftar di sini
                        </Link>
                    </p>
                </div>

                {/* Error */}
                {error && (
                    <div className="mb-5 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                        {error}
                    </div>
                )}

                {/* Form */}
                <form onSubmit={handleSubmit} className="bg-ink-muted border border-white/10 rounded-lg divide-y divide-white/5">
                    <div className="px-6 py-5 space-y-4">
                        <div>
                            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                Email
                            </label>
                            <input
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder="kamu@mysawit.com"
                                className={inputCls}
                                required
                                disabled={loading}
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                Password
                            </label>
                            <input
                                type="password"
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                placeholder="••••••••"
                                className={inputCls}
                                required
                                disabled={loading}
                            />
                        </div>
                    </div>

                    <div className="px-6 py-4 bg-ink-soft rounded-b-lg space-y-3">
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full px-4 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 disabled:cursor-not-allowed transition"
                        >
                            {loading ? "Memproses..." : "Masuk"}
                        </button>

                        <a
                            href={`${API}/oauth2/authorization/google`}
                            className="flex items-center justify-center gap-2 w-full px-4 py-2 border border-white/10 text-bone/70 text-sm rounded-md hover:border-white/20 hover:text-bone transition"
                        >
                            <span>G</span>
                            <span>Masuk dengan Google</span>
                        </a>
                    </div>
                </form>
            </div>
        </div>
    );
}
