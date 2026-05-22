"use client";

import { useState, useEffect } from "react";

interface Harvest {
    id: number;
    tanggalPanen: string;
    kilogram: number;
    berita: string;
    status: string;
    fotoUrls: string[];
    rejectionReason: string;
    namaBuruh?: string;
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

export default function MandorDashboard() {
    const [harvests, setHarvests] = useState<Harvest[]>([]);
    const [selectedHarvestId, setSelectedHarvestId] = useState<number | null>(null);
    const [rejectReason, setRejectReason] = useState("");
    const [isModalOpen, setIsModalOpen] = useState(false);

    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [statusFilter, setStatusFilter] = useState("");
    const [searchNama, setSearchNama] = useState("");

    const [refreshTrigger, setRefreshTrigger] = useState(0);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const queryParams = new URLSearchParams();
                if (startDate) queryParams.append("startDate", startDate);
                if (endDate) queryParams.append("endDate", endDate);
                if (statusFilter) queryParams.append("status", statusFilter);
                if (searchNama) queryParams.append("searchNama", searchNama);

                const response = await fetch(`http://localhost:8080/api/harvest/mandor?${queryParams.toString()}`, { credentials: "include" });

                if (response.ok) {
                    const data = await response.json();
                    setHarvests(data);
                }
            } catch (error) {
                console.error("Terjadi kesalahan:", error);
            }
        };

        fetchData();
    }, [startDate, endDate, statusFilter, searchNama, refreshTrigger]);

    const handleApprove = async (id: number) => {
        try {
            const response = await fetch(`http://localhost:8080/api/harvest/${id}/approve`, { method: "PUT", credentials: "include" });
            if (response.ok) {
                alert("Berhasil disetujui!");
                setRefreshTrigger(prev => prev + 1);
            }
        } catch (error) {
            console.error("Error approve:", error);
        }
    };

    const openRejectModal = (id: number) => {
        setSelectedHarvestId(id);
        setRejectReason("");
        setIsModalOpen(true);
    };

    const handleRejectSubmit = async () => {
        if (!selectedHarvestId) return;

        try {
            const response = await fetch(`http://localhost:8080/api/harvest/${selectedHarvestId}/reject`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ alasan: rejectReason }),
            });

            if (response.ok) {
                alert("Berhasil ditolak!");
                setIsModalOpen(false);
                setRefreshTrigger(prev => prev + 1);
            }
        } catch (error) {
            console.error("Error reject:", error);
        }
    };

    const pendingCount = harvests.filter(h => h.status === 'Pending').length;

    return (
        <div className="px-8 py-6 max-w-5xl">

            {/* Page header */}
            <div className="flex items-start justify-between mb-8">
                <div>
                    <h1 className="text-xl font-semibold text-bone">Dashboard Mandor</h1>
                    <p className="text-sm text-bone/40 mt-1">
                        Review dan approval laporan panen buruh
                        {pendingCount > 0 && (
                            <span className="ml-2 px-1.5 py-0.5 bg-verdant-soft text-verdant text-xs rounded-full font-medium">
                                {pendingCount} pending
                            </span>
                        )}
                    </p>
                </div>
            </div>

            {/* Filter bar */}
            <div className="flex flex-wrap items-center gap-3 mb-5">
                <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-bone/30 text-sm">⌕</span>
                    <input
                        type="text"
                        placeholder="Cari nama buruh..."
                        className={`${inputCls} pl-8 w-48`}
                        value={searchNama}
                        onChange={(e) => setSearchNama(e.target.value)}
                    />
                </div>
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
                {(startDate || endDate || statusFilter || searchNama) && (
                    <button
                        onClick={() => { setStartDate(""); setEndDate(""); setStatusFilter(""); setSearchNama(""); }}
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
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">ID</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Tanggal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Buruh</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kilogram</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Status</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="bg-ink-muted divide-y divide-white/5">
                        {harvests.length > 0 ? (
                            harvests.map((panen, i) => (
                                <tr key={panen.id} className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}>
                                    <td className="px-5 py-3.5 text-bone/30 font-mono text-xs">{panen.id}</td>
                                    <td className="px-5 py-3.5 text-bone/70 whitespace-nowrap">{panen.tanggalPanen}</td>
                                    <td className="px-5 py-3.5 text-bone font-medium">{panen.namaBuruh || "-"}</td>
                                    <td className="px-5 py-3.5 text-bone">{panen.kilogram} kg</td>
                                    <td className="px-5 py-3.5"><StatusBadge status={panen.status} /></td>
                                    <td className="px-5 py-3.5">
                                        <div className="flex items-center gap-2">
                                            <button
                                                onClick={() => handleApprove(panen.id)}
                                                className="px-2.5 py-1 text-xs bg-verdant text-ink font-semibold rounded hover:bg-verdant-hover disabled:opacity-30 disabled:cursor-not-allowed transition"
                                                disabled={panen.status !== 'Pending'}
                                            >
                                                Approve
                                            </button>
                                            <button
                                                onClick={() => openRejectModal(panen.id)}
                                                className="px-2.5 py-1 text-xs border border-red-500/30 text-red-400 rounded hover:bg-red-500/10 disabled:opacity-30 disabled:cursor-not-allowed transition"
                                                disabled={panen.status !== 'Pending'}
                                            >
                                                Reject
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={6} className="px-5 py-12 text-center text-bone/30 text-sm">
                                    Tidak ada data panen ditemukan.
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

            {/* Reject modal */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div
                        className="absolute inset-0 bg-ink/80 backdrop-blur-sm"
                        onClick={() => setIsModalOpen(false)}
                    />
                    <div className="relative bg-ink-muted border border-white/10 rounded-lg w-full max-w-md shadow-2xl">

                        {/* Modal header */}
                        <div className="px-6 py-4 border-b border-white/10 flex items-center justify-between">
                            <div>
                                <h3 className="text-sm font-semibold text-bone">Tolak Hasil Panen</h3>
                                <p className="text-xs text-bone/40 mt-0.5">ID #{selectedHarvestId}</p>
                            </div>
                            <button
                                onClick={() => setIsModalOpen(false)}
                                className="text-bone/30 hover:text-bone transition text-lg leading-none"
                            >
                                ✕
                            </button>
                        </div>

                        {/* Modal body */}
                        <div className="px-6 py-5">
                            <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-2">
                                Alasan Penolakan
                            </label>
                            <textarea
                                className="w-full bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-red-500 focus:border-red-500 focus:outline-none placeholder:text-bone/30 resize-none transition"
                                placeholder="Contoh: Buah masih mentah, berat tidak sesuai..."
                                value={rejectReason}
                                onChange={(e) => setRejectReason(e.target.value)}
                                rows={4}
                                autoFocus
                            />
                        </div>

                        {/* Modal footer */}
                        <div className="px-6 py-4 border-t border-white/10 flex items-center justify-end gap-3">
                            <button
                                className="text-sm text-bone/40 hover:text-bone transition"
                                onClick={() => setIsModalOpen(false)}
                            >
                                Batal
                            </button>
                            <button
                                className="px-4 py-2 bg-red-600 text-white text-sm font-semibold rounded-md hover:bg-red-700 disabled:opacity-40 disabled:cursor-not-allowed transition"
                                onClick={handleRejectSubmit}
                                disabled={!rejectReason.trim()}
                            >
                                Tolak Panen
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
