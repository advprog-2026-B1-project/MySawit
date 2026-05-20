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

export default function MandorDashboard() {
    const [harvests, setHarvests] = useState<Harvest[]>([]);
    const [selectedHarvestId, setSelectedHarvestId] = useState<number | null>(null);
    const [rejectReason, setRejectReason] = useState("");
    const [isModalOpen, setIsModalOpen] = useState(false);

    // State Filter
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

                const response = await fetch(`http://localhost:8080/api/harvest/mandor?${queryParams.toString()}`);

                if (response.ok) {
                    const data = await response.json();
                    setHarvests(data);
                }
            } catch (error) {
                console.error("Terjadi kesalahan:", error);
            }
        };

        fetchData();
        // eslint-disable-next-line react-hooks/set-state-in-effect
    }, [startDate, endDate, statusFilter, searchNama, refreshTrigger]);

    const handleApprove = async (id: number) => {
        try {
            const response = await fetch(`http://localhost:8080/api/harvest/${id}/approve`, { method: "PUT" });
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

    return (
        <div className="p-6 max-w-6xl mx-auto text-gray-800">
            <h1 className="text-2xl font-bold mb-6">Daftar Panen Buruh</h1>

            {/* Filter UI Standar Tailwind */}
            <div className="flex flex-wrap gap-4 mb-6 bg-gray-100 p-4 rounded-lg border border-gray-200">
                <input
                    type="text"
                    placeholder="Cari Nama Buruh..."
                    className="border border-gray-300 rounded-md px-3 py-2 w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                    value={searchNama}
                    onChange={(e) => setSearchNama(e.target.value)}
                />

                <input
                    type="date"
                    className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                />
                <span className="self-center font-bold text-gray-500">-</span>
                <input
                    type="date"
                    className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                />

                <select
                    className="border border-gray-300 rounded-md px-3 py-2 w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                >
                    <option value="">Semua Status</option>
                    <option value="Pending">Pending</option>
                    <option value="Approved">Approved</option>
                    <option value="Rejected">Rejected</option>
                </select>
            </div>

            {/* Tabel Standar Tailwind */}
            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-gray-200">
                <table className="min-w-full divide-y divide-gray-200 text-left text-sm">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-6 py-3 font-semibold text-gray-600">ID</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Tanggal</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Nama Buruh</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Kilogram (Kg)</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Status</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Aksi</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                    {harvests.length > 0 ? (
                        harvests.map((panen) => (
                            <tr key={panen.id} className="hover:bg-gray-50">
                                <td className="px-6 py-4">{panen.id}</td>
                                <td className="px-6 py-4">{panen.tanggalPanen}</td>
                                <td className="px-6 py-4">{panen.namaBuruh || "-"}</td>
                                <td className="px-6 py-4">{panen.kilogram}</td>
                                <td className="px-6 py-4">
                                    {/* Badge Standar */}
                                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                                        panen.status === 'Approved' ? 'bg-green-100 text-green-800' :
                                            panen.status === 'Rejected' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'
                                    }`}>
                      {panen.status}
                    </span>
                                </td>
                                <td className="px-6 py-4 flex gap-2">
                                    <button
                                        onClick={() => handleApprove(panen.id)}
                                        className="px-3 py-1 bg-green-600 text-white text-xs font-medium rounded shadow hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                                        disabled={panen.status !== 'Pending'}
                                    >
                                        Approve
                                    </button>
                                    <button
                                        onClick={() => openRejectModal(panen.id)}
                                        className="px-3 py-1 bg-red-600 text-white text-xs font-medium rounded shadow hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                                        disabled={panen.status !== 'Pending'}
                                    >
                                        Reject
                                    </button>
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={6} className="px-6 py-8 text-center text-gray-500">Tidak ada data panen ditemukan.</td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>

            {/* Modal Reject Standar Tailwind (Render Conditional) */}
            {isModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
                    <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
                        <h3 className="text-lg font-bold text-gray-900 mb-2">Tolak Hasil Panen</h3>
                        <p className="text-sm text-gray-600 mb-4">Silakan masukkan alasan penolakan hasil panen ini:</p>

                        <textarea
                            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-red-500 resize-none"
                            placeholder="Contoh: Buah masih mentah..."
                            value={rejectReason}
                            onChange={(e) => setRejectReason(e.target.value)}
                            rows={3}
                        ></textarea>

                        <div className="flex justify-end gap-3 mt-6">
                            <button
                                className="px-4 py-2 bg-gray-200 text-gray-800 font-medium rounded hover:bg-gray-300 transition"
                                onClick={() => setIsModalOpen(false)}
                            >
                                Batal
                            </button>
                            <button
                                className="px-4 py-2 bg-red-600 text-white font-medium rounded hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                                onClick={handleRejectSubmit}
                                disabled={!rejectReason.trim()}
                            >
                                Submit Tolak
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}