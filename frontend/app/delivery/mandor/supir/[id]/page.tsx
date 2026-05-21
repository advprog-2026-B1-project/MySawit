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

    if (!driver) return <div className="p-8 text-center text-red-400 bg-red-500/10 border border-red-500/20 max-w-2xl mx-auto rounded-lg mt-8">Supir tidak ditemukan.</div>;

    return (
        <div className="px-8 py-6 max-w-5xl mx-auto text-bone">
            <button onClick={() => router.back()} className="mb-6 text-bone/40 hover:text-bone text-sm flex items-center gap-2 transition">
                <span>&larr;</span> Kembali
            </button>
            
            <div className="bg-ink-muted p-6 rounded-lg border border-white/10 mb-6 flex items-center gap-5">
                <div className="w-16 h-16 bg-verdant/10 border border-verdant/30 rounded-full flex items-center justify-center text-2xl font-bold text-verdant">
                    {driver.nama.charAt(0)}
                </div>
                <div>
                    <h1 className="text-2xl font-bold text-bone mb-1">{driver.nama}</h1>
                    <p className="text-sm text-bone/40 uppercase tracking-wider">Profil Supir Truk</p>
                </div>
            </div>

            <h2 className="text-lg font-semibold text-bone mb-4">Daftar Pengiriman oleh {driver.nama}</h2>

            <div className="rounded-lg border border-white/10 overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead>
                        <tr className="bg-ink-soft border-b border-white/10">
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kebun Asal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Berat</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Tanggal</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Status Truk</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Keputusan</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="bg-ink-muted divide-y divide-white/5">
                        {loading ? (
                            <tr><td colSpan={6} className="px-5 py-12 text-center text-bone/30">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d, i) => (
                                <tr key={d.id} className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}>
                                    <td className="px-5 py-3.5 text-bone font-medium">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-3.5 text-bone">{d.hasilPanen?.kilogram} <span className="text-bone/50 text-xs">Kg</span></td>
                                    <td className="px-5 py-3.5 text-bone/50 text-xs">{formatDate(d.createdAt)}</td>
                                    <td className="px-5 py-3.5">
                                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                                            d.status === "Memuat" ? "bg-yellow-500/10 text-yellow-400" :
                                            d.status === "Mengirim" ? "bg-blue-500/10 text-blue-400" :
                                            "bg-verdant-soft text-verdant"
                                        }`}>
                                            {d.status}
                                        </span>
                                    </td>
                                    <td className="px-5 py-3.5">
                                        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                                            d.mandorDecision === "Approved" ? "bg-verdant-soft text-verdant" :
                                            d.mandorDecision === "Rejected" ? "bg-red-500/10 text-red-400" :
                                            "bg-white/5 text-bone/50"
                                        }`}>
                                            {d.mandorDecision === "Pending" ? "Menunggu" : d.mandorDecision === "Approved" ? "Disetujui" : "Ditolak"}
                                        </span>
                                    </td>
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
                                <td colSpan={6} className="px-5 py-12 text-center text-bone/30">Tidak ada riwayat pengiriman untuk supir ini.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
