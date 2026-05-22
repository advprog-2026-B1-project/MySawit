"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { deliveryApi, Delivery } from "../../../services/deliveryApi";

const inputCls = "bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-1.5 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition";

export default function SupirDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    const [viewMode, setViewMode] = useState<"assigned" | "history">("assigned");
    const [dateFilter, setDateFilter] = useState("");
    const [loading, setLoading] = useState(true);
    const [driverId, setDriverId] = useState<number | null>(null);

    useEffect(() => {
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/me`, { credentials: "include" })
            .then(r => r.ok ? r.json() : null)
            .then(u => { if (u?.id) setDriverId(u.id); })
            .catch(() => {});
    }, []);

    useEffect(() => {
        if (!driverId) return;
        const fetchData = async () => {
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
        fetchData();
    }, [viewMode, dateFilter, driverId]);

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" });
    };

    return (
        <div className="px-8 py-6 max-w-6xl mx-auto">
            <h1 className="text-xl font-semibold text-bone mb-8">Tugas Pengiriman</h1>

            <div className="flex gap-3 mb-6">
                <button
                    onClick={() => setViewMode("assigned")}
                    className={`px-4 py-2 text-sm font-semibold rounded-md transition ${viewMode === "assigned" ? "bg-verdant-soft text-verdant" : "text-bone/60 hover:text-bone hover:bg-white/5"}`}
                >
                    Dalam Perjalanan
                </button>
                <button
                    onClick={() => setViewMode("history")}
                    className={`px-4 py-2 text-sm font-semibold rounded-md transition ${viewMode === "history" ? "bg-verdant-soft text-verdant" : "text-bone/60 hover:text-bone hover:bg-white/5"}`}
                >
                    Riwayat
                </button>
            </div>

            {viewMode === "history" && (
                <div className="mb-6 flex items-center gap-3">
                    <label className="text-sm font-semibold text-bone/60">Filter Tanggal:</label>
                    <input
                        type="date"
                        className={`${inputCls} text-bone`}
                        value={dateFilter}
                        onChange={(e) => setDateFilter(e.target.value)}
                    />
                    {dateFilter && (
                        <button onClick={() => setDateFilter("")} className="text-xs text-bone/40 hover:text-bone transition">Reset</button>
                    )}
                </div>
            )}

            <div className="rounded-lg border border-white/10 overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead>
                        <tr className="bg-ink-soft border-b border-white/10">
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kebun Asal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Berat</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Mandor</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Tanggal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Status</th>
                            {viewMode === "history" && <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Verifikasi</th>}
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="bg-ink-muted divide-y divide-white/5">
                        {loading ? (
                            <tr><td colSpan={7} className="px-5 py-12 text-center text-bone/30">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d, i) => (
                                <tr key={d.id} className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}>
                                    <td className="px-5 py-3.5 font-medium text-bone">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-3.5 text-bone">{d.hasilPanen?.kilogram} <span className="text-bone/50 text-xs">Kg</span></td>
                                    <td className="px-5 py-3.5 text-bone/80">{d.mandor?.nama}</td>
                                    <td className="px-5 py-3.5 text-bone/50 text-xs">{formatDate(d.createdAt)}</td>
                                    <td className="px-5 py-3.5">
                                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                                            d.status === "Memuat" ? "bg-yellow-500/10 text-yellow-400" :
                                            d.status === "Mengirim" ? "bg-blue-500/10 text-blue-400" :
                                            "bg-verdant-soft text-verdant"
                                        }`}>
                                            {d.status}
                                        </span>
                                    </td>
                                    {viewMode === "history" && (
                                        <td className="px-5 py-3.5">
                                            <div className="flex flex-col gap-1 items-start">
                                                <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                                                    d.mandorDecision === "Approved" ? "bg-verdant-soft text-verdant" :
                                                    d.mandorDecision === "Rejected" ? "bg-red-500/10 text-red-400" :
                                                    "bg-white/5 text-bone/50"
                                                }`}>
                                                    {d.mandorDecision === "Approved" ? "Disetujui" : d.mandorDecision === "Rejected" ? "Ditolak" : "Menunggu"}
                                                </span>
                                                {d.mandorDecision === "Rejected" && d.mandorRejectionReason && (
                                                    <span className="text-[10px] text-red-400 bg-red-500/10 px-2 py-0.5 rounded border border-red-500/20 max-w-[150px] truncate" title={d.mandorRejectionReason}>
                                                        Alasan: {d.mandorRejectionReason}
                                                    </span>
                                                )}
                                            </div>
                                        </td>
                                    )}
                                    <td className="px-5 py-3.5 text-center">
                                        <Link
                                            href={`/delivery/supir/${d.id}`}
                                            className="px-2.5 py-1 text-xs border border-verdant/30 text-verdant rounded hover:bg-verdant-soft transition inline-block"
                                        >
                                            {viewMode === "assigned" ? "Update" : "Detail"}
                                        </Link>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={7} className="px-5 py-12 text-center text-bone/30">Tidak ada pengiriman.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}