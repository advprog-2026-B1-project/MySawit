"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { deliveryApi, Delivery } from "../../../services/deliveryApi";

export default function SupirDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    const [viewMode, setViewMode] = useState<"assigned" | "history">("assigned");
    const [dateFilter, setDateFilter] = useState("");
    const [loading, setLoading] = useState(true);

    const driverId = 2;

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            const data = await deliveryApi.getByDriver(driverId);
            let filtered = viewMode === "assigned"
                ? data.filter(d => d.status !== "Tiba")
                : data.filter(d => d.status === "Tiba");
            
            if (dateFilter) {
                filtered = filtered.filter(d => d.createdAt?.startsWith(dateFilter));
            }
            
            setDeliveries(filtered);
            setLoading(false);
        };
        fetch();
    }, [viewMode, dateFilter]);

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" });
    };

    return (
        <div className="p-6 max-w-6xl mx-auto text-ink">
            <h1 className="text-2xl font-bold mb-6">Tugas Pengiriman</h1>

            <div className="flex gap-3 mb-6">
                <button
                    onClick={() => setViewMode("assigned")}
                    className={`px-4 py-2 font-semibold rounded-md transition ${viewMode === "assigned" ? "bg-verdant text-ink" : "bg-white text-ink border border-ink/20"}`}
                >
                    Dalam Perjalanan
                </button>
                <button
                    onClick={() => setViewMode("history")}
                    className={`px-4 py-2 font-semibold rounded-md transition ${viewMode === "history" ? "bg-verdant text-ink" : "bg-white text-ink border border-ink/20"}`}
                >
                    Riwayat
                </button>
            </div>

            {viewMode === "history" && (
                <div className="mb-6 bg-white p-4 rounded-lg border border-ink/10 shadow-sm flex items-center gap-3">
                    <label className="text-sm font-semibold">Filter Tanggal:</label>
                    <input
                        type="date"
                        className="border border-ink/20 rounded-md px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-verdant bg-bone text-sm text-ink"
                        value={dateFilter}
                        onChange={(e) => setDateFilter(e.target.value)}
                    />
                    {dateFilter && (
                        <button onClick={() => setDateFilter("")} className="text-xs text-red-500 hover:underline">Reset</button>
                    )}
                </div>
            )}

            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-ink/10">
                <table className="min-w-full divide-y divide-ink/10 text-left text-sm">
                    <thead className="bg-ink text-bone">
                        <tr>
                            <th className="px-5 py-3 font-semibold">Kebun Asal</th>
                            <th className="px-5 py-3 font-semibold">Berat</th>
                            <th className="px-5 py-3 font-semibold">Mandor</th>
                            <th className="px-5 py-3 font-semibold">Tanggal</th>
                            <th className="px-5 py-3 font-semibold">Status</th>
                            {viewMode === "history" && <th className="px-5 py-3 font-semibold">Verifikasi</th>}
                            <th className="px-5 py-3 font-semibold text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-ink/5">
                        {loading ? (
                            <tr><td colSpan={7} className="px-5 py-8 text-center text-ink/50">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d) => (
                                <tr key={d.id} className="hover:bg-bone/60 transition">
                                    <td className="px-5 py-4 font-medium">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-4">{d.hasilPanen?.kilogram} Kg</td>
                                    <td className="px-5 py-4">{d.mandor?.nama}</td>
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
                                    {viewMode === "history" && (
                                        <td className="px-5 py-4">
                                            <div className="flex flex-col gap-1 items-start">
                                                <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                                    d.mandorDecision === "Approved" ? "bg-green-100 text-green-800" :
                                                    d.mandorDecision === "Rejected" ? "bg-red-100 text-red-800" :
                                                    "bg-gray-100 text-gray-600"
                                                }`}>
                                                    {d.mandorDecision === "Approved" ? "Disetujui" : d.mandorDecision === "Rejected" ? "Ditolak" : "Menunggu"}
                                                </span>
                                                {d.mandorDecision === "Rejected" && d.mandorRejectionReason && (
                                                    <span className="text-[10px] text-red-600 bg-red-50 px-2 py-0.5 rounded-sm border border-red-100 max-w-[150px] truncate" title={d.mandorRejectionReason}>
                                                        Alasan: {d.mandorRejectionReason}
                                                    </span>
                                                )}
                                            </div>
                                        </td>
                                    )}
                                    <td className="px-5 py-4 text-center">
                                        <Link
                                            href={`/delivery/supir/${d.id}`}
                                            className="px-3 py-1.5 bg-blue-600 text-white rounded-md text-xs font-medium hover:bg-blue-700 transition"
                                        >
                                            {viewMode === "assigned" ? "Update" : "Detail"}
                                        </Link>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={7} className="px-5 py-8 text-center text-ink/50">Tidak ada pengiriman.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}