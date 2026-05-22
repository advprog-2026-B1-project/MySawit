"use client";
import { useEffect, useState } from 'react';

interface HarvestResponse {
    id: number;
    tanggalPanen: string;
    kilogram: number;
    berita: string;
    status: string;
}

function StatusBadge({ status }: { status: string }) {
    const cls =
        status === 'Approved' ? 'bg-verdant-soft text-verdant' :
        status === 'Rejected' ? 'bg-red-500/10 text-red-400' :
        'bg-white/5 text-bone/50';
    return (
        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${cls}`}>
            {status}
        </span>
    );
}

export default function HarvestHistory() {
    const [history, setHistory] = useState<HarvestResponse[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/harvest/me`, { credentials: "include" })
            .then(res => res.json())
            .then(data => {
                setHistory(data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch history:", err);
                setLoading(false);
            });
    }, []);

    return (
        <div className="px-8 py-6 max-w-4xl">

            <div className="mb-8">
                <h1 className="text-xl font-semibold text-bone">Riwayat Hasil Panen</h1>
                <p className="text-sm text-bone/40 mt-1">Rekap seluruh laporan panen yang telah dikirim</p>
            </div>

            {loading ? (
                <div className="space-y-2">
                    {[...Array(4)].map((_, i) => (
                        <div key={i} className="h-12 bg-white/5 rounded animate-pulse" />
                    ))}
                </div>
            ) : (
                <div className="rounded-lg border border-white/10 overflow-hidden">
                    <table className="min-w-full text-sm text-left">
                        <thead>
                            <tr className="bg-ink-soft border-b border-white/10">
                                <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Tanggal</th>
                                <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kilogram</th>
                                <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Berita</th>
                                <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Status</th>
                            </tr>
                        </thead>
                        <tbody className="bg-ink-muted divide-y divide-white/5">
                            {history.length === 0 ? (
                                <tr>
                                    <td colSpan={4} className="px-5 py-12 text-center text-bone/30 text-sm">
                                        Belum ada riwayat panen.
                                    </td>
                                </tr>
                            ) : (
                                history.map((item, i) => (
                                    <tr key={item.id} className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}>
                                        <td className="px-5 py-3.5 text-bone/70 whitespace-nowrap">{item.tanggalPanen}</td>
                                        <td className="px-5 py-3.5 text-bone font-medium">{item.kilogram} kg</td>
                                        <td className="px-5 py-3.5 text-bone/60 max-w-xs truncate">{item.berita || "-"}</td>
                                        <td className="px-5 py-3.5"><StatusBadge status={item.status} /></td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                    {history.length > 0 && (
                        <div className="px-5 py-3 bg-ink-soft border-t border-white/10">
                            <span className="text-xs text-bone/30">{history.length} entri</span>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
