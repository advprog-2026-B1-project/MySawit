"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { deliveryApi, Delivery } from "../../../services/deliveryApi";

export default function MandorDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    const [viewMode, setViewMode] = useState<"ongoing" | "history">("ongoing");

    const [keyword, setKeyword] = useState("");
    const [statusFilter, setStatusFilter] = useState("");
    const [dateFilter, setDateFilter] = useState("");

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const mandorId = 1;

    const fetchDeliveries = async () => {
        setLoading(true);
        setError("");
        try {
            const data = viewMode === "ongoing"
                ? await deliveryApi.getMandorOngoing(mandorId)
                : await deliveryApi.getMandorHistory(mandorId);

            let filtered = data;

            if (keyword) {
                const kw = keyword.toLowerCase();
                filtered = filtered.filter(d =>
                    d.driver?.nama?.toLowerCase().includes(kw) ||
                    d.hasilPanen?.kebun?.namaKebun?.toLowerCase().includes(kw)
                );
            }
            if (statusFilter) {
                filtered = filtered.filter(d => d.status === statusFilter);
            }
            if (dateFilter) {
                filtered = filtered.filter(d => d.createdAt?.startsWith(dateFilter));
            }

            setDeliveries(filtered);
        } catch (err: any) {
            setError(err.message || "Gagal memuat data.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDeliveries();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [viewMode, keyword, statusFilter, dateFilter]);

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" });
    };

    return (
        <div className="p-6 max-w-6xl mx-auto text-ink">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Manajemen Pengiriman</h1>
                <Link
                    href="/delivery/mandor/create"
                    className="px-4 py-2 bg-verdant text-ink font-bold rounded-md shadow hover:opacity-90 transition"
                >
                    + Buat Penugasan
                </Link>
            </div>

            {/* Tabs */}
            <div className="flex gap-3 mb-4">
                <button
                    onClick={() => setViewMode("ongoing")}
                    className={`px-4 py-2 font-semibold rounded-md transition ${viewMode === "ongoing" ? "bg-verdant text-ink" : "bg-white text-ink border border-ink/20"}`}
                >
                    Sedang Berlangsung
                </button>
                <button
                    onClick={() => setViewMode("history")}
                    className={`px-4 py-2 font-semibold rounded-md transition ${viewMode === "history" ? "bg-verdant text-ink" : "bg-white text-ink border border-ink/20"}`}
                >
                    Riwayat
                </button>
            </div>

            {/* Filters */}
            <div className="flex flex-wrap gap-3 mb-6 bg-white p-4 rounded-lg border border-ink/10 shadow-sm">
                <input
                    type="text"
                    placeholder="Cari nama supir / kebun..."
                    className="border border-ink/20 rounded-md px-3 py-2 w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-verdant bg-bone text-sm"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                />
                <select
                    className="border border-ink/20 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-verdant bg-bone text-sm"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                    <option value="">Semua Status</option>
                    <option value="Memuat">Memuat</option>
                    <option value="Mengirim">Mengirim</option>
                    <option value="Tiba">Tiba</option>
                </select>
                <input
                    type="date"
                    className="border border-ink/20 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-verdant bg-bone text-sm text-ink"
                    value={dateFilter}
                    onChange={(e) => setDateFilter(e.target.value)}
                />
            </div>

            {error && <div className="mb-4 text-red-600 bg-red-50 border border-red-200 p-3 rounded text-sm">{error}</div>}

            {/* Table */}
            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-ink/10">
                <table className="min-w-full divide-y divide-ink/10 text-left text-sm">
                    <thead className="bg-ink text-bone">
                        <tr>
                            <th className="px-5 py-3 font-semibold">Supir</th>
                            <th className="px-5 py-3 font-semibold">Kebun Asal</th>
                            <th className="px-5 py-3 font-semibold">Berat</th>
                            <th className="px-5 py-3 font-semibold">Tanggal</th>
                            <th className="px-5 py-3 font-semibold">Status</th>
                            <th className="px-5 py-3 font-semibold">Keputusan</th>
                            <th className="px-5 py-3 font-semibold text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-ink/5">
                        {loading ? (
                            <tr><td colSpan={7} className="px-5 py-8 text-center text-ink/50">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d) => (
                                <tr key={d.id} className="hover:bg-bone/60 transition">
                                    <td className="px-5 py-4 font-medium">
                                        <Link href={`/delivery/mandor/supir/${d.driver?.id}`} className="text-blue-600 hover:underline">
                                            {d.driver?.nama}
                                        </Link>
                                    </td>
                                    <td className="px-5 py-4">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-4">{d.hasilPanen?.kilogram} Kg</td>
                                    <td className="px-5 py-4 text-ink/70">{formatDate(d.createdAt)}</td>
                                    <td className="px-5 py-4">
                                        <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                            d.status === "Memuat" ? "bg-yellow-100 text-yellow-800" :
                                            d.status === "Mengirim" ? "bg-blue-100 text-blue-800" :
                                            "bg-green-100 text-green-800"
                                        }`}>
                                            {d.status}
                                        </span>
                                    </td>
                                    <td className="px-5 py-4">
                                        <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                            d.mandorDecision === "Approved" ? "bg-green-100 text-green-800" :
                                            d.mandorDecision === "Rejected" ? "bg-red-100 text-red-800" :
                                            "bg-gray-100 text-gray-600"
                                        }`}>
                                            {d.mandorDecision === "Pending" ? "Menunggu" : d.mandorDecision === "Approved" ? "Disetujui" : "Ditolak"}
                                        </span>
                                    </td>
                                    <td className="px-5 py-4 text-center">
                                        <Link
                                            href={`/delivery/mandor/${d.id}`}
                                            className="px-3 py-1.5 bg-blue-600 text-white rounded-md text-xs font-medium hover:bg-blue-700 transition"
                                        >
                                            Detail
                                        </Link>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={7} className="px-5 py-8 text-center text-ink/50">Tidak ada pengiriman ditemukan.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}