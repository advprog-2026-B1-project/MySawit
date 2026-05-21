"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { deliveryApi, Delivery } from "../../../services/deliveryApi";

function StatusBadge({ status }: { status: string }) {
    const cls =
        status === "Tiba" ? "bg-verdant-soft text-verdant" :
        status === "Mengirim" ? "bg-blue-500/10 text-blue-400" :
        "bg-yellow-500/10 text-yellow-400";
    return <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${cls}`}>{status}</span>;
}

function DecisionBadge({ decision }: { decision: string }) {
    const map: Record<string, { cls: string; label: string }> = {
        Pending: { cls: "bg-white/5 text-bone/50", label: "Menunggu" },
        Approved: { cls: "bg-verdant-soft text-verdant", label: "Disetujui" },
        Rejected: { cls: "bg-red-500/10 text-red-400", label: "Ditolak" },
    };
    const { cls, label } = map[decision] ?? map.Pending;
    return <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${cls}`}>{label}</span>;
}

const inputCls = "bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition";

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
        <div className="px-8 py-6 max-w-6xl mx-auto">
            <div className="flex items-start justify-between mb-8">
                <div>
                    <h1 className="text-xl font-semibold text-bone">Manajemen Pengiriman</h1>
                    <p className="text-sm text-bone/40 mt-1">Pantau dan verifikasi pengiriman sawit</p>
                </div>
                <Link
                    href="/delivery/mandor/create"
                    className="px-4 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover transition shrink-0"
                >
                    + Buat Penugasan
                </Link>
            </div>

            {/* Tabs */}
            <div className="flex gap-3 mb-6">
                <button
                    onClick={() => setViewMode("ongoing")}
                    className={`px-4 py-2 text-sm font-semibold rounded-md transition ${viewMode === "ongoing" ? "bg-verdant-soft text-verdant" : "text-bone/60 hover:text-bone hover:bg-white/5"}`}
                >
                    Sedang Berlangsung
                </button>
                <button
                    onClick={() => setViewMode("history")}
                    className={`px-4 py-2 text-sm font-semibold rounded-md transition ${viewMode === "history" ? "bg-verdant-soft text-verdant" : "text-bone/60 hover:text-bone hover:bg-white/5"}`}
                >
                    Riwayat
                </button>
            </div>

            {/* Filters */}
            <div className="flex flex-wrap gap-3 mb-6">
                <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-bone/30 text-sm">⌕</span>
                    <input
                        type="text"
                        placeholder="Cari nama supir / kebun..."
                        className={`${inputCls} pl-8 w-56 placeholder:text-bone/30`}
                        value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                    />
                </div>
                <select
                    className={`${inputCls} min-w-[150px]`}
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
                    className={inputCls}
                    value={dateFilter}
                    onChange={(e) => setDateFilter(e.target.value)}
                />
                {(keyword || statusFilter || dateFilter) && (
                    <button
                        onClick={() => { setKeyword(""); setStatusFilter(""); setDateFilter(""); }}
                        className="text-xs text-bone/40 hover:text-bone transition px-2"
                    >
                        Reset
                    </button>
                )}
            </div>

            {error && (
                <div className="mb-6 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}

            {/* Table */}
            <div className="rounded-lg border border-white/10 overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead>
                        <tr className="bg-ink-soft border-b border-white/10">
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Supir</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kebun Asal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Berat</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Tanggal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Status</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Keputusan</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="bg-ink-muted divide-y divide-white/5">
                        {loading ? (
                            <tr><td colSpan={7} className="px-5 py-12 text-center text-bone/30">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d, i) => (
                                <tr key={d.id} className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}>
                                    <td className="px-5 py-3.5 font-medium text-bone">
                                        <Link href={`/delivery/mandor/supir/${d.driver?.id}`} className="hover:text-verdant transition underline underline-offset-2 decoration-white/20 hover:decoration-verdant/50">
                                            {d.driver?.nama}
                                        </Link>
                                    </td>
                                    <td className="px-5 py-3.5 text-bone/80">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-3.5 text-bone">{d.hasilPanen?.kilogram} <span className="text-bone/50 text-xs">Kg</span></td>
                                    <td className="px-5 py-3.5 text-bone/50 text-xs">{formatDate(d.createdAt)}</td>
                                    <td className="px-5 py-3.5"><StatusBadge status={d.status} /></td>
                                    <td className="px-5 py-3.5"><DecisionBadge decision={d.mandorDecision} /></td>
                                    <td className="px-5 py-3.5 text-center">
                                        <Link
                                            href={`/delivery/mandor/${d.id}`}
                                            className="px-2.5 py-1 text-xs border border-verdant/30 text-verdant rounded hover:bg-verdant-soft transition inline-block"
                                        >
                                            Detail
                                        </Link>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={7} className="px-5 py-12 text-center text-bone/30">Tidak ada pengiriman ditemukan.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
                {deliveries.length > 0 && (
                    <div className="px-5 py-3 bg-ink-soft border-t border-white/10">
                        <span className="text-xs text-bone/30">{deliveries.length} pengiriman</span>
                    </div>
                )}
            </div>
        </div>
    );
}