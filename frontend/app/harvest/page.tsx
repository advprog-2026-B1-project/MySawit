"use client";
import { useEffect, useState } from 'react';

interface HarvestResponse {
    id: number;
    tanggalPanen: string;
    kilogram: number;
    berita: string;
    status: string;
}

export default function HarvestHistory() {
    const [history, setHistory] = useState<HarvestResponse[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/harvest/me')
            .then(res => res.json())
            .then(data => {
                setHistory(data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch history:", err);
                setLoading(false);
            });
    }, []);

    if (loading) return <div className="p-4">Memuat data...</div>;

    return (
        <div className="max-w-4xl mx-auto mt-10 p-4">
            <h2 className="text-2xl font-bold mb-6">Riwayat Hasil Panen</h2>
            <div className="overflow-x-auto">
                <table className="min-w-full bg-white border border-gray-200 shadow-sm rounded">
                    <thead className="bg-gray-50">
                    <tr>
                        <th className="px-4 py-3 border-b text-left text-sm font-semibold text-gray-700">Tanggal</th>
                        <th className="px-4 py-3 border-b text-left text-sm font-semibold text-gray-700">Kilogram</th>
                        <th className="px-4 py-3 border-b text-left text-sm font-semibold text-gray-700">Berita</th>
                        <th className="px-4 py-3 border-b text-left text-sm font-semibold text-gray-700">Status</th>
                    </tr>
                    </thead>
                    <tbody>
                    {history.length === 0 ? (
                        <tr>
                            <td colSpan={4} className="px-4 py-3 border-b text-center text-gray-500">Belum ada riwayat panen.</td>
                        </tr>
                    ) : (
                        history.map(item => (
                            <tr key={item.id} className="hover:bg-gray-50">
                                <td className="px-4 py-3 border-b text-black">{item.tanggalPanen}</td>
                                <td className="px-4 py-3 border-b text-black">{item.kilogram} kg</td>
                                <td className="px-4 py-3 border-b text-black">{item.berita}</td>
                                <td className="px-4 py-3 border-b">
                                        <span className={`px-2 py-1 text-xs rounded-full font-medium
                                            ${item.status === 'Approved' ? 'bg-green-100 text-green-800' :
                                            item.status === 'Rejected' ? 'bg-red-100 text-red-800' :
                                                'bg-yellow-100 text-yellow-800'}`}>
                                            {item.status}
                                        </span>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}