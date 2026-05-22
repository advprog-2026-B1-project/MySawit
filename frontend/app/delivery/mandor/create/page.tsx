"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { deliveryApi, DUMMY_DRIVERS, DUMMY_HARVESTS } from "../../../../services/deliveryApi";

const inputCls = "w-full bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2.5 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition appearance-none";

export default function MandorCreateDelivery() {
    const router = useRouter();
    const [selectedDriverId, setSelectedDriverId] = useState("");
    const [selectedHarvestId, setSelectedHarvestId] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [mandorId, setMandorId] = useState<number | null>(null);

    useEffect(() => {
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/me`, { credentials: "include" })
            .then(r => r.ok ? r.json() : null)
            .then(u => { if (u?.id) setMandorId(u.id); })
            .catch(() => {});
    }, []);

    const selectedHarvest = DUMMY_HARVESTS.find(h => h.id.toString() === selectedHarvestId);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedDriverId || !selectedHarvestId || !mandorId) return;

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
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        } catch (err: any) {
            setError(err.message || "Gagal membuat penugasan.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="px-8 py-6 max-w-2xl mx-auto">
            <button onClick={() => router.back()} className="mb-6 text-bone/40 hover:text-bone text-sm flex items-center gap-2 transition">
                <span>&larr;</span> Kembali ke daftar
            </button>
            
            <h1 className="text-xl font-semibold text-bone mb-6">Buat Penugasan Pengiriman</h1>

            {error && (
                <div className="mb-6 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} className="bg-ink-muted p-6 rounded-lg border border-white/10 space-y-6">

                {/* Hasil Panen */}
                <div>
                    <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-2">Hasil Panen (Approved)</label>
                    <div className="relative">
                        <select
                            className={inputCls}
                            value={selectedHarvestId}
                            onChange={(e) => setSelectedHarvestId(e.target.value)}
                            required
                        >
                            <option value="" className="bg-ink text-bone/50">-- Pilih Hasil Panen --</option>
                            {DUMMY_HARVESTS.map(h => (
                                <option key={h.id} value={h.id} className="bg-ink">
                                    {h.kebun.namaKebun} — {h.kilogram} Kg ({h.tanggalPanen})
                                </option>
                            ))}
                        </select>
                        <span className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-bone/40 text-xs">▼</span>
                    </div>
                </div>

                {/* Read-only weight display */}
                {selectedHarvest && (
                    <div className="bg-white/5 border border-white/10 p-4 rounded-md">
                        <p className="text-xs text-bone/40 mb-1">Berat Sawit (otomatis dari hasil panen)</p>
                        <p className={`text-2xl font-semibold ${selectedHarvest.kilogram > 400 ? "text-red-400" : "text-bone"}`}>
                            {selectedHarvest.kilogram} <span className="text-base font-normal text-bone/50">Kg</span>
                        </p>
                        <p className="text-xs text-bone/30 mt-1">Nilai ini tidak dapat diubah.</p>
                    </div>
                )}

                {/* Supir */}
                <div>
                    <label className="block text-xs font-semibold text-bone/40 uppercase tracking-wider mb-2">Supir Truk</label>
                    <div className="relative">
                        <select
                            className={inputCls}
                            value={selectedDriverId}
                            onChange={(e) => setSelectedDriverId(e.target.value)}
                            required
                        >
                            <option value="" className="bg-ink text-bone/50">-- Pilih Supir --</option>
                            {DUMMY_DRIVERS.map(d => (
                                <option key={d.id} value={d.id} className="bg-ink">{d.nama}</option>
                            ))}
                        </select>
                        <span className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-bone/40 text-xs">▼</span>
                    </div>
                </div>

                {/* Actions */}
                <div className="flex justify-end gap-3 pt-4 border-t border-white/10">
                    <button
                        type="button"
                        onClick={() => router.back()}
                        className="px-4 py-2 border border-white/10 text-bone/60 rounded-md text-sm hover:border-white/20 hover:text-bone transition"
                    >
                        Batal
                    </button>
                    <button
                        type="submit"
                        disabled={loading || !selectedDriverId || !selectedHarvestId}
                        className="px-5 py-2 bg-verdant text-ink font-semibold rounded-md text-sm disabled:opacity-50 hover:bg-verdant-hover transition"
                    >
                        {loading ? "Menyimpan..." : "Simpan Penugasan"}
                    </button>
                </div>
            </form>
        </div>
    );
}
