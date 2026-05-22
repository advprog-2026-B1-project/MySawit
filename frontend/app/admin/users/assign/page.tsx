"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";

interface User {
    id: number;
    nama: string;
    username: string;
    email: string;
    role: string;
}

export default function AssignWorkerPage() {
    const router = useRouter();
    const [buruhList, setBuruhList] = useState<User[]>([]);
    const [mandorList, setMandorList] = useState<User[]>([]);
    const [workerId, setWorkerId] = useState("");
    const [mandorId, setMandorId] = useState("");
    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    useEffect(() => {
        const load = async () => {
            try {
                const res = await fetch(`${API}/api/admin/users`, { credentials: "include" });
                if (res.status === 401) { router.push("/login"); return; }
                if (!res.ok) { setError("Gagal memuat daftar user."); return; }
                const users: User[] = await res.json();
                setBuruhList(users.filter(u => u.role === "Buruh"));
                setMandorList(users.filter(u => u.role === "Mandor"));
            } catch {
                setError("Tidak dapat terhubung ke server.");
            } finally {
                setFetching(false);
            }
        };
        load();
    }, [router]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!workerId || !mandorId) return;
        setError("");
        setSuccess("");
        setLoading(true);
        try {
            const res = await fetch(`${API}/api/admin/assign`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    workerId: Number(workerId),
                    mandorId: Number(mandorId),
                }),
            });

            if (res.status === 422) {
                const data = await res.json().catch(() => ({}));
                setError(data.message || "Validasi gagal.");
                return;
            }
            if (res.status === 404) {
                setError("Buruh atau Mandor tidak ditemukan.");
                return;
            }
            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                setError(data.message || `Gagal assign (${res.status}).`);
                return;
            }

            const buruh = buruhList.find(u => u.id === Number(workerId));
            const mandor = mandorList.find(u => u.id === Number(mandorId));
            setSuccess(`${buruh?.nama ?? "Buruh"} berhasil ditugaskan ke Mandor ${mandor?.nama ?? ""}.`);
            setWorkerId("");
            setMandorId("");
        } catch {
            setError("Tidak dapat terhubung ke server.");
        } finally {
            setLoading(false);
        }
    };

    const selectCls = "w-full bg-ink border border-line text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition";

    if (fetching) {
        return (
            <div className="px-8 py-6 max-w-xl">
                <div className="h-4 w-48 bg-white/5 rounded animate-pulse mb-8" />
                <div className="h-48 bg-white/5 rounded-lg animate-pulse" />
            </div>
        );
    }

    return (
        <div className="px-8 py-6 max-w-xl">

            {/* Breadcrumb */}
            <div className="flex items-center gap-2 text-sm mb-6">
                <Link href="/admin/users" className="text-bone/40 hover:text-bone transition">Manajemen User</Link>
                <span className="text-bone/20">/</span>
                <span className="text-bone/70">Assign Buruh ke Mandor</span>
            </div>

            <div className="mb-7">
                <h1 className="text-xl font-semibold text-bone">Assign Buruh ke Mandor</h1>
                <p className="text-sm text-bone/40 mt-1">
                    Jika Buruh sudah terikat Mandor lain, assignment lama akan otomatis dilepas.
                </p>
            </div>

            {error && (
                <div className="mb-5 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}
            {success && (
                <div className="mb-5 px-4 py-3 bg-verdant-soft border border-verdant/20 rounded-lg text-sm text-verdant">
                    {success}
                </div>
            )}

            <form onSubmit={handleSubmit} className="bg-ink-soft border border-line rounded-lg divide-y divide-line">
                <div className="px-6 py-5 space-y-5">

                    <div>
                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                            Buruh <span className="text-verdant/60">*</span>
                        </label>
                        {buruhList.length === 0 ? (
                            <p className="text-sm text-bone/30 py-2">Tidak ada Buruh terdaftar.</p>
                        ) : (
                            <select
                                value={workerId}
                                onChange={e => setWorkerId(e.target.value)}
                                className={selectCls}
                                required
                                disabled={loading}
                            >
                                <option value="">-- Pilih Buruh --</option>
                                {buruhList.map(u => (
                                    <option key={u.id} value={u.id}>
                                        {u.nama} ({u.email})
                                    </option>
                                ))}
                            </select>
                        )}
                    </div>

                    <div className="flex items-center gap-3">
                        <div className="flex-1 h-px bg-line" />
                        <span className="text-xs text-bone/20 uppercase tracking-wider">ditugaskan ke</span>
                        <div className="flex-1 h-px bg-line" />
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                            Mandor <span className="text-verdant/60">*</span>
                        </label>
                        {mandorList.length === 0 ? (
                            <p className="text-sm text-bone/30 py-2">Tidak ada Mandor terdaftar.</p>
                        ) : (
                            <select
                                value={mandorId}
                                onChange={e => setMandorId(e.target.value)}
                                className={selectCls}
                                required
                                disabled={loading}
                            >
                                <option value="">-- Pilih Mandor --</option>
                                {mandorList.map(u => (
                                    <option key={u.id} value={u.id}>
                                        {u.nama} ({u.email})
                                    </option>
                                ))}
                            </select>
                        )}
                    </div>
                </div>

                <div className="px-6 py-4 flex items-center justify-between bg-ink rounded-b-lg">
                    <Link href="/admin/users" className="text-sm text-bone/40 hover:text-bone transition">
                        Kembali
                    </Link>
                    <button
                        type="submit"
                        disabled={loading || !workerId || !mandorId}
                        className="px-5 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 disabled:cursor-not-allowed transition"
                    >
                        {loading ? "Memproses..." : "Tugaskan"}
                    </button>
                </div>
            </form>
        </div>
    );
}
