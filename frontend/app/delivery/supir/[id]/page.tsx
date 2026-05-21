"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { deliveryApi, Delivery } from "../../../../services/deliveryApi";

export default function SupirDeliveryDetail() {
    const params = useParams();
    const router = useRouter();
    const deliveryId = Number(params.id);
    const [delivery, setDelivery] = useState<Delivery | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [updating, setUpdating] = useState(false);

    const driverId = 2;

    useEffect(() => {
        deliveryApi.getDetail(deliveryId)
            .then(data => setDelivery(data))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [deliveryId]);

    const handleUpdateStatus = async (nextStatus: "Memuat" | "Mengirim" | "Tiba") => {
        setUpdating(true);
        await deliveryApi.updateStatus(deliveryId, driverId, { status: nextStatus });
        alert(`Status berhasil diperbarui ke "${nextStatus}"`);
        router.push("/delivery/supir");
    };

    if (loading) return <div className="p-6 text-center text-ink/50">Memuat data...</div>;
    if (error || !delivery) return <div className="p-6 text-red-500">Error: {error || "Tidak ditemukan"}</div>;

    const statuses: ("Memuat" | "Mengirim" | "Tiba")[] = ["Memuat", "Mengirim", "Tiba"];
    const currentIndex = statuses.indexOf(delivery.status);
    const statusLabels: Record<string, string> = { Memuat: "Memuat Barang", Mengirim: "Dalam Perjalanan", Tiba: "Tiba di Tujuan" };

    return (
        <div className="p-6 max-w-3xl mx-auto text-ink">
            <button onClick={() => router.back()} className="mb-4 text-blue-600 hover:underline text-sm">
                &larr; Kembali
            </button>
            <h1 className="text-2xl font-bold mb-6">Detail Pengiriman</h1>

            <div className="bg-white p-6 rounded-lg shadow border border-ink/10 space-y-6">

                {/* Status progression */}
                <div className="relative flex justify-between items-center px-2 py-4">
                    {/* Progress bar background */}
                    <div className="absolute top-9 left-0 right-0 h-1 bg-gray-200 -translate-y-1/2 rounded-full" />
                    {/* Progress bar fill */}
                    <div
                        className="absolute top-9 left-0 h-1 bg-verdant -translate-y-1/2 rounded-full transition-all duration-500"
                        style={{ width: `${(currentIndex / (statuses.length - 1)) * 100}%` }}
                    />
                    {statuses.map((status, i) => {
                        const done = i <= currentIndex;
                        const active = i === currentIndex;
                        return (
                            <div key={status} className="relative flex flex-col items-center z-10">
                                <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm border-4 transition ${
                                    done ? "bg-verdant border-verdant text-ink" : "bg-white border-gray-300 text-gray-400"
                                }`}>
                                    {done ? "✓" : i + 1}
                                </div>
                                <span className={`mt-2 text-xs font-semibold text-center ${active ? "text-ink" : "text-gray-400"}`}>
                                    {statusLabels[status]}
                                </span>
                            </div>
                        );
                    })}
                </div>

                {/* Info */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4 pt-2">
                    <InfoField label="Kebun Asal" value={delivery.hasilPanen?.kebun?.namaKebun} />
                    <InfoField label="Berat Sawit" value={`${delivery.hasilPanen?.kilogram} Kg`} />
                    <InfoField label="Mandor" value={delivery.mandor?.nama} />
                    <InfoField label="Tanggal Panen" value={delivery.hasilPanen?.tanggalPanen} />
                </div>

                {/* Action buttons */}
                {delivery.status !== "Tiba" && (
                    <div className="border-t pt-6">
                        <h3 className="text-sm font-bold mb-3 text-gray-500 uppercase tracking-wider">Perbarui Status</h3>
                        {delivery.status === "Memuat" && (
                            <button
                                onClick={() => handleUpdateStatus("Mengirim")}
                                disabled={updating}
                                className="px-5 py-2.5 bg-blue-600 text-white font-bold rounded-md hover:bg-blue-700 disabled:opacity-50 transition"
                            >
                                🚛 Mulai Perjalanan
                            </button>
                        )}
                        {delivery.status === "Mengirim" && (
                            <button
                                onClick={() => handleUpdateStatus("Tiba")}
                                disabled={updating}
                                className="px-5 py-2.5 bg-verdant text-ink font-bold rounded-md hover:opacity-90 disabled:opacity-50 transition"
                            >
                                📍 Sudah Sampai Tujuan
                            </button>
                        )}
                    </div>
                )}

                {delivery.status === "Tiba" && (
                    <div className="border-t pt-6 text-center">
                        <p className="text-green-700 font-semibold">✅ Pengiriman sudah tiba. Menunggu verifikasi Mandor.</p>
                    </div>
                )}
            </div>
        </div>
    );
}

function InfoField({ label, value }: { label: string; value?: string }) {
    return (
        <div>
            <p className="text-xs text-gray-400 mb-0.5">{label}</p>
            <p className="font-semibold text-ink">{value || "-"}</p>
        </div>
    );
}
