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

    if (loading) return <div className="p-8 text-center text-bone/30 text-sm">Memuat data...</div>;
    if (error || !delivery) return <div className="p-8 text-red-400 bg-red-500/10 border border-red-500/20 max-w-2xl mx-auto rounded-lg mt-8">Error: {error || "Tidak ditemukan"}</div>;

    const statuses: ("Memuat" | "Mengirim" | "Tiba")[] = ["Memuat", "Mengirim", "Tiba"];
    const currentIndex = statuses.indexOf(delivery.status);
    const statusLabels: Record<string, string> = { Memuat: "Memuat Barang", Mengirim: "Dalam Perjalanan", Tiba: "Tiba di Tujuan" };

    return (
        <div className="px-8 py-6 max-w-3xl mx-auto">
            <button onClick={() => router.back()} className="mb-6 text-bone/40 hover:text-bone text-sm flex items-center gap-2 transition">
                <span>&larr;</span> Kembali
            </button>
            <h1 className="text-xl font-semibold text-bone mb-6">Detail Pengiriman</h1>

            <div className="bg-ink-muted p-6 rounded-lg border border-white/10 space-y-6">

                {/* Status progression */}
                <div className="relative flex justify-between items-center px-2 py-4">
                    {/* Progress bar background */}
                    <div className="absolute top-9 left-0 right-0 h-1 bg-white/10 -translate-y-1/2 rounded-full" />
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
                                <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm border-4 transition bg-ink ${
                                    done ? "border-verdant text-verdant" : "border-white/10 text-bone/30"
                                }`}>
                                    {done ? "✓" : i + 1}
                                </div>
                                <span className={`mt-2 text-xs font-semibold text-center ${active ? "text-verdant" : "text-bone/40"}`}>
                                    {statusLabels[status]}
                                </span>
                            </div>
                        );
                    })}
                </div>

                {/* Info */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-6 pt-2">
                    <InfoField label="Kebun Asal" value={delivery.hasilPanen?.kebun?.namaKebun} />
                    <InfoField label="Berat Sawit" value={`${delivery.hasilPanen?.kilogram} Kg`} />
                    <InfoField label="Mandor" value={delivery.mandor?.nama} />
                    <InfoField label="Tanggal Panen" value={delivery.hasilPanen?.tanggalPanen} />
                </div>

                {/* Action buttons */}
                {delivery.status !== "Tiba" && (
                    <div className="border-t border-white/10 pt-6">
                        <h3 className="text-xs font-semibold text-bone/40 uppercase tracking-wider mb-4">Perbarui Status</h3>
                        {delivery.status === "Memuat" && (
                            <button
                                onClick={() => handleUpdateStatus("Mengirim")}
                                disabled={updating}
                                className="px-5 py-2.5 bg-[#2A69F6] text-white font-semibold rounded-md hover:bg-[#1E52C9] disabled:opacity-50 transition"
                            >
                                🚛 Mulai Perjalanan
                            </button>
                        )}
                        {delivery.status === "Mengirim" && (
                            <button
                                onClick={() => handleUpdateStatus("Tiba")}
                                disabled={updating}
                                className="px-5 py-2.5 bg-verdant text-ink font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 transition"
                            >
                                📍 Sudah Sampai Tujuan
                            </button>
                        )}
                    </div>
                )}

                {delivery.status === "Tiba" && (
                    <div className="border-t border-white/10 pt-6 text-center">
                        <p className="text-verdant font-medium text-sm">✓ Pengiriman sudah tiba. Menunggu verifikasi Mandor.</p>
                    </div>
                )}
            </div>
        </div>
    );
}

function InfoField({ label, value }: { label: string; value?: string }) {
    return (
        <div>
            <p className="text-xs text-bone/40 uppercase tracking-wider mb-1">{label}</p>
            <p className="font-medium text-bone">{value || "-"}</p>
        </div>
    );
}
