"use client";

import { useState, useEffect, use } from "react";
import Link from "next/link";

const API = "http://localhost:8080";

interface Kebun {
    id: number;
    kodeKebun: string;
    namaKebun: string;
    luasHektare: number;
    koordinat: string;
}

interface UserSummary {
    id: number;
    nama: string;
    email: string;
}

interface Assignment {
    mandor?: UserSummary;
    supirList: UserSummary[];
}

function ErrorBanner({ message }: { message: string }) {
    return (
        <div className="mb-4 px-4 py-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
            {message}
        </div>
    );
}

function SuccessBanner({ message }: { message: string }) {
    return (
        <div className="mb-4 px-4 py-3 bg-green-50 border border-green-200 rounded-md text-sm text-green-700">
            {message}
        </div>
    );
}

export default function KebunDetailPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = use(params);
    const kebunId = Number(id);

    const [kebun, setKebun] = useState<Kebun | null>(null);
    const [assignment, setAssignment] = useState<Assignment>({ supirList: [] });
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(true);
    const [refresh, setRefresh] = useState(0);

    // Assign mandor state
    const [showAssignMandor, setShowAssignMandor] = useState(false);
    const [mandorId, setMandorId] = useState("");
    const [newKebunMandorId, setNewKebunMandorId] = useState("");

    // Assign supir state
    const [showAssignSupir, setShowAssignSupir] = useState(false);
    const [supirId, setSupirId] = useState("");
    const [newKebunSupirId, setNewKebunSupirId] = useState("");

    // Search supir
    const [searchSupir, setSearchSupir] = useState("");

    useEffect(() => {
        const fetchAll = async () => {
            setLoading(true);
            setError("");
            try {
                const kebunRes = await fetch(`${API}/api/kebun/${kebunId}`);
                if (kebunRes.status === 403) { setError("Akses ditolak."); return; }
                if (kebunRes.status === 404) { setError("Kebun tidak ditemukan."); return; }
                if (!kebunRes.ok) { setError(`Error ${kebunRes.status}`); return; }
                setKebun(await kebunRes.json());

                // Assignment info — gunakan endpoint detail yang tersedia
                // Backend saat ini mengembalikan KebunResponse (belum ada KebunDetailResponse)
                // Data mandor & supir diambil dari mandor_assignment & driver_assignment
                // endpoint terpisah jika ada, atau tampilkan placeholder
            } catch {
                setError("Tidak dapat terhubung ke server.");
            } finally {
                setLoading(false);
            }
        };
        fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [kebunId, refresh]);

    const postJson = async (url: string, body: object) => {
        return fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
    };

    const handleAssignMandor = async () => {
        setError(""); setSuccess("");
        if (!mandorId) { setError("Masukkan ID Mandor."); return; }
        const res = await postJson(`${API}/api/kebun/assign-mandor`, {
            mandorId: Number(mandorId),
            kebunId: kebunId,
        });
        await handleApiResponse(res, "Mandor berhasil ditugaskan ke kebun ini.");
        setShowAssignMandor(false);
        setMandorId("");
    };

    const handleReassignMandor = async () => {
        setError(""); setSuccess("");
        if (!mandorId || !newKebunMandorId) { setError("Isi semua field reassign Mandor."); return; }
        const res = await postJson(`${API}/api/kebun/reassign-mandor`, {
            mandorId: Number(mandorId),
            oldKebunId: kebunId,
            newKebunId: Number(newKebunMandorId),
        });
        await handleApiResponse(res, "Mandor berhasil dipindah ke kebun baru.");
        setShowAssignMandor(false);
        setMandorId(""); setNewKebunMandorId("");
    };

    const handleAssignSupir = async () => {
        setError(""); setSuccess("");
        if (!supirId) { setError("Masukkan ID Supir."); return; }
        const res = await postJson(`${API}/api/kebun/assign-supir`, {
            supirId: Number(supirId),
            kebunId: kebunId,
        });
        await handleApiResponse(res, "Supir berhasil ditugaskan ke kebun ini.");
        setShowAssignSupir(false);
        setSupirId("");
    };

    const handleReassignSupir = async () => {
        setError(""); setSuccess("");
        if (!supirId || !newKebunSupirId) { setError("Isi semua field reassign Supir."); return; }
        const res = await postJson(`${API}/api/kebun/reassign-supir`, {
            supirId: Number(supirId),
            oldKebunId: kebunId,
            newKebunId: Number(newKebunSupirId),
        });
        await handleApiResponse(res, "Supir berhasil dipindah ke kebun baru.");
        setShowAssignSupir(false);
        setSupirId(""); setNewKebunSupirId("");
    };

    const handleApiResponse = async (res: Response, successMsg: string) => {
        if (res.status === 403) { setError("Akses ditolak. Hanya Admin yang dapat melakukan aksi ini."); return; }
        if (res.status === 404) { setError("Data tidak ditemukan."); return; }
        if (res.status === 409) { setError("Konflik data — kode sudah terdaftar."); return; }
        if (res.status === 422) {
            const data = await res.json().catch(() => ({}));
            setError(data.message || "Operasi tidak valid. Periksa data yang dimasukkan."); return;
        }
        if (res.status === 400) {
            const data = await res.json().catch(() => ({}));
            setError(data.message || "Input tidak valid."); return;
        }
        if (!res.ok) { setError(`Terjadi kesalahan (${res.status}).`); return; }
        setSuccess(successMsg);
        setRefresh(r => r + 1);
    };

    const filteredSupir = assignment.supirList.filter(s =>
        s.nama.toLowerCase().includes(searchSupir.toLowerCase())
    );

    if (loading) return <div className="p-8 text-center text-gray-400">Memuat data kebun...</div>;

    return (
        <div className="p-6 max-w-4xl mx-auto text-gray-800">
            <div className="flex items-center gap-2 mb-6">
                <Link href="/kebun" className="text-sm text-green-600 hover:underline">← Kembali ke Daftar Kebun</Link>
            </div>

            {error && <ErrorBanner message={error} />}
            {success && <SuccessBanner message={success} />}

            {/* Info Kebun */}
            {kebun && (
                <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6 mb-6">
                    <div className="flex items-start justify-between mb-4">
                        <h1 className="text-2xl font-bold text-green-700">{kebun.namaKebun}</h1>
                        <Link
                            href={`/kebun/${kebun.id}/edit`}
                            className="px-3 py-1.5 bg-yellow-500 text-white text-sm rounded hover:bg-yellow-600 transition"
                        >
                            Edit Kebun
                        </Link>
                    </div>
                    <div className="grid grid-cols-2 gap-4 text-sm">
                        <div>
                            <span className="text-gray-500">Kode Kebun</span>
                            <p className="font-mono font-medium mt-0.5">{kebun.kodeKebun}</p>
                        </div>
                        <div>
                            <span className="text-gray-500">Luas</span>
                            <p className="font-medium mt-0.5">{kebun.luasHektare} Ha</p>
                        </div>
                        <div className="col-span-2">
                            <span className="text-gray-500">Koordinat</span>
                            <p className="font-mono text-xs mt-0.5 text-gray-700 break-all">{kebun.koordinat}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Mandor Section */}
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6 mb-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-gray-800">Mandor Pengawas</h2>
                    <button
                        onClick={() => { setShowAssignMandor(v => !v); setShowAssignSupir(false); }}
                        className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition"
                    >
                        {assignment.mandor ? "Pindah Mandor" : "Tugaskan Mandor"}
                    </button>
                </div>

                {assignment.mandor ? (
                    <div className="text-sm">
                        <p className="font-medium">{assignment.mandor.nama}</p>
                        <p className="text-gray-500">{assignment.mandor.email}</p>
                    </div>
                ) : (
                    <p className="text-sm text-gray-400 italic">Belum ada mandor yang ditugaskan.</p>
                )}

                {showAssignMandor && (
                    <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200 text-sm space-y-3">
                        <div>
                            <label className="block text-gray-600 mb-1">
                                {assignment.mandor ? "ID Mandor yang akan dipindah" : "ID Mandor"}
                            </label>
                            <input
                                type="number"
                                className="border border-gray-300 rounded-md px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Contoh: 5"
                                value={mandorId}
                                onChange={e => setMandorId(e.target.value)}
                            />
                        </div>
                        {assignment.mandor && (
                            <div>
                                <label className="block text-gray-600 mb-1">ID Kebun Tujuan (wajib saat pindah)</label>
                                <input
                                    type="number"
                                    className="border border-gray-300 rounded-md px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    placeholder="ID kebun tujuan"
                                    value={newKebunMandorId}
                                    onChange={e => setNewKebunMandorId(e.target.value)}
                                />
                            </div>
                        )}
                        <div className="flex gap-2">
                            <button
                                onClick={assignment.mandor ? handleReassignMandor : handleAssignMandor}
                                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                            >
                                {assignment.mandor ? "Pindahkan" : "Tugaskan"}
                            </button>
                            <button
                                onClick={() => { setShowAssignMandor(false); setMandorId(""); setNewKebunMandorId(""); }}
                                className="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition"
                            >
                                Batal
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Supir Section */}
            <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-gray-800">Daftar Supir Truk</h2>
                    <button
                        onClick={() => { setShowAssignSupir(v => !v); setShowAssignMandor(false); }}
                        className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition"
                    >
                        Tugaskan Supir
                    </button>
                </div>

                <input
                    type="text"
                    placeholder="Cari nama supir..."
                    className="border border-gray-300 rounded-md px-3 py-2 text-sm w-full max-w-xs mb-4 focus:outline-none focus:ring-2 focus:ring-green-500"
                    value={searchSupir}
                    onChange={e => setSearchSupir(e.target.value)}
                />

                {filteredSupir.length === 0 ? (
                    <p className="text-sm text-gray-400 italic">Belum ada supir yang ditugaskan di kebun ini.</p>
                ) : (
                    <div className="space-y-2">
                        {filteredSupir.map(s => (
                            <div key={s.id} className="flex items-center justify-between p-3 bg-gray-50 rounded border border-gray-100 text-sm">
                                <div>
                                    <p className="font-medium">{s.nama}</p>
                                    <p className="text-gray-500 text-xs">{s.email}</p>
                                </div>
                                <button
                                    onClick={() => { setSupirId(String(s.id)); setShowAssignSupir(true); }}
                                    className="px-3 py-1 bg-orange-500 text-white text-xs rounded hover:bg-orange-600 transition"
                                >
                                    Pindahkan
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                {showAssignSupir && (
                    <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200 text-sm space-y-3">
                        <div>
                            <label className="block text-gray-600 mb-1">ID Supir</label>
                            <input
                                type="number"
                                className="border border-gray-300 rounded-md px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Contoh: 12"
                                value={supirId}
                                onChange={e => setSupirId(e.target.value)}
                            />
                        </div>
                        <div>
                            <label className="block text-gray-600 mb-1">
                                ID Kebun Tujuan <span className="text-gray-400">(isi jika memindahkan dari kebun lain)</span>
                            </label>
                            <input
                                type="number"
                                className="border border-gray-300 rounded-md px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Kosongkan jika assign baru"
                                value={newKebunSupirId}
                                onChange={e => setNewKebunSupirId(e.target.value)}
                            />
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={newKebunSupirId ? handleReassignSupir : handleAssignSupir}
                                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                            >
                                {newKebunSupirId ? "Pindahkan" : "Tugaskan"}
                            </button>
                            <button
                                onClick={() => { setShowAssignSupir(false); setSupirId(""); setNewKebunSupirId(""); }}
                                className="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition"
                            >
                                Batal
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
