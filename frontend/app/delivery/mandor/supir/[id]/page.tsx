"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { deliveryApi, Delivery, DUMMY_DRIVERS } from "../../../../../services/deliveryApi";

export default function ProfilSupirPage() {
    const params = useParams();
    const router = useRouter();
    const driverId = Number(params.id);
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    const [loading, setLoading] = useState(true);

    const mandorId = 1;
    const driver = DUMMY_DRIVERS.find(d => d.id === driverId);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            const data = await deliveryApi.getByDriver(driverId);
            // Filter only deliveries relevant to this mandor
            setDeliveries(data.filter(d => d.mandor.id === mandorId));
            setLoading(false);
        };
        if (driver) fetch();
        else setLoading(false);
    }, [driverId, driver]);

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" });
    };

    if (!driver) return <div className="p-6 text-center text-red-500">Supir tidak ditemukan.</div>;

    return (
        <div className="p-6 max-w-5xl mx-auto text-ink">
            <button onClick={() => router.back()} className="mb-4 text-blue-600 hover:underline text-sm">
                &larr; Kembali
            </button>
            
            <div className="bg-white p-6 rounded-lg shadow-sm border border-ink/10 mb-6 flex items-center gap-4">
                <div className="w-16 h-16 bg-verdant/20 rounded-full flex items-center justify-center text-2xl font-bold text-verdant">
                    {driver.nama.charAt(0)}
                </div>
                <div>
                    <h1 className="text-2xl font-bold">{driver.nama}</h1>
                    <p className="text-gray-500 text-sm">Profil Supir Truk</p>
                </div>
            </div>

            <h2 className="text-lg font-bold mb-4">Daftar Pengiriman oleh {driver.nama}</h2>

            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-ink/10">
                <table className="min-w-full divide-y divide-ink/10 text-left text-sm">
                    <thead className="bg-ink text-bone">
                        <tr>
                            <th className="px-5 py-3 font-semibold">Kebun Asal</th>
                            <th className="px-5 py-3 font-semibold">Berat</th>
                            <th className="px-5 py-3 font-semibold">Tanggal</th>
                            <th className="px-5 py-3 font-semibold">Status Truk</th>
                            <th className="px-5 py-3 font-semibold">Keputusan</th>
                            <th className="px-5 py-3 font-semibold text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-ink/5">
                        {loading ? (
                            <tr><td colSpan={6} className="px-5 py-8 text-center text-ink/50">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d) => (
                                <tr key={d.id} className="hover:bg-bone/60 transition">
                                    <td className="px-5 py-4">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-4">{d.hasilPanen?.kilogram} Kg</td>
                                    <td className="px-5 py-4 text-ink/70">{formatDate(d.createdAt)}</td>
                                    <td className="px-5 py-4">
                                        <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                            d.status === "Memuat" ? "bg-yellow-100 text-yellow-800" :
                                            d.status === "Mengirim" ? "bg-blue-100 text-blue-800" :
                                            "bg-green-100 text-green-800"
                                        }`}>
                                            {d.status}
                                        </span>
                                    </td>
                                    <td className="px-5 py-4">
                                        <span className={`px-2 py-1 text-xs font-bold rounded-full ${
                                            d.mandorDecision === "Approved" ? "bg-green-100 text-green-800" :
                                            d.mandorDecision === "Rejected" ? "bg-red-100 text-red-800" :
                                            "bg-gray-100 text-gray-600"
                                        }`}>
                                            {d.mandorDecision === "Pending" ? "Menunggu" : d.mandorDecision === "Approved" ? "Disetujui" : "Ditolak"}
                                        </span>
                                    </td>
                                    <td className="px-5 py-4 text-center">
                                        <Link
                                            href={`/delivery/mandor/${d.id}`}
                                            className="px-3 py-1.5 bg-blue-600 text-white rounded-md text-xs font-medium hover:bg-blue-700 transition"
                                        >
                                            Detail
                                        </Link>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={6} className="px-5 py-8 text-center text-ink/50">Tidak ada riwayat pengiriman untuk supir ini.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
