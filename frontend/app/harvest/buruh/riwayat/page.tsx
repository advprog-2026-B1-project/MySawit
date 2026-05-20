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
}

export default function RiwayatPanenBuruh() {
    const [harvests, setHarvests] = useState<Harvest[]>([]);

    // State untuk Filter (Memenuhi Checklist 1)
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [statusFilter, setStatusFilter] = useState("");

    // Integrasi API (Memenuhi Checklist 2)
    useEffect(() => {
        const fetchData = async () => {
            try {
                const queryParams = new URLSearchParams();
                if (startDate) queryParams.append("startDate", startDate);
                if (endDate) queryParams.append("endDate", endDate);
                if (statusFilter) queryParams.append("status", statusFilter);

                // Memanggil API /me (Hanya data milik buruh yang sedang login/dummy auth)
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
        <div className="p-6 max-w-5xl mx-auto text-gray-800">
            <h1 className="text-2xl font-bold mb-6 text-gray-900">Riwayat Panen Saya</h1>

            {/* Filter UI Standar Tailwind */}
            <div className="flex flex-wrap gap-4 mb-6 bg-gray-100 p-4 rounded-lg border border-gray-200">

                {/* Datepicker Rentang Tanggal */}
                <div className="flex items-center gap-2">
                    <input
                        type="date"
                        className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                        value={startDate}
                        onChange={(e) => setStartDate(e.target.value)}
                    />
                    <span className="font-bold text-gray-500">-</span>
                    <input
                        type="date"
                        className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                        value={endDate}
                        onChange={(e) => setEndDate(e.target.value)}
                    />
                </div>

                {/* Dropdown Status */}
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
                        <th className="px-6 py-3 font-semibold text-gray-600">Tanggal</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Kilogram (Kg)</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Berita</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Status</th>
                        <th className="px-6 py-3 font-semibold text-gray-600">Catatan Mandor</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                    {harvests.length > 0 ? (
                        harvests.map((panen) => (
                            <tr key={panen.id} className="hover:bg-gray-50">
                                <td className="px-6 py-4 whitespace-nowrap">{panen.tanggalPanen}</td>
                                <td className="px-6 py-4 whitespace-nowrap">{panen.kilogram}</td>
                                {/* Potong teks jika berita terlalu panjang */}
                                <td className="px-6 py-4 max-w-xs truncate" title={panen.berita}>
                                    {panen.berita || "-"}
                                </td>

                                {/* Validasi Badge Status (Memenuhi Checklist 3) */}
                                <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        panen.status === 'Approved' ? 'bg-green-100 text-green-800' :
                            panen.status === 'Rejected' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {panen.status}
                    </span>
                                </td>

                                {/* Menampilkan Alasan Penolakan dari Mandor */}
                                <td className="px-6 py-4 text-red-600 text-sm max-w-xs truncate" title={panen.rejectionReason}>
                                    {panen.rejectionReason || "-"}
                                </td>
                            </tr>
                        ))
                    ) : (
                        <tr>
                            <td colSpan={5} className="px-6 py-8 text-center text-gray-500">
                                Belum ada riwayat panen sesuai filter.
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}