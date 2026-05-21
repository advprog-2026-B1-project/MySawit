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
}

export default function SupirDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    
    // State Filter
    const [viewMode, setViewMode] = useState<"assigned" | "history">("assigned");

    const [refreshTrigger, setRefreshTrigger] = useState(0);

    // Hardcode Driver ID for now
    const driverId = 2; 

    useEffect(() => {
        const fetchData = async () => {
            try {
                // To fetch all deliveries by driver. (We will just filter ongoing vs history in the frontend)
                const response = await fetch(`http://localhost:8080/api/delivery/driver/${driverId}`);

                if (response.ok) {
                    const data: Delivery[] = await response.json();
                    if (viewMode === "assigned") {
                        setDeliveries(data.filter(d => d.status !== "Tiba"));
                    } else {
                        setDeliveries(data.filter(d => d.status === "Tiba"));
                    }
                }
            } catch (error) {
                console.error("Terjadi kesalahan:", error);
            }
        };

        fetchData();
    }, [viewMode, refreshTrigger]);

    const handleUpdateStatus = async (id: number, currentStatus: string) => {
        let nextStatus = currentStatus === "Memuat" ? "Mengirim" : "Tiba";
        
        try {
            const response = await fetch(`http://localhost:8080/api/delivery/${id}/status?driverId=${driverId}`, { 
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ status: nextStatus })
             });
            if (response.ok) {
                alert(`Status diperbarui ke ${nextStatus}!`);
                setRefreshTrigger(prev => prev + 1);
            } else {
                alert("Gagal memperbarui status");
            }
        } catch (error) {
            console.error("Error update status:", error);
        }
    };

    return (
        <div className="p-6 max-w-6xl mx-auto text-ink">
            <h1 className="text-2xl font-bold mb-6">Tugas Pengiriman - Supir</h1>

            <div className="flex gap-4 mb-6">
                <button
                    onClick={() => setViewMode("assigned")}
                    className={`px-4 py-2 font-semibold rounded-md ${viewMode === "assigned" ? "bg-verdant text-ink border border-ink font-bold" : "bg-white text-ink border border-ink/20"}`}
                >
                    Dalam Perjalanan
                </button>
                <button
                    onClick={() => setViewMode("history")}
                    className={`px-4 py-2 font-semibold rounded-md ${viewMode === "history" ? "bg-verdant text-ink border border-ink font-bold" : "bg-white text-ink border border-ink/20"}`}
                >
                    Riwayat Selesai
                </button>
            </div>

            {/* Tabel */}
            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-ink/20">
                <table className="min-w-full divide-y divide-ink/10 text-left text-sm">
                    <thead className="bg-ink text-bone">
                    <tr>
                        <th className="px-6 py-3 font-semibold">ID Delivery</th>
                        <th className="px-6 py-3 font-semibold">ID Panen</th>
                        <th className="px-6 py-3 font-semibold">Status</th>
                        {viewMode === "history" && (
                            <>
                                <th className="px-6 py-3 font-semibold">Keputusan Mandor</th>
                                <th className="px-6 py-3 font-semibold">Keterangan Ditolak</th>
                            </>
                        )}
                        {viewMode === "assigned" && (
                            <th className="px-6 py-3 font-semibold">Aksi</th>
                        )}
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-ink/10">
                    {deliveries.length > 0 ? (
                        deliveries.map((delivery) => (
                            <tr key={delivery.id} className="hover:bg-bone/50">
                                <td className="px-6 py-4 font-medium text-ink">{delivery.id}</td>
                                <td className="px-6 py-4 font-medium text-ink">{delivery.hasilPanenId}</td>
                                <td className="px-6 py-4">
                                    <span className="px-2 py-1 inline-flex text-xs font-bold rounded-md bg-ink text-verdant border border-verdant">
                                        {delivery.status}
                                    </span>
                                </td>
                                {viewMode === "history" && (
                                    <>
                                        <td className="px-6 py-4">
                                            <span className={`px-2 py-1 inline-flex text-xs font-bold rounded-md ${
                                                delivery.mandorDecision === 'Approved' ? 'bg-verdant text-ink' :
                                                    delivery.mandorDecision === 'Rejected' ? 'bg-red-500 text-bone' : 'bg-ink/10 text-ink'
                                            }`}>
                                                {delivery.mandorDecision}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 font-medium">{delivery.mandorRejectionReason || "-"}</td>
                                    </>
                                )}
                                {viewMode === "assigned" && (
                                    <td className="px-6 py-4">
                                        <button
                                            onClick={() => handleUpdateStatus(delivery.id, delivery.status)}
                                            className="px-3 py-1 bg-verdant text-ink border border-ink font-bold rounded shadow hover:opacity-90 transition"
                                        >
                                            {delivery.status === "Memuat" ? "Selesai Memuat (Berangkat)" : "Tiba di Tujuan"}
                                        </button>
                                    </td>
                                )}
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={viewMode === "history" ? 5 : 4} className="px-6 py-8 text-center text-ink/70">Tidak ada pengiriman.</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}