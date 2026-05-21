"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { deliveryApi, Delivery } from "../../../services/deliveryApi";

export default function AdminDeliveryDashboard() {
    const [deliveries, setDeliveries] = useState<Delivery[]>([]);
    const [mandorFilter, setMandorFilter] = useState("");
    const [dateFilter, setDateFilter] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetch = async () => {
            setLoading(true);
            let data = await deliveryApi.getAdminReady();

            if (mandorFilter) {
                const kw = mandorFilter.toLowerCase();
                data = data.filter(d => d.mandor?.nama?.toLowerCase().includes(kw));
            }
            if (dateFilter) {
                data = data.filter(d => d.mandorDecidedAt?.startsWith(dateFilter));
            }

            setDeliveries(data);
            setLoading(false);
        };
        fetch();
    }, [mandorFilter, dateFilter]);

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "short", year: "numeric" });
    };

    return (
        <div className="p-6 max-w-6xl mx-auto text-ink">
            <h1 className="text-2xl font-bold mb-6">Review Pengiriman — Admin</h1>

            {/* Filters */}
            <div className="flex flex-wrap gap-3 mb-6 bg-white p-4 rounded-lg border border-ink/10 shadow-sm">
                <input
                    type="text"
                    placeholder="Cari nama mandor..."
                    className="border border-ink/20 rounded-md px-3 py-2 w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-verdant bg-bone text-sm"
                    value={mandorFilter}
                    onChange={(e) => setMandorFilter(e.target.value)}
                />
                <input
                    type="date"
                    className="border border-ink/20 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-verdant bg-bone text-sm text-ink"
                    value={dateFilter}
                    onChange={(e) => setDateFilter(e.target.value)}
                />
            </div>

            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-ink/10">
                <table className="min-w-full divide-y divide-ink/10 text-left text-sm">
                    <thead className="bg-ink text-bone">
                        <tr>
                            <th className="px-5 py-3 font-semibold">Mandor</th>
                            <th className="px-5 py-3 font-semibold">Supir</th>
                            <th className="px-5 py-3 font-semibold">Kebun Asal</th>
                            <th className="px-5 py-3 font-semibold">Berat</th>
                            <th className="px-5 py-3 font-semibold">Tgl Verifikasi</th>
                            <th className="px-5 py-3 font-semibold">Status Admin</th>
                            <th className="px-5 py-3 font-semibold text-center">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-ink/5">
                        {loading ? (
                            <tr><td colSpan={7} className="px-5 py-8 text-center text-ink/50">Memuat data...</td></tr>
                        ) : deliveries.length > 0 ? (
                            deliveries.map((d) => (
                                <tr key={d.id} className="hover:bg-bone/60 transition">
                                    <td className="px-5 py-4 font-medium">{d.mandor?.nama}</td>
                                    <td className="px-5 py-4">{d.driver?.nama}</td>
                                    <td className="px-5 py-4">{d.hasilPanen?.kebun?.namaKebun}</td>
                                    <td className="px-5 py-4">{d.hasilPanen?.kilogram} Kg</td>
                                    <td className="px-5 py-4 text-ink/70">{formatDate(d.mandorDecidedAt)}</td>
                                    <td className="px-5 py-4">
                                        <span className="px-2 py-1 text-xs font-bold rounded-full bg-amber-100 text-amber-800">
                                            Menunggu Review
                                        </span>
                                    </td>
                                    <td className="px-5 py-4 text-center">
                                        <Link
                                            href={`/delivery/admin/${d.id}`}
                                            className="px-3 py-1.5 bg-blue-600 text-white rounded-md text-xs font-medium hover:bg-blue-700 transition"
                                        >
                                            Review
                                        </Link>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={7} className="px-5 py-8 text-center text-ink/50">Tidak ada pengiriman yang menunggu review.</td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}