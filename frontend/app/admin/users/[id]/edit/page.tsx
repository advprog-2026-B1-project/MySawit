"use client";

import { useState, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";
const ROLES = ["Buruh", "Mandor", "Supir", "Admin"];

export default function EditUserPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = use(params);
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
    const [fetching, setFetching] = useState(true);

    useEffect(() => {
        const load = async () => {
            try {
                const res = await fetch(`${API}/api/admin/users`, { credentials: "include" });
                if (res.status === 401) { router.push("/login"); return; }
                if (!res.ok) { setError(`Gagal memuat data (${res.status})`); return; }
                const users = await res.json();
                const user = users.find((u: { id: number }) => u.id === Number(id));
                if (!user) { setError("User tidak ditemukan."); return; }
                setForm({
                    username: user.username ?? "",
                    nama: user.nama ?? "",
                    email: user.email ?? "",
                    password: "",
                    role: user.role ?? "Buruh",
                    nomorSertifikasi: user.nomorSertifikasi ?? "",
                });
            } catch {
                setError("Tidak dapat terhubung ke server.");
            } finally {
                setFetching(false);
            }
        };
        load();
    }, [id, router]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            const body: Record<string, string> = {
                username: form.username.trim(),
                nama: form.nama.trim(),
                email: form.email.trim(),
                role: form.role,
            };
            if (form.password) body.password = form.password;
            if (form.role === "Mandor" && form.nomorSertifikasi.trim()) {
                body.nomorSertifikasi = form.nomorSertifikasi.trim();
            }

            const res = await fetch(`${API}/api/admin/users/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify(body),
            });

            if (res.status === 409) {
                setError("Email sudah digunakan user lain.");
                return;
            }
            if (res.status === 404) {
                setError("User tidak ditemukan.");
                return;
            }
            if (res.status === 400) {
                const data = await res.json().catch(() => ({}));
                setError(data.message || "Input tidak valid.");
                return;
            }
            if (!res.ok) {
                setError(`Gagal memperbarui user (${res.status}).`);
                return;
            }
            router.push("/admin/users");
        } catch {
            setError("Tidak dapat terhubung ke server.");
        } finally {
            setLoading(false);
        }
    };

    const inputCls = "w-full bg-ink border border-line text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition";

    if (fetching) {
        return (
            <div className="px-8 py-6 max-w-xl">
                <div className="h-4 w-48 bg-white/5 rounded animate-pulse mb-8" />
                <div className="h-64 bg-white/5 rounded-lg animate-pulse" />
            </div>
        );
    }

    return (
        <div className="px-8 py-6 max-w-xl">

            {/* Breadcrumb */}
            <div className="flex items-center gap-2 text-sm mb-6">
                <Link href="/admin/users" className="text-bone/40 hover:text-bone transition">Manajemen User</Link>
                <span className="text-bone/20">/</span>
                <span className="text-bone/70">Edit User</span>
            </div>

            <div className="mb-7">
                <h1 className="text-xl font-semibold text-bone">Edit Pengguna</h1>
                <p className="text-sm text-bone/40 mt-1">Kosongkan password jika tidak ingin mengubahnya.</p>
            </div>

            {error && (
                <div className="mb-5 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="bg-ink-soft border border-line rounded-lg divide-y divide-line">
                <div className="px-6 py-5 space-y-4">

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                Username
                            </label>
                            <input
                                name="username"
                                value={form.username}
                                onChange={handleChange}
                                className={inputCls}
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
                                {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                            </select>
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                            Nama Lengkap
                        </label>
                        <input
                            name="nama"
                            value={form.nama}
                            onChange={handleChange}
                            className={inputCls}
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
                            className={inputCls}
                            disabled={loading}
                        />
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                            Password Baru
                        </label>
                        <input
                            type="password"
                            name="password"
                            value={form.password}
                            onChange={handleChange}
                            placeholder="Kosongkan jika tidak diubah"
                            className={inputCls}
                            disabled={loading}
                        />
                    </div>

                    {form.role === "Mandor" && (
                        <div>
                            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                                Nomor Sertifikasi Mandor
                            </label>
                            <input
                                name="nomorSertifikasi"
                                value={form.nomorSertifikasi}
                                onChange={handleChange}
                                placeholder="SERT-2024-XXXX"
                                className={inputCls}
                                disabled={loading}
                            />
                        </div>
                    )}
                </div>

                <div className="px-6 py-4 flex items-center justify-between bg-ink rounded-b-lg">
                    <Link href="/admin/users" className="text-sm text-bone/40 hover:text-bone transition">
                        Batal
                    </Link>
                    <button
                        type="submit"
                        disabled={loading}
                        className="px-5 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 disabled:cursor-not-allowed transition"
                    >
                        {loading ? "Menyimpan..." : "Simpan Perubahan"}
                    </button>
                </div>
            </form>
        </div>
    );
}
