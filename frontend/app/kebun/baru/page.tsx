"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";

interface FieldError {
    [key: string]: string;
}

export default function KebunBaruPage() {
    const router = useRouter();

    const [form, setForm] = useState({
        kodeKebun: "",
        namaKebun: "",
        luasHektare: "",
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
                body: JSON.stringify({
                    kodeKebun: form.kodeKebun.trim(),
                    namaKebun: form.namaKebun.trim(),
                    luasHektare: parseFloat(form.luasHektare),
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

    return (
        <div className="p-6 max-w-2xl mx-auto text-gray-800">
            <div className="flex items-center gap-2 mb-6">
                <Link href="/kebun" className="text-sm text-green-600 hover:underline">← Kembali ke Daftar Kebun</Link>
            </div>

            <h1 className="text-2xl font-bold text-green-700 mb-6">Tambah Kebun Baru</h1>

            {error && (
                <div className="mb-5 px-4 py-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="bg-white rounded-lg border border-gray-200 shadow-sm p-6 space-y-5">
                {/* Kode Kebun */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Kode Kebun <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        name="kodeKebun"
                        value={form.kodeKebun}
                        onChange={handleChange}
                        placeholder="Contoh: KB-001"
                        className={`w-full border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 ${
                            fieldErrors.kodeKebun ? "border-red-400 bg-red-50" : "border-gray-300"
                        }`}
                        required
                    />
                    {fieldErrors.kodeKebun && (
                        <p className="mt-1 text-xs text-red-600">{fieldErrors.kodeKebun}</p>
                    )}
                </div>

                {/* Nama Kebun */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Nama Kebun <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        name="namaKebun"
                        value={form.namaKebun}
                        onChange={handleChange}
                        placeholder="Contoh: Kebun Sawit Alpha"
                        className={`w-full border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 ${
                            fieldErrors.namaKebun ? "border-red-400 bg-red-50" : "border-gray-300"
                        }`}
                        required
                    />
                    {fieldErrors.namaKebun && (
                        <p className="mt-1 text-xs text-red-600">{fieldErrors.namaKebun}</p>
                    )}
                </div>

                {/* Luas Hektare */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Luas (Hektare) <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="number"
                        name="luasHektare"
                        value={form.luasHektare}
                        onChange={handleChange}
                        placeholder="Contoh: 50.5"
                        step="0.01"
                        min="0.01"
                        className={`w-full border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 ${
                            fieldErrors.luasHektare ? "border-red-400 bg-red-50" : "border-gray-300"
                        }`}
                        required
                    />
                    {fieldErrors.luasHektare && (
                        <p className="mt-1 text-xs text-red-600">{fieldErrors.luasHektare}</p>
                    )}
                </div>

                {/* Koordinat */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                        Koordinat (4 titik Lat, Lon) <span className="text-red-500">*</span>
                    </label>
                    <textarea
                        name="koordinat"
                        value={form.koordinat}
                        onChange={handleChange}
                        placeholder="Contoh: [(0,0),(100,0),(100,100),(0,100)]"
                        rows={3}
                        className={`w-full border rounded-md px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-green-500 resize-none ${
                            fieldErrors.koordinat ? "border-red-400 bg-red-50" : "border-gray-300"
                        }`}
                        required
                    />
                    <p className="mt-1 text-xs text-gray-400">Format: [(lat,lon),(lat,lon),(lat,lon),(lat,lon)]</p>
                    {fieldErrors.koordinat && (
                        <p className="mt-1 text-xs text-red-600">{fieldErrors.koordinat}</p>
                    )}
                </div>

                <div className="flex gap-3 pt-2">
                    <button
                        type="submit"
                        disabled={loading}
                        className="px-6 py-2 bg-green-600 text-white text-sm font-medium rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                    >
                        {loading ? "Menyimpan..." : "Simpan Kebun"}
                    </button>
                    <Link
                        href="/kebun"
                        className="px-6 py-2 bg-gray-200 text-gray-800 text-sm font-medium rounded-md hover:bg-gray-300 transition"
                    >
                        Batal
                    </Link>
                </div>
            </form>
        </div>
    );
}
