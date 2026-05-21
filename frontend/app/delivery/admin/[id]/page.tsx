"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { deliveryApi, Delivery } from "../../../../services/deliveryApi";

const inputCls = "w-full bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2.5 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition placeholder:text-bone/30";

export default function AdminDeliveryDetail() {
    const params = useParams();
    const router = useRouter();
    const deliveryId = Number(params.id);
    const [delivery, setDelivery] = useState<Delivery | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [actionState, setActionState] = useState<"None" | "Rejected" | "PartiallyApproved">("None");
    const [rejectReason, setRejectReason] = useState("");
    const [acknowledgedKg, setAcknowledgedKg] = useState("");
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        deliveryApi.getDetail(deliveryId)
            .then(data => setDelivery(data))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [deliveryId]);

    const handleDecide = async (decision: "Approved" | "Rejected" | "PartiallyApproved") => {
        if (decision === "Rejected" && !rejectReason.trim()) {
            alert("Alasan penolakan harus diisi!");
            return;
        }
        if (decision === "PartiallyApproved") {
            if (!rejectReason.trim()) {
                alert("Alasan persetujuan parsial harus diisi!");
                return;
            }
            if (!acknowledgedKg || isNaN(Number(acknowledgedKg)) || Number(acknowledgedKg) <= 0) {
                alert("Kilogram yang diakui harus valid!");
                return;
            }
        }

        setSubmitting(true);
        await deliveryApi.decideByAdmin(deliveryId, {
            decision,
            rejectionReason: decision === "Approved" ? undefined : rejectReason,
            acknowledgedKg: decision === "PartiallyApproved" ? Number(acknowledgedKg) : undefined,
        });
        alert("Keputusan berhasil disimpan!");
        router.push("/delivery/admin");
    };

    const formatDate = (iso?: string) => {
        if (!iso) return "-";
        return new Date(iso).toLocaleDateString("id-ID", { day: "numeric", month: "long", year: "numeric", hour: "2-digit", minute: "2-digit" });
    };

    if (loading) return <div className="p-8 text-center text-bone/30 text-sm">Memuat data...</div>;
    if (error || !delivery) return <div className="p-8 text-red-400 bg-red-500/10 border border-red-500/20 max-w-2xl mx-auto rounded-lg mt-8">Error: {error || "Tidak ditemukan"}</div>;

    const canDecide = delivery.adminDecision === "Pending" && delivery.mandorDecision === "Approved";

    return (
        <div className="px-8 py-6 max-w-3xl mx-auto">
            <button onClick={() => router.back()} className="mb-6 text-bone/40 hover:text-bone text-sm flex items-center gap-2 transition">
                <span>&larr;</span> Kembali ke daftar
            </button>
            <h1 className="text-xl font-semibold text-bone mb-6">Review Pengiriman</h1>

            <div className="bg-ink-muted p-6 rounded-lg border border-white/10 space-y-6">

                <h3 className="text-xs font-semibold text-bone/40 uppercase tracking-wider border-b border-white/10 pb-3">Informasi Pengiriman</h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-6">
                    <InfoField label="Kebun Asal" value={delivery.hasilPanen?.kebun?.namaKebun} />
                    <InfoField label="Berat Awal (Dilaporkan)" value={`${delivery.hasilPanen?.kilogram} Kg`} highlight />
                    <InfoField label="Mandor Verifikator" value={delivery.mandor?.nama} />
                    <InfoField label="Supir Transportasi" value={delivery.driver?.nama} />
                    <InfoField label="Tanggal Panen" value={delivery.hasilPanen?.tanggalPanen} />
                    <InfoField label="Tgl Verifikasi Mandor" value={formatDate(delivery.mandorDecidedAt)} />
                </div>

                {/* Review history */}
                {delivery.adminDecision !== "Pending" && (
                    <div className="p-4 bg-white/5 border border-white/10 rounded-md text-sm space-y-1 mt-4">
                        <p className="font-semibold text-bone/60 mb-2">Hasil Review Admin:</p>
                        <p className="text-bone">Keputusan: <strong className={
                            delivery.adminDecision === "Approved" ? "text-verdant" :
                            delivery.adminDecision === "PartiallyApproved" ? "text-yellow-400" :
                            "text-red-400"
                        }>{delivery.adminDecision}</strong></p>
                        {delivery.adminDecision === "PartiallyApproved" && (
                            <p className="text-bone">Berat Diakui: <strong>{delivery.acknowledgedKg} Kg</strong></p>
                        )}
                        {delivery.adminRejectionReason && (
                            <p className="text-bone/80 mt-1">Alasan: {delivery.adminRejectionReason}</p>
                        )}
                    </div>
                )}

                {/* Actions */}
                {canDecide && (
                    <div className="border-t border-white/10 pt-6 mt-6">
                        <h3 className="text-xs font-semibold text-bone/40 uppercase tracking-wider mb-5">Final Review Produksi</h3>

                        {actionState === "None" ? (
                            <div className="flex flex-wrap gap-3">
                                <button
                                    onClick={() => handleDecide("Approved")}
                                    disabled={submitting}
                                    className="px-5 py-2.5 bg-verdant text-ink font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-50 transition"
                                >
                                    ✅ Approve Penuh
                                </button>
                                <button
                                    onClick={() => setActionState("PartiallyApproved")}
                                    className="px-5 py-2.5 border border-yellow-500/30 text-yellow-400 font-semibold rounded-md hover:bg-yellow-500/10 transition"
                                >
                                    ⚖️ Approve Parsial
                                </button>
                                <button
                                    onClick={() => setActionState("Rejected")}
                                    className="px-5 py-2.5 border border-red-500/30 text-red-400 font-semibold rounded-md hover:bg-red-500/10 transition"
                                >
                                    ❌ Tolak Seluruhnya
                                </button>
                            </div>
                        ) : (
                            <div className="bg-white/5 border border-white/10 p-5 rounded-md space-y-5">
                                <h4 className="font-semibold text-sm text-bone">
                                    {actionState === "Rejected" ? "Form Penolakan" : "Form Persetujuan Parsial"}
                                </h4>

                                {actionState === "PartiallyApproved" && (
                                    <div>
                                        <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-2">Kilogram yang Diakui Valid</label>
                                        <input
                                            type="number"
                                            className={inputCls}
                                            value={acknowledgedKg}
                                            onChange={e => setAcknowledgedKg(e.target.value)}
                                            placeholder={`Maksimal ${delivery.hasilPanen?.kilogram} Kg`}
                                            max={delivery.hasilPanen?.kilogram}
                                            min={1}
                                        />
                                        <p className="text-xs text-bone/30 mt-1.5">Sisanya dianggap hilang/rusak saat transportasi.</p>
                                    </div>
                                )}

                                <div>
                                    <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-2">
                                        {actionState === "Rejected" ? "Alasan Penolakan" : "Alasan Kekurangan"}
                                    </label>
                                    <textarea
                                        className={inputCls}
                                        rows={3}
                                        value={rejectReason}
                                        onChange={e => setRejectReason(e.target.value)}
                                        placeholder={actionState === "Rejected" ? "Contoh: Buah mentah, kualitas buruk" : "Contoh: 50 Kg rusak di perjalanan"}
                                    />
                                </div>

                                <div className="flex gap-2 pt-2">
                                    <button
                                        onClick={() => { setActionState("None"); setRejectReason(""); setAcknowledgedKg(""); }}
                                        className="px-4 py-2 border border-white/10 text-bone/60 rounded-md text-sm hover:border-white/20 hover:text-bone transition"
                                    >
                                        Batal
                                    </button>
                                    <button
                                        onClick={() => handleDecide(actionState)}
                                        disabled={submitting}
                                        className="px-5 py-2 bg-verdant text-ink font-semibold rounded-md text-sm disabled:opacity-50 transition"
                                    >
                                        Konfirmasi {actionState === "Rejected" ? "Penolakan" : "Parsial"}
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

function InfoField({ label, value, highlight }: { label: string; value?: string; highlight?: boolean }) {
    return (
        <div>
            <p className="text-xs text-bone/40 uppercase tracking-wider mb-1">{label}</p>
            <p className={`font-medium ${highlight ? "text-verdant text-lg" : "text-bone"}`}>{value || "-"}</p>
        </div>
    );
}
