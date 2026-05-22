"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";

interface FieldError {
    [key: string]: string;
}

function Field({
    label,
    required,
    error,
    hint,
    children,
}: {
    label: string;
    required?: boolean;
    error?: string;
    hint?: string;
    children: React.ReactNode;
}) {
    return (
        <div>
            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-1.5">
                {label}
                {required && <span className="ml-1 text-verdant/60">*</span>}
            </label>
            {children}
            {hint && !error && <p className="mt-1.5 text-xs text-bone/25">{hint}</p>}
            {error && <p className="mt-1.5 text-xs text-red-400">{error}</p>}
        </div>
    );
}

export default function KebunBaruPage() {
    const router = useRouter();

    const [form, setForm] = useState({
        kodeKebun: "",
        namaKebun: "",
        koordinat: "",
    });
    const [error, setError] = useState("");
    const [fieldErrors, setFieldErrors] = useState<FieldError>({});
    const [loading, setLoading] = useState(false);

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

        try {
            const res = await fetch(`${API}/api/kebun`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    kodeKebun: form.kodeKebun.trim(),
                    namaKebun: form.namaKebun.trim(),
                    koordinat: form.koordinat.trim(),
                }),
            });

            if (res.status === 403) {
                setError("Akses ditolak. Hanya Admin yang dapat membuat kebun baru.");
                return;
            }
            if (res.status === 409) {
                setFieldErrors({ kodeKebun: "Kode kebun sudah terdaftar. Gunakan kode yang berbeda." });
                return;
            }
            if (res.status === 422) {
                const data = await res.json().catch(() => ({}));
                setError(data.message || "Koordinat tumpang tindih dengan kebun yang sudah ada.");
                return;
            }
            if (res.status === 400) {
                const data = await res.json().catch(() => ({}));
                if (data.errors) {
                    const mapped: FieldError = {};
                    for (const err of data.errors) {
                        mapped[err.field] = err.message;
                    }
                    setFieldErrors(mapped);
                } else {
                    setError(data.message || "Input tidak valid.");
                }
                return;
            }
            if (!res.ok) {
                setError(`Gagal membuat kebun (${res.status}).`);
                return;
            }

            router.push("/kebun");
        } catch {
            setError("Tidak dapat terhubung ke server. Pastikan backend berjalan.");
        } finally {
            setLoading(false);
        }
    };

    const inputCls = (field: string) =>
        `w-full bg-ink border ${fieldErrors[field] ? "border-red-500/50" : "border-white/10"} text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition`;

    return (
        <div className="px-8 py-6 max-w-2xl">

            {/* Breadcrumb */}
            <div className="flex items-center gap-2 text-sm mb-6">
                <Link href="/kebun" className="text-bone/40 hover:text-bone transition">Daftar Kebun</Link>
                <span className="text-bone/20">/</span>
                <span className="text-bone/70">Tambah Kebun</span>
            </div>

            {/* Page header */}
            <div className="mb-7">
                <h1 className="text-xl font-semibold text-bone">Tambah Kebun Baru</h1>
                <p className="text-sm text-bone/40 mt-1">Isi data kebun. Kode tidak dapat diubah setelah disimpan.</p>
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
                    <Field label="Kode Kebun" required error={fieldErrors.kodeKebun}>
                        <input
                            type="text"
                            name="kodeKebun"
                            value={form.kodeKebun}
                            onChange={handleChange}
                            placeholder="KB-001"
                            className={inputCls("kodeKebun")}
                            required
                        />
                    </Field>

                    <Field label="Nama Kebun" required error={fieldErrors.namaKebun}>
                        <input
                            type="text"
                            name="namaKebun"
                            value={form.namaKebun}
                            onChange={handleChange}
                            placeholder="Kebun Sawit Alpha"
                            className={inputCls("namaKebun")}
                            required
                        />
                    </Field>

                    <Field
                        label="Koordinat"
                        required
                        error={fieldErrors.koordinat}
                        hint="Format: [(x1,y1),(x2,y2),(x3,y3),(x4,y4)] — 4 titik sudut persegi. Luas dikalkulasi otomatis."
                    >
                        <textarea
                            name="koordinat"
                            value={form.koordinat}
                            onChange={handleChange}
                            placeholder="[(0,0),(100,0),(100,100),(0,100)]"
                            rows={3}
                            className={`${inputCls("koordinat")} font-mono resize-none`}
                            required
                        />
                    </Field>
                </div>

                {/* Footer actions */}
                <div className="px-6 py-4 flex items-center justify-between bg-ink-soft rounded-b-lg">
                    <Link
                        href="/kebun"
                        className="text-sm text-bone/40 hover:text-bone transition"
                    >
                        Batal
                    </Link>
                    <button
                        type="submit"
                        disabled={loading}
                        className="px-5 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 disabled:cursor-not-allowed transition"
                    >
                        {loading ? "Menyimpan..." : "Simpan Kebun"}
                    </button>
                </div>
            </form>
        </div>
    );
}
