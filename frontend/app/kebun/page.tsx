"use client";

import { useState, useEffect } from "react";
import Link from "next/link";

const API = "http://localhost:8080";

interface Kebun {
    id: number;
    kodeKebun: string;
    namaKebun: string;
    luasHektare: number;
    koordinat: string;
}

function ErrorBanner({ message }: { message: string }) {
    return (
        <div className="mb-4 px-4 py-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
            {message}
        </div>
    );
}

export default function KebunListPage() {
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

            const res = await fetch(`${API}/api/kebun?${params}`);

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
            const res = await fetch(`${API}/api/kebun/${id}`, { method: "DELETE" });
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
        <div className="p-6 max-w-6xl mx-auto text-gray-800">
            <div className="flex items-center justify-between mb-6">
                <h1 className="text-2xl font-bold text-green-700">Manajemen Kebun Sawit</h1>
                <Link
                    href="/kebun/baru"
                    className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-md hover:bg-green-700 transition"
                >
                    + Tambah Kebun
                </Link>
            </div>

            {/* Filter */}
            <div className="flex flex-wrap gap-3 mb-5 bg-gray-100 p-4 rounded-lg border border-gray-200">
                <input
                    type="text"
                    placeholder="Cari nama kebun..."
                    className="border border-gray-300 rounded-md px-3 py-2 text-sm w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-green-500"
                    value={searchNama}
                    onChange={e => setSearchNama(e.target.value)}
                />
                <input
                    type="text"
                    placeholder="Cari kode kebun..."
                    className="border border-gray-300 rounded-md px-3 py-2 text-sm w-full max-w-xs focus:outline-none focus:ring-2 focus:ring-green-500"
                    value={searchKode}
                    onChange={e => setSearchKode(e.target.value)}
                />
            </div>

            {error && <ErrorBanner message={error} />}

            {/* Tabel */}
            <div className="overflow-x-auto bg-white shadow-sm rounded-lg border border-gray-200">
                <table className="min-w-full divide-y divide-gray-200 text-sm text-left">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-5 py-3 font-semibold text-gray-600">Kode</th>
                            <th className="px-5 py-3 font-semibold text-gray-600">Nama Kebun</th>
                            <th className="px-5 py-3 font-semibold text-gray-600">Luas (Ha)</th>
                            <th className="px-5 py-3 font-semibold text-gray-600">Koordinat</th>
                            <th className="px-5 py-3 font-semibold text-gray-600">Aksi</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                        {loading ? (
                            <tr>
                                <td colSpan={5} className="px-5 py-8 text-center text-gray-400">Memuat data...</td>
                            </tr>
                        ) : kebunList.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-5 py-8 text-center text-gray-400">Tidak ada kebun ditemukan.</td>
                            </tr>
                        ) : (
                            kebunList.map(kebun => (
                                <tr key={kebun.id} className="hover:bg-gray-50">
                                    <td className="px-5 py-4 font-mono text-xs">{kebun.kodeKebun}</td>
                                    <td className="px-5 py-4 font-medium">{kebun.namaKebun}</td>
                                    <td className="px-5 py-4">{kebun.luasHektare}</td>
                                    <td className="px-5 py-4 text-xs text-gray-500 max-w-xs truncate">{kebun.koordinat}</td>
                                    <td className="px-5 py-4 flex gap-2">
                                        <Link
                                            href={`/kebun/${kebun.id}`}
                                            className="px-3 py-1 bg-blue-600 text-white text-xs rounded hover:bg-blue-700 transition"
                                        >
                                            Detail
                                        </Link>
                                        <Link
                                            href={`/kebun/${kebun.id}/edit`}
                                            className="px-3 py-1 bg-yellow-500 text-white text-xs rounded hover:bg-yellow-600 transition"
                                        >
                                            Edit
                                        </Link>
                                        <button
                                            onClick={() => handleDelete(kebun.id)}
                                            className="px-3 py-1 bg-red-600 text-white text-xs rounded hover:bg-red-700 transition"
                                        >
                                            Hapus
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
