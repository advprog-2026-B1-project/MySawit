"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { deliveryApi, DUMMY_DRIVERS, DUMMY_HARVESTS } from "../../../../services/deliveryApi";

export default function MandorCreateDelivery() {
    const router = useRouter();
    const [selectedDriverId, setSelectedDriverId] = useState("");
    const [selectedHarvestId, setSelectedHarvestId] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const mandorId = 1;

    const selectedHarvest = DUMMY_HARVESTS.find(h => h.id.toString() === selectedHarvestId);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedDriverId || !selectedHarvestId) return;

        if (selectedHarvest && selectedHarvest.kilogram > 400) {
            setError("Kapasitas truk sawit maksimal 400 Kg. Hasil panen ini terlalu berat.");
            return;
        }

        setLoading(true);
        setError("");
        try {
            await deliveryApi.createDelivery(mandorId, {
                driverId: Number(selectedDriverId),
                hasilPanenId: Number(selectedHarvestId),
            });
            alert("Penugasan pengiriman berhasil dibuat!");
            router.push("/delivery/mandor");
        } catch (err: any) {
            setError(err.message || "Gagal membuat penugasan.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="p-6 max-w-2xl mx-auto text-ink">
            <button onClick={() => router.back()} className="mb-4 text-blue-600 hover:underline text-sm">
                &larr; Kembali ke daftar
            </button>
            <h1 className="text-2xl font-bold mb-6">Buat Penugasan Pengiriman</h1>

            {error && <div className="mb-4 text-red-600 bg-red-50 border border-red-200 p-3 rounded text-sm">{error}</div>}

            <form onSubmit={handleSubmit} className="bg-white p-6 rounded-lg shadow border border-ink/10 space-y-5">

                {/* Hasil Panen */}
                <div>
                    <label className="block text-sm font-bold mb-1.5">Hasil Panen (Approved)</label>
                    <select
                        className="w-full border border-ink/20 rounded-md px-3 py-2.5 bg-bone text-sm focus:outline-none focus:ring-2 focus:ring-verdant"
                        value={selectedHarvestId}
                        onChange={(e) => setSelectedHarvestId(e.target.value)}
                        required
                    >
                        <option value="">-- Pilih Hasil Panen --</option>
                        {DUMMY_HARVESTS.map(h => (
                            <option key={h.id} value={h.id}>
                                {h.kebun.namaKebun} — {h.kilogram} Kg ({h.tanggalPanen})
                            </option>
                        ))}
                    </select>
                </div>

                {/* Read-only weight display */}
                {selectedHarvest && (
                    <div className="bg-verdant/10 border border-verdant/30 p-4 rounded-md">
                        <p className="text-xs text-gray-500 mb-1">Berat Sawit (otomatis dari hasil panen)</p>
                        <p className="text-2xl font-bold text-ink">{selectedHarvest.kilogram} <span className="text-base font-normal text-ink/60">Kg</span></p>
                        <p className="text-xs text-gray-400 mt-1">Nilai ini tidak dapat diubah.</p>
                    </div>
                )}

                {/* Supir */}
                <div>
                    <label className="block text-sm font-bold mb-1.5">Supir Truk</label>
                    <select
                        className="w-full border border-ink/20 rounded-md px-3 py-2.5 bg-bone text-sm focus:outline-none focus:ring-2 focus:ring-verdant"
                        value={selectedDriverId}
                        onChange={(e) => setSelectedDriverId(e.target.value)}
                        required
                    >
                        <option value="">-- Pilih Supir --</option>
                        {DUMMY_DRIVERS.map(d => (
                            <option key={d.id} value={d.id}>{d.nama}</option>
                        ))}
                    </select>
                </div>

                {/* Actions */}
                <div className="flex justify-end gap-3 pt-2">
                    <button
                        type="button"
                        onClick={() => router.back()}
                        className="px-4 py-2 border border-ink/20 rounded-md text-sm hover:bg-gray-50 transition"
                    >
                        Batal
                    </button>
                    <button
                        type="submit"
                        disabled={loading || !selectedDriverId || !selectedHarvestId}
                        className="px-5 py-2 bg-verdant text-ink font-bold rounded-md text-sm disabled:opacity-50 hover:opacity-90 transition"
                    >
                        {loading ? "Menyimpan..." : "Simpan Penugasan"}
                    </button>
                </div>
            </form>
        </div>
    );
}
