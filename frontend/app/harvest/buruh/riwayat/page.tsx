"use client";

import { useState, useEffect } from "react";
import Link from "next/link";

interface Harvest {
    id: number;
    tanggalPanen: string;
    kilogram: number;
    berita: string;
    status: string;
    fotoUrls: string[];
    rejectionReason: string;
}

function StatusBadge({ status }: { status: string }) {
    const cls =
        status === 'Approved' ? 'bg-verdant-soft text-verdant' :
        status === 'Rejected' ? 'bg-red-500/10 text-red-400' :
        'bg-white/5 text-bone/50';
    return (
        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${cls}`}>
            {status}
        </span>
    );
}

const inputCls = "bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition";

export default function RiwayatPanenBuruh() {
    const [harvests, setHarvests] = useState<Harvest[]>([]);
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [statusFilter, setStatusFilter] = useState("");

    useEffect(() => {
        const fetchData = async () => {
            try {
                const queryParams = new URLSearchParams();
                if (startDate) queryParams.append("startDate", startDate);
                if (endDate) queryParams.append("endDate", endDate);
                if (statusFilter) queryParams.append("status", statusFilter);

                const response = await fetch(`http://localhost:8080/api/harvest/me?${queryParams.toString()}`);

                if (response.ok) {
                    const data = await response.json();
                    setHarvests(data);
                } else {
                    console.error("Gagal mengambil data riwayat");
                }
            } catch (error) {
                console.error("Terjadi kesalahan koneksi:", error);
            }
        };

        fetchData();
    }, [startDate, endDate, statusFilter]);

    return (
        <div className="px-8 py-6 max-w-5xl">

            {/* Page header */}
            <div className="flex items-start justify-between mb-8">
                <div>
                    <h1 className="text-xl font-semibold text-bone">Riwayat Panen Saya</h1>
                    <p className="text-sm text-bone/40 mt-1">Semua laporan panen yang pernah kamu kirim</p>
                </div>
                <Link
                    href="/harvest/buruh/lapor"
                    className="px-4 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover transition shrink-0"
                >
                    + Lapor Panen
                </Link>
            </div>

            {/* Filter bar */}
            <div className="flex flex-wrap items-center gap-3 mb-5">
                <input
                    type="date"
                    className={inputCls}
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                />
                <span className="text-bone/20 text-sm">→</span>
                <input
                    type="date"
                    className={inputCls}
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                />
                <select
                    className={`${inputCls} min-w-[140px]`}
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                    <option value="">Semua Status</option>
                    <option value="Pending">Pending</option>
                    <option value="Approved">Approved</option>
                    <option value="Rejected">Rejected</option>
                </select>
                {(startDate || endDate || statusFilter) && (
                    <button
                        onClick={() => { setStartDate(""); setEndDate(""); setStatusFilter(""); }}
                        className="text-xs text-bone/40 hover:text-bone transition px-2"
                    >
                        Reset
                    </button>
                )}
            </div>

            {/* Table */}
            <div className="rounded-lg border border-white/10 overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead>
                        <tr className="bg-ink-soft border-b border-white/10">
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Tanggal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kilogram</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Berita</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Status</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Catatan Mandor</th>
                        </tr>
                    </thead>
                    <tbody className="bg-ink-muted divide-y divide-white/5">
                        {harvests.length > 0 ? (
                            harvests.map((panen, i) => (
                                <tr key={panen.id} className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}>
                                    <td className="px-5 py-3.5 text-bone/70 whitespace-nowrap">{panen.tanggalPanen}</td>
                                    <td className="px-5 py-3.5 text-bone font-medium whitespace-nowrap">{panen.kilogram} kg</td>
                                    <td className="px-5 py-3.5 text-bone/60 max-w-[200px] truncate" title={panen.berita}>
                                        {panen.berita || "-"}
                                    </td>
                                    <td className="px-5 py-3.5 whitespace-nowrap">
                                        <StatusBadge status={panen.status} />
                                    </td>
                                    <td className="px-5 py-3.5 text-red-400/70 text-xs max-w-[180px] truncate" title={panen.rejectionReason}>
                                        {panen.rejectionReason || <span className="text-bone/20">—</span>}
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={5} className="px-5 py-12 text-center text-bone/30 text-sm">
                                    Belum ada riwayat panen sesuai filter.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
                {harvests.length > 0 && (
                    <div className="px-5 py-3 bg-ink-soft border-t border-white/10">
                        <span className="text-xs text-bone/30">{harvests.length} entri</span>
                    </div>
                )}
            </div>
        </div>
    );
}
