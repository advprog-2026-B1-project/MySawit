"use client";

import { useState, useEffect, use } from "react";
import Link from "next/link";



interface KebunDetail {
    id: number;
    kodeKebun: string;
    namaKebun: string;
    luasHektare: number;
    koordinat: string;
    mandor?: { id: number; nama: string; email: string };
    supirList: { id: number; nama: string; email: string }[];
}

interface UserOption { id: number; nama: string; email: string; }
interface KebunOption { id: number; kodeKebun: string; namaKebun: string; }

const selectCls = "w-full bg-ink border border-white/10 text-bone text-sm rounded-md px-3 py-2 focus:ring-1 focus:ring-verdant focus:border-verdant focus:outline-none transition";

export default function KebunDetailPage({ params }: { params: Promise<{ id: string }> }) {
    const { id } = use(params);
    const kebunId = Number(id);

    const [detail, setDetail] = useState<KebunDetail | null>(null);
    const [mandorOptions, setMandorOptions] = useState<UserOption[]>([]);
    const [supirOptions, setSupirOptions] = useState<UserOption[]>([]);
    const [kebunOptions, setKebunOptions] = useState<KebunOption[]>([]);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(true);
    const [refresh, setRefresh] = useState(0);
    const [searchSupir, setSearchSupir] = useState("");

    // form state
    const [showMandorForm, setShowMandorForm] = useState(false);
    const [selectedMandorId, setSelectedMandorId] = useState("");
    const [targetKebunMandorId, setTargetKebunMandorId] = useState("");

    const [showSupirForm, setShowSupirForm] = useState(false);
    const [selectedSupirId, setSelectedSupirId] = useState("");
    const [targetKebunSupirId, setTargetKebunSupirId] = useState("");
    const [isReassignSupir, setIsReassignSupir] = useState(false);

    useEffect(() => {
        const init = async () => {
            setLoading(true);
            try {
                const [detailRes, usersRes, kebunRes] = await Promise.all([
                    fetch(`/api/kebun/${kebunId}/detail`, { credentials: "include" }),
                    fetch(`/api/users`, { credentials: "include" }),
                    fetch(`/api/kebun`, { credentials: "include" }),
                ]);

                if (detailRes.ok) setDetail(await detailRes.json());
                else setError(`Gagal memuat kebun (${detailRes.status})`);

                if (usersRes.ok) {
                    const users: (UserOption & { role: string })[] = await usersRes.json();
                    setMandorOptions(users.filter(u => u.role === "Mandor"));
                    setSupirOptions(users.filter(u => u.role === "Supir"));
                }

                if (kebunRes.ok) {
                    const kebunList: KebunOption[] = await kebunRes.json();
                    setKebunOptions(kebunList.filter(k => k.id !== kebunId));
                }
            } catch {
                setError("Tidak dapat terhubung ke server.");
            } finally {
                setLoading(false);
            }
        };
        init();
    }, [kebunId, refresh]);

    const post = (url: string, body: object) =>
        fetch(url, { method: "POST", headers: { "Content-Type": "application/json" }, credentials: "include", body: JSON.stringify(body) });

    const handleResponse = async (res: Response, msg: string) => {
        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            setError(data.message || `Error ${res.status}`);
        } else {
            setSuccess(msg);
            setRefresh(r => r + 1);
        }
    };

    const handleAssignMandor = async () => {
        if (!selectedMandorId) return;
        setError(""); setSuccess("");
        const res = await post(`/api/kebun/assign-mandor`, { mandorId: Number(selectedMandorId), kebunId });
        await handleResponse(res, "Mandor berhasil ditugaskan.");
        setShowMandorForm(false); setSelectedMandorId("");
    };

    const handleReassignMandor = async () => {
        if (!targetKebunMandorId || !detail?.mandor) return;
        setError(""); setSuccess("");
        const res = await post(`/api/kebun/reassign-mandor`, {
            mandorId: detail.mandor.id,
            oldKebunId: kebunId,
            newKebunId: Number(targetKebunMandorId),
        });
        await handleResponse(res, "Mandor berhasil dipindahkan ke kebun lain.");
        setShowMandorForm(false); setTargetKebunMandorId("");
    };

    const handleAssignSupir = async () => {
        if (!selectedSupirId) return;
        setError(""); setSuccess("");
        const res = await post(`/api/kebun/assign-supir`, { supirId: Number(selectedSupirId), kebunId });
        await handleResponse(res, "Supir berhasil ditugaskan.");
        setShowSupirForm(false); setSelectedSupirId("");
    };

    const handleReassignSupir = async () => {
        if (!selectedSupirId || !targetKebunSupirId) return;
        setError(""); setSuccess("");
        const res = await post(`/api/kebun/reassign-supir`, {
            supirId: Number(selectedSupirId),
            oldKebunId: kebunId,
            newKebunId: Number(targetKebunSupirId),
        });
        await handleResponse(res, "Supir berhasil dipindahkan ke kebun lain.");
        setShowSupirForm(false); setSelectedSupirId(""); setTargetKebunSupirId(""); setIsReassignSupir(false);
    };

    const openReassignSupir = (supirId: number) => {
        setSelectedSupirId(String(supirId));
        setIsReassignSupir(true);
        setShowSupirForm(true);
        setShowMandorForm(false);
    };

    const filteredSupir = (detail?.supirList ?? []).filter(s =>
        !searchSupir || s.nama.toLowerCase().includes(searchSupir.toLowerCase())
    );

    if (loading) return (
        <div className="px-8 py-6">
            <div className="h-4 w-32 bg-white/5 rounded animate-pulse mb-8" />
            <div className="h-40 bg-white/5 rounded-lg animate-pulse mb-4" />
            <div className="h-32 bg-white/5 rounded-lg animate-pulse" />
        </div>
    );

    return (
        <div className="px-8 py-6 max-w-3xl">

            <div className="flex items-center gap-2 text-sm mb-6">
                <Link href="/kebun" className="text-bone/40 hover:text-bone transition">Daftar Kebun</Link>
                <span className="text-bone/20">/</span>
                <span className="text-bone/70">{detail?.namaKebun ?? "Detail"}</span>
            </div>

            {error && <div className="mb-4 px-4 py-3 bg-red-500/10 border border-red-500/20 rounded-lg text-sm text-red-400">{error}</div>}
            {success && <div className="mb-4 px-4 py-3 bg-verdant-soft border border-verdant/20 rounded-lg text-sm text-verdant">{success}</div>}

            {/* Info kebun */}
            {detail && (
                <div className="bg-ink-soft border border-line rounded-lg p-6 mb-4">
                    <div className="flex items-start justify-between mb-4">
                        <div>
                            <p className="text-xs font-semibold text-bone/30 uppercase tracking-wider mb-1">{detail.kodeKebun}</p>
                            <h1 className="text-xl font-semibold text-bone">{detail.namaKebun}</h1>
                        </div>
                        <Link href={`/kebun/${detail.id}/edit`}
                            className="px-3 py-1.5 text-xs border border-line text-bone/60 rounded-md hover:border-white/20 hover:text-bone transition">
                            Edit
                        </Link>
                    </div>
                    <div className="grid grid-cols-3 gap-4 pt-4 border-t border-line">
                        <div>
                            <p className="text-xs text-bone/30 uppercase tracking-wider mb-1">Luas</p>
                            <p className="text-sm font-medium text-bone">{detail.luasHektare} ha</p>
                        </div>
                        <div className="col-span-2">
                            <p className="text-xs text-bone/30 uppercase tracking-wider mb-1">Koordinat</p>
                            <p className="text-xs font-mono text-bone/50 break-all leading-relaxed">{detail.koordinat}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Mandor card */}
            <div className="bg-ink-soft border border-line rounded-lg p-6 mb-4">
                <div className="flex items-center justify-between mb-4">
                    <p className="text-xs font-semibold text-bone/30 uppercase tracking-wider">Mandor Pengawas</p>
                    <button
                        onClick={() => { setShowMandorForm(v => !v); setShowSupirForm(false); setSelectedMandorId(""); setTargetKebunMandorId(""); }}
                        className="px-3 py-1.5 bg-verdant text-ink text-xs font-semibold rounded-md hover:bg-verdant-hover transition"
                    >
                        {detail?.mandor ? "Copot & Pindahkan" : "+ Tugaskan"}
                    </button>
                </div>

                {detail?.mandor ? (
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-verdant-soft flex items-center justify-center text-verdant text-sm font-bold shrink-0">
                            {detail.mandor.nama.charAt(0).toUpperCase()}
                        </div>
                        <div>
                            <p className="text-sm font-medium text-bone">{detail.mandor.nama}</p>
                            <p className="text-xs text-bone/40">{detail.mandor.email}</p>
                        </div>
                    </div>
                ) : (
                    <p className="text-sm text-bone/30">Belum ada mandor yang ditugaskan.</p>
                )}

                {showMandorForm && (
                    <div className="mt-4 pt-4 border-t border-line space-y-3">
                        {!detail?.mandor ? (
                            <>
                                <div>
                                    <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">Pilih Mandor</label>
                                    <select value={selectedMandorId} onChange={e => setSelectedMandorId(e.target.value)} className={selectCls}>
                                        <option value="">-- Pilih Mandor --</option>
                                        {mandorOptions.map(m => (
                                            <option key={m.id} value={m.id}>{m.nama} ({m.email})</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={handleAssignMandor} disabled={!selectedMandorId}
                                        className="px-4 py-1.5 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-40 transition">
                                        Tugaskan
                                    </button>
                                    <button onClick={() => { setShowMandorForm(false); setSelectedMandorId(""); }}
                                        className="px-4 py-1.5 text-sm text-bone/40 hover:text-bone transition">Batal</button>
                                </div>
                            </>
                        ) : (
                            <>
                                <p className="text-xs text-bone/50">
                                    Mandor <span className="text-bone font-medium">{detail.mandor.nama}</span> akan dicopot dari kebun ini dan dipindahkan ke kebun berikut:
                                </p>
                                <div>
                                    <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">Kebun Tujuan</label>
                                    <select value={targetKebunMandorId} onChange={e => setTargetKebunMandorId(e.target.value)} className={selectCls}>
                                        <option value="">-- Pilih Kebun --</option>
                                        {kebunOptions.map(k => (
                                            <option key={k.id} value={k.id}>{k.kodeKebun} — {k.namaKebun}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={handleReassignMandor} disabled={!targetKebunMandorId}
                                        className="px-4 py-1.5 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-40 transition">
                                        Pindahkan
                                    </button>
                                    <button onClick={() => { setShowMandorForm(false); setTargetKebunMandorId(""); }}
                                        className="px-4 py-1.5 text-sm text-bone/40 hover:text-bone transition">Batal</button>
                                </div>
                            </>
                        )}
                    </div>
                )}
            </div>

            {/* Supir card */}
            <div className="bg-ink-soft border border-line rounded-lg p-6">
                <div className="flex items-center justify-between mb-4">
                    <p className="text-xs font-semibold text-bone/30 uppercase tracking-wider">
                        Supir Truk
                        {(detail?.supirList.length ?? 0) > 0 && (
                            <span className="ml-2 px-1.5 py-0.5 bg-verdant-soft text-verdant rounded text-xs">
                                {detail?.supirList.length}
                            </span>
                        )}
                    </p>
                    <button
                        onClick={() => { setShowSupirForm(v => !v); setIsReassignSupir(false); setShowMandorForm(false); setSelectedSupirId(""); setTargetKebunSupirId(""); }}
                        className="px-3 py-1.5 bg-verdant text-ink text-xs font-semibold rounded-md hover:bg-verdant-hover transition"
                    >
                        + Tugaskan Supir
                    </button>
                </div>

                {(detail?.supirList.length ?? 0) > 0 && (
                    <div className="relative mb-3">
                        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-bone/30 text-sm">⌕</span>
                        <input type="text" placeholder="Cari nama supir..."
                            className="w-full bg-ink border border-line text-bone text-sm rounded-md pl-8 pr-3 py-2 focus:ring-1 focus:ring-verdant focus:outline-none placeholder:text-bone/30 transition"
                            value={searchSupir} onChange={e => setSearchSupir(e.target.value)} />
                    </div>
                )}

                {filteredSupir.length === 0 ? (
                    <p className="text-sm text-bone/30">Belum ada supir yang ditugaskan di kebun ini.</p>
                ) : (
                    <div className="space-y-1">
                        {filteredSupir.map(s => (
                            <div key={s.id}
                                className="flex items-center justify-between px-3 py-2.5 rounded-md bg-ink hover:bg-white/5 transition group">
                                <div className="flex items-center gap-2.5">
                                    <div className="w-7 h-7 rounded-full bg-ink-soft border border-line flex items-center justify-center text-bone/50 text-xs font-medium shrink-0">
                                        {s.nama.charAt(0).toUpperCase()}
                                    </div>
                                    <div>
                                        <p className="text-sm text-bone">{s.nama}</p>
                                        <p className="text-xs text-bone/40">{s.email}</p>
                                    </div>
                                </div>
                                <button onClick={() => openReassignSupir(s.id)}
                                    className="text-xs text-bone/30 hover:text-verdant transition opacity-0 group-hover:opacity-100">
                                    Pindahkan
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                {showSupirForm && (
                    <div className="mt-4 pt-4 border-t border-line space-y-3">
                        {isReassignSupir ? (
                            <>
                                <p className="text-xs text-bone/50">
                                    Pindahkan <span className="text-bone font-medium">
                                        {detail?.supirList.find(s => s.id === Number(selectedSupirId))?.nama}
                                    </span> ke kebun lain:
                                </p>
                                <div>
                                    <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">Kebun Tujuan</label>
                                    <select value={targetKebunSupirId} onChange={e => setTargetKebunSupirId(e.target.value)} className={selectCls}>
                                        <option value="">-- Pilih Kebun --</option>
                                        {kebunOptions.map(k => (
                                            <option key={k.id} value={k.id}>{k.kodeKebun} — {k.namaKebun}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={handleReassignSupir} disabled={!targetKebunSupirId}
                                        className="px-4 py-1.5 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-40 transition">
                                        Pindahkan
                                    </button>
                                    <button onClick={() => { setShowSupirForm(false); setIsReassignSupir(false); setSelectedSupirId(""); setTargetKebunSupirId(""); }}
                                        className="px-4 py-1.5 text-sm text-bone/40 hover:text-bone transition">Batal</button>
                                </div>
                            </>
                        ) : (
                            <>
                                <div>
                                    <label className="block text-xs text-bone/40 uppercase tracking-wider mb-1.5">Pilih Supir</label>
                                    <select value={selectedSupirId} onChange={e => setSelectedSupirId(e.target.value)} className={selectCls}>
                                        <option value="">-- Pilih Supir --</option>
                                        {supirOptions.map(s => (
                                            <option key={s.id} value={s.id}>{s.nama} ({s.email})</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={handleAssignSupir} disabled={!selectedSupirId}
                                        className="px-4 py-1.5 bg-verdant text-ink text-sm font-semibold rounded-md hover:bg-verdant-hover disabled:opacity-40 transition">
                                        Tugaskan
                                    </button>
                                    <button onClick={() => { setShowSupirForm(false); setSelectedSupirId(""); }}
                                        className="px-4 py-1.5 text-sm text-bone/40 hover:text-bone transition">Batal</button>
                                </div>
                            </>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
