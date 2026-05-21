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

    if (loading) return <div className="p-8 text-center text-bone/30 text-sm">Memuat data...</div>;
    if (error || !delivery) return <div className="p-8 text-red-400 bg-red-500/10 border border-red-500/20 max-w-2xl mx-auto rounded-lg mt-8">Error: {error || "Tidak ditemukan"}</div>;

    const canDecide = delivery.status === "Tiba" && delivery.mandorDecision === "Pending";

    return (
        <div className="px-8 py-6 max-w-3xl mx-auto">
            <button onClick={() => router.back()} className="mb-6 text-bone/40 hover:text-bone text-sm flex items-center gap-2 transition">
                <span>&larr;</span> Kembali ke daftar
            </button>
            <h1 className="text-xl font-semibold text-bone mb-6">Detail Pengiriman</h1>

            <div className="bg-ink-muted p-6 rounded-lg border border-white/10 space-y-6">

                {/* Info grid */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-6">
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
                    <div className="p-4 bg-red-500/10 border border-red-500/20 rounded-md text-sm mt-4">
                        <p className="font-semibold text-red-400 mb-1">Alasan Penolakan:</p>
                        <p className="text-red-300/80">{delivery.mandorRejectionReason}</p>
                    </div>
                )}

                {/* Actions */}
                {canDecide && (
                    <div className="border-t border-white/10 pt-6 mt-6">
                        <h3 className="text-sm font-semibold text-bone/60 mb-4">Verifikasi Pengiriman</h3>

                        {!isRejecting ? (
                            <div className="flex gap-3">
                                <button
                                    onClick={handleApprove}
                                    disabled={submitting}
                                    className="px-5 py-2 bg-verdant text-ink font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 transition"
                                >
                                    Setujui Pengiriman
                                </button>
                                <button
                                    onClick={() => setIsRejecting(true)}
                                    className="px-5 py-2 border border-red-500/30 text-red-400 font-semibold rounded-md hover:bg-red-500/10 transition"
                                >
                                    Tolak Pengiriman
                                </button>
                            </div>
                        ) : (
                            <div className="bg-white/5 border border-white/10 p-5 rounded-md space-y-4">
                                <div>
                                    <label className="block text-xs font-semibold text-red-400 uppercase tracking-wider mb-2">Alasan Penolakan:</label>
                                    <textarea
                                        className="w-full bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-red-500 focus:border-red-500 focus:outline-none placeholder:text-bone/30 transition"
                                        rows={3}
                                        value={rejectReason}
                                        onChange={e => setRejectReason(e.target.value)}
                                        placeholder="Masukkan alasan penolakan..."
                                        autoFocus
                                    />
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={() => setIsRejecting(false)} className="px-4 py-2 border border-white/10 text-bone/60 rounded-md text-sm hover:border-white/20 hover:text-bone transition">
                                        Batal
                                    </button>
                                    <button
                                        onClick={handleReject}
                                        disabled={submitting || !rejectReason.trim()}
                                        className="px-4 py-2 bg-red-600 text-white rounded-md font-semibold text-sm disabled:opacity-40 hover:bg-red-700 transition"
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
            <p className="text-xs text-bone/40 uppercase tracking-wider mb-1">{label}</p>
            {children || <p className="font-medium text-bone">{value || "-"}</p>}
        </div>
    );
}

function StatusBadge({ status }: { status: string }) {
    const cls = status === "Memuat" ? "bg-yellow-500/10 text-yellow-400"
        : status === "Mengirim" ? "bg-blue-500/10 text-blue-400"
        : "bg-verdant-soft text-verdant";
    return <span className={`px-2 py-0.5 text-xs font-semibold rounded-full ${cls}`}>{status}</span>;
}

function DecisionBadge({ decision }: { decision: string }) {
    const map: Record<string, { cls: string; label: string }> = {
        Pending: { cls: "bg-white/5 text-bone/50", label: "Menunggu" },
        Approved: { cls: "bg-verdant-soft text-verdant", label: "Disetujui" },
        Rejected: { cls: "bg-red-500/10 text-red-400", label: "Ditolak" },
    };
    const { cls, label } = map[decision] ?? map.Pending;
    return <span className={`px-2 py-0.5 text-xs font-semibold rounded-full ${cls}`}>{label}</span>;
}
