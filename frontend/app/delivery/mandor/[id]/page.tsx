"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { deliveryApi, Delivery } from "../../../../services/deliveryApi";

export default function MandorDeliveryDetail() {
    const params = useParams();
    const router = useRouter();
    const deliveryId = Number(params.id);
    const [delivery, setDelivery] = useState<Delivery | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [rejectReason, setRejectReason] = useState("");
    const [isRejecting, setIsRejecting] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const mandorId = 1;

    useEffect(() => {
        deliveryApi.getDetail(deliveryId)
            .then(data => setDelivery(data))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [deliveryId]);

    const handleApprove = async () => {
        setSubmitting(true);
        await deliveryApi.decideByMandor(deliveryId, mandorId, { decision: "Approved" });
        alert("Pengiriman berhasil disetujui!");
        router.push("/delivery/mandor");
    };

    const handleReject = async () => {
        if (!rejectReason.trim()) {
            alert("Alasan penolakan harus diisi!");
            return;
        }
        setSubmitting(true);
        await deliveryApi.decideByMandor(deliveryId, mandorId, { decision: "Rejected", rejectionReason: rejectReason });
        alert("Pengiriman ditolak.");
        router.push("/delivery/mandor");
    };

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "long", year: "numeric", hour: "2-digit", minute: "2-digit" });
    };

    if (loading) return <div className="p-6 text-center text-ink/50">Memuat data...</div>;
    if (error || !delivery) return <div className="p-6 text-red-500">Error: {error || "Tidak ditemukan"}</div>;

    const canDecide = delivery.status === "Tiba" && delivery.mandorDecision === "Pending";

    return (
        <div className="p-6 max-w-3xl mx-auto text-ink">
            <button onClick={() => router.back()} className="mb-4 text-blue-600 hover:underline text-sm">
                &larr; Kembali ke daftar
            </button>
            <h1 className="text-2xl font-bold mb-6">Detail Pengiriman</h1>

            <div className="bg-white p-6 rounded-lg shadow border border-ink/10 space-y-6">

                {/* Info grid */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4">
                    <InfoField label="Supir" value={delivery.driver?.nama} />
                    <InfoField label="Kebun Asal" value={delivery.hasilPanen?.kebun?.namaKebun} />
                    <InfoField label="Berat Sawit" value={`${delivery.hasilPanen?.kilogram} Kg`} />
                    <InfoField label="Tanggal Panen" value={delivery.hasilPanen?.tanggalPanen} />
                    <InfoField label="Status Transportasi">
                        <StatusBadge status={delivery.status} />
                    </InfoField>
                    <InfoField label="Keputusan Mandor">
                        <DecisionBadge decision={delivery.mandorDecision} />
                    </InfoField>
                    {delivery.arrivedAt && <InfoField label="Waktu Tiba" value={formatDate(delivery.arrivedAt)} />}
                    <InfoField label="Dibuat" value={formatDate(delivery.createdAt)} />
                </div>

                {/* Rejection reason */}
                {delivery.mandorDecision === "Rejected" && delivery.mandorRejectionReason && (
                    <div className="p-4 bg-red-50 border border-red-200 rounded-md text-sm">
                        <p className="font-bold text-red-800 mb-1">Alasan Penolakan:</p>
                        <p className="text-red-700">{delivery.mandorRejectionReason}</p>
                    </div>
                )}

                {/* Actions */}
                {canDecide && (
                    <div className="border-t pt-6">
                        <h3 className="text-lg font-bold mb-4">Verifikasi Pengiriman</h3>

                        {!isRejecting ? (
                            <div className="flex gap-3">
                                <button
                                    onClick={handleApprove}
                                    disabled={submitting}
                                    className="px-5 py-2 bg-verdant text-ink font-bold rounded-md hover:opacity-90 disabled:opacity-50 transition"
                                >
                                    Setujui Pengiriman
                                </button>
                                <button
                                    onClick={() => setIsRejecting(true)}
                                    className="px-5 py-2 bg-red-600 text-white font-bold rounded-md hover:bg-red-700 transition"
                                >
                                    Tolak Pengiriman
                                </button>
                            </div>
                        ) : (
                            <div className="bg-red-50 border border-red-200 p-4 rounded-md space-y-3">
                                <label className="block text-sm font-bold text-red-800">Alasan Penolakan:</label>
                                <textarea
                                    className="w-full border border-red-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-400"
                                    rows={3}
                                    value={rejectReason}
                                    onChange={e => setRejectReason(e.target.value)}
                                    placeholder="Masukkan alasan penolakan..."
                                />
                                <div className="flex gap-2">
                                    <button onClick={() => setIsRejecting(false)} className="px-4 py-2 border rounded-md text-sm hover:bg-gray-50 transition">
                                        Batal
                                    </button>
                                    <button
                                        onClick={handleReject}
                                        disabled={submitting || !rejectReason.trim()}
                                        className="px-4 py-2 bg-red-600 text-white rounded-md font-bold text-sm disabled:opacity-50 transition"
                                    >
                                        Konfirmasi Tolak
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

function InfoField({ label, value, children }: { label: string; value?: string; children?: React.ReactNode }) {
    return (
        <div>
            <p className="text-xs text-gray-400 mb-0.5">{label}</p>
            {children || <p className="font-semibold text-ink">{value || "-"}</p>}
        </div>
    );
}

function StatusBadge({ status }: { status: string }) {
    const cls = status === "Memuat" ? "bg-yellow-100 text-yellow-800"
        : status === "Mengirim" ? "bg-blue-100 text-blue-800"
        : "bg-green-100 text-green-800";
    return <span className={`px-2 py-0.5 text-xs font-bold rounded-full ${cls}`}>{status}</span>;
}

function DecisionBadge({ decision }: { decision: string }) {
    const map: Record<string, { cls: string; label: string }> = {
        Pending: { cls: "bg-gray-100 text-gray-600", label: "Menunggu" },
        Approved: { cls: "bg-green-100 text-green-800", label: "Disetujui" },
        Rejected: { cls: "bg-red-100 text-red-800", label: "Ditolak" },
    };
    const { cls, label } = map[decision] ?? map.Pending;
    return <span className={`px-2 py-0.5 text-xs font-bold rounded-full ${cls}`}>{label}</span>;
}
