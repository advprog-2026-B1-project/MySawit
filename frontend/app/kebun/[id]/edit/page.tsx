"use client";

import { useState, useEffect, use } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";

interface Kebun {
    id: number;
    kodeKebun: string;
    namaKebun: string;
    luasHektare: number;
    koordinat: string;
}

interface FieldError {
    [key: string]: string;
}

function Field({
    label,
    error,
    hint,
    children,
}: {
    label: string;
    error?: string;
    hint?: string;
    children: React.ReactNode;
}) {
    return (
        <div>
            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                {label}
            </label>
            {children}
            {hint && !error && <p className="mt-1.5 text-xs text-bone/25">{hint}</p>}
            {error && <p className="mt-1.5 text-xs text-red-400">{error}</p>}
        </div>
    );
}

export default function KebunEditPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = use(params);
    const router = useRouter();

    const [kodeKebun, setKodeKebun] = useState("");
    const [form, setForm] = useState({ namaKebun: "", luasHektare: "", koordinat: "" });
    const [error, setError] = useState("");
    const [fieldErrors, setFieldErrors] = useState<FieldError>({});
    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);

    useEffect(() => {
        const load = async () => {
            try {
                const res = await fetch(`${API}/api/kebun/${id}`, { credentials: "include" });
                if (res.status === 403) { setError("Akses ditolak."); return; }
                if (res.status === 404) { setError("Kebun tidak ditemukan."); return; }
                if (!res.ok) { setError(`Error ${res.status}`); return; }
                const data: Kebun = await res.json();
                setKodeKebun(data.kodeKebun);
                setForm({
                    namaKebun: data.namaKebun,
                    luasHektare: String(data.luasHektare),
                    koordinat: data.koordinat ?? "",
                });
            } catch {
                setError("Tidak dapat terhubung ke server.");
            } finally {
                setFetching(false);
            }
        };
        load();
    }, [id]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
        setFieldErrors(prev => ({ ...prev, [name]: "" }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setFieldErrors({});
        setLoading(true);

        const body: Record<string, unknown> = {};
        if (form.namaKebun.trim()) body.namaKebun = form.namaKebun.trim();
        if (form.luasHektare) body.luasHektare = parseFloat(form.luasHektare);
        if (form.koordinat.trim()) body.koordinat = form.koordinat.trim();

        try {
            const res = await fetch(`${API}/api/kebun/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify(body),
            });

            if (res.status === 403) {
                setError("Akses ditolak. Hanya Admin yang dapat mengedit kebun.");
                return;
            }
            if (res.status === 404) {
                setError("Kebun tidak ditemukan.");
                return;
            }
            if (res.status === 422) {
                const data = await res.json().catch(() => ({}));
                setError(data.message || "Koordinat baru tumpang tindih dengan kebun lain.");
                return;
            }
            if (res.status === 400) {
                const data = await res.json().catch(() => ({}));
                if (data.errors) {
                    const mapped: FieldError = {};
                    for (const err of data.errors) mapped[err.field] = err.message;
                    setFieldErrors(mapped);
                } else {
                    setError(data.message || "Input tidak valid.");
                }
                return;
            }
            if (!res.ok) {
                setError(`Gagal memperbarui kebun (${res.status}).`);
                return;
            }

            router.push(`/kebun/${id}`);
        } catch {
            setError("Tidak dapat terhubung ke server.");
        } finally {
            setLoading(false);
        }
    };

    const inputCls = (field: string) =>
        `w-full bg-ink border ${fieldErrors[field] ? "border-red-500/50" : "border-white/10"} text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition`;

    if (fetching) {
        return (
            <div className="px-8 py-6 max-w-2xl">
                <div className="h-4 w-48 bg-white/5 rounded animate-pulse mb-8" />
                <div className="h-64 bg-white/5 rounded-lg animate-pulse" />
            </div>
        );
    }

    return (
        <div className="px-8 py-6 max-w-2xl">

            {/* Breadcrumb */}
            <div className="flex items-center gap-2 text-sm mb-6">
                <Link href="/kebun" className="text-bone/40 hover:text-bone transition">Daftar Kebun</Link>
                <span className="text-bone/20">/</span>
                <Link href={`/kebun/${id}`} className="text-bone/40 hover:text-bone transition">Detail</Link>
                <span className="text-bone/20">/</span>
                <span className="text-bone/70">Edit</span>
            </div>

            {/* Page header */}
            <div className="mb-7">
                <h1 className="text-xl font-semibold text-bone">Edit Kebun</h1>
                <p className="text-sm text-bone/40 mt-1">
                    Mengedit <span className="font-mono text-bone/60">{kodeKebun}</span> — kode tidak dapat diubah.
                </p>
            </div>

            {/* Global error */}
            {error && (
                <div className="mb-5 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}

            {/* Form card */}
            <form onSubmit={handleSubmit} className="bg-ink-muted border border-white/10 rounded-lg divide-y divide-white/5">

                {/* Fields */}
                <div className="px-6 py-5 space-y-5">

                    {/* Kode — read only, displayed as metadata */}
                    <div className="flex items-center gap-3 px-3 py-2.5 bg-ink rounded-md border border-white/5">
                        <span className="text-xs text-bone/30 uppercase tracking-wider">Kode</span>
                        <span className="font-mono text-sm text-bone/50">{kodeKebun}</span>
                        <span className="ml-auto text-xs text-bone/20">Tidak dapat diubah</span>
                    </div>

                    <div className="grid grid-cols-2 gap-5">
                        <Field label="Nama Kebun" error={fieldErrors.namaKebun}>
                            <input
                                type="text"
                                name="namaKebun"
                                value={form.namaKebun}
                                onChange={handleChange}
                                className={inputCls("namaKebun")}
                            />
                        </Field>

                        <Field label="Luas (Hektare)" error={fieldErrors.luasHektare}>
                            <input
                                type="number"
                                name="luasHektare"
                                value={form.luasHektare}
                                onChange={handleChange}
                                step="0.01"
                                min="0.01"
                                className={inputCls("luasHektare")}
                            />
                        </Field>
                    </div>

                    <Field
                        label="Koordinat"
                        error={fieldErrors.koordinat}
                        hint="Format: [(lat,lon),(lat,lon),(lat,lon),(lat,lon)]"
                    >
                        <textarea
                            name="koordinat"
                            value={form.koordinat}
                            onChange={handleChange}
                            rows={3}
                            className={`${inputCls("koordinat")} font-mono resize-none`}
                        />
                    </Field>
                </div>

                {/* Footer actions */}
                <div className="px-6 py-4 flex items-center justify-between bg-ink-soft rounded-b-lg">
                    <Link
                        href={`/kebun/${id}`}
                        className="text-sm text-bone/40 hover:text-bone transition"
                    >
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
