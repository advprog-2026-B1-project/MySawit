"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { deliveryApi, Delivery } from "../../../../services/deliveryApi";

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

    if (loading) return <div className="p-6 text-center text-ink/50">Memuat data...</div>;
    if (error || !delivery) return <div className="p-6 text-red-500">Error: {error || "Tidak ditemukan"}</div>;

    const canDecide = delivery.adminDecision === "Pending" && delivery.mandorDecision === "Approved";

    return (
        <div className="p-6 max-w-3xl mx-auto text-ink">
            <button onClick={() => router.back()} className="mb-4 text-blue-600 hover:underline text-sm">
                &larr; Kembali ke daftar
            </button>
            <h1 className="text-2xl font-bold mb-6">Review Pengiriman</h1>

            <div className="bg-white p-6 rounded-lg shadow border border-ink/10 space-y-6">

                <h3 className="text-sm font-bold text-gray-400 uppercase tracking-wider border-b pb-2">Informasi Pengiriman</h3>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4">
                    <InfoField label="Kebun Asal" value={delivery.hasilPanen?.kebun?.namaKebun} />
                    <InfoField label="Berat Awal (Dilaporkan)" value={`${delivery.hasilPanen?.kilogram} Kg`} highlight />
                    <InfoField label="Mandor Verifikator" value={delivery.mandor?.nama} />
                    <InfoField label="Supir Transportasi" value={delivery.driver?.nama} />
                    <InfoField label="Tanggal Panen" value={delivery.hasilPanen?.tanggalPanen} />
                    <InfoField label="Tgl Verifikasi Mandor" value={formatDate(delivery.mandorDecidedAt)} />
                </div>

                {/* Review history */}
                {delivery.adminDecision !== "Pending" && (
                    <div className="p-4 bg-gray-50 border rounded-md text-sm space-y-1">
                        <p className="font-bold">Hasil Review Admin:</p>
                        <p>Keputusan: <strong>{delivery.adminDecision}</strong></p>
                        {delivery.adminDecision === "PartiallyApproved" && (
                            <p>Berat Diakui: <strong>{delivery.acknowledgedKg} Kg</strong></p>
                        )}
                        {delivery.adminRejectionReason && (
                            <p>Alasan: {delivery.adminRejectionReason}</p>
                        )}
                    </div>
                )}

                {/* Actions */}
                {canDecide && (
                    <div className="border-t pt-6">
                        <h3 className="text-sm font-bold text-gray-400 uppercase tracking-wider mb-4">Final Review Produksi</h3>

                        {actionState === "None" ? (
                            <div className="flex flex-wrap gap-3">
                                <button
                                    onClick={() => handleDecide("Approved")}
                                    disabled={submitting}
                                    className="px-5 py-2 bg-verdant text-ink font-bold rounded-md hover:opacity-90 disabled:opacity-50 transition"
                                >
                                    ✅ Approve Penuh
                                </button>
                                <button
                                    onClick={() => setActionState("PartiallyApproved")}
                                    className="px-5 py-2 bg-amber-400 text-ink font-bold rounded-md hover:bg-amber-500 transition"
                                >
                                    ⚖️ Approve Parsial
                                </button>
                                <button
                                    onClick={() => setActionState("Rejected")}
                                    className="px-5 py-2 bg-red-600 text-white font-bold rounded-md hover:bg-red-700 transition"
                                >
                                    ❌ Tolak Seluruhnya
                                </button>
                            </div>
                        ) : (
                            <div className="bg-gray-50 border rounded-md p-5 space-y-4">
                                <h4 className="font-bold text-sm">
                                    {actionState === "Rejected" ? "Form Penolakan" : "Form Persetujuan Parsial"}
                                </h4>

                                {actionState === "PartiallyApproved" && (
                                    <div>
                                        <label className="block text-sm font-semibold mb-1">Kilogram yang Diakui Valid:</label>
                                        <input
                                            type="number"
                                            className="w-full border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-verdant"
                                            value={acknowledgedKg}
                                            onChange={e => setAcknowledgedKg(e.target.value)}
                                            placeholder={`Maksimal ${delivery.hasilPanen?.kilogram} Kg`}
                                            max={delivery.hasilPanen?.kilogram}
                                            min={1}
                                        />
                                        <p className="text-xs text-gray-400 mt-1">Sisanya dianggap hilang/rusak saat transportasi.</p>
                                    </div>
                                )}

                                <div>
                                    <label className="block text-sm font-semibold mb-1">
                                        {actionState === "Rejected" ? "Alasan Penolakan:" : "Alasan Kekurangan:"}
                                    </label>
                                    <textarea
                                        className="w-full border rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-verdant"
                                        rows={3}
                                        value={rejectReason}
                                        onChange={e => setRejectReason(e.target.value)}
                                        placeholder={actionState === "Rejected" ? "Contoh: Buah mentah, kualitas buruk" : "Contoh: 50 Kg rusak di perjalanan"}
                                    />
                                </div>

                                <div className="flex gap-2">
                                    <button
                                        onClick={() => { setActionState("None"); setRejectReason(""); setAcknowledgedKg(""); }}
                                        className="px-4 py-2 border rounded-md text-sm hover:bg-gray-100 transition"
                                    >
                                        Batal
                                    </button>
                                    <button
                                        onClick={() => handleDecide(actionState)}
                                        disabled={submitting}
                                        className="px-4 py-2 bg-ink text-white rounded-md font-bold text-sm disabled:opacity-50 transition"
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
            <p className="text-xs text-gray-400 mb-0.5">{label}</p>
            <p className={`font-semibold ${highlight ? "text-verdant text-lg" : "text-ink"}`}>{value || "-"}</p>
        </div>
    );
}
