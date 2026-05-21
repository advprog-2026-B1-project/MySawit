"use client";

import { useState, useEffect } from "react";

interface Delivery {
    id: number;
    driverId: number;
    mandorId: number;
    hasilPanenId: number;
    status: string;
    mandorDecision: string;
    adminDecision: string;
    mandorRejectionReason: string | null;
    rejectedKg: number;
    adminRejectionReason: string | null;
}

export default function MandorDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    const [selectedDeliveryId, setSelectedDeliveryId] = useState<number | null>(null);
    const [rejectReason, setRejectReason] = useState("");
    const [isModalOpen, setIsModalOpen] = useState(false);
    
    // State Filter
    const [driverId, setDriverId] = useState("");
    const [date, setDate] = useState("");
    const [keyword, setKeyword] = useState("");
    const [viewMode, setViewMode] = useState<"ongoing" | "history">("ongoing");

    // Modal Create Delivery State
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [selectedDriverForTask, setSelectedDriverForTask] = useState("");
    const [selectedHarvestForTask, setSelectedHarvestForTask] = useState("");

    const [refreshTrigger, setRefreshTrigger] = useState(0);

    // Hardcode Mandor ID for now, since we don't have auth context set up here
    const mandorId = 1; 

    useEffect(() => {
        const fetchData = async () => {
            try {
                const queryParams = new URLSearchParams();
                if (driverId) queryParams.append("driverId", driverId);
                if (date) queryParams.append("date", date);
                if (keyword) queryParams.append("keyword", keyword);

                const endpoint = viewMode === "ongoing" 
                    ? `http://localhost:8080/api/delivery/mandor/${mandorId}/ongoing` 
                    : `http://localhost:8080/api/delivery/mandor/${mandorId}/history`;

                const response = await fetch(`${endpoint}?${queryParams.toString()}`);

                if (response.ok) {
                    const data = await response.json();
                    setDeliveries(data);
                }
            } catch (error) {
                console.error("Terjadi kesalahan:", error);
            }
        };

        fetchData();
    }, [viewMode, driverId, date, keyword, refreshTrigger]);

    const handleApprove = async (id: number) => {
        try {
            const response = await fetch(`http://localhost:8080/api/delivery/${id}/mandor-decision?mandorId=${mandorId}`, { 
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ decision: "Approved", rejectionReason: null })
             });
            if (response.ok) {
                alert("Berhasil disetujui!");
                setRefreshTrigger(prev => prev + 1);
            }
        } catch (error) {
            console.error("Error approve:", error);
        }
    };

    const openRejectModal = (id: number) => {
        setSelectedDeliveryId(id);
        setRejectReason("");
        setIsModalOpen(true);
    };

    const handleRejectSubmit = async () => {
        if (!selectedDeliveryId) return;

        try {
            const response = await fetch(`http://localhost:8080/api/delivery/${selectedDeliveryId}/mandor-decision?mandorId=${mandorId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ decision: "Rejected", rejectionReason: rejectReason }),
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

    const handleCreateDelivery = async () => {
        if (!selectedDriverForTask || !selectedHarvestForTask) return;
        
        try {
            const response = await fetch(`http://localhost:8080/api/delivery?mandorId=${mandorId}`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ 
                    driverId: Number(selectedDriverForTask), 
                    hasilPanenId: Number(selectedHarvestForTask) 
                }),
            });

            if (response.ok) {
                alert("Penugasan pengiriman berhasil dibuat!");
                setIsCreateModalOpen(false);
                setRefreshTrigger(prev => prev + 1);
            } else {
                const err = await response.json();
                alert(err.error || "Gagal membuat penugasan.");
            }
        } catch (error) {
            console.error("Error create:", error);
        }
    };

    return (
        <div className="p-6 max-w-6xl mx-auto text-ink">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Manajemen Pengiriman - Mandor</h1>
                <button
                    onClick={() => setIsCreateModalOpen(true)}
                    className="px-4 py-2 bg-verdant text-ink font-bold rounded-md shadow hover:opacity-90 transition"
                >
                    + Buat Penugasan Pengiriman
                </button>
            </div>

            <div className="flex gap-4 mb-4">
                <button
                    onClick={() => setViewMode("ongoing")}
                    className={`px-4 py-2 font-semibold rounded-md ${viewMode === "ongoing" ? "bg-verdant text-ink font-bold" : "bg-white text-ink border border-ink/20"}`}
                >
                    Sedang Berlangsung
                </button>
                <button
                    onClick={() => setViewMode("history")}
                    className={`px-4 py-2 font-semibold rounded-md ${viewMode === "history" ? "bg-verdant text-ink font-bold" : "bg-white text-ink border border-ink/20"}`}
                >
                    Riwayat
                </button>
            </div>

            {/* Filter UI */}
            <div className="flex flex-wrap gap-4 mb-6 bg-white p-4 rounded-lg border border-ink/20 shadow-sm">
                <input
                    type="text"
                    placeholder="Search Keyword..."
                    className="border border-ink/30 rounded-md px-3 py-2 w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-verdant bg-bone"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                />

                <input
                    type="number"
                    placeholder="Filter ID Supir..."
                    className="border border-ink/30 rounded-md px-3 py-2 w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-verdant bg-bone"
                    value={driverId}
                    onChange={(e) => setDriverId(e.target.value)}
                />

                <input
                    type="date"
                    className="border border-ink/30 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-verdant bg-bone text-ink"
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                />
            </div>

            {/* Tabel */}
            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-ink/20">
                <table className="min-w-full divide-y divide-ink/10 text-left text-sm">
                    <thead className="bg-ink text-bone">
                    <tr>
                        <th className="px-6 py-3 font-semibold">ID Delivery</th>
                        <th className="px-6 py-3 font-semibold">ID Panen</th>
                        <th className="px-6 py-3 font-semibold">ID Supir</th>
                        <th className="px-6 py-3 font-semibold">Status</th>
                        <th className="px-6 py-3 font-semibold">Mandor Decision</th>
                        <th className="px-6 py-3 font-semibold">Aksi</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-ink/10">
                    {deliveries.length > 0 ? (
                        deliveries.map((delivery) => (
                            <tr key={delivery.id} className="hover:bg-bone/50">
                                <td className="px-6 py-4 font-medium text-ink">{delivery.id}</td>
                                <td className="px-6 py-4 font-medium text-ink">{delivery.hasilPanenId}</td>
                                <td className="px-6 py-4 font-medium text-ink">{delivery.driverId}</td>
                                <td className="px-6 py-4">
                                    <span className="px-2 py-1 text-xs font-bold rounded-md bg-ink text-verdant border border-verdant">
                                        {delivery.status}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`px-2 py-1 inline-flex text-xs font-bold rounded-md ${
                                        delivery.mandorDecision === 'Approved' ? 'bg-verdant text-ink' :
                                            delivery.mandorDecision === 'Rejected' ? 'bg-red-500 text-bone' : 'bg-ink/10 text-ink'
                                    }`}>
                                        {delivery.mandorDecision}
                                    </span>
                                </td>
                                <td className="px-6 py-4 flex gap-2">
                                    {viewMode === "history" && delivery.mandorDecision === 'Pending' && (
                                        <>
                                            <button
                                                onClick={() => handleApprove(delivery.id)}
                                                className="px-3 py-1 bg-verdant text-ink font-bold rounded shadow hover:opacity-90 transition"
                                            >
                                                Approve
                                            </button>
                                            <button
                                                onClick={() => openRejectModal(delivery.id)}
                                                className="px-3 py-1 bg-red-600 text-bone font-bold rounded shadow hover:bg-red-700 transition"
                                            >
                                                Reject
                                            </button>
                                        </>
                                    )}
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={6} className="px-6 py-8 text-center text-ink/70">Tidak ada pengiriman ditemukan.</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* Modal Reject */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/70 p-4">
                    <div className="bg-bone border-2 border-verdant rounded-lg shadow-xl w-full max-w-md p-6">
                        <h3 className="text-lg font-bold text-ink mb-2">Tolak Pengiriman</h3>
                        <p className="text-sm text-ink/70 mb-4">Silakan masukkan alasan penolakan pengiriman ini:</p>

                        <textarea
                            className="w-full border border-ink/30 bg-white text-ink rounded-md px-3 py-2 mb-4 focus:outline-none focus:ring-2 focus:ring-verdant resize-none"
                            placeholder="Contoh: Truck rusak / buah hilang..."
                            value={rejectReason}
                            onChange={(e) => setRejectReason(e.target.value)}
                            rows={3}
                        />

                        <div className="flex justify-end gap-2">
                            <button
                                onClick={() => setIsModalOpen(false)}
                                className="px-4 py-2 border border-ink/30 rounded-md text-ink font-bold hover:bg-white transition"
                            >
                                Batal
                            </button>
                            <button
                                onClick={handleRejectSubmit}
                                disabled={!rejectReason.trim()}
                                className="px-4 py-2 bg-ink text-bone border border-transparent rounded-md font-bold hover:bg-ink/90 disabled:opacity-50 disabled:cursor-not-allowed transition"
                            >
                                Konfirmasi Tolak
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Modal Create Penugasan (Fake Data for Supir and Harvest) */}
            {isCreateModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/70 p-4">
                    <div className="bg-bone border-2 border-verdant rounded-lg shadow-xl w-full max-w-md p-6">
                        <h3 className="text-lg font-bold text-ink mb-4">Tugaskan Supir Truk</h3>

                        <div className="mb-4">
                            <label className="block text-sm font-bold text-ink mb-1">Pilih Supir Truk (Pencarian Nama)</label>
                            <select
                                className="w-full border border-ink/30 rounded-md px-3 py-2 bg-white text-ink focus:outline-none focus:ring-2 focus:ring-verdant"
                                value={selectedDriverForTask}
                                onChange={(e) => setSelectedDriverForTask(e.target.value)}
                            >
                                <option value="" disabled>-- Pilih Supir --</option>
                                <option value="2">Budi (ID: 2)</option>
                                <option value="4">Agus (ID: 4)</option>
                            </select>
                            <p className="text-xs text-ink/70 mt-1">Hanya menampilkan supir di kebun yang sama.</p>
                        </div>

                        <div className="mb-6">
                            <label className="block text-sm font-bold text-ink mb-1">Pilih Hasil Panen (Approved)</label>
                            <select
                                className="w-full border border-ink/30 rounded-md px-3 py-2 bg-white text-ink focus:outline-none focus:ring-2 focus:ring-verdant"
                                value={selectedHarvestForTask}
                                onChange={(e) => setSelectedHarvestForTask(e.target.value)}
                            >
                                <option value="" disabled>-- Pilih Hasil Panen --</option>
                                <option value="1">Panen #1 (250 Kg)</option>
                                <option value="3">Panen #3 (150 Kg)</option>
                            </select>
                            <p className="text-xs text-ink/70 mt-1">Kapasitas maksimal truk: 400 Kg.</p>
                        </div>

                        <div className="flex justify-end gap-2">
                            <button
                                onClick={() => setIsCreateModalOpen(false)}
                                className="px-4 py-2 border border-ink/30 rounded-md text-ink font-bold hover:bg-white transition"
                            >
                                Batal
                            </button>
                            <button
                                onClick={handleCreateDelivery}
                                disabled={!selectedDriverForTask || !selectedHarvestForTask}
                                className="px-4 py-2 bg-verdant text-ink border border-ink rounded-md font-bold hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition"
                            >
                                Simpan Penugasan
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}