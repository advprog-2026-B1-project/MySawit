"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

const API = "http://localhost:8080";

interface Kebun {
    id: number;
    kodeKebun: string;
    namaKebun: string;
    luasHektare: number;
    koordinat: string;
}

export default function KebunListPage() {
    const router = useRouter();
    const [kebunList, setKebunList] = useState<Kebun[]>([]);
    const [searchNama, setSearchNama] = useState("");
    const [searchKode, setSearchKode] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [deleteId, setDeleteId] = useState<number | null>(null);

    const fetchKebun = async () => {
        setLoading(true);
        setError("");
        try {
            const params = new URLSearchParams();
            if (searchNama) params.append("nama", searchNama);
            if (searchKode) params.append("kode", searchKode);

            const res = await fetch(`${API}/api/kebun?${params}`, { credentials: "include" });

            if (res.status === 401) {
                router.push("/login");
                return;
            }
            if (res.status === 403) {
                setError("Akses ditolak. Hanya Admin yang dapat melihat daftar kebun.");
                return;
            }
            if (!res.ok) {
                setError(`Gagal memuat data kebun (${res.status}).`);
                return;
            }
            const data = await res.json();
            setKebunList(data);
        } catch {
            setError("Tidak dapat terhubung ke server. Pastikan backend berjalan.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchKebun();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [searchNama, searchKode]);

    const handleDelete = async (id: number) => {
        if (!confirm("Yakin ingin menghapus kebun ini?")) return;
        setError("");
        try {
            const res = await fetch(`${API}/api/kebun/${id}`, { method: "DELETE", credentials: "include" });
            if (res.status === 403) {
                setError("Akses ditolak. Hanya Admin yang dapat menghapus kebun.");
                return;
            }
            if (res.status === 422) {
                const data = await res.json();
                setError(data.message || "Kebun tidak dapat dihapus karena masih terikat dengan Mandor aktif.");
                return;
            }
            if (!res.ok) {
                setError(`Gagal menghapus kebun (${res.status}).`);
                return;
            }
            setDeleteId(id);
            setKebunList(prev => prev.filter(k => k.id !== id));
        } catch {
            setError("Tidak dapat terhubung ke server.");
        }
    };

    void deleteId;

    return (
        <div className="px-8 py-6 max-w-5xl">

            {/* Page header */}
            <div className="flex items-start justify-between mb-8">
                <div>
                    <h1 className="text-xl font-semibold text-bone">Daftar Kebun</h1>
                    <p className="text-sm text-bone/40 mt-1">Kelola seluruh unit kebun sawit</p>
                </div>
                <Link
                    href="/kebun/baru"
                    className="px-4 py-2 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover transition shrink-0"
                >
                    + Tambah Kebun
                </Link>
            </div>

            {/* Error */}
            {error && (
                <div className="mb-6 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}

            {/* Search bar */}
            <div className="flex gap-3 mb-5">
                <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-bone/30 text-sm">⌕</span>
                    <input
                        type="text"
                        placeholder="Cari nama kebun..."
                        className="bg-ink-muted border border-white/10 text-bone text-sm rounded-md pl-8 pr-3 py-2 w-56 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition"
                        value={searchNama}
                        onChange={e => setSearchNama(e.target.value)}
                    />
                </div>
                <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-bone/30 text-sm">⌕</span>
                    <input
                        type="text"
                        placeholder="Cari kode kebun..."
                        className="bg-ink-muted border border-white/10 text-bone text-sm rounded-md pl-8 pr-3 py-2 w-48 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition"
                        value={searchKode}
                        onChange={e => setSearchKode(e.target.value)}
                    />
                </div>
                {(searchNama || searchKode) && (
                    <button
                        onClick={() => { setSearchNama(""); setSearchKode(""); }}
                        className="text-xs text-bone/40 hover:text-bone transition px-2"
                    >
                        Reset
                    </button>
                )}
            </div>

            {/* Table */}
            <div className="rounded-lg border border-white/10 overflow-hidden">
                <table className="min-w-full text-sm text-left">
                    <thead>
                        <tr className="bg-ink-soft border-b border-white/10">
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Kode</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Nama Kebun</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Luas (Ha)</th>
                            <th className="px-5 py-3 text-xs font-semibold text-bone/40 uppercase tracking-wider">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="bg-ink-muted divide-y divide-white/5">
                        {loading ? (
                            <tr>
                                <td colSpan={4} className="px-5 py-12 text-center text-bone/30 text-sm">
                                    Memuat data...
                                </td>
                            </tr>
                        ) : kebunList.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-5 py-12 text-center text-bone/30 text-sm">
                                    Tidak ada kebun ditemukan.
                                </td>
                            </tr>
                        ) : (
                            kebunList.map((kebun, i) => (
                                <tr
                                    key={kebun.id}
                                    className={`hover:bg-white/5 transition ${i % 2 === 1 ? "bg-white/[0.02]" : ""}`}
                                >
                                    <td className="px-5 py-3.5 font-mono text-xs text-bone/50">{kebun.kodeKebun}</td>
                                    <td className="px-5 py-3.5 font-medium text-bone">{kebun.namaKebun}</td>
                                    <td className="px-5 py-3.5 text-bone/70">{kebun.luasHektare} ha</td>
                                    <td className="px-5 py-3.5">
                                        <div className="flex items-center gap-2">
                                            <Link
                                                href={`/kebun/${kebun.id}`}
                                                className="px-2.5 py-1 text-xs border border-verdant/30 text-verdant rounded hover:bg-verdant-soft transition"
                                            >
                                                Detail
                                            </Link>
                                            <Link
                                                href={`/kebun/${kebun.id}/edit`}
                                                className="px-2.5 py-1 text-xs border border-white/10 text-bone/60 rounded hover:border-white/20 hover:text-bone transition"
                                            >
                                                Edit
                                            </Link>
                                            <button
                                                onClick={() => handleDelete(kebun.id)}
                                                className="px-2.5 py-1 text-xs text-red-400/70 hover:text-red-400 transition"
                                            >
                                                Hapus
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>

                {/* Table footer */}
                {kebunList.length > 0 && (
                    <div className="px-5 py-3 bg-ink-soft border-t border-white/10">
                        <span className="text-xs text-bone/30">{kebunList.length} kebun ditemukan</span>
                    </div>
                )}
            </div>
        </div>
    );
}
