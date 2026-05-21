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
    adminRejectionReason: string | null;
    rejectedKg: number;
}

export default function AdminDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    
    const [selectedDeliveryId, setSelectedDeliveryId] = useState<number | null>(null);
    const [rejectReason, setRejectReason] = useState("");
    const [rejectedKg, setRejectedKg] = useState<number | "">("");
    const [decisionMode, setDecisionMode] = useState<"Rejected" | "PartialRejected">("Rejected");
    const [isModalOpen, setIsModalOpen] = useState(false);
    
    // State Filter
    const [mandorName, setMandorName] = useState("");
    const [date, setDate] = useState("");

    const [refreshTrigger, setRefreshTrigger] = useState(0);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const queryParams = new URLSearchParams();
                if (mandorName) queryParams.append("mandorName", mandorName);
                if (date) queryParams.append("date", date);

                const response = await fetch(`http://localhost:8080/api/delivery/admin/ready?${queryParams.toString()}`);

                if (response.ok) {
                    const data = await response.json();
                    setDeliveries(data);
                }
            } catch (error) {
                console.error("Terjadi kesalahan:", error);
            }
        };

        fetchData();
    }, [mandorName, date, refreshTrigger]);

    const handleApprove = async (id: number) => {
        try {
            const response = await fetch(`http://localhost:8080/api/delivery/${id}/admin-decision`, { 
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ decision: "Approved", rejectionReason: null, rejectedKg: 0 })
             });
            if (response.ok) {
                alert("Berhasil disetujui Admin!");
                setRefreshTrigger(prev => prev + 1);
            }
        } catch (error) {
            console.error("Error approve:", error);
        }
    };

    const openRejectModal = (id: number, mode: "Rejected" | "PartialRejected") => {
        setSelectedDeliveryId(id);
        setDecisionMode(mode);
        setRejectReason("");
        setRejectedKg("");
        setIsModalOpen(true);
    };

    const handleRejectSubmit = async () => {
        if (!selectedDeliveryId) return;

        try {
            const response = await fetch(`http://localhost:8080/api/delivery/${selectedDeliveryId}/admin-decision`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ 
                    decision: decisionMode, 
                    rejectionReason: rejectReason,
                    rejectedKg: decisionMode === "PartialRejected" ? Number(rejectedKg) : 0 
                }),
            });

            if (response.ok) {
                alert(`Berhasil diproses sebagai ${decisionMode}!`);
                setIsModalOpen(false);
                setRefreshTrigger(prev => prev + 1);
            }
        } catch (error) {
            console.error("Error reject:", error);
        }
    };

    return (
        <div className="p-6 max-w-6xl mx-auto text-ink">
            <h1 className="text-2xl font-bold mb-6">Peninjauan Pengiriman - Admin</h1>

            {/* Filter UI */}
            <div className="flex flex-wrap gap-4 mb-6 bg-white p-4 rounded-lg border border-ink/20 shadow-sm">
                <input
                    type="text"
                    placeholder="Search Nama Mandor..."
                    className="border border-ink/30 rounded-md px-3 py-2 w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-verdant bg-bone"
                    value={mandorName}
                    onChange={(e) => setMandorName(e.target.value)}
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
                        <th className="px-6 py-3 font-semibold">Mandor</th>
                        <th className="px-6 py-3 font-semibold">Keputusan Admin</th>
                        <th className="px-6 py-3 font-semibold">Aksi</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-ink/10">
                    {deliveries.length > 0 ? (
                        deliveries.map((delivery) => (
                            <tr key={delivery.id} className="hover:bg-bone/50">
                                <td className="px-6 py-4 font-medium text-ink">{delivery.id}</td>
                                <td className="px-6 py-4 font-medium text-ink">{delivery.hasilPanenId}</td>
                                <td className="px-6 py-4 font-medium text-ink">{delivery.mandorId}</td>
                                <td className="px-6 py-4">
                                    <span className={`px-2 py-1 inline-flex text-xs font-bold rounded-md ${
                                        delivery.adminDecision === 'Approved' ? 'bg-verdant text-ink' :
                                            delivery.adminDecision === 'Rejected' ? 'bg-red-500 text-bone' : 
                                            delivery.adminDecision === 'PartialRejected' ? 'bg-orange-400 text-ink' : 'bg-ink/10 text-ink'
                                    }`}>
                                        {delivery.adminDecision}
                                    </span>
                                    {delivery.adminDecision === "PartialRejected" && (
                                        <div className="text-xs text-red-500 font-bold mt-1">Ditolak: {delivery.rejectedKg} kg</div>
                                    )}
                                </td>
                                <td className="px-6 py-4 flex flex-wrap gap-2">
                                    {delivery.adminDecision === 'Pending' && (
                                        <>
                                            <button
                                                onClick={() => handleApprove(delivery.id)}
                                                className="px-3 py-1 bg-verdant text-ink border border-ink font-bold rounded shadow hover:opacity-90 transition"
                                            >
                                                Approve
                                            </button>
                                            <button
                                                onClick={() => openRejectModal(delivery.id, "PartialRejected")}
                                                className="px-3 py-1 bg-orange-400 text-ink font-bold border border-ink rounded shadow hover:opacity-90 transition"
                                            >
                                                Reject Parsial
                                            </button>
                                            <button
                                                onClick={() => openRejectModal(delivery.id, "Rejected")}
                                                className="px-3 py-1 bg-red-600 text-bone font-bold rounded shadow hover:bg-red-700 transition"
                                            >
                                                Reject Total
                                            </button>
                                        </>
                                    )}
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={5} className="px-6 py-8 text-center text-ink/70">Tidak ada pengiriman yang butuh tinjauan.</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* Modal Reject */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/70 p-4">
                    <div className="bg-bone border-2 border-verdant rounded-lg shadow-xl w-full max-w-md p-6">
                        <h3 className="text-lg font-bold text-ink mb-2">
                            {decisionMode === "PartialRejected" ? "Tolak Sebagian Pengiriman" : "Tolak Total Pengiriman"}
                        </h3>
                        
                        {decisionMode === "PartialRejected" && (
                            <div className="mb-4">
                                <label className="block text-sm font-bold text-ink mb-1">Jumlah Kg yang ditolak (tidak diakui)</label>
                                <input
                                    type="number"
                                    className="w-full border border-ink/30 bg-white text-ink rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-verdant"
                                    value={rejectedKg}
                                    onChange={(e) => setRejectedKg(e.target.value ? Number(e.target.value) : "")}
                                    placeholder="Contoh: 50"
                                />
                            </div>
                        )}

                        <div className="mb-4">
                            <label className="block text-sm font-bold text-ink mb-1">Alasan Penolakan</label>
                            <textarea
                                className="w-full border border-ink/30 bg-white text-ink rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-verdant resize-none"
                                placeholder="Contoh: Kualitas sawit kurang baik..."
                                value={rejectReason}
                                onChange={(e) => setRejectReason(e.target.value)}
                                rows={3}
                            />
                        </div>

                        <div className="flex justify-end gap-2">
                            <button
                                onClick={() => setIsModalOpen(false)}
                                className="px-4 py-2 border border-ink/30 rounded-md text-ink font-bold hover:bg-white transition"
                            >
                                Batal
                            </button>
                            <button
                                onClick={handleRejectSubmit}
                                disabled={!rejectReason.trim() || (decisionMode === "PartialRejected" && !rejectedKg)}
                                className={`px-4 py-2 text-bone border border-transparent rounded-md font-bold disabled:opacity-50 disabled:cursor-not-allowed transition ${decisionMode === 'PartialRejected' ? 'bg-orange-500 hover:bg-orange-600' : 'bg-red-600 hover:bg-red-700'}`}
                            >
                                Konfirmasi Rekam
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}