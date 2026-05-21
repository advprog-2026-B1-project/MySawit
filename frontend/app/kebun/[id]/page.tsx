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

function inputCls(hasError = false) {
    return `w-full bg-ink border ${hasError ? "border-red-500/50" : "border-white/10"} text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition`;
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

    const [showAssignMandor, setShowAssignMandor] = useState(false);
    const [mandorId, setMandorId] = useState("");
    const [newKebunMandorId, setNewKebunMandorId] = useState("");

    const [showAssignSupir, setShowAssignSupir] = useState(false);
    const [supirId, setSupirId] = useState("");
    const [newKebunSupirId, setNewKebunSupirId] = useState("");

    const [searchSupir, setSearchSupir] = useState("");

    useEffect(() => {
        const fetchAll = async () => {
            setLoading(true);
            setError("");
            try {
                const params = new URLSearchParams();
                if (searchSupir) params.append("searchNamaSupir", searchSupir);
                const res = await fetch(`${API}/api/kebun/${kebunId}/detail?${params}`);
                if (res.status === 403) { setError("Akses ditolak."); return; }
                if (res.status === 404) { setError("Kebun tidak ditemukan."); return; }
                if (!res.ok) { setError(`Error ${res.status}`); return; }
                const data = await res.json();
                setKebun(data);
                setAssignment({ mandor: data.mandor ?? undefined, supirList: data.supirList ?? [] });
            } catch {
                setError("Tidak dapat terhubung ke server.");
            } finally {
                setLoading(false);
            }
        };
        fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [kebunId, refresh, searchSupir]);

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

    if (loading) {
        return (
            <div className="px-8 py-6">
                <div className="h-4 w-32 bg-white/5 rounded animate-pulse mb-8" />
                <div className="h-40 bg-white/5 rounded-lg animate-pulse mb-4" />
                <div className="h-32 bg-white/5 rounded-lg animate-pulse" />
            </div>
        );
    }

    return (
        <div className="px-8 py-6 max-w-3xl">

            {/* Breadcrumb */}
            <div className="flex items-center gap-2 text-sm mb-6">
                <Link href="/kebun" className="text-bone/40 hover:text-bone transition">Daftar Kebun</Link>
                <span className="text-bone/20">/</span>
                <span className="text-bone/70">{kebun?.namaKebun ?? "Detail"}</span>
            </div>

            {/* Alerts */}
            {error && (
                <div className="mb-5 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">
                    {error}
                </div>
            )}
            {success && (
                <div className="mb-5 px-4 py-3 bg-verdant-soft border border-verdant/20 rounded-lg text-sm text-verdant">
                    {success}
                </div>
            )}

            {/* Kebun info card */}
            {kebun && (
                <div className="bg-ink-muted border border-white/10 rounded-lg p-6 mb-4">
                    <div className="flex items-start justify-between mb-5">
                        <div>
                            <p className="text-xs font-semibold text-bone/30 uppercase tracking-wider mb-1">
                                {kebun.kodeKebun}
                            </p>
                            <h1 className="text-xl font-semibold text-bone">{kebun.namaKebun}</h1>
                        </div>
                        <Link
                            href={`/kebun/${kebun.id}/edit`}
                            className="px-3 py-1.5 text-xs border border-white/10 text-bone/60 rounded-md hover:border-white/20 hover:text-bone transition"
                        >
                            Edit
                        </Link>
                    </div>
                    <div className="grid grid-cols-3 gap-4 pt-4 border-t border-white/10">
                        <div>
                            <p className="text-xs text-bone/30 uppercase tracking-wider mb-1">Luas</p>
                            <p className="text-sm font-medium text-bone">{kebun.luasHektare} ha</p>
                        </div>
                        <div className="col-span-2">
                            <p className="text-xs text-bone/30 uppercase tracking-wider mb-1">Koordinat</p>
                            <p className="text-xs font-mono text-bone/50 break-all leading-relaxed">{kebun.koordinat}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Mandor card */}
            <div className="bg-ink-muted border border-white/10 rounded-lg p-6 mb-4">
                <div className="flex items-center justify-between mb-4">
                    <div>
                        <p className="text-xs font-semibold text-bone/30 uppercase tracking-wider">Mandor Pengawas</p>
                    </div>
                    <button
                        onClick={() => { setShowAssignMandor(v => !v); setShowAssignSupir(false); }}
                        className="px-3 py-1.5 bg-verdant text-ink text-xs font-semibold rounded-md hover:bg-verdant-hover transition"
                    >
                        {assignment.mandor ? "Pindah Mandor" : "+ Tugaskan"}
                    </button>
                </div>

                {assignment.mandor ? (
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-verdant-soft flex items-center justify-center text-verdant text-sm font-bold shrink-0">
                            {assignment.mandor.nama.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <p className="text-sm font-medium text-bone">{assignment.mandor.nama}</p>
                            <p className="text-xs text-bone/40">{assignment.mandor.email}</p>
                        </div>
                    </div>
                ) : (
                    <p className="text-sm text-bone/30">Belum ada mandor yang ditugaskan.</p>
                )}

                {showAssignMandor && (
                    <div className="mt-4 pt-4 border-t border-white/10 space-y-3">
                        <div>
                            <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">
                                {assignment.mandor ? "ID Mandor yang dipindah" : "ID Mandor"}
                            </label>
                            <input
                                type="number"
                                className={inputCls()}
                                placeholder="Contoh: 5"
                                value={mandorId}
                                onChange={e => setMandorId(e.target.value)}
                            />
                        </div>
                        {assignment.mandor && (
                            <div>
                                <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">
                                    ID Kebun Tujuan
                                </label>
                                <input
                                    type="number"
                                    className={inputCls()}
                                    placeholder="ID kebun tujuan"
                                    value={newKebunMandorId}
                                    onChange={e => setNewKebunMandorId(e.target.value)}
                                />
                            </div>
                        )}
                        <div className="flex gap-2 pt-1">
                            <button
                                onClick={assignment.mandor ? handleReassignMandor : handleAssignMandor}
                                className="px-4 py-1.5 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover transition"
                            >
                                {assignment.mandor ? "Pindahkan" : "Tugaskan"}
                            </button>
                            <button
                                onClick={() => { setShowAssignMandor(false); setMandorId(""); setNewKebunMandorId(""); }}
                                className="px-4 py-1.5 text-sm text-bone/40 hover:text-bone transition"
                            >
                                Batal
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Supir card */}
            <div className="bg-ink-muted border border-white/10 rounded-lg p-6">
                <div className="flex items-center justify-between mb-4">
                    <div>
                        <p className="text-xs font-semibold text-bone/30 uppercase tracking-wider">
                            Supir Truk
                            {assignment.supirList.length > 0 && (
                                <span className="ml-2 px-1.5 py-0.5 bg-verdant-soft text-verdant rounded text-xs">
                                    {assignment.supirList.length}
                                </span>
                            )}
                        </p>
                    </div>
                    <button
                        onClick={() => { setShowAssignSupir(v => !v); setShowAssignMandor(false); }}
                        className="px-3 py-1.5 bg-verdant text-ink text-xs font-semibold rounded-md hover:bg-verdant-hover transition"
                    >
                        + Tugaskan
                    </button>
                </div>

                {/* Search supir */}
                {assignment.supirList.length > 0 && (
                    <div className="relative mb-3">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-bone/30 text-sm">⌕</span>
                        <input
                            type="text"
                            placeholder="Cari nama supir..."
                            className="w-full bg-ink border border-white/10 text-bone text-sm rounded-md pl-8 pr-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none placeholder:text-bone/30 transition"
                            value={searchSupir}
                            onChange={e => setSearchSupir(e.target.value)}
                        />
                    </div>
                )}

                {filteredSupir.length === 0 ? (
                    <p className="text-sm text-bone/30">Belum ada supir yang ditugaskan di kebun ini.</p>
                ) : (
                    <div className="space-y-1">
                        {filteredSupir.map(s => (
                            <div
                                key={s.id}
                                className="flex items-center justify-between px-3 py-2.5 rounded-md bg-ink-soft hover:bg-white/5 transition group"
                            >
                                <div className="flex items-center gap-2.5">
                                    <div className="w-7 h-7 rounded-full bg-ink-muted border border-white/10 flex items-center justify-center text-bone/50 text-xs font-medium shrink-0">
                                        {s.nama.charAt(0).toUpperCase()}
                                    </div>
                                    <div>
                                        <p className="text-sm text-bone">{s.nama}</p>
                                        <p className="text-xs text-bone/40">{s.email}</p>
                                    </div>
                                </div>
                                <button
                                    onClick={() => { setSupirId(String(s.id)); setShowAssignSupir(true); }}
                                    className="text-xs text-bone/30 hover:text-verdant transition opacity-0 group-hover:opacity-100"
                                >
                                    Pindahkan
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                {showAssignSupir && (
                    <div className="mt-4 pt-4 border-t border-white/10 space-y-3">
                        <div>
                            <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">ID Supir</label>
                            <input
                                type="number"
                                className={inputCls()}
                                placeholder="Contoh: 12"
                                value={supirId}
                                onChange={e => setSupirId(e.target.value)}
                            />
                        </div>
                        <div>
                            <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">
                                ID Kebun Tujuan
                                <span className="ml-1 normal-case text-bone/25">(isi jika memindahkan dari kebun lain)</span>
                            </label>
                            <input
                                type="number"
                                className={inputCls()}
                                placeholder="Kosongkan jika assign baru"
                                value={newKebunSupirId}
                                onChange={e => setNewKebunSupirId(e.target.value)}
                            />
                        </div>
                        <div className="flex gap-2 pt-1">
                            <button
                                onClick={newKebunSupirId ? handleReassignSupir : handleAssignSupir}
                                className="px-4 py-1.5 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover transition"
                            >
                                {newKebunSupirId ? "Pindahkan" : "Tugaskan"}
                            </button>
                            <button
                                onClick={() => { setShowAssignSupir(false); setSupirId(""); setNewKebunSupirId(""); }}
                                className="px-4 py-1.5 text-sm text-bone/40 hover:text-bone transition"
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
