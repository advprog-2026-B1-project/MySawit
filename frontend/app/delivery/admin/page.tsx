"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { deliveryApi, Delivery } from "../../../services/deliveryApi";

const inputCls = "bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition";

export default function AdminDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    const [mandorFilter, setMandorFilter] = useState("");
    const [dateFilter, setDateFilter] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            let data = await deliveryApi.getAdminReady();

            if (mandorFilter) {
                const kw = mandorFilter.toLowerCase();
                data = data.filter(d => d.mandor?.nama?.toLowerCase().includes(kw));
            }
            if (dateFilter) {
                data = data.filter(d => d.mandorDecidedAt?.startsWith(dateFilter));
            }

            setDeliveries(data);
            setLoading(false);
        };
        fetch();
    }, [mandorFilter, dateFilter]);

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" });
    };

    return (
        <div className="px-8 py-6 max-w-6xl mx-auto">
            <h1 className="text-xl font-semibold text-bone mb-1">Review Pengiriman — Admin</h1>
            <p className="text-sm text-bone/40 mb-8">Persetujuan akhir sebelum pencairan payroll</p>

            {/* Filters */}
            <div className="flex flex-wrap gap-3 mb-6">
                <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-bone/30 text-sm">⌕</span>
                    <input
                        type="text"
                        placeholder="Cari nama mandor..."
                        className={`${inputCls} pl-8 w-56 placeholder:text-bone/30`}
                        value={mandorFilter}
                        onChange={(e) => setMandorFilter(e.target.value)}
                    />
                </div>
                <input
                    type="date"
                    className={inputCls}
                    value={dateFilter}
                    onChange={(e) => setDateFilter(e.target.value)}
                />
                {(mandorFilter || dateFilter) && (
                    <button
                        onClick={() => { setMandorFilter(""); setDateFilter(""); }}
                        className="text-xs text-bone/40 hover:text-bone transition px-2"
                    >
                        Reset
                    </button>
                )}
            </div>

            <div className="rounded-lg border border-white/10 overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead>
                        <tr className="bg-ink-soft border-b border-white/10">
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Mandor</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Supir</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kebun Asal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Berat</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Tgl Verifikasi</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Status Admin</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="bg-ink-muted divide-y divide-white/5">
                        {loading ? (
                            <tr><td colSpan={7} className="px-5 py-12 text-center text-bone/30">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d, i) => (
                                <tr key={d.id} className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}>
                                    <td className="px-5 py-3.5 font-medium text-bone">{d.mandor?.nama}</td>
                                    <td className="px-5 py-3.5 text-bone/80">{d.driver?.nama}</td>
                                    <td className="px-5 py-3.5 text-bone/80">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-3.5 text-bone">{d.hasilPanen?.kilogram} <span className="text-bone/50 text-xs">Kg</span></td>
                                    <td className="px-5 py-3.5 text-bone/50 text-xs">{formatDate(d.mandorDecidedAt)}</td>
                                    <td className="px-5 py-3.5">
                                        <span className="px-2 py-1 text-xs font-semibold rounded-full bg-yellow-500/10 text-yellow-400">
                                            Menunggu Review
                                        </span>
                                    </td>
                                    <td className="px-5 py-3.5 text-center">
                                        <Link
                                            href={`/delivery/admin/${d.id}`}
                                            className="px-2.5 py-1 text-xs bg-verdant text-ink font-semibold rounded hover:bg-verdant-hover transition inline-block"
                                        >
                                            Review
                                        </Link>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={7} className="px-5 py-12 text-center text-bone/30">Tidak ada pengiriman yang menunggu review.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
                {deliveries.length > 0 && (
                    <div className="px-5 py-3 bg-ink-soft border-t border-white/10">
                        <span className="text-xs text-bone/30">{deliveries.length} butuh review</span>
                    </div>
                )}
            </div>
        </div>
    );
}