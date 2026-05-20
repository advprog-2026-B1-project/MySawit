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
                const res = await fetch(`${API}/api/kebun/${id}`);
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

    if (fetching) return <div className="p-8 text-center text-gray-400">Memuat data kebun...</div>;

    return (
        <div className="p-6 max-w-2xl mx-auto text-gray-800">
            <div className="flex items-center gap-2 mb-6">
                <Link href={`/kebun/${id}`} className="text-sm text-green-600 hover:underline">← Kembali ke Detail Kebun</Link>
            </div>

            <h1 className="text-2xl font-bold text-green-700 mb-6">Edit Kebun</h1>

            {error && (
                <div className="mb-5 px-4 py-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="bg-white rounded-lg border border-gray-200 shadow-sm p-6 space-y-5">
                {/* Kode Kebun — read only */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Kode Kebun</label>
                    <input
                        type="text"
                        value={kodeKebun}
                        readOnly
                        className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm bg-gray-100 text-gray-500 cursor-not-allowed font-mono"
                    />
                    <p className="mt-1 text-xs text-gray-400">Kode kebun tidak dapat diubah setelah dibuat.</p>
                </div>

                {/* Nama Kebun */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Nama Kebun</label>
                    <input
                        type="text"
                        name="namaKebun"
                        value={form.namaKebun}
                        onChange={handleChange}
                        className={`w-full border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 ${
                            fieldErrors.namaKebun ? "border-red-400 bg-red-50" : "border-gray-300"
                        }`}
                    />
                    {fieldErrors.namaKebun && <p className="mt-1 text-xs text-red-600">{fieldErrors.namaKebun}</p>}
                </div>

                {/* Luas Hektare */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Luas (Hektare)</label>
                    <input
                        type="number"
                        name="luasHektare"
                        value={form.luasHektare}
                        onChange={handleChange}
                        step="0.01"
                        min="0.01"
                        className={`w-full border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-green-500 ${
                            fieldErrors.luasHektare ? "border-red-400 bg-red-50" : "border-gray-300"
                        }`}
                    />
                    {fieldErrors.luasHektare && <p className="mt-1 text-xs text-red-600">{fieldErrors.luasHektare}</p>}
                </div>

                {/* Koordinat */}
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Koordinat</label>
                    <textarea
                        name="koordinat"
                        value={form.koordinat}
                        onChange={handleChange}
                        rows={3}
                        className={`w-full border rounded-md px-3 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-green-500 resize-none ${
                            fieldErrors.koordinat ? "border-red-400 bg-red-50" : "border-gray-300"
                        }`}
                    />
                    <p className="mt-1 text-xs text-gray-400">Format: [(lat,lon),(lat,lon),(lat,lon),(lat,lon)]</p>
                    {fieldErrors.koordinat && <p className="mt-1 text-xs text-red-600">{fieldErrors.koordinat}</p>}
                </div>

                <div className="flex gap-3 pt-2">
                    <button
                        type="submit"
                        disabled={loading}
                        className="px-6 py-2 bg-green-600 text-white text-sm font-medium rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                    >
                        {loading ? "Menyimpan..." : "Simpan Perubahan"}
                    </button>
                    <Link
                        href={`/kebun/${id}`}
                        className="px-6 py-2 bg-gray-200 text-gray-800 text-sm font-medium rounded-md hover:bg-gray-300 transition"
                    >
                        Batal
                    </Link>
                </div>
            </form>
        </div>
    );
}
