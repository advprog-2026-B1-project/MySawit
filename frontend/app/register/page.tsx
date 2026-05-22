"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = ``;

const ROLES = ["Buruh", "Mandor", "Supir", "Admin"];

export default function RegisterPage() {
    const router = useRouter();
    const [form, setForm] = useState({
        username: "",
        nama: "",
        email: "",
        password: "",
        role: "Buruh",
        nomorSertifikasi: "",
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            const res = await fetch(`${API}/api/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    username: form.username.trim(),
                    nama: form.nama.trim(),
                    email: form.email.trim(),
                    password: form.password,
                    role: form.role,
                    nomorSertifikasiMandor: form.role === "Mandor" ? form.nomorSertifikasi.trim() : null,
                }),
            });

            if (res.status === 409) {
                setError("Email sudah terdaftar. Gunakan email lain.");
                return;
            }
            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                setError(data.message || `Gagal mendaftar (${res.status}).`);
                return;
            }

            router.push("/login");
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
                    <h1 className="text-2xl font-bold text-bone">Buat akun baru</h1>
                    <p className="text-sm text-bone/40 mt-1">
                        Sudah punya akun?{" "}
                        <Link href="/login" className="text-verdant hover:underline">
                            Masuk di sini
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

                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                    Username
                                </label>
                                <input
                                    type="text"
                                    name="username"
                                    value={form.username}
                                    onChange={handleChange}
                                    placeholder="username"
                                    className={inputCls}
                                    required
                                    disabled={loading}
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                    Role
                                </label>
                                <select
                                    name="role"
                                    value={form.role}
                                    onChange={handleChange}
                                    className={inputCls}
                                    disabled={loading}
                                >
                                    {ROLES.map(r => (
                                        <option key={r} value={r}>{r}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div>
                            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                Nama Lengkap
                            </label>
                            <input
                                type="text"
                                name="nama"
                                value={form.nama}
                                onChange={handleChange}
                                placeholder="Nama lengkap"
                                className={inputCls}
                                required
                                disabled={loading}
                            />
                        </div>

                        <div>
                            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                Email
                            </label>
                            <input
                                type="email"
                                name="email"
                                value={form.email}
                                onChange={handleChange}
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
                                name="password"
                                value={form.password}
                                onChange={handleChange}
                                placeholder="••••••••"
                                className={inputCls}
                                required
                                disabled={loading}
                            />
                        </div>

                        {/* Nomor Sertifikasi — hanya muncul jika role Mandor */}
                        {form.role === "Mandor" && (
                            <div>
                                <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                    Nomor Sertifikasi
                                </label>
                                <input
                                    type="text"
                                    name="nomorSertifikasi"
                                    value={form.nomorSertifikasi}
                                    onChange={handleChange}
                                    placeholder="Contoh: CERT-2024-001"
                                    className={inputCls}
                                    disabled={loading}
                                />
                            </div>
                        )}
                    </div>

                    <div className="px-6 py-4 bg-ink-soft rounded-b-lg">
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full px-4 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 disabled:cursor-not-allowed transition"
                        >
                            {loading ? "Mendaftar..." : "Daftar"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
